package com.mnunezpoggi.qa.gantlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class used to define a Page<br>
 * Its fields should be the WebElements the framework will search for<br>
 * 
 * TODO:  add URL helper methods
 * 
 */
public abstract class BasePage extends PageSection{
    

    public abstract String getID();
   
    
    /**
     * Parses the query portion of a URL and maps it to a Map of values
     * @return  Map matching the query
     */
    public Map<String, String> urlParametersToMap(){
        Map<String, String> parameters = new HashMap();
        try {            
            URL url = new URL(driver.getCurrentUrl());
            for(String pairs: url.getQuery().split("&")){
                if(pairs.isEmpty())
                    continue;
                String[] keyValue = pairs.split("=");
                parameters.put(keyValue[0], keyValue[1]);
            }
            return parameters;
            
        } catch (MalformedURLException ex) {
            return null;
        }
    }
    
    public String getAlertText(){
        return driver.switchTo().alert().getText();
    }
    
    public void acceptAlert(){
        driver.switchTo().alert().accept();
    }
    
    public void cancelAlert(){
        driver.switchTo().alert().dismiss();
    }
    
    public void setAlertText(String text){
        driver.switchTo().alert().sendKeys(text);        
    }    
}
