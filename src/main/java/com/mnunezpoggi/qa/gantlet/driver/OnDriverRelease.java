/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.driver;

import org.openqa.selenium.WebDriver;

/**
 *
 * @author mauricio
 */
public interface OnDriverRelease {
    
    public void releaseDriver(WebDriver driver);
    
}
