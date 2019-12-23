package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.WebElementPageSwitcher.KeyFinder;
import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * 
 */
public class WebElementPageSwitcher implements PageSwitcher {
            
    public interface KeyFinder{
        public String findKey(WebElement element);
    }

    private final Element IDHolder;
    private final KeyFinder finder;
    private Logger logger = LogHelper.getLogger(this);
    
    public WebElementPageSwitcher(Element e) {
        this.finder = new KeyFinder(){
            @Override
            public String findKey(WebElement element) {
                return element.getText();
            }
        };
        this.IDHolder = e;
    }
    
    public WebElementPageSwitcher(Element e, KeyFinder f){
        this.finder = f;
        this.IDHolder = e;
    }

    @Override
    public BasePage pageSwitch(WebDriver driver, BasePage actualPage, HashMap<String, BasePage> pages) {
        WebElement tag = findElement (this.IDHolder, driver);
        if(tag == null){
            logger.warn("I can't find the following element: " + IDHolder.name() + " by " + IDHolder.type());
             return null;
        } else {
            String key = finder.findKey(tag);
            BasePage bp = pages.get(key);
            return bp;
        }
       
    }

    private WebElement findElement(Element el, WebDriver driver) throws IndexOutOfBoundsException {
        Element.Type type = el.type();
        String name = el.name();
        int index = el.index();
        switch (type) {
            case CLASS:
                if (index < 0) {
                    return driver.findElement((By.className(name)));
                } else {
                    return driver.findElements(By.className(name)).get(index);
                }

            case ID:
                if (index < 0) {
                    return driver.findElement((By.id(name)));
                } else {
                    return driver.findElements(By.id(name)).get(index);
                }

            case CSS:
                if (index < 0) {
                    return driver.findElement((By.cssSelector(name)));
                } else {
                    return driver.findElements(By.cssSelector(name)).get(index);
                }

            case LINK:
                if (index < 0) {
                    return driver.findElement((By.linkText(name)));
                } else {
                    return driver.findElements(By.linkText(name)).get(index);
                }

            case NAME:
                if (index < 0) {
                    return driver.findElement((By.name(name)));
                } else {
                    return driver.findElements(By.name(name)).get(index);
                }

            case PARTIAL_LINK_TEXT:
                if (index < 0) {
                    return driver.findElement((By.partialLinkText(name)));
                } else {
                    return driver.findElements(By.partialLinkText(name)).get(index);
                }

            case TAG_NAME:
                if (index < 0) {
                    return driver.findElement((By.tagName(name)));
                } else {
                    return driver.findElements(By.tagName(name)).get(index);
                }

            case XPATH:
                if (index < 0) {
                    return driver.findElement((By.xpath(name)));
                } else {
                    return driver.findElements(By.xpath(name)).get(index);
                }

            default:
                return null;
        }
    }

}
