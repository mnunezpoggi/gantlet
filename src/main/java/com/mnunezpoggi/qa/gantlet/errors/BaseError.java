package com.mnunezpoggi.qa.gantlet.errors;

import com.mnunezpoggi.qa.gantlet.BaseSection;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import org.apache.logging.log4j.Logger;

/**
 * Original class that will handle error, to be replaced with reporter<br>
 * TODO replace with Reporter
 * @deprecated
 * 
 */
public abstract class BaseError {
    
    private final BaseSection OriginReference;
    private final String OriginName;
    
    protected final Logger logger = LogHelper.getLogger(this);
    
    public BaseError(BaseSection section){
        this.OriginReference = section;
        this.OriginName = section.getClass().getSimpleName();
    }
    
    public String getOriginName(){
        return OriginName;
    }
    
    public BaseSection getOriginReference(){
        return OriginReference;
    }
    
    public abstract String getMessage();
    
}
