/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.spark;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

class SubmitTest extends AbstractTest {

    @Test
    void valid() {
        // This constructs an in-memory SparkSession that the Submit program is then expected to access. In the real
        // world, spark-submit will provide that SparkSession instead.
        newSparkSession();

        Submit.main(new String[]{
            "import-files",
            "--path", "src/test/resources/mixed-files/hello.json",
            "--path", "src/test/resources/mixed-files/hello.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", "submit-files"
        });

        assertCollectionSize(
            "Verifying that the Submit program works as long as a SparkSession has already bee constructed for it to use.",
            "submit-files", 2
        );
    }

    @Test
    void noCommand() {
        assertStderrContains(
            () -> Submit.main(new String[]{}),
            "You must specify a command."
        );
    }

    /**
     * This isn't quite what we want yet, as it's including "Usage: ./bin/flux import-files", which isn't correct when
     * using spark-submit. A future enhancement should provide better usage, though the user should be reading our
     * documentation that shows how to use Submit with spark-submit.
     */
    @Test
    void missingRequiredOptions() {
        assertStderrContains(
            () -> Submit.main(new String[]{"import-files"}),
            "Missing required option: '--path <path>'"
        );
    }
}
