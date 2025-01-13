/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.impl.importdata.SplitterParams;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class ClassifierOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        AtomicReference<SplitterOptions> reference = new AtomicReference<>();

        Flux.importGenericFiles()
            .to(options -> options.splitter(splitterOptions -> {
                splitterOptions
                    .outputClassifierHost("classifier.host.com")
                    .outputClassifierPort("443")
                    .outputClassifierProtocol("https")
                    .outputClassifierEndpoint("/cls/endpoint")
                    .outputClassifierApiKey("MyApiKey")
                    .outputClassifierTokenEndpoint("token/endpoint");
                reference.set(splitterOptions);
            }));

        SplitterParams params = (SplitterParams) reference.get();
        assertOptions(params.makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_PROTOCOL, "https",
            Options.WRITE_CLASSIFIER_ENDPOINT, "/cls/endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "MyApiKey",
            Options.WRITE_CLASSIFIER_TOKEN_ENDPOINT, "token/endpoint"
        );
    }
}
