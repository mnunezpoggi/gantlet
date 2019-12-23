/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.helpers.sourcecode;

import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

/**
 *
 * 
 */
public class SourceCodeGrabber {

    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String SRC = "src";
    private static final char SEPARATOR = File.separatorChar;
    private static final String TEST = "test";
    private static final String MAIN = "main";

    private final Logger log = LogHelper.getLogger(this);

    private final String PackageName;

    public SourceCodeGrabber(String pakage) {
        log.debug("Package " + pakage);
        this.PackageName = pakage;
    }

    public List<SourceCode> grab(StackTraceElement[] es) throws ClassNotFoundException, IOException {
        LinkedList<SourceCode> list = new LinkedList();
        log.debug("Looping through StackTrace");
        for (StackTraceElement e : es) {
            log.debug("Class " + e.getClassName());
            Class clazz = null;
            try {
                clazz = Class.forName(e.getClassName());
                if (!clazz.getName().contains(PackageName)) {
                    log.trace("Skip " + clazz.getSimpleName());
                    continue;
                }
            } catch (ClassNotFoundException ex) {
                log.warn("Not found " + e.getClassName());
                continue;
            }

            SourceCode sc = new SourceCode();
            sc.ClassName = clazz.getSimpleName();
            sc.line = e.getLineNumber();
            String path = getPath(clazz, e.getFileName());
            sc.code = trim((ArrayList) FileUtils.readLines(new File(path)), e.getLineNumber());
            list.add(sc);
        }
        log.debug("Reversing list");
        Collections.reverse(list);
        return list;
    }

    private String getPath(Class e, String fileName) {
        StringBuilder sb = new StringBuilder();
        String path = e.getCanonicalName().replace('.', SEPARATOR);
        String ext = FilenameUtils.getExtension(fileName);
        sb.append(USER_DIR)
                .append(SEPARATOR)
                .append(SRC)
                .append(SEPARATOR);
        if (isTest(e)) {
            sb.append(TEST);
        } else {
            sb.append(MAIN);
        }
        sb.append(SEPARATOR)
                .append(ext)
                .append(SEPARATOR)
                .append(path)
                .append(".")
                .append(ext);
        String finalPath = sb.toString();
        log.debug("Path " + finalPath);
        return finalPath;
    }

    private boolean isTest(Class clazz) {
        return clazz.getClassLoader().getResource(clazz.getName().replace('.', SEPARATOR) + ".class").toString().contains("/test-classes/");
    }

    private String trim(ArrayList<String> code, int line) {
        StringBuilder sb = new StringBuilder();
        for (int i = (line - 5); i < (line + 2); i++) {
            sb.append(i + 1);
            sb.append(code.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        NullPointerException ex = new NullPointerException();
        SourceCodeGrabber s = new SourceCodeGrabber("com.mnunezpoggi.qa");
        for (SourceCode c : s.grab(ex.getStackTrace())) {
            System.out.println(c);
        }
    }

}
