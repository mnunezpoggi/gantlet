/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.driver.factories;

import com.mnunezpoggi.qa.gantlet.driver.DriverFactory;
import com.mnunezpoggi.qa.gantlet.helpers.ConfigurationHolder;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author mnunez
 */
/*
    This needs to be changed to a json based parametrized
 */
public class RemoteDriverFactory extends DriverFactory{
    
    private static final int REMOTE_TIMEOUT = 30000;
    
    private ConfigurationHolder config = ConfigurationHolder.getInstance();
    private Logger log = LogHelper.getLogger(this);
    
    @Override
    public String getName() {
        return "remote";
    }

    @Override
    public WebDriver createDriver(String json) {
        //  Map<String, String> config = jsonToMap(json);
        String remoteUrl = config.get("remote_driver_url");
        String remoteBrowser = config.get("remote_driver_browser").toLowerCase();
        int remoteTimeout;
        try {
            remoteTimeout = Integer.parseInt(config.get("remote_driver_timeout"));
            log.info("Set remote timeout to " + remoteTimeout);
        } catch (Exception ex) {
            remoteTimeout = REMOTE_TIMEOUT;
            log.warn("Failed to set specified timeout, defaulting to " + remoteTimeout);
        }
        DesiredCapabilities dc = null;
        switch (remoteBrowser) {
            case "chrome":
                dc = DesiredCapabilities.chrome();
                break;
            case "firefox":
                dc = DesiredCapabilities.firefox();
                break;
            default:
                log.fatal("Unknown remote WebDriver");
                System.exit(0);
                break;
        }
        URL url = null;
        try {
            url = new URL(remoteUrl);
        } catch (MalformedURLException ex) {
            log.fatal("Malformed URL, exiting");
            System.exit(0);
        }
        log.info("Building RemoteDriver: " + remoteUrl + " driver: " + remoteBrowser);
        RemoteWebDriver driver = new RemoteWebDriver(url, dc);
        driver.manage().window().maximize();
        return driver;
    }

}
