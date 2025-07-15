/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.junit5.TwoWaySslConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SslTest extends AbstractTest {

    private static final String SERVER_NAME = "flux-test-ssl";

    @TempDir
    private Path tempDir;
    private TwoWaySslConfigurer twoWaySslConfigurer;
    private TwoWaySslConfigurer.TwoWaySslConfig twoWaySslConfig;

    @BeforeEach
    void setup() {
        twoWaySslConfigurer = new TwoWaySslConfigurer(newManageClient(), newDatabaseClient("Security"));
        twoWaySslConfig = twoWaySslConfigurer.setupTwoWaySsl(tempDir, SERVER_NAME);
    }

    @AfterEach
    void teardown() {
        twoWaySslConfigurer.teardownTwoWaySsl(SERVER_NAME);
    }

    @Test
    void twoWaySslTest() {
        Flux.importGenericFiles()
            .from("src/test/resources/mixed-files")
            .connection(options -> options
                .host(getDatabaseClient().getHost())
                .port(8005)
                .username(DEFAULT_USER)
                .password(DEFAULT_PASSWORD)
                .keyStorePath(twoWaySslConfig.getKeyStoreFile().getAbsolutePath())
                .keyStorePassword(twoWaySslConfig.getKeyStorePassword())
                .trustStorePath(twoWaySslConfig.getTrustStoreFile().getAbsolutePath())
                .trustStorePassword(twoWaySslConfig.getTrustStorePassword())
                .sslHostnameVerifier(SslHostnameVerifier.ANY))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("ssl-files"))
            .execute();

        assertCollectionSize("ssl-files", 6);
    }
}
