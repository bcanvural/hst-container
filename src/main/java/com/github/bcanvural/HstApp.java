package com.github.bcanvural;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.hippoecm.hst.configuration.model.HstManager;
import org.hippoecm.hst.configuration.model.HstManagerImpl;
import org.hippoecm.hst.container.HstDelegateeFilterBean;
import org.hippoecm.hst.container.HstFilter;
import org.hippoecm.hst.content.tool.DefaultContentBeansTool;
import org.hippoecm.hst.mock.core.component.MockHstResponse;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.site.addon.module.model.ModuleDefinition;
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
        componentManager.setAddonModuleDefinitions(Collections.singletonList(loadPageModelAddon()));
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

    private static ModuleDefinition loadPageModelAddon() {
        try {
            URL addonUrl = HstApp.class.getClassLoader().getResource(PAGEMODEL_ADDON_PATH);
            if (addonUrl == null) {
                throw new IOException("Error while loading the pagemodel addon module");
            }
            return loadModuleDefinition(addonUrl);
        } catch (IOException | JAXBException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static ModuleDefinition loadModuleDefinition(URL url) throws JAXBException, IOException {
        ModuleDefinition moduleDefinition = null;

        JAXBContext jc = JAXBContext.newInstance(ModuleDefinition.class);
        Unmarshaller um = jc.createUnmarshaller();

        InputStream is = null;
        BufferedInputStream bis = null;

        try {
            is = url.openStream();
            bis = new BufferedInputStream(is);
            moduleDefinition = (ModuleDefinition) um.unmarshal(url.openStream());
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(is);
        }

        return moduleDefinition;
    }
}