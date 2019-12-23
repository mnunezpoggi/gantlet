package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.errors.WebElementErrorCollector;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import static org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Class that handles the defined instances of BasePage Extends an adapter for
 * interface {@link WebDriverEventListener}<br>
 * Using the adapter automatically detects the current page and sets it to field
 * ActualPage.
 *
 * 
 * @see BasePage
 */
public class PageManager extends WebDriverEventAdapter {

    protected HashMap<String, BasePage> PagesURLs;

    protected HashMap<String, BasePage> PagesNames;

    private final ArrayList<OnPageChange> OnPageChangeListeners;

    private final WebElementErrorCollector _WebElementErrorCollector;

//    private URL ActualURL;
    private BasePage ActualPage;

    private final Logger logger = LogHelper.getLogger(this);

    //TODO: Needs to be set by properties or external parameters
    private final int pageLoadTimeOut = 10;

    private EventFiringWebDriver EventDriver;

    /*
     * =======================================================================
     * =======================================================================
     */
    private PageSwitcher switcher;

    public final void setPageSwitcher(PageSwitcher switcher) {
        this.switcher = switcher;
    }

    /*
     * =======================================================================
     * =======================================================================
     */
    /**
     * Constructs a new PageManager setting all to Empty.<br>
     * Self sets as handler for the WebDriver
     *
     * @param driver the driver that this PageManager will handle. Is
     * transformed to EventFiringWebDriver for event handling.
     */
    public PageManager(WebDriver driver) {
        logger.info("Creating HashMaps");
        PagesURLs = new HashMap();
        PagesNames = new HashMap();
        _WebElementErrorCollector = new WebElementErrorCollector();

        EventDriver = new EventFiringWebDriver(driver);
        logger.info("Registering EventDriver");
        EventDriver.register(this);
        OnPageChangeListeners = new ArrayList();
        OnPageChangeListeners.add(new OnPageChange() {
            @Override
            public void pageChanged(BasePage page) {
                page.setAllStaticElements();
            }
        });
        setPageSwitcher(new URLPageSwitcher());
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        
    }

    /**
     * Method that loads all clases that extends {@link BasePage} and are non-abstract
     *
     * @param packageDir Fully qualified package name to search for BasePages
     * e.g. com.mnunezpoggi.qa.pages
     * @throws IOException If it can't access the classes on the filesystem
     * @throws InstantiationException If it can't instantiate the classes
     * @throws IllegalAccessException If the access is restricted to the classes
     */
    public void loadPages(String packageDir) throws IOException, InstantiationException, IllegalAccessException {
        logger.info("Loading all BasePages from package " + packageDir);
        ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());

