package com.mnunezpoggi.qa.gantlet.tests;

import com.mnunezpoggi.qa.gantlet.BasePage;
import com.mnunezpoggi.qa.gantlet.BaseTest;
import com.mnunezpoggi.qa.gantlet.PageSection;
import com.mnunezpoggi.qa.gantlet.annotations.Element;
import com.mnunezpoggi.qa.gantlet.annotations.PageNavigator;
import com.mnunezpoggi.qa.gantlet.annotations.Section;
import com.mnunezpoggi.qa.gantlet.annotations.TestableElement;
import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.testng.annotations.Test;

/**
 *
 * 
 */
public abstract class Navigator extends BaseTest {

    private static final Random rnd = new Random();
    private Logger logger = LogHelper.getLogger(this);

    private LinkedList<BasePage> pages;

    private static final String ACTION_IDENTATION = "     - ";

    @Test
    public void navigateDesktop() {
        try {
            navigate(false);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Navigator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void navigateMobile() {
        try {
            PageM.getDriver().manage().window().setSize(new Dimension(640,1136));
            navigate(true);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Navigator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void navigate(boolean mobile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InterruptedException, NoSuchMethodException {
        // Grab all of our instantiated Pages
        setPages();
        System.out.println(pages);
        // Loop while the list is NOT empty (While we have not visited all of the pages)
        while (!pages.isEmpty()) {
            verify(CurrentPage, mobile);
            pages.remove(CurrentPage);
            System.out.println(pages);
            //Grab the old page (from)
            BasePage oldpage = CurrentPage;

            execMethod(CurrentPage);

            if (CurrentPage.equals(oldpage)) {
                ArrayList<Pair<Method, Object>> a = getAllMethods(CurrentPage, true);
                Pair<Method, Object> p = a.get(rnd.nextInt(a.size()));
                p.getKey().invoke(p.getValue());

            }
        }
    }

    private void execMethod(PageSection section) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InterruptedException, NoSuchMethodException {
        ArrayList<Pair<Method, Object>> nextPages = getMethods(CurrentPage);
        if (nextPages.isEmpty()) {
            nextPages = getAllMethods(CurrentPage, false);
            logger.info("Couldn't find methods on page " + CurrentPage.getClass().getSimpleName());
        }
        if (nextPages.isEmpty()) {
            nextPages = getAllMethods(CurrentPage, true);
            logger.info("Couldn't find methods on all sections of " + CurrentPage.getClass().getSimpleName());
        }

        Pair<Method, Object> next = nextPages.get(rnd.nextInt(nextPages.size()));
        logger.info("Invoking " + next.getKey().getName() + " on " + next.getValue().getClass().getSimpleName());
        next.getKey().invoke(next.getValue());
    }

    private ArrayList<Pair<Method, Object>> getMethods(PageSection section) throws IllegalArgumentException, IllegalAccessException {
        ArrayList<Pair<Method, Object>> ret = new ArrayList();
        for (Method m : section.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PageNavigator.class)) {
                PageNavigator navigator = m.getAnnotation(PageNavigator.class);
                BasePage page = PageM.getPageByName(navigator.to());
                if (pages.contains(page)) {
                    ret.add(new ImmutablePair(m, section));
                }

            }
        }
        return ret;
    }

    private ArrayList<Pair<Method, Object>> getAllMethods(PageSection section, boolean includeAll) throws IllegalArgumentException, IllegalAccessException {
        ArrayList<Pair<Method, Object>> ret = new ArrayList();
        for (Method m : section.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PageNavigator.class)) {
                PageNavigator navigator = m.getAnnotation(PageNavigator.class);
                BasePage page = PageM.getPageByName(navigator.to());
                if (pages.contains(page) || includeAll) {
                    ret.add(new ImmutablePair(m, section));
                }
            }
        }
        for (Field f : section.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Section.class)) {
                ret.addAll(getAllMethods((PageSection) f.get(section), includeAll));
            }
        }

        return ret;
    }

    private void verify(PageSection section, boolean mobile) throws IllegalArgumentException, IllegalAccessException, InterruptedException, NoSuchMethodException, InvocationTargetException {
        ArrayList<Pair<PageSection, Field>> fields = getAllTestableElements(section, mobile);
        for (Pair<PageSection, Field> toBeChecked : fields) {
            
            PageSection ps = toBeChecked.getKey();
            Field fd = toBeChecked.getValue();
            
            if (fd.isAnnotationPresent(TestableElement.class)) {
                String method = fd.getAnnotation(TestableElement.class).activatedBy();
                if (!method.isEmpty()) {
                    ps.getClass().getMethod(method).invoke(ps);
                }
            }
            
            ps.highlight(fd.getName(), "rgba(255,0,0,0.5)");
        }

    }

    private ArrayList<Pair<PageSection, Field>> getAllTestableElements(PageSection section, boolean mobile) throws IllegalArgumentException, IllegalAccessException {
        ArrayList<Pair<PageSection, Field>> list = new ArrayList();

        for (Field f : section.getClass().getDeclaredFields()) {

            if (f.isAnnotationPresent(Element.class)) {

                Element e = f.getAnnotation(Element.class);

                if (!e.dynamic()) {

                    list.add(new ImmutablePair(section, f));
                    continue;
                }

                if (f.isAnnotationPresent(TestableElement.class)) {

                    TestableElement testable = f.getAnnotation(TestableElement.class);

                    if (!(testable.mobile() ^ mobile)) {
                        list.add(new ImmutablePair(section, f));
                    }
                }
            }

            if (f.isAnnotationPresent(Section.class)) {

                list.addAll(getAllTestableElements((PageSection) f.get(section), mobile));
            }
        }
        return list;
    }

    private void setPages() {
        pages = new LinkedList(PageM.getPages());
    }

}
