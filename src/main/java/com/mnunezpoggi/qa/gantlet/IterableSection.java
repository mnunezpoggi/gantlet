/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet;

import static com.mnunezpoggi.qa.gantlet.BaseSection.formatMessage;
import com.mnunezpoggi.qa.gantlet.annotations.Iterable;
import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.annotations.ElementList;
import com.mnunezpoggi.qa.gantlet.annotations.Section;
import com.mnunezpoggi.qa.gantlet.errors.WebElementError;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This class represent a PageSection that is not static, but it repeats over
 * the page.<br>
 * The properties {@code ContainerName} and {@code Index} must be set right 
 * after this class is instantiated or no method will work.
 * @author Gerardo Miranda
 */
public class IterableSection extends BaseSection{
    
    /**This property must be set before any method call. <br>
     * It is not mandatory in the constructor because we need the nullary
     * constructor to be available due the reflection used.
     */
    protected String ContainerName;
    
    /**This property must be set before any method call. <br>
     * It is not mandatory in the constructor because we need the nullary
     * constructor to be available due the reflection used.
     */
    protected int Index;
    
    private String finalContainer(){
        return this.ContainerName.replace("?", Integer.toString(this.Index));
    }
    
    /**
     * All the objects annotated as {@code @Element}, are located using
     * CSS selectors.
     * @return 
     */
    public final WebElementError setAllElements(){
        String finalContainer = finalContainer();
        WebElementError error = new WebElementError(this);
        logger.info("Looping through all fields of " + getClass().getSimpleName());
        for (Field field : getClass().getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Section.class)){
                    logger.warn("Section annotations are not allowed inside an IterableSection");
                    continue;
                }
                if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(Iterable.class) && !field.isAnnotationPresent(ElementList.class)) {
                    logger.info(field.getName() + " is not annotated");
                    continue;
                }
                if (field.isAnnotationPresent(Iterable.class)) {
                    logger.info("Iterable section " + field.getName());
                    com.mnunezpoggi.qa.gantlet.annotations.Iterable iterable = field.getAnnotation(com.mnunezpoggi.qa.gantlet.annotations.Iterable.class);
                    if(!iterable.autoset()){
                        logger.info("Do not autoset section");
                        continue;
                    }
                    Object fieldInstance = field.getType().newInstance();
                    if (fieldInstance instanceof List){
                        handleIterableSection(field, finalContainer, error);
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
                        handleElementList(field, finalContainer, error);
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
                ex.printStackTrace();
                System.exit(0);
            }
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
                handleElementList(field, finalContainer(), error);
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
        if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(ElementList.class) && !field.isAnnotationPresent(Iterable.class)) {
            logger.fatal("Field " + field.getName() + " is not annotated", field.getName());
            System.exit(0);
        }
        try {
//            if (field.isAnnotationPresent(Section.class)) {
//                PageSection ps = (PageSection) field.getType().newInstance();
//                ps.setDriver(driver);
//                field.set(this, ps);
//                return null;
//            }
            if (field.isAnnotationPresent(ElementList.class)){
                logger.info("ElementList  " + field.getName());
                Object fieldInstance = field.getType().newInstance();
                if (fieldInstance instanceof List){
                    handleElementList(field, finalContainer(), error);
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
    public WebElementError waitForElement(String element, int timeOutInSeconds) {
        WebElementError error = new WebElementError(this);
        Field field = null;
        try {
            field = getClass().getDeclaredField(element);
        } catch (NoSuchFieldException | SecurityException ex) {
            logger.fatal(formatMessage(ex, element));
            System.exit(0);
        }
        if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(ElementList.class) && !field.isAnnotationPresent(Iterable.class)) {
            logger.fatal("Field " + field.getName() + " is not annotated", field.getName());
            System.exit(0);
        }
        try {
//            if (field.isAnnotationPresent(Section.class)) {
//                PageSection ps = (PageSection) field.getType().newInstance();
//                ps.setDriver(driver);
//                field.set(this, ps);
//                return null;
//            }
            if (field.isAnnotationPresent(ElementList.class)){
                logger.info("ElementList  " + field.getName());
                ElementList el = field.getAnnotation(ElementList.class);
                String finalContainer = finalContainer();
                String innerContainer = finalContainer + " > " + el.name();
                By by = mapBy(innerContainer, Element.Type.CSS);
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
                String finalContainer = finalContainer();
                String innerContainer = finalContainer + " > " + el.name();
                By by = mapBy(innerContainer, el.type());
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

    @Override
    protected WebElement findElement(Element el) throws IndexOutOfBoundsException {
        String finalContainer = finalContainer();
        String innerContainer = finalContainer + " > " + el.name();
        int index = el.index();
        By by = mapBy(innerContainer, Element.Type.CSS);
        if (index < 0){
            return driver.findElement(by);
        }else{
            return driver.findElements(by).get(index);
        }
    }
}
