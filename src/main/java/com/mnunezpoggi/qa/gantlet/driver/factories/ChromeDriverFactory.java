package com.mnunezpoggi.qa.gantlet.driver.factories;

import com.mnunezpoggi.qa.gantlet.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import static com.mnunezpoggi.qa.gantlet.helpers.PlatformInfo.*;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *
 * @author mauricio
 */
public class ChromeDriverFactory extends DriverFactory {

    private static final String DRIVER_NAME = "chromedriver";

    private static final String DEFAULT_DRIVER_CONFIG = "{name: chrome, "
            + "arguments: [start-maximized, disable-infobars],"
            + "preferences: "
            + "{credentials_enable_service: false,"
            + " profile.password_manager_enabled: false}"
            + "}";

    private static final String HEADLESS_DRIVER_CONFIG = "{name: chrome, "
            + "arguments : ['start-maximized','--headless','--disable-gpu','--no-sandbox','--disable-dev-shm-usage','--enable-features=NetworkService,NetworkServiceInProcess']}";
     
    public ChromeDriverFactory() {
        if(DRIVER_DIR != null && !DRIVER_DIR.isEmpty()){
            System.setProperty("webdriver.chrome.driver", USER_DIR + SEPARATOR + DRIVER_DIR + PLATFORM + SEPARATOR + DRIVER_NAME);
        }
        System.setProperty("webdriver.chrome.silentOutput", "true");
    }

    @Override
    public String getName() {
        return "chrome";
    }

    @Override
    public WebDriver createDriver(String Json) {
        String conf = HEADLESS ? HEADLESS_DRIVER_CONFIG : DEFAULT_DRIVER_CONFIG;
        if (isJson(Json)) {
            return createDriverFromJson(Json);
        } else {
            return createDriverFromJson(conf);
        }
    }

    private WebDriver createDriverFromJson(String json) {
        Map<String, String> config = jsonToMap(json);
        List<String> arguments = removeQuotes(jsonToList(config.get("arguments")));
        String preferences = config.get("preferences");
        ChromeOptions chromeOptions = new ChromeOptions();
        if (arguments != null) {
            chromeOptions.addArguments(arguments);
        }
        if (preferences != null) {
            chromeOptions.setExperimentalOption("prefs", parsePreferences(preferences));
        }
        return new ChromeDriver(chromeOptions);

    }
    
    

    private Map<String, Object> parsePreferences(String prefs){
        HashMap m = new HashMap();
        for(Map.Entry<String, JsonElement> set: parser.parse(prefs).getAsJsonObject().entrySet()){
            m.put(set.getKey(), set.getValue().getAsBoolean());
        }
        return m;
    }

}
