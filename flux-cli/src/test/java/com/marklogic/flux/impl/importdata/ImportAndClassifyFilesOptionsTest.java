/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportAndClassifyFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void classifyOptions() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--connection-string", "user:password@host:8001",
            "--path", "anywhere",
            "--classifier-host", "classifier.host.com",
            "--classifier-port", "443",
            "--classifier-https",
            "--classifier-endpoint", "cls-endpoint",
            "--classifier-api-key", "secret key",
            "--classifier-token-endpoint", "token-endpoint"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTPS, "true",
            Options.WRITE_CLASSIFIER_ENDPOINT, "cls-endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "secret key",
            Options.WRITE_CLASSIFIER_TOKEN_ENDPOINT, "token-endpoint"
        );
    }

    @Test
    void noHttpsSwitch() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--connection-string", "user:password@host:8001",
            "--path", "anywhere",
            "--classifier-host", "classifier.host.com",
            "--classifier-port", "443",
            "--classifier-endpoint", "cls-endpoint",
            "--classifier-api-key", "secret key",
            "--classifier-token-endpoint", "token-endpoint"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTPS, "false",
            Options.WRITE_CLASSIFIER_ENDPOINT, "cls-endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "secret key",
            Options.WRITE_CLASSIFIER_TOKEN_ENDPOINT, "token-endpoint"
        );
    }
}
