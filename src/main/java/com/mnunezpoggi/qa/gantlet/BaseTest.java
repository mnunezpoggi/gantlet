package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.driver.DriverManager;
import com.mnunezpoggi.qa.gantlet.helpers.ConfigurationHolder;
import com.mnunezpoggi.qa.gantlet.helpers.sourcecode.SourceCode;
import com.mnunezpoggi.qa.gantlet.helpers.sourcecode.SourceCodeGrabber;
import java.io.IOException;
import java.lang.reflect.Method;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;

/**
 *
 * 
 */
public abstract class BaseTest extends Assertions implements OnPageChange {

    public static PageManager PageM;
    public static DriverManager DriverM;
    public static BasePage CurrentPage;

    private static SourceCodeGrabber SourceGrabber;
    private static ConfigurationHolder config = ConfigurationHolder.getInstance();
    private static final String BROWSER_VAR = "driver";
    private static final String BASE_URL_VAR = "base_url";

    private static final String SUITE_IDENTATION = "* ";
    private static final String CLASS_IDENTATION = "  * ";
    private static final String METHOD_IDENTATION = "    * ";
    private static final String MESSAGE_IDENTATION = "     · ";
    private static final String RUNNING_TEXT = "Running ";
    private static final String ENTERING_CLASS_TEXT = "Entering class ";
    private static final String EXECUTING_TEXT = "Executing ";


    public abstract String getPagesPackages();

    public abstract String getSourcesPackage();
    
    /*
        For removing logging (cosmetic thing)
     */
    private static final Logger[] pin;

    static {
        pin = new Logger[]{
            Logger.getLogger("com.gargoylesoftware.htmlunit"),
            Logger.getLogger("org.apache.commons.httpclient"),
            Logger.getLogger("org.openqa.selenium.remote.ProtocolHandshake")
        };

        for (Logger l : pin) {
            l.setLevel(Level.OFF);
        }
    }

    @BeforeSuite
    public void managersInit(ITestContext context) throws IOException, InstantiationException, IllegalAccessException {
        DriverM = new DriverManager();
        PageM = new PageManager(DriverM.createDriver(config.get(BROWSER_VAR)));
        PageM.addOnPageChangeListener(this);
        PageM.loadPages(getPagesPackages());
        SourceGrabber = new SourceCodeGrabber(getSourcesPackage());
        System.out.println(SUITE_IDENTATION + RUNNING_TEXT + context.getSuite().getName());
    }

    @BeforeClass
    public void printClassEntering(ITestContext context) {
        System.out.println(CLASS_IDENTATION + ENTERING_CLASS_TEXT + this.getClass().getSimpleName());
    }

    @BeforeMethod
    public void printMethodExecution(Method method) {
        System.out.println(METHOD_IDENTATION + EXECUTING_TEXT + method.getName());
        PageM.to(config.get(BASE_URL_VAR));
    }


    @AfterMethod
    public void printCode(ITestResult result) throws ClassNotFoundException, IOException {
        if (result.getStatus() == ITestResult.FAILURE) {
            printTestMessage(result.getThrowable().getLocalizedMessage());
            for (SourceCode code : SourceGrabber.grab(result.getThrowable().getStackTrace())) {
                System.out.println(code);
            }
        }
    }

    @AfterMethod
    public void cleanDriver(ITestResult result) {
        DriverM.cleanDriver(PageM.getDriver());
    }

    public void printTestMessage(Object message){
        System.out.println(MESSAGE_IDENTATION + message);
    }

    @Override
    public void pageChanged(BasePage page) {
        CurrentPage = page;
    }

}
