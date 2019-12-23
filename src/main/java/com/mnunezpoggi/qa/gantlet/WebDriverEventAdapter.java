
package com.mnunezpoggi.qa.gantlet;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

/**
 * Class used as the adapter for {@link WebDriverEventListener}<br>
 * So that classes won't have to override all of WebDriverEventListener's methods
 * (quite a lot)
 * 
 */
public class WebDriverEventAdapter implements WebDriverEventListener {

    @Override
    public void beforeNavigateTo(String string, WebDriver wd) {

    }

    @Override
    public void afterNavigateTo(String string, WebDriver wd) {
         
    }

    @Override
    public void beforeNavigateBack(WebDriver wd) {

    }

    @Override
    public void afterNavigateBack(WebDriver wd) {

    }

    @Override
    public void beforeNavigateForward(WebDriver wd) {

    }

    @Override
    public void afterNavigateForward(WebDriver wd) {

    }

    @Override
    public void beforeNavigateRefresh(WebDriver wd) {

    }

    @Override
    public void afterNavigateRefresh(WebDriver wd) {

    }

    @Override
    public void beforeFindBy(By by, WebElement we, WebDriver wd) {

    }

    @Override
    public void afterFindBy(By by, WebElement we, WebDriver wd) {

    }

    @Override
    public void beforeClickOn(WebElement we, WebDriver wd) {

    }

    @Override
    public void afterClickOn(WebElement we, WebDriver wd) {

    }

    @Override
    public void beforeChangeValueOf(WebElement we, WebDriver wd, CharSequence[] css) {

    }

    @Override
    public void afterChangeValueOf(WebElement we, WebDriver wd, CharSequence[] css) {

    }

    @Override
    public void beforeScript(String string, WebDriver wd) {

    }

    @Override
    public void afterScript(String string, WebDriver wd) {

    }

    @Override
    public void onException(Throwable thrwbl, WebDriver wd) {

    }

    @Override
    public void beforeAlertAccept(WebDriver wd) {

    }

    @Override
    public void afterAlertAccept(WebDriver wd) {
        
    }

    @Override
    public void afterAlertDismiss(WebDriver wd) {

    }

    @Override
    public void beforeAlertDismiss(WebDriver wd) {

    }

    @Override
    public void beforeSwitchToWindow(String string, WebDriver wd) {
        
    }

    @Override
    public void afterSwitchToWindow(String string, WebDriver wd) {
        
    }

    @Override
    public <X> void beforeGetScreenshotAs(OutputType<X> ot) {
        
    }

    @Override
    public <X> void afterGetScreenshotAs(OutputType<X> ot, X x) {
        
    }

}
