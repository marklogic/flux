/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ClassifierOptions;
import com.marklogic.flux.api.Flux;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ClassifierOptionsTest extends AbstractOptionsTest {

    @Test
    void testOptions() {
        AtomicReference<ClassifierOptions> reference = new AtomicReference<>();

        Flux.importGenericFiles()
            .to(options -> options.classifier(classifierOptions -> {
                classifierOptions
                    .host("classifier.host.com")
                    .port(443)
                    .http()
                    .path("/cls/endpoint")
                    .apiKey("MyApiKey")
                    .tokenPath("token/endpoint")
                    .batchSize(30)
                    .socketTimeout(30000);
                reference.set(classifierOptions);
            }));

        ClassifierParams params = (ClassifierParams) reference.get();
        assertOptions(params.makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTP, "true",
            Options.WRITE_CLASSIFIER_PATH, "/cls/endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "MyApiKey",
            Options.WRITE_CLASSIFIER_TOKEN_PATH, "token/endpoint",
            Options.WRITE_CLASSIFIER_BATCH_SIZE, "30",
            Options.WRITE_CLASSIFIER_SOCKET_TIMEOUT, "30000"
        );
    }

    @Test
    void defaultTimeoutNotIncludedWhenNotSet() {
        AtomicReference<ClassifierOptions> reference = new AtomicReference<>();

        Flux.importGenericFiles()
            .to(options -> options.classifier(classifierOptions -> {
                classifierOptions.host("h");
                reference.set(classifierOptions);
            }));

        ClassifierParams params = (ClassifierParams) reference.get();
        assertFalse(params.makeOptions().containsKey(Options.WRITE_CLASSIFIER_SOCKET_TIMEOUT),
            "When no socket timeout is configured, the option key should be absent so the Spark connector uses its default.");
    }
}
