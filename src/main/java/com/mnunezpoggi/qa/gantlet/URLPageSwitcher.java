package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 *
 * 
 */
public class URLPageSwitcher implements PageSwitcher {

    private URL ActualURL;

    private Logger logger = LogHelper.getLogger(this);
    
    private static final String EMPTY_URL = "http://";

    public URLPageSwitcher() {
        try {
            this.ActualURL = new URL(EMPTY_URL);
        } catch (MalformedURLException ex) {
            logger.fatal("Malformed URL", ex);
        }
    }

    @Override
    public BasePage pageSwitch(WebDriver driver, BasePage actualPage, HashMap<String, BasePage> pages) {
        try {
            URL newURL = new URL(driver.getCurrentUrl());
            if (!newURL.getPath().equals(ActualURL.getPath())) {
                logger.info("URL changed to " + newURL);
                ActualURL = newURL;
                BasePage bp = pages.get(ActualURL.getPath());
                if (bp == null) {
                    logger.warn("I don't have a page for this URL: " + ActualURL.getPath());
                    return null;
                } else {
                    return bp;               
                }
            } else {
                return actualPage;
            }
        } catch (MalformedURLException ex) {
            logger.fatal("Malformed URL", ex);            
            return null; 
        }
    }

}
