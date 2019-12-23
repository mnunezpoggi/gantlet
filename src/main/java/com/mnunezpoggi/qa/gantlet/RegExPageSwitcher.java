/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.helpers.RegExHashMap;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author mauricio
 */
public class RegExPageSwitcher implements PageSwitcher {

    RegExHashMap<String, BasePage> RegExPages;

    private URL ActualURL;

    private Logger logger = LogHelper.getLogger(this);

    private static final String EMPTY_URL = "http://";

    public RegExPageSwitcher() {
        try {
            this.ActualURL = new URL(EMPTY_URL);
        } catch (MalformedURLException ex) {
            logger.fatal("Malformed URL", ex);
        }
    }

    @Override
    public BasePage pageSwitch(WebDriver driver, BasePage actualPage, HashMap<String, BasePage> pages) {
        if (RegExPages == null) {
            logger.info("Setting RegExHashMap from: " + pages);
            
            RegExPages = new RegExHashMap(pages);
            System.out.println(RegExPages);
        }
        
        try {
            URL newURL = new URL(driver.getCurrentUrl());
            if (!newURL.getPath().equals(ActualURL.getPath())) {
                logger.info("URL changed to " + newURL);
                ActualURL = newURL;
                BasePage bp = RegExPages.get(ActualURL.getPath());
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
