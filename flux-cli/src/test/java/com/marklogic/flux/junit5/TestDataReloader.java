/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.junit5;

import com.marklogic.appdeployer.AppConfig;
import com.marklogic.appdeployer.DefaultAppConfigFactory;
import com.marklogic.appdeployer.command.data.LoadDataCommand;
import com.marklogic.appdeployer.impl.SimpleAppDeployer;
import com.marklogic.mgmt.util.SimplePropertySource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static org.springframework.test.util.AssertionErrors.fail;

/**
 * Intended to minimize the chance of MLE-40 causing tests that read with Optic to
 * intermittently fail.
 */
public class TestDataReloader implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        Properties props = new Properties();
        String path = "../test-app/gradle.properties";
        try (FileReader reader = new FileReader(path)) {
            props.load(reader);
        } catch (IOException ex) {
            fail("Unable to read from " + path);
        }

        AppConfig appConfig = new DefaultAppConfigFactory(new SimplePropertySource(props)).newAppConfig();
        appConfig.getDataConfig().setDataPaths(Arrays.asList("../test-app/src/main/ml-data"));
        new SimpleAppDeployer(null, null, new LoadDataCommand()).deploy(appConfig);
    }
}