         for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive(packageDir)) {
            Class basePageClass = classInfo.load();
            if (Modifier.isAbstract(basePageClass.getModifiers())) {
                logger.info("Skip abstract page: " + basePageClass.getSimpleName());
                continue;
            }
            if(!BasePage.class.isAssignableFrom(basePageClass)){
                logger.debug("Skip non-page " + basePageClass.getSimpleName());
                continue;
            }
            logger.info("Creating page: " + basePageClass.getSimpleName());
            BasePage basePage = (BasePage) basePageClass.newInstance();
            basePage.setDriver(EventDriver);
            basePage.addErrorCollector(_WebElementErrorCollector);
            PagesNames.put(basePageClass.getSimpleName(), basePage);
            PagesURLs.put(basePage.getID(), basePage);
        }
    }
    
    /**
     * Method that returns all of the instantiated (non-abstract) pages this PageManager is handling
     * 
     * @return Collection of Pages
     */
    public Collection<BasePage> getPages(){
        return PagesURLs.values();
    }
    
    /**
     * Method that adds a single page. <br>WARNING<br>
     * This should be used by specific tests only.
     * 
     * @param page The page to be added to this PageManager
     */
    public void addPage(BasePage page){
        page.setDriver(EventDriver);
        page.addErrorCollector(_WebElementErrorCollector);
        PagesNames.put(page.getClass().getSimpleName(), page);
        PagesURLs.put(page.getID(), page);
    }

    /**
     * Function that returns a {@link BasePage} by its ID
     *
     * @param id The ID to search
     * @return The page mapped to the url
     */
    public BasePage getPageByID(String id) {
        return PagesURLs.get(id);
    }

    /**
     * Function that returns a {@link BasePage} by its name
     *
     * @param name The page's name to search for
     * @return the BasePage associated to that name
     */
    public BasePage getPageByName(String name) {
        return PagesNames.get(name);
    }

    /**
     * Function that returns the WebDriver used by this Manager
     *
     * @return WebDriver
     */
    public WebDriver getDriver() {
        return EventDriver;
    }

    /**
     * Method that overrides
     * {@link WebDriverEventAdapter#afterClickOn(org.openqa.selenium.WebElement, org.openqa.selenium.WebDriver)}<br>
     * Is called after {@link WebElement#click()}<br>
     * 1. Checks if the page was changed after a click<br>
     * 2. If the page was changed it grabs the new BasePage by its ID and sets
     * it to ActualPage<br>
     * 3. Currently it calls {@link BasePage#setAllStaticElements()}<br>
     * 4. This method calls the implementation of PageSwitcher
     *
     * @param we
     * @param wd
     */
    @Override
    public void afterClickOn(WebElement we, WebDriver wd) {        
        waitForPageToLoad(wd);
        BasePage newPage = switcher.pageSwitch(wd, ActualPage, PagesURLs);
        if (newPage != null && newPage != ActualPage) {
            ActualPage = newPage;
            callOnPageChangeListeners(ActualPage);
        }
    }

    /*
        TO ADD DOC
    
     */
    @Override
    public void afterChangeValueOf(WebElement we, WebDriver wd, CharSequence[] css) {
        BasePage newPage = switcher.pageSwitch(wd, ActualPage, PagesURLs);
        if (newPage != null && newPage != ActualPage) {
            ActualPage = newPage;
            callOnPageChangeListeners(ActualPage);
        }
    }

    /**
     * Method that overrides
     * {@link WebDriverEventAdapter#afterNavigateTo(java.lang.String, org.openqa.selenium.WebDriver)}<br>
     * Is called after {@link WebDriver#get(java.lang.String)}<br>1. Checks if
     * the page was changed after a click <br>2. If the page was changed it
     * grabs the new BasePage by its ID and sets it to ActualPage<br> 3.
     * Currently it calls {@link BasePage#setAllStaticElements()}<br> This
     * method calls the implementation of PageSwitcher
     *
     * @param string
     * @param wd
     */
    @Override
    public void afterNavigateTo(String string, WebDriver wd) {
        waitForPageToLoad(wd);
        logger.info("WebDriver navigate to: " + wd.getCurrentUrl());
        BasePage newPage = switcher.pageSwitch(wd, ActualPage, PagesURLs);
        if (newPage != null) {
            ActualPage = newPage;
            callOnPageChangeListeners(ActualPage);
        }

    }

    /**
     * Adds an OnPageChangeListener <br>
     * This implementation will be called each time gantlet detects that a page
     * has changed.
     *
     * @param listener The implementation to add
     */
    public void addOnPageChangeListener(OnPageChange listener) {
        OnPageChangeListeners.add(listener);
    }
    
    public void removeOnPageChangeListener(OnPageChange listener){
        OnPageChangeListeners.remove(listener);
    }
    
    public void removeOnPageChangeListener(int index) throws IndexOutOfBoundsException{
        if(index >= OnPageChangeListeners.size()){
            throw new IndexOutOfBoundsException();
        } else{
            OnPageChangeListeners.remove(index);
        }
    }

    /**
     * Helper method that loops through all added listeners and calls them upon
     * on page event
     *
     * @param page The actual page that has changed
     */
    private void callOnPageChangeListeners(BasePage page) {
        for (OnPageChange listeners : OnPageChangeListeners) {
            listeners.pageChanged(page);
        }
    }

    /**
     * Wrapper method for {@link WebDriver#get(java.lang.String)}
     *
     * @param url
     */
    public void to(String url) {
        EventDriver.get(url);
    }

    /**
     * Wrapper method for {@link Navigation#back()} Also refreshes
     */
    public void back() {
        EventDriver.navigate().back();
        EventDriver.navigate().refresh();
    }

    /**
     * Returns WebElementErrorCollector TODO: replace with Reporter
     *
     * @deprecated
     * @return This instance's WebElementErrorCollector
     */
    public WebElementErrorCollector getCollector() {
        return _WebElementErrorCollector;
    }

    /**
     * Returns ActualPage
     *
     * @return the ActualPage
     */
    public BasePage getActualPage() {
        return ActualPage;
    }
    
    private void waitForPageToLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoad;
        pageLoad = new
                    ExpectedCondition<Boolean>() {
                        @Override
                        public Boolean apply(WebDriver driver) {
                            return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
                        }
                    };
        Wait<WebDriver>wait = new WebDriverWait(driver, this.pageLoadTimeOut);
        
        try {
            wait.until(pageLoad);
        } catch (Throwable pageLoadWaitError) {
            logger.fatal("Page doesn't fully load after " + 
                    this.pageLoadTimeOut + " seconds");
        }
    }

    /**
     * In case of asynchronous page navigation, this method waits for the page
     * corresponding with the pageName parameter to be loaded by the browser
     * and updates the actualPage of this PageManager with that page
     * 
     * @param pageName name of the page class
     * 
     * @throws IllegalArgumentException If the pageName does not match any
     * page in this PageManager
     */
    public void waitForPageChange(String pageName) throws IllegalArgumentException{
        Wait<WebDriver>wait = new WebDriverWait(EventDriver, this.pageLoadTimeOut);
        BasePage newPage = getPageByName(pageName);
        if (newPage == null){
            throw new IllegalArgumentException("The page " + pageName + 
                    " does not exist");
        }
        wait.until(ExpectedConditions.urlMatches(newPage.getID()));
        waitForPageToLoad(EventDriver);
        newPage = switcher.pageSwitch(EventDriver, ActualPage, PagesURLs);
        if (newPage != null && newPage != ActualPage) {
            ActualPage = newPage;
            callOnPageChangeListeners(ActualPage);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (BasePage page : this.PagesNames.values()) {
            s.append(page.getClass().getSimpleName());
        }
        return s.toString();
    }

}
