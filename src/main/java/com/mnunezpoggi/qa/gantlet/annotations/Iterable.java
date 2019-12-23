/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.annotations;

import com.mnunezpoggi.qa.gantlet.IterableSection;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Annotation that defines an Iterable (repeatable {@link Section}). 
 * Fields under this annotation must be a {@link List} of an
 * {@link IterableSection} object.
 * @author Gerardo Miranda
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Iterable {
    
    /**
     * Container css selector with a '?' character instead of the index
     * @return
     */
    String container() default "";
    
    /**
     * Starting index of the iterable section, in case of headers or
     * similar elements present using the initial index positions
     * @return 
     */
    int startingIndex() default 1;
    
    /**
     * Flag to auto load this iterable, useful for dynamic sections or
     * big sections
     * @return 
     */
    boolean autoset() default true;
    
    
}
