/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Gerardo Miranda
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementList {
    String name();
    boolean dynamic() default false;
    
    /**
     * This starting index is to ignore the first elements in the list.<br>
     * This index starts at 0
     * @return 
     */
    int startingIndex() default 0;
}
