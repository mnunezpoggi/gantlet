package com.mnunezpoggi.qa.gantlet.helpers;

import java.util.ResourceBundle;

/**
 * Helper class extracted from encore-automation<br>
 * Used to grab the global configuration<br>
 * TODO: replace with Apache Commons Configurations 
 * @deprecated
 * 
 */
public class ConfigurationHelper {

    private static final ResourceBundle DefaultConfiguration;

    public static final String BASE_URL;

    public static final String BROWSER;

    public static final String DRIVER_DIR;

    public static final String USERNAME;

    public static final String PASSWORD;
    
    public static final String SERVER_HOST;
    
    public static final String SERVER_USER;
    
    public static final String SERVER_USER_KEY;
    
    public static final String DB_HOST;
    
    public static final String DB_USER;
    
    public static final String DB_PASSWORD;
    
    public static final String DB_SCHEMA;

    static {
        DefaultConfiguration = ResourceBundle.getBundle("DefaultConfiguration");
        //=================================================================================
        String _browser = System.getProperty("browser");
        if (_browser == null || _browser.isEmpty() || _browser.equals("${browser}")) {
            BROWSER = DefaultConfiguration.getString("browser");
        } else {
            BROWSER = _browser;
        }
        //=================================================================================
        //=================================================================================
        String _url = System.getProperty("base_url");
        if (_url == null || _url.isEmpty() || _url.equals("${base_url}")) {
            BASE_URL = DefaultConfiguration.getString("base_url");
        } else {
            BASE_URL = _url;
        }
        //=================================================================================
        //=================================================================================
        String driver_dir = System.getProperty("driver_dir");
        if (driver_dir == null || driver_dir.isEmpty() || driver_dir.equals("${driver_dir}")) {
            DRIVER_DIR = DefaultConfiguration.getString("driver_dir");
        } else {
            DRIVER_DIR = driver_dir;
        }
        //=================================================================================
        //=================================================================================
        USERNAME = DefaultConfiguration.getString("username");
        //=================================================================================
        //=================================================================================
        PASSWORD = DefaultConfiguration.getString("password");
        //=================================================================================
        SERVER_HOST = DefaultConfiguration.getString("server_host");
        //=================================================================================
        SERVER_USER = DefaultConfiguration.getString("server_user");
        //=================================================================================
        SERVER_USER_KEY = DefaultConfiguration.getString("server_user_key");
        //=================================================================================
        DB_HOST = DefaultConfiguration.getString("db_host");
        //=================================================================================
        DB_USER = DefaultConfiguration.getString("db_user");
        //=================================================================================
        DB_PASSWORD = DefaultConfiguration.getString("db_password");
        //=================================================================================
        DB_SCHEMA = DefaultConfiguration.getString("db_schema");
        
    }
}
