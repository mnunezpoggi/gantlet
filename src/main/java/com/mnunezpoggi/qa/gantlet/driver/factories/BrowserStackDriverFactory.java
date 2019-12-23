/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.driver.factories;

import com.mnunezpoggi.qa.gantlet.driver.DriverFactory;
import com.mnunezpoggi.qa.gantlet.helpers.ConfigurationHolder;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * 
 */
public class BrowserStackDriverFactory extends DriverFactory {

    private Logger log = LogHelper.getLogger(this);
    private ConfigurationHolder config = ConfigurationHolder.getInstance();

    private JsonParser jp = new JsonParser();

    private static final String BROWSER_STACK_URL = "@hub-cloud.browserstack.com/wd/hub";

    public BrowserStackDriverFactory() {

    }

    @Override
    public String getName() {
        return "browserstack";
    }

    @Override
    public WebDriver createDriver(String Json) {
        checkCredentials();
        if (isJson(Json)) {
            log.info("Building browserstack driver from: " + Json);
            return createDriverFromJson(Json);
        } else {
            String defaultJson = config.get("browserstack_json");
            return createDriverFromJson(defaultJson);
        }
    }

    private WebDriver createDriverFromJson(String json) {
        Map<String, String> parsed = new Gson().fromJson(json, HashMap.class);
        parsed.remove("name");
        Capabilities config = new DesiredCapabilities(parsed);
        StringBuilder sb = new StringBuilder();
        sb.append("https://").append(parsed.get("username")).append(":").append(parsed.get("key")).append(BROWSER_STACK_URL);
        try {
            URL url = new URL(sb.toString());
            return new RemoteWebDriver(url, config);
        } catch (MalformedURLException ex) {
            log.fatal("Couldn't instantiate browserstack driver");
            System.exit(0);
        }
        return null;
    }

    private void checkCredentials() {
        if (config.get("BROWSERSTACK_ACCESSKEY") == null || config.get("BROWSERSTACK_USER") == null) {
            log.fatal("No credentials supplied for BrowserStack, exiting");
            System.exit(0);
        }
    }

}
