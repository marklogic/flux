/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ClassifierOptions;
import com.marklogic.flux.api.Flux;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import com.marklogic.spark.udf.TextClassifierUdf;
import com.smartlogic.classificationserver.client.ClassificationConfiguration;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassifierOptionsTest extends AbstractOptionsTest {

    @Test
    void testOptions() {
        AtomicReference<ClassifierOptions> reference = new AtomicReference<>();

        Flux.importGenericFiles()
            .to(options -> options.classifier(classifierOptions -> {
                classifierOptions
                    .host("classifier.host.com")
                    .port("443")
                    .https()
                    .endpoint("/cls/endpoint")
                    .apiKey("MyApiKey")
                    .tokenEndpoint("token/endpoint");
                reference.set(classifierOptions);
            }));

        ClassifierParams params = (ClassifierParams) reference.get();
        assertOptions(params.makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTPS, "true",
            Options.WRITE_CLASSIFIER_ENDPOINT, "/cls/endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "MyApiKey",
            Options.WRITE_CLASSIFIER_TOKEN_ENDPOINT, "token/endpoint"
        );
    }


    @Test
    void testTextClassifierUdfConfig() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--path", "anywhere",
            "--host", "somehost",
            "--port", "8001",
            "--classifier-host", "serverName",
            "--classifier-https",
            "--classifier-port", "443",
            "--classifier-endpoint", "/lorem/",
            "--classifier-api-key", "key",
            "--classifier-token-endpoint", "token"
        );

        command.getWriteParams().getClassifierParams().buildTextClassifier();
        ClassificationConfiguration config = TextClassifierUdf.getClassificationConfiguration();
        assertEquals("serverName", config.getHostName(), "The host name should match");
        assertEquals("https", config.getProtocol(), "The protocol should match");
        assertEquals(443, config.getHostPort(), "The port should match");
        assertEquals("/lorem/", config.getHostPath(), "The endpoint should match");
        assertEquals("key", config.getAdditionalParameters().get("apiKey"), "The API key should match");
        assertEquals("token", config.getAdditionalParameters().get("tokenEndpoint"), "The token endpoint should match");
    }

}
