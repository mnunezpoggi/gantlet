/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.tests;

import com.mnunezpoggi.qa.gantlet.BasePage;
import com.mnunezpoggi.qa.gantlet.BaseSection;
import com.mnunezpoggi.qa.gantlet.BaseTest;
import com.mnunezpoggi.qa.gantlet.IterableSection;
import com.mnunezpoggi.qa.gantlet.PageSection;
import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.annotations.Section;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;
import com.mnunezpoggi.qa.gantlet.annotations.PageNavigator;
import com.mnunezpoggi.qa.gantlet.annotations.TestableElement;
import com.gargoylesoftware.htmlunit.javascript.configuration.WebBrowser;
import org.openqa.selenium.WebElement;

/**
 *
 * 
 */
public abstract class Navigation extends BaseTest {

    private static final Random rnd = new Random();
    private Logger logger = LogHelper.getLogger(this);

    private static final String ACTION_IDENTATION = "     - ";

    @Test
    public void navigate() {

        String route = "";
        
        logger.info("Remove setAllStaticElements() for speed");
        BaseTest.PageM.removeOnPageChangeListener(0);

        while (true) {

            try {
                
              //  checkElements(CurrentPage);
                
                Pair<Method, Object> toBeCalled;

                toBeCalled = route.isEmpty() ? getAnyMethod(CurrentPage) : getRouteMethod(CurrentPage, route);

                Method m = toBeCalled.getKey();

                PageNavigator meta = m.getAnnotation(PageNavigator.class);

                //Grab the old page (from)
                String oldPageName = CurrentPage.getClass().getSimpleName();

                //new page is the same old page if to = self
                String newPageName = meta.to().equals("self") ? oldPageName : meta.to();

                //update route value based on a 50% chance of skipping the route
                route = skipRoute() ? "" : meta.route();

                //invoke method for page switching
                toBeCalled.getKey().invoke(toBeCalled.getValue());
                printAction("Executing " + toBeCalled.getKey().getName());
                
//                if (meta.strict()) {
//                    assertEquals(PageM.getPageByName(newPageName), CurrentPage);
//                } else {
//                    assertTrue((CurrentPage.setAllStaticElements() == null) || (CurrentPage.equals(PageM.getPageByName(newPageName))));
//                }
                BaseTest.PageM.waitForPageChange(newPageName);
                checkElements(CurrentPage);

            } catch (Exception ex) {
                if (!(ex instanceof IllegalArgumentException)) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void checkElements(BaseSection CurrentPage) throws IllegalArgumentException, IllegalAccessException, InterruptedException, NoSuchMethodException, InvocationTargetException {
        for (Field f : CurrentPage.getClass().getDeclaredFields()) {
            printAction("Entering field " + f.getName());
            if (f.isAnnotationPresent(Element.class)) {
                printAction("Found Element " + f.getName());
                Element e = f.getAnnotation(Element.class);
                if (f.isAnnotationPresent(TestableElement.class)) {
                    String method = f.getAnnotation(TestableElement.class).activatedBy();
                    CurrentPage.getClass().getMethod(method).invoke(CurrentPage);                    
                }
                if (!e.dynamic() || f.isAnnotationPresent(TestableElement.class)) {
                    printAction("Highlightning element " + f.getName());
                    CurrentPage.setElement(f.getName());
                    CurrentPage.highlight(f.getName(), "rgba(255,0,0,0.5)");
                } 
            }
            if (f.isAnnotationPresent(Section.class)) {
                printAction("Entering section " + f.getName());
                if(f.get(CurrentPage) == null){
                    CurrentPage.setElement(f.getName());
                }
                checkElements((PageSection) f.get(CurrentPage));
            }
            if(f.isAnnotationPresent(com.mnunezpoggi.qa.gantlet.annotations.Iterable.class)){
                printAction("Entering iterable section");
                if(f.get(CurrentPage) == null){
                    CurrentPage.setIterableSection(f.getName());
                }
                ArrayList<IterableSection> l = (ArrayList) f.get(CurrentPage);
                for(IterableSection i: l){
                    checkElements(i);
                }
                
            }

        }
    }

    private Pair<Method, Object> getAnyMethod(PageSection CurrentPage) throws IllegalArgumentException, IllegalAccessException {
        ArrayList<Pair<Method, Object>> switchingMethods = getSwitchingMethods(CurrentPage);
        return switchingMethods.get(rnd.nextInt(switchingMethods.size()));
    }

    private Pair<Method, Object> getRouteMethod(PageSection CurrentPage, String route) throws IllegalArgumentException, IllegalAccessException {
        randomize();
        for (Method m : CurrentPage.getClass().getDeclaredMethods()) {
            if (m.getName().equals(route)) {
                return (new ImmutablePair(m, CurrentPage));
            }
        }
        return getAnyMethod(CurrentPage);
    }

    private ArrayList<Pair<Method, Object>> getSwitchingMethods(PageSection allMethods) throws IllegalArgumentException, IllegalAccessException {
        ArrayList<Pair<Method, Object>> ret = new ArrayList();
        for (Method m : allMethods.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PageNavigator.class)) {
                ret.add(new ImmutablePair(m, allMethods));
            }
        }
        for (Field f : allMethods.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Section.class)) {
                ret.addAll(getSwitchingMethods((PageSection) f.get(allMethods)));
            }
        }

        return ret;
    }

    private static boolean skipRoute() {
        return rnd.nextInt(100) < 20;
    }

    private static void randomize() {
        if (rnd.nextBoolean()) {
            rnd.setSeed(System.nanoTime());
        }
    }

    private static void printAction(String action) {
        System.out.println(ACTION_IDENTATION + action);
    }

}
