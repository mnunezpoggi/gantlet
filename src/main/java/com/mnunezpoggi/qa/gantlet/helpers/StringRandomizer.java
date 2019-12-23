/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.helpers;

import java.math.BigInteger;
import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author mauricio
 */
public class StringRandomizer {
    
    public static final int ADD = 0;
    public static final int REMOVE = 1;
    
    private static Random rnd = new Random();
    
    public static String randomizeString(String string){
        int size = rnd.nextInt(string.length());
        if(size < 1) size++;
        return randomizeString(string, rnd.nextInt(2), size);
                
    }
    
    public static String randomizeString(String string, int type, int size){
        
        switch(type){
            case ADD:
                return string + getRandomString(size);
                
            case REMOVE:
                return string.substring(0, string.length() - size);
            default:
                return null;
        }
    }
    
    public static String getRandomString(int size){
        return RandomStringUtils.random(size, true, true);
    }
    
    public static String getRandomString(){
        return RandomStringUtils.randomAscii(rnd.nextInt(200) + 1);
    }
    
}
