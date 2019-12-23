
package com.mnunezpoggi.qa.gantlet.driver;

import com.mnunezpoggi.qa.gantlet.driver.factories.ChromeDriverFactory;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import com.google.common.collect.ArrayListMultimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;

/**
 * 
 * 
 */
public class DriverManager extends DriverFactory{

    private static final String FACTORIES_PACKAGE = "com.mnunezpoggi.qa.gantlet.driver.factories";

    private final HashMap<WebDriver, String> LockedDrivers;

    private final ArrayListMultimap<String, WebDriver> AvailableDrivers;

    private final HashMap<String, DriverFactory> DriverFactories;

    private ArrayList<OnDriverRelease> OnDriverReleaseListeners;
    
    private final Logger logger = LogHelper.getLogger(this);

    public DriverManager() {
        LockedDrivers = new HashMap();
        DriverFactories = new HashMap();
        AvailableDrivers = ArrayListMultimap.create();
        OnDriverReleaseListeners = new ArrayList();
        loadFactories();
        setShutdownHook();
    }
    
    public void addDriverFactory(DriverFactory factory){
        DriverFactories.put(factory.getName(), factory);
    }

    public void addOnDriverReleaseListener(OnDriverRelease r) {
        OnDriverReleaseListeners.add(r);
    }

    public void releaseDriver(WebDriver driver) {
        driver.manage().deleteAllCookies();
        String driverConfig = LockedDrivers.get(driver);
        LockedDrivers.remove(driver);
        AvailableDrivers.put(driverConfig, driver);
        for (OnDriverRelease l : OnDriverReleaseListeners) {
            l.releaseDriver(driver);
        }
    }

    @Override
    public WebDriver createDriver(String what) {
        /*
            Minify the string, easier for the compiler
        */
        String json = minify(what);        
        /*
            1. Check if we have available drivers matching our configuration
        */
        WebDriver driver = null;
        List<WebDriver> list = AvailableDrivers.get(json);
        /*
            If there's an avaialble driver, we mark it as locked and return it
        */
        if (!list.isEmpty()) {
            driver = list.get(0);
            AvailableDrivers.remove(json, driver);
            LockedDrivers.put(driver, json);
            return driver;
        }
       /*
            If there are no drivers then:
            Check if its a json object and grab it's name value
            Grab the corresponding factory
        */
        String factoryName = json;
        if(isJson(json)){
            factoryName = getJsonAttribute(json, "name");
            if(factoryName == null){
                logger.warn("There's no name attribute in json string");
                return null;
            }                
        }
        DriverFactory f = DriverFactories.get(factoryName);
        if(f == null){
            logger.warn("There's no factory with name: " + factoryName);
            return null;
        }
        driver = f.createDriver(json);
        LockedDrivers.put(driver, json);
        return driver;
    }

    private void loadFactories() {
        Reflections reflections = new Reflections(FACTORIES_PACKAGE);
        Set<Class<? extends DriverFactory>> factories = reflections.getSubTypesOf(DriverFactory.class);
        for (Class<? extends DriverFactory> factoryClass : factories) {
            try {
                DriverFactory f = factoryClass.newInstance();
                logger.info("Created factory " + f.getName());
                DriverFactories.put(f.getName(), f);
            } catch (InstantiationException | IllegalAccessException ex) {
                LogHelper.getLogger(this).fatal("Coudln't instantiate factories", ex);
            }
        }
    }
    
    public void cleanDriver(WebDriver driver){
        logger.info("Clearing local storage");
        ((JavascriptExecutor)driver).executeScript("window.localStorage.clear();");
        logger.info("Clearing session storage");
        ((JavascriptExecutor)driver).executeScript("window.sessionStorage.clear();");
        driver.manage().deleteAllCookies();
        
    }

    private void setShutdownHook() {
        Runnable r = new Runnable(){
            @Override
            public void run() {
                for(WebDriver w1: AvailableDrivers.values()){
                    w1.quit();
                }
                for(WebDriver w2: LockedDrivers.keySet()){
                    w2.quit();
                }
            }
            
        };
        Runtime.getRuntime().addShutdownHook(new Thread(r));
    }

    @Override
    public String getName() {
        return "";
    }
    
    public static void main(String[] args){
         String DEFAULT = "{name: chrome, arguments:ASDF}";
        
        DriverManager manager = new DriverManager();
        try {
        
        
        WebDriver d = manager.createDriver(DEFAULT);
                
        
        
        d.get("https://google.com");
        
        Thread.sleep(5000);
        
        WebDriver d1 = manager.createDriver("chrome");
        
        
        
        d1.get("https://facebook.com");
        
        Thread.sleep(5000);
        manager.releaseDriver(d);
        
        
        WebDriver d2 = manager.createDriver("{name:chrome,      options:     ['asdf','qwerty']}");
        
        
        d2.get("https://twitter.com");
        
        
        
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
           
        }
    }
   

}
