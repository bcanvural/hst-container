package com.github.bcanvural;

import javax.servlet.ServletContext;

public class MockHstRequest extends org.hippoecm.hst.mock.core.component.MockHstRequest {

    private ServletContext servletContext;

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }
}
