
package com.mnunezpoggi.qa.gantlet.driver.factories;
 
import com.mnunezpoggi.qa.gantlet.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import static com.mnunezpoggi.qa.gantlet.helpers.PlatformInfo.*;

/**
 *
 * @author mauricio
 */
public class FirefoxDriverFactory extends DriverFactory{

    private static final String DRIVER_NAME = "geckodriver";
    
    public FirefoxDriverFactory(){
         System.setProperty("webdriver.gecko.driver", USER_DIR + SEPARATOR + DRIVER_DIR  + PLATFORM + SEPARATOR + DRIVER_NAME);       
    }
    
    @Override
    public String getName() {
        return "firefox";
    }
    @Override
    public WebDriver createDriver(String Json) {
        return new FirefoxDriver();
    }
    
}
