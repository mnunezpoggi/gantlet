package com.mnunezpoggi.qa.gantlet;

import java.util.HashMap;
import org.openqa.selenium.WebDriver;

/**
 *
 * 
 */
public interface PageSwitcher{
    
    public BasePage pageSwitch(WebDriver driver, BasePage actualPage, HashMap<String, BasePage> pages);

}
