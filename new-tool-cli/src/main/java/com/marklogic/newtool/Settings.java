package com.marklogic.newtool;

import com.beust.jcommander.Parameter;

public class Settings {

    @Parameter(names = "--provider")
    private String providerClass;

    public String getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }
}
