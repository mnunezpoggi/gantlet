/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet;

import org.testng.annotations.Test;

/**
 *
 * @author mauricio
 */
public class WikiTest extends BasicTests {
    
    @Test
    public void searchUSA(){
        CurrentPage.setText("SearchInput", "United States");
        CurrentPage.click("SearchButton");
    }
    
}
