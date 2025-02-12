/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ClassifierOptions;
import com.marklogic.flux.api.Flux;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

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
                    .tokenPath("token/endpoint");
                reference.set(classifierOptions);
            }));

        ClassifierParams params = (ClassifierParams) reference.get();
        assertOptions(params.makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTP, "true",
            Options.WRITE_CLASSIFIER_PATH, "/cls/endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "MyApiKey",
            Options.WRITE_CLASSIFIER_TOKEN_PATH, "token/endpoint"
        );
    }
}
