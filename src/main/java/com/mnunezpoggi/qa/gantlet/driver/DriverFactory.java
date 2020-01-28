
package com.mnunezpoggi.qa.gantlet.driver;

import com.mnunezpoggi.qa.gantlet.driver.factories.ChromeDriverFactory;
import com.mnunezpoggi.qa.gantlet.driver.factories.FirefoxDriverFactory;
import com.mnunezpoggi.qa.gantlet.helpers.ConfigurationHolder;
import com.mnunezpoggi.qa.gantlet.helpers.JsonHandler;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * 
 * 
 */
public abstract class DriverFactory extends JsonHandler{
    
    protected static final ConfigurationHolder config = ConfigurationHolder.getInstance();

    protected static final String DRIVER_DIR_VAR = "driver_dir";

    protected static final boolean HEADLESS = config.get("headless") != null;
    
    protected static String DRIVER_DIR = config.get(DRIVER_DIR_VAR);

    public abstract String getName();
       
    public abstract WebDriver createDriver(String Json);
   
    
    public DesiredCapabilities jsonToCapabilities(String json){        
        return new DesiredCapabilities(jsonToMap(json));
    }
    
    
}
