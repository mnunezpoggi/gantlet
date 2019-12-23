
package com.mnunezpoggi.qa.gantlet.errors;

/**
 * Interface used to define ErrorCollectors
 * @deprecated
 * @author mauricio
 */
public interface ErrorCollector {
    
    public void collectError(BaseError field);
    
}
