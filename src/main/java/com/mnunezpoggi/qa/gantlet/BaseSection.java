/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.annotations.ElementList;
import com.mnunezpoggi.qa.gantlet.annotations.Section;
import com.mnunezpoggi.qa.gantlet.errors.BaseError;
import com.mnunezpoggi.qa.gantlet.errors.ErrorCollector;
import com.mnunezpoggi.qa.gantlet.errors.WebElementError;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

/**
 *
 * @author Gerardo Miranda
 */
public abstract class BaseSection {
    
    protected WebDriver driver;

    protected ArrayList<ErrorCollector> ErrorCollectors;

    protected Logger logger = LogHelper.getLogger(this);
    
    /**
     * Adds new ErrorCollectors
     *
     * @deprecated
     * @param collector
     */
    public void addErrorCollector(ErrorCollector collector) {
        if (ErrorCollectors == null) {
            ErrorCollectors = new ArrayList();
        }        
        ErrorCollectors.add(collector);
    }
    
    /**
     * Sets the driver to handle
     *
     * @param driver
     */
    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }
    
    public abstract WebElementError setIterableSection(String sectionName);
    
    public abstract WebElementError setElement(String element);
    
    public abstract WebElementError waitForElement(String element, int timeOutInSeconds);
    
    protected abstract WebElement findElement(Element el) throws IndexOutOfBoundsException;
    
    /**
     * Method that sets all Elements to null<br>
     * * The {@link Field} NEEDS to be:<br>
     * - Annotatated with {@link Element} or {@link Section}<br>
     * - Set to public (not private or protected)<br>
     * Otherwise the application will crash
     */
    public void unsetAllElements() {
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Element.class) && !field.isAnnotationPresent(Section.class)) {
                continue;
            }
            try {
                field.set(this, null);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                logger.fatal(formatMessage(ex, field));
                System.exit(0);
            }

        }
    }
    
    /**
     * This method search for an element by the name and type of selector
     * using the WebDriver's {@code findElement} method.
     * @param name name of the element
     * @param type type of selector to be used
     * @return true if the element is present in the page, false otherwise
     */
    public boolean isElementPresent(String name, Element.Type type){
        try{
            By by = mapBy(name, type);
            WebElement element = this.driver.findElement(by);
            return element != null;
        }catch(NoSuchElementException e){
            return false;
        }
    }
    
    /**
     * This method search for an iterable section by the 
     * section complete name and the section index using the WebDriver's 
     * {@code findElement} method with the CSS selector.
     * @param name name of the section
     * @param index of the section
     * @return true if the section is present in the page, false otherwise
     */
    public boolean isElementPresent(String name, int index){
        String elementName = name.replace("?", Integer.toString(index));
        return this.isElementPresent(elementName, Element.Type.CSS);
    }
    
    /**
     * This method handles the creation of the an iterable section list and set
     * it to the field provided.
     * <br>
     * The method loops searching in the page for iterable sections until
     * it doesn't find a next one, then it set the result list in the field
     * provided by parameter.
     * @param field field to be set
     * @param finalContainer name of the container of the iterable section
     * @param error WebElementError to be nested until the page is fully loaded
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException 
     */
    protected void handleIterableSection(Field field, String finalContainer, 
            WebElementError error)
                    throws IllegalAccessException, IllegalArgumentException, InstantiationException{
        List<IterableSection> iterableSectionList = (List) field.getType().newInstance();
        com.mnunezpoggi.qa.gantlet.annotations.Iterable iterable = field.getAnnotation(com.mnunezpoggi.qa.gantlet.annotations.Iterable.class);
        String innerContainer;
        if (finalContainer.isEmpty()){
            innerContainer = iterable.container();
        }else{
            innerContainer = finalContainer + " > " + iterable.container();
        }
        int startingIndex = iterable.startingIndex();
        ParameterizedType paramType = (ParameterizedType)field.getGenericType();
        Type iterableSectionClass = paramType.getActualTypeArguments()[0];
        for (int i = startingIndex; i < Integer.MAX_VALUE; i++) {
            if (this.isElementPresent(innerContainer, i)){
                IterableSection newSection;
                try{
                    newSection = (IterableSection)Class.forName(iterableSectionClass.getTypeName()).newInstance();
                    newSection.ContainerName = innerContainer;
                    newSection.Index = i;
                    newSection.setDriver(driver);
                    WebElementError sectionError = newSection.setAllElements();
                    if (sectionError != null) {
                        error.merge(sectionError);
                    }
                    iterableSectionList.add(newSection);
                }catch(ClassNotFoundException ex){
                    logger.fatal("Cannot instantiate " + iterableSectionClass.getTypeName() + ". Exiting", ex);
                    System.exit(0);
                }
            }else{
                field.set(this, iterableSectionList);
                break;
            }
        }
    }
    
    /**
     * This method handles the creation of an element list and set it to the 
     * field provided.<br>
     * This method find all the elements referenced by the cssSelector and 
     * add them to the list of WebElements
     * @param field list to be filled with elements
     * @param container
     * @param error WebElementError to be nested until the page is fully loaded
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.InstantiationException
     */
    protected void handleElementList(Field field, String container, 
            WebElementError error)
            throws IllegalAccessException, IllegalArgumentException, InstantiationException{
        List<WebElement> elementList;
        ElementList elementListAnnotation = field.getAnnotation(ElementList.class);
        String cssSelector;
        if (container.isEmpty()){
            cssSelector = elementListAnnotation.name();
        }else{
            cssSelector = container + " > " + elementListAnnotation.name();
        }
        elementList = this.driver.findElements(mapBy(cssSelector, Element.Type.CSS));
        for (int i = 0; i < elementListAnnotation.startingIndex(); i++) {
            elementList.remove(i);
        }
        field.set(this, elementList);
    }
    
    /**
     * Method that notifies the multiple {@link ErrorCollector}
     *
     * @param error
     */
    protected void notifyErrorCollectors(BaseError error) {
        for (ErrorCollector collector : ErrorCollectors) {
            collector.collectError(error);
        }
    }
    
    /**
     * Private mathod that returns a <code>By</code> object from the 
     * Element Type and the name provided.
     * 
     * @param name Identificator of the element
     * @param type Type of the identificator
     * @return By object
     */
    protected By mapBy(String name, Element.Type type){
        By finalBy;
        switch (type) {
            case CLASS:
                finalBy = By.className(name); break;
            case ID:
                finalBy = By.id(name); break;
            case CSS:
                finalBy = By.cssSelector(name); break;
            case LINK:
                finalBy = By.linkText(name); break;
            case NAME:
                finalBy = By.name(name); break;
            case PARTIAL_LINK_TEXT:
                finalBy = By.partialLinkText(name); break;
            case TAG_NAME:
                finalBy = By.tagName(name); break;
            case XPATH:
                finalBy = By.xpath(name); break;
            default:
                finalBy = null;
        }
        return finalBy;
    }
    
    
    /**
     * Helper method that parses a table defined by multiple rows and multiple
     * data inside of it<br>
     * The {@link Field} NEEDS to:<br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     * - Point to an existing table
     *
     * @param elementName The field's name that is going to be instantiated
     * @return a list of list (matrix) of WebElements
     */
    public ArrayList<ArrayList<WebElement>> tableToList(String elementName) {
        WebElementError error = setElement(elementName);
        if (error != null) {
            return null;
        }
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        ArrayList<ArrayList<WebElement>> finalList = new ArrayList();
        for (WebElement row : element.findElements(By.cssSelector("tr"))) {
            ArrayList<WebElement> elementList = new ArrayList();
            for (WebElement cell : row.findElements(By.cssSelector("td"))) {
                elementList.add(cell);
            }
            finalList.add(elementList);
        }
        return finalList;
    }

    /**
     * Helper method that parses a dropdown defined by multiple rows inside of
     * it<br>
     * The {@link Field} NEEDS to:<br>
     * - Be annotatated with {@link Element}<br>
     * - Be set to public (not private or protected)<br>
     * - Point to an existing dropdown (Select)
     *
     * @param elementName
     * @return
     */
    public ArrayList<WebElement> dropdownToList(String elementName) {
        WebElementError error = setElement(elementName);
        if (error != null) {
            return null;
        }
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        Select select = new Select(element);
        return new ArrayList(select.getOptions());
    }
    
    /**
     * Private helper method that handles messages for the multiple exceptions
     * catched in this class
     *
     * @param ex Exception catched
     * @param o Field or any class that throwed the error
     * @return the formatted message
     */
    protected static String formatMessage(Exception ex, Object o) {
        StringBuilder sb = new StringBuilder();
        sb.append("Field ");

        if (o instanceof Field) {
            sb.append(((Field) o).getName());
        } else {
            sb.append(o);
        }

        switch (ex.getClass().getSimpleName()) {
            case "IllegalArgumentException":
                sb.append(" is not a PageSection or a WebElement");
                break;
            case "IllegalAccessException":
                sb.append(" is inaccessible (needs to be public)");
                break;
            case "SecurityException":
                sb.append(" is inaccessible");
                break;
            case "NoSuchFieldException":
                sb.append(" doesn't exist");
                break;
            case "InstantiationException":
                sb.append(" couldn't be instantiated");
                break;

        }
        return sb.toString();
    }
    
    /**
     * Helper method that instantiates an {@link Element} and then clicks it<br
     * The {@link Field} NEEDS to:<br> - Be annotatated with {@link Element}<br>
     * - Be set to public (not private or protected)<br>
     * TODO: add listener
     *
     * @param elementName The field's name that is going to be instantiated
     */
    public void click(String elementName) {
        WebElementError error = setElement(elementName);
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        element.click();
    }

    /**
     * Helper method that instantiates an {@link Element} and then hovers the
     * pointer over it<br>
     * The {@link Field} NEEDS to:<br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     * TODO: add listener
     *
     * @param elementName The field's name that is going to be instantiated
     */
    public void hover(String elementName) {
        WebElementError error = setElement(elementName);
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
            new Actions(driver).moveToElement(element).perform();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
    }
    
    /**Helper method that instantiates an {@link Element} and then performs
     * a drag and drop operation over it.
     * The {@link Field} MUST:<br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     * @param elementName The field name that is going to be dragged
     * @param offsetX horizontal offset to which to move the mouse
     * @param offsetY vertical offset to which to move the mouse
     */
    public void dragAndDrop(String elementName, int offsetX, int offsetY){
        WebElementError error = setElement(elementName);
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
            new Actions(driver).dragAndDropBy(element, offsetX, offsetY).perform();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
    }
    
    /**
     * Helper method that instantiates an {@link Element} and sends text to it
     * The {@link Field} NEEDS to:<br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     *
     * @param elementName The field's name that is going to be instantiated
     * @param text The text to send to the element
     */
    public void setText(String elementName, String text) {
        WebElementError error = setElement(elementName);

        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        element.sendKeys(text);
    }

    /**
     * Helper method that instantiates an {@link Element} and sends a sequence
     * of chars to it<br>
     * <b>SLOWER</b><br>
     * The {@link Field} NEEDS to:<br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     *
     * @param elementName The field's name that is going to be instantiated
     * @param text The text to send to the element
     */
    public void setChars(String elementName, String text) {
        WebElementError error = setElement(elementName);

        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }

        for (int i = 0; i < text.length(); i++) {
            element.sendKeys(Character.toString(text.charAt(i)));
            try {
                Thread.sleep(10);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Helper method that instantiates an {@link Element} and gathers its
     * text<br>
     * The {@link Field} NEEDS to:<br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     *
     * @param elementName The field's name that is going to be instantiated
     * @return The inner text of the field
     */
    public String getText(String elementName) {
        WebElementError error = setElement(elementName);
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        return element.getText();
    }
    
    /**
     * Helper method that instantiates an {@link Element} and gathers the
     * attribute requested<br>
     * The {@link Field} MUST: <br>
     * - Be annotatated with {@link Element} <br>
     * - Be set to public (not private or protected)<br>
     * 
     * @param elementName The field's name that is going to be instantiated
     * @param attribute name to be obtained
     * @return the value text of the field
     */
    public String getAttribute(String elementName, String attribute){
        WebElement element = null;
        try{
            element = (WebElement) getClass().getField(elementName).get(this);
        }catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        return element.getAttribute(attribute);
    }
    
    /**
     * Method that clears the content of an input element
     * @param elementName 
     */
    public void clearValue(String elementName){
        WebElement element = null;
        try{
            element = (WebElement) getClass().getField(elementName).get(this);
            element.clear();
        }catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
    }
    
    /**
     * Method that sends a single backspace key to the input element referenced 
     * with the elementName.
     * @param elementName 
     */
    public void backspaceInput(String elementName){
        backspaceInput(elementName, 1);
    }
    
    /**
     * Method that sends several of backspace keys to the input element 
     * referenced with the elementName.
     * @param elementName
     * @param count number of backspace keys to send to the element
     */
    public void backspaceInput(String elementName, int count){
        //TODO
        WebElement element = null;
        try{
            element = (WebElement) getClass().getField(elementName).get(this);
            for (int i = 0; i < count; i++) {
                element.sendKeys(Keys.BACK_SPACE);
            }
        }catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
    }
    
    /**
     * Helper method that change the color of an element for a period of time
     * and then returns it to its original color.
     * <br>
     * This method is useful in demos or UI test
     * @param elementName element to be highlighted
     * @param color color to highlight
     * @param time period of time that the color change endures in milliseconds
     * @throws InterruptedException 
     */
    public void highlight(String elementName, String color, int time) throws InterruptedException {
        WebElement element = null;
        try {
            element = (WebElement) getClass().getField(elementName).get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            logger.fatal(formatMessage(ex, elementName));
            System.exit(0);
        }
        if(element == null){
            logger.warn("Element is null, not highlightning");
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String originalColor = element.getCssValue("background-color");
        js.executeScript("arguments[0].style.background='" + color +" '", element);
        Thread.sleep(time);
        js.executeScript("arguments[0].style.background='" + originalColor +" '", element);
    }

    public void highlight(String elementName, String color) throws InterruptedException {
        highlight(elementName, color, 60);
    }
    
    

    

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    
    
}
