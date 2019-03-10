package com.github.bcanvural;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import junit.framework.Assert;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PageModelTest extends AbstractPageModelTest {

    @BeforeAll
    public void init() {
        super.init();
    }

    @AfterAll
    public void destroy() {
        super.destroy();
    }

    @Override
    protected String getAnnotatedHstBeansClasses() {
        return "classpath*:com/github/bcanvural/beans/*.class,";
    }

    @Test
    public void test() {
        getHstRequest().setRequestURI("/site/resourceapi/news");
        String response1 = invokeFilter();
        String response2 = invokeFilter();
        Assert.assertEquals(response1, response2);
    }

    @Test
    public void test2() {
        getHstRequest().setRequestURI("/site/resourceapi/news");
        String response1 = invokeFilter();
        String response2 = invokeFilter();
        Assert.assertEquals(response1, response2);
    }
}
