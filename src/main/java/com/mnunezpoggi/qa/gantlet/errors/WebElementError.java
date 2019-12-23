package com.mnunezpoggi.qa.gantlet.errors;

import com.mnunezpoggi.qa.gantlet.BaseSection;
import com.mnunezpoggi.qa.gantlet.annotations.Element;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.openqa.selenium.WebElement;

/**
 * Class that holds the Fields representing a {@link WebElement} which wasn't
 * found (error)<br> TODO: Merge with Reports
 *
 * 
 */
public class WebElementError extends BaseError {

    private ArrayList<Field> FailedElements;

    /**
     * Constructs a WebElementError for a Section or Page
     *
     * @param section the section that this WebElementError will handle
     */
    public WebElementError(BaseSection section) {
        super(section);
        FailedElements = new ArrayList();
    }
    /**
     * 
     * @return A list of failed elements
     */
    public ArrayList<Field> getFailedElements() {
        return FailedElements;
    }
    /**
     * 
     * @param f  adds a Field that failed to instantiate
     */
    public void addFailedElement(Field f) {
        FailedElements.add(f);
    }
    
    /**
     * Method that modifies this WebElementError FailedElements and merges with
     * other ones
     * @param with WebElementError to merge with 
     */
    public void merge(WebElementError with) {
        logger.info( "Merging with " + with.getOriginName());
        for (Field f : with.getFailedElements()) {
            addFailedElement(f);
        }
    }
    /**
     * TODO: replace with Reports
     * @deprecated
     * @return This WebElementError's failed Fields in a pretty message
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOriginName());
        for (Field field : FailedElements) {
            Element elementData = field.getAnnotation(Element.class);
            sb.append("\n");
            sb.append("  |").append("\n");
            sb.append("  +-").append(field.getName());

            sb.append(" (By ").append(elementData.type()).append(", Name: ").append(elementData.name());
            if (elementData.index() > -1) {
                sb.append(", Index: ").append(elementData.index());
            }
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Field f: FailedElements){
            sb.append(f.getName()).append(", ");
        }
        return sb.toString().trim();
    }

}
