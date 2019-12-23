package com.mnunezpoggi.qa.gantlet.helpers;

import java.io.File;

/**
 *
 * 
 */
public class PlatformInfo {

    public static final Platform PLATFORM;    
    public static final char SEPARATOR = File.separatorChar;
    public static final String USER_DIR = System.getProperty("user.dir");

    public enum Platform {
        linux64,
        win32,
        macOS
    }

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            PLATFORM = Platform.linux64;
        } else if (os.contains("windows")) {
            PLATFORM = Platform.win32;
        } else {
            PLATFORM = Platform.macOS;
        }

    }
}
