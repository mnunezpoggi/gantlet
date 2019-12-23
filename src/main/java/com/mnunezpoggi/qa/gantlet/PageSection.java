package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.annotations.ElementList;
import com.mnunezpoggi.qa.gantlet.annotations.Section;
import com.mnunezpoggi.qa.gantlet.annotations.Iterable;
import com.mnunezpoggi.qa.gantlet.errors.WebElementError;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Core class used to define a Section (multiple Elements) or a BasePage<br>
 * It handles a WebDriver instance<br>
 * Is responsible for searching and finding the WebElements (Fields of a
 * BasePage)<br>
 *
 * 
 */
public class PageSection extends BaseSection{

    /**
     * Method that loops through all fields marked as static and tries to<br>
     * instantiate them using reflection and the metadata provied by
     * {@link Element} annotation.<br>
     * If it finds a {@link Section}, it recursively calls that section's
     * setAllStaticElements<br>
     * If there are errors on that section, they are merged<br>
     * This DOES NOT returns WebElements, only instantiate them
     *
     * @return if there's an error finding the elements
     */
    public final WebElementError setAllStaticElements() {
        WebElementError error = new WebElementError(this);
        logger.info("Looping through all fields of " + getClass().getSimpleName());
        for (Field field : getClass().getDeclaredFields()) {
            try {
                if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(Section.class) && !field.isAnnotationPresent(Iterable.class) && !field.isAnnotationPresent(ElementList.class)) {
                    logger.info(field.getName() + " is not annotated");
                    continue;
                }
                if (field.isAnnotationPresent(Section.class)) {
                    logger.info("Section " + field.getName());
                    PageSection ps = (PageSection) field.getType().newInstance();
                    ps.setDriver(driver);
                    WebElementError sectionError = ps.setAllStaticElements();
                    if (sectionError != null) {
                        error.merge(sectionError);
                    }
                    field.set(this, ps);
                    continue;
                }
                if (field.isAnnotationPresent(Iterable.class)){
                    logger.info("Iterable section " + field.getName());
                    com.mnunezpoggi.qa.gantlet.annotations.Iterable iterable = field.getAnnotation(com.mnunezpoggi.qa.gantlet.annotations.Iterable.class);
                    if(!iterable.autoset()){
                        logger.info("Do not autoset section");
                        continue;
                    }
                    Object fieldInstance = field.getType().newInstance();
                    if (fieldInstance instanceof List){
                        handleIterableSection(field, "", error);
                    }else{
                        logger.warn("field annotated as Iterable is not an instance of List<E>");
                    }
                    continue;
                }
                if (field.isAnnotationPresent(ElementList.class)){
                    logger.info("ElementList  " + field.getName());
                    ElementList elementListAnnotation = field.getAnnotation(ElementList.class);
                    if (elementListAnnotation.dynamic()){
                        logger.info(field.getName() + " is dynamic, skipping it");
                        continue;
                    }
                    Object fieldInstance = field.getType().newInstance();
                    if (fieldInstance instanceof List){
                        handleElementList(field, "", error);
                    }else{
                        logger.warn("field annotated as ElementList is not an instance of List<E>");
                    }
                    continue;
                }
                logger.info("Element " + field.getName());
                Element el = field.getAnnotation(Element.class);
                if (el.dynamic()) {
                    logger.info(field.getName() + " is dynamic, skipping it");
                    continue;
                }
                WebElement webEl = findElement(el);
                field.set(this, webEl);
            } catch (NoSuchElementException ex) {
                logger.warn("Couldn't find element " + field.getName());
                error.addFailedElement(field);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
                logger.fatal(formatMessage(ex, field));
                System.exit(0);
            }
        }
        if (!error.getFailedElements().isEmpty() && this instanceof BasePage) {
            logger.warn("Errors found, notifying error collectors");
            notifyErrorCollectors(error);

        }
        if (error.getFailedElements().isEmpty()) {
            logger.trace("Didn't find any errors");
            error = null;
        }
        return error;
    }

    @Override
    public WebElementError setIterableSection(String sectionName) {
        WebElementError error = new WebElementError(this);
        Field field = null;
        try {
            field = getClass().getDeclaredField(sectionName);
        } catch (Exception ex) {
            logger.fatal(formatMessage(ex, sectionName));
            System.exit(0);
        }
        if (!field.isAnnotationPresent(Iterable.class) ) {
            logger.fatal("Field " + field.getName() + " is not annotated as Iterable", field.getName());
            System.exit(0);
        }
        try {
            logger.info("Section " + field.getName());
            Object fieldInstance = field.getType().newInstance();
            if (fieldInstance instanceof List){
                handleIterableSection(field, "", error);
            }else{
                logger.warn("field annotated as Iterable is not an instance of List<E>");
            }
        } catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            error.addFailedElement(field);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            logger.fatal(formatMessage(ex, field));
            System.exit(0);
        }
        return error;
    }
 
    /**
     * Method that searchs an element by it field's name and tries to 
     * instantiate it<br>
     * The {@link Field} NEEDS to be:<br>
     * - Annotatated with {@link Element} or {@link Section}<br>
     * - Set to public (not private or protected)<br>
     * Otherwise the application will crash<br>
     * DOES NOT returns the {@link WebElement}<br>
     *
     * @param element The field's name that is going to be instantiated
     * @return if there's an error
     */
    @Override
    public WebElementError setElement(String element) {
        WebElementError error = new WebElementError(this);
        Field field = null;
        try {
            field = getClass().getDeclaredField(element);
        } catch (Exception ex) {
            logger.fatal(formatMessage(ex, element));
            System.exit(0);
        }
        if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(Section.class) && !field.isAnnotationPresent(ElementList.class) && !field.isAnnotationPresent(Iterable.class) ) {
            logger.fatal("Field " + field.getName() + " is not annotated", field.getName());
            System.exit(0);
        }
        try {
            if (field.isAnnotationPresent(Section.class)) {
                PageSection ps = (PageSection) field.getType().newInstance();
                ps.setDriver(driver);
                field.set(this, ps);
                return null;
            }
            if (field.isAnnotationPresent(ElementList.class)){
                logger.info("ElementList  " + field.getName());
                Object fieldInstance = field.getType().newInstance();
                if (fieldInstance instanceof List){
                    handleElementList(field, "", error);
                }else{
                    logger.warn("field annotated as ElementList is not an instance of List<E>");
                }
            }else{
                Element el = field.getAnnotation(Element.class);
                WebElement webEl = findElement(el);
                field.set(this, webEl);
                error = null;
            }
        } catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            error.addFailedElement(field);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            logger.fatal(formatMessage(ex, field));
            System.exit(0);
        }
        return error;
    }
    
    @Override
    public WebElementError waitForElement(String element, int timeOutInSeconds){
        WebElementError error = new WebElementError(this);
        Field field = null;
        try {
            field = getClass().getDeclaredField(element);
        } catch (NoSuchFieldException | SecurityException ex) {
            logger.fatal(formatMessage(ex, element));
            System.exit(0);
        }
        if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(Section.class) && !field.isAnnotationPresent(Iterable.class) && !field.isAnnotationPresent(Iterable.class)) {
            logger.fatal("Field " + field.getName() + " is not annotated", field.getName());
            System.exit(0);
        }
        try {
            if (field.isAnnotationPresent(Section.class)) {
                PageSection ps = (PageSection) field.getType().newInstance();
                ps.setDriver(driver);
                field.set(this, ps);
                return null;
            }
            if (field.isAnnotationPresent(ElementList.class)){
                logger.info("ElementList  " + field.getName());
                ElementList el = field.getAnnotation(ElementList.class);
                By by = mapBy(el.name(), Element.Type.CSS);
                driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
                WebDriverWait wait = new WebDriverWait(this.driver, timeOutInSeconds);
                List<WebElement> webEl = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
                Object fieldInstance = field.getType().newInstance();
                if (fieldInstance instanceof List){
                    for (int i = 0; i < el.startingIndex(); i++) {
                        webEl.remove(i);
                    }
                    field.set(this, webEl);
                    error = null;
                }else{
                    logger.warn("field annotated as ElementList is not an instance of List<E>");
                    error.addFailedElement(field);
                }
            }else{
                Element el = field.getAnnotation(Element.class);
                By by = mapBy(el.name(), el.type());
                driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
                WebDriverWait wait = new WebDriverWait(this.driver, timeOutInSeconds);
                WebElement webEl = wait.until(ExpectedConditions.presenceOfElementLocated(by));
                field.set(this, webEl);
                error = null;
            }
        } catch (NoSuchElementException | IndexOutOfBoundsException ex) {
            error.addFailedElement(field);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            logger.fatal(formatMessage(ex, field));
            System.exit(0);
        }
        return error;
    }
        
    /**
     * Private method that directly calls
     * {@link WebDriver#findElement(org.openqa.selenium.By)}
     *
     * @param el The metadata that will be used to search the WebElement
     * @return {@link WebElement} found, otherwise null
     * @throws IndexOutOfBoundsException
     */
    @Override
    protected WebElement findElement(Element el) throws IndexOutOfBoundsException {
        Element.Type type = el.type();
        String name = el.name();
        int index = el.index();
        By by = mapBy(name, type);
        if (index < 0){
            return driver.findElement(by);
        }else{
            return driver.findElements(by).get(index);
        }
    }


}
