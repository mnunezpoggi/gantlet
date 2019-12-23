/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.helpers;

import java.io.File;
import java.util.Collection;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;

/**
 *
 * 
 */
public class ConfigurationHolder {

    private static final String DEFAULT_CONFIG_PATH = "config/";    
    private static final String XML_EXTENSION = "xml";
    private static final String PROPERTIES_EXTENSION = "properties";
    private static final String[] CONFIG_EXTENSIONS = new String[]{PROPERTIES_EXTENSION, XML_EXTENSION};
    
    private static final Logger logger = LogManager.getLogger(ConfigurationHolder.class);
    
    private static ConfigurationHolder instance;

    public static final ConfigurationHolder getInstance() {
        return getInstance(DEFAULT_CONFIG_PATH);
    }

    public static final ConfigurationHolder getInstance(String path) {
        if (instance == null) {
            logger.debug("Create new from " + path);
            instance = new ConfigurationHolder(path);
        } else {
            if( ! new File(path).getAbsolutePath().equals(instance.ConfigPath.getAbsolutePath())){
                logger.warn("Already instantiated, ignoring " + path);
            }
        }
        return instance;
    }

    private final CompositeConfiguration configuration;
    private final File ConfigPath;    

    private ConfigurationHolder(String path) {
        configuration = new CompositeConfiguration();
        File f = new File(path);
        if (!f.isDirectory()) {
            logger.fatal(path + " is not a directory, exiting");
            System.exit(0);
        }
        ConfigPath = f;
        addSystemConfig();
        addEnvironmentConfig();
        try {
            readConfigFiles();
        } catch (ConfigurationException ex) {
            logger.fatal("Could not read config files");
            logger.fatal("Message: " + ex.getLocalizedMessage());
            System.exit(0);
        }
        
    }

    private void readConfigFiles() throws ConfigurationException {                
        CombinedConfiguration combined_configuration = new CombinedConfiguration(new UnionCombiner());
        Collection<File> files = FileUtils.listFiles(ConfigPath, CONFIG_EXTENSIONS, true);
        Configurations configs = new Configurations();
        for(File file: files){
            logger.debug("Read " + file.getName());
            String extension = FilenameUtils.getExtension(file.getAbsolutePath());
            Configuration config = null;
            switch(extension){
                case XML_EXTENSION:
                    config = configs.xml(file);
                    break;
                case PROPERTIES_EXTENSION:
                    config = configs.properties(file);
                    break;
            }
            combined_configuration.addConfiguration(config);
        }
        configuration.addConfiguration(combined_configuration);
    }

    private void addSystemConfig() {
        configuration.addConfiguration(new SystemConfiguration());
        logger.debug("Added System configuration");
    }
    
    private void addEnvironmentConfig(){
        configuration.addConfiguration(new EnvironmentConfiguration());
        logger.debug("Added Environment configuration");
    }

    public String get(String key) {
        logger.debug("Querying " + key);
        return configuration.getString(key);
    }

}
