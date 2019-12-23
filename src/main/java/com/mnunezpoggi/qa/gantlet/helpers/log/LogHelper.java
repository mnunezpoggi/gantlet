package com.mnunezpoggi.qa.gantlet.helpers.log;

//import java.util.logging.ConsoleHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.apache.logging.log4j.Logger;
/**
 * Helper method that returns a new {@link Logger} object for a name<br>
 * Adds the custom formatter {@link LogFormatter}
 *
 * 
 */
public class LogHelper {

//    public static Logger getLogger(String name) {
//        ConsoleHandler ch = new ConsoleHandler();
//        ch.setFormatter(new LogFormatter());
//        Logger logger = Logger.getLogger(name);
//        logger.setUseParentHandlers(false);
//        logger.addHandler(ch);
//        return logger;
//    }

    public static Logger getLogger(Object o) {
       
       
        Logger logger = LogManager.getLogger(o.getClass());
       
        return logger;
    }

}
