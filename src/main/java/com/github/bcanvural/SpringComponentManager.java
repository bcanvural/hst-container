package com.github.bcanvural;

public class SpringComponentManager extends org.hippoecm.hst.site.container.SpringComponentManager {
    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public String[] getConfigurationResources() {
        return new String[]{"/hst/*.xml"};
    }
}
