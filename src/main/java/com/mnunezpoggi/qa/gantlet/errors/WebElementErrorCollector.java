package com.mnunezpoggi.qa.gantlet.errors;

import java.util.ArrayList;

/**
 * Class that collects all WebElementErrors <br>
 * Replace with Reports
 * @deprecated
 * @author mauricio
 */
public class WebElementErrorCollector implements ErrorCollector{
    
    ArrayList<WebElementError> Elements;
    
    private static final String TITLE = "+-----------------------------+\n"
                                      + "| Missing WebElements         |\n"
                                      + "+-----------------------------+\n";
    /**
     * Sets the list of WebElementError to empty
     */
    public WebElementErrorCollector(){
        Elements = new ArrayList();
    }

    /**
     * Pretty prints all Errors
     * @return The formatted string
     */
    public String printAllErrors(){
        StringBuilder sb = new StringBuilder();
        sb.append(TITLE);
        for(WebElementError error: Elements){
            sb.append(error.getMessage());
            sb.append("\n\n");
        }
        return sb.toString();
    }
    
    /**
     * Overriden method to collect error
     * @param field BaseError that is mapped to a Field
     */
    @Override
    public void collectError(BaseError field) {
          Elements.add((WebElementError)field);
    }
    
    

    
}
