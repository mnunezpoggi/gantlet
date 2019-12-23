/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.driver.factories;

import com.mnunezpoggi.qa.gantlet.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 *
 * @author mauricio
 */
public class HTMLUnitDriverFactory extends DriverFactory{

    @Override
    public String getName() {
        return "htmlunit";
    }

    @Override
    public WebDriver createDriver(String Json) {
        // Enable javascript with (true)
        return new HtmlUnitDriver(true);
    }
    
}
