package com.mnunezpoggi.qa.gantlet.annotations;

import com.mnunezpoggi.qa.gantlet.BasePage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Annotation used to define a WebElement inside a {@link BasePage}<br>
 * type() Defines how the WebDriver will search the element <br>
 * name() Defines the value that the WebDriver will search for<br>
 * dynamic() Defines wether the element is static and should always be instantiated<br>
 * index() If there are more WebElements, the index to get it<br>
 * 
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Element {

    public enum Type {
        XPATH,
        CSS,
        CLASS,
        ID,
        LINK,
        TAG_NAME,
        PARTIAL_LINK_TEXT,
        NAME
    }
    
    Type type() default Type.ID;
    String name();
    boolean dynamic() default false;
    int index() default -1;

}
