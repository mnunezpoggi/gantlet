/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.pages;

import com.mnunezpoggi.qa.gantlet.BasePage;
import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.annotations.Element.Type;
import org.openqa.selenium.WebElement;

/**
 *
 * @author mauricio
 */
public class WikipediaWelcomePage extends BasePage{
    
    @Element(dynamic = false, name = "searchInput")
    public WebElement SearchInput;
    
    @Element(dynamic = false, name = "#search-form > fieldset > button", type = Type.CSS)
    public WebElement SearchButton;
    
    public void search(String what){
        setText("SearchInput", what);
        click("SearchButton");
    }
    
    @Override
    public String getID() {
        return "/";
    }
    
}
