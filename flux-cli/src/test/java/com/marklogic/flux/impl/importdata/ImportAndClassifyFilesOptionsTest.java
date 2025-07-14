/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
            "--classifier-http",
            "--classifier-path", "cls-endpoint",
            "--classifier-api-key", "secret key",
            "--classifier-token-path", "token-endpoint",
            "-Lthreshold=17",
            "-LsomethingElse=can-be-anything"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTP, "true",
            Options.WRITE_CLASSIFIER_PATH, "cls-endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "secret key",
            Options.WRITE_CLASSIFIER_TOKEN_PATH, "token-endpoint",
            Options.WRITE_CLASSIFIER_OPTION_PREFIX + "threshold", "17",
            Options.WRITE_CLASSIFIER_OPTION_PREFIX + "somethingElse", "can-be-anything"
        );
    }

    @Test
    void noHttpOption() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--connection-string", "user:password@host:8001",
            "--path", "anywhere",
            "--classifier-host", "classifier.host.com",
            "--classifier-port", "443",
            "--classifier-path", "cls-endpoint",
            "--classifier-api-key", "secret key",
            "--classifier-token-path", "token-endpoint"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_CLASSIFIER_HOST, "classifier.host.com",
            Options.WRITE_CLASSIFIER_PORT, "443",
            Options.WRITE_CLASSIFIER_HTTP, "false",
            Options.WRITE_CLASSIFIER_PATH, "cls-endpoint",
            Options.WRITE_CLASSIFIER_APIKEY, "secret key",
            Options.WRITE_CLASSIFIER_TOKEN_PATH, "token-endpoint"
        );
    }
}
