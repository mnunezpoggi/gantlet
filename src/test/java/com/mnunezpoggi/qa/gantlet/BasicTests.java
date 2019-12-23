/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet;

import com.mnunezpoggi.qa.gantlet.helpers.ConfigurationHolder;
import org.testng.annotations.Test;

/**
 *
 * @author mauricio
 */
public abstract class BasicTests extends BaseTest{

    @Override
    public String getPagesPackages() {
        return "com.mnunezpoggi.qa.gantlet.pages";
    }

    @Override
    public String getSourcesPackage() {
        return "com.mnunezpoggi.qa.gantlet.pages";
    }

    
}
