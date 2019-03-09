package com.github.bcanvural;

import java.util.Collections;

import org.hippoecm.hst.configuration.model.HstManager;
import org.hippoecm.hst.configuration.model.HstManagerImpl;
import org.hippoecm.hst.container.HstDelegateeFilterBean;
import org.hippoecm.hst.container.HstFilter;
import org.hippoecm.hst.content.tool.DefaultContentBeansTool;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import org.hippoecm.hst.site.HstServices;
import org.onehippo.cms7.services.ServletContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockServletContext;

public class HstApp {

    private static Logger LOGGER = LoggerFactory.getLogger(HstApp.class);

    private static final String PAGEMODEL_ADDON_PATH = "hst/pagemodel-addon/module.xml";

    public static void main(String args[]) {

        //Component Manager
        SpringComponentManager componentManager = new SpringComponentManager();
        componentManager.setAddonModuleDefinitions(Collections.singletonList(Utils.loadAddonModule(PAGEMODEL_ADDON_PATH)));
        componentManager.initialize();
        HstServices.setComponentManager(componentManager);

        //Request
        MockHstRequest hstRequest = new MockHstRequest();
        hstRequest.setRequestURI("/site/resourceapi/news");
        hstRequest.setContextPath("/site");
        hstRequest.setHeader("Host", "localhost:8080");
        hstRequest.setHeader("X-Forwarded-Proto", "http");

        //Servlet context
        MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath("/site");
        servletContext.setInitParameter(DefaultContentBeansTool.BEANS_ANNOTATED_CLASSES_CONF_PARAM,
                "classpath*:com/github/bcanvural/beans/*.class,");
        hstRequest.setServletContext(servletContext);
        componentManager.setServletContext(servletContext);
        ServletContextRegistry.register(servletContext, ServletContextRegistry.WebAppType.HST);
        HstManagerImpl hstManager = (HstManagerImpl) componentManager.getComponent(HstManager.class);
        hstManager.setServletContext(hstRequest.getServletContext());

        //Response
        MockHstResponse hstResponse = new MockHstResponse();

        //Invoke
        HstDelegateeFilterBean filter = componentManager.getComponent(HstFilter.class.getName());
        try {
            filter.doFilter(hstRequest, hstResponse, null);
            String contentAsString = hstResponse.getContentAsString();
            LOGGER.info(contentAsString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}