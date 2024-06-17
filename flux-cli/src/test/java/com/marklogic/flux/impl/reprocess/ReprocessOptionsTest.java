/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.reprocess;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

/**
 * All the tests in here that verify options have to include at least one read/write piece of custom code so that
 * the overall class validation of parameters does not fail.
 */
class ReprocessOptionsTest extends AbstractOptionsTest {

    @Test
    void readInvoke() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-invoke", "/my/invoke.sjs",
            "--read-partitions-invoke", "/my/other-invoke.sjs",
            "--read-var", "param1=value1",
            "--read-var", "param2=spaces work!",
            "--write-invoke", "/my/invoke.sjs"
        );

        assertOptions(command.readParams.get(),
            Options.READ_INVOKE, "/my/invoke.sjs",
            Options.READ_PARTITIONS_INVOKE, "/my/other-invoke.sjs",
            Options.READ_VARS_PREFIX + "param1", "value1",
            Options.READ_VARS_PREFIX + "param2", "spaces work!"
        );
    }

    @Test
    void writeInvoke() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-invoke", "/my/invoke.sjs",
            "--write-invoke", "/my/invoke.sjs",
            "--external-variable-name", "MY_VAR",
            "--external-variable-delimiter", ";",
            "--abort-on-write-failure",
            "--batch-size", "123",
            "--write-var", "param1=value1",
            "--write-var", "param2=spaces work!"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_INVOKE, "/my/invoke.sjs",
            Options.WRITE_EXTERNAL_VARIABLE_NAME, "MY_VAR",
            Options.WRITE_EXTERNAL_VARIABLE_DELIMITER, ";",
            Options.WRITE_ABORT_ON_FAILURE, "true",
            Options.WRITE_BATCH_SIZE, "123",
            Options.WRITE_VARS_PREFIX + "param1", "value1",
            Options.WRITE_VARS_PREFIX + "param2", "spaces work!"
        );
    }

    @Test
    void readJavascript() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-javascript", "fn.currentDate()",
            "--read-partitions-javascript", "console.log('')",
            "--write-javascript", "fn.currentDate()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_JAVASCRIPT, "fn.currentDate()",
            Options.READ_PARTITIONS_JAVASCRIPT, "console.log('')"
        );
    }

    @Test
    void writeJavascript() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-javascript", "fn.currentDate()",
            "--write-javascript", "fn.currentDate()"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_JAVASCRIPT, "fn.currentDate()"
        );
    }

    @Test
    void readXquery() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-xquery", "fn:current-date()",
            "--read-partitions-xquery", "xdmp:log('')",
            "--write-xquery", "fn:current-date()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_XQUERY, "fn:current-date()",
            Options.READ_PARTITIONS_XQUERY, "xdmp:log('')"
        );
    }

    @Test
    void writeXquery() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-xquery", "fn:current-date()",
            "--write-xquery", "fn:current-date()"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_XQUERY, "fn:current-date()"
        );
    }

    @Test
    void readJavascriptFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-javascript-file", "my-code.js",
            "--read-partitions-javascript-file", "path/my-partitions.js",
            "--write-javascript", "fn.currentDate()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_JAVASCRIPT_FILE, "my-code.js",
            Options.READ_PARTITIONS_JAVASCRIPT_FILE, "path/my-partitions.js"
        );
    }

    @Test
    void readXqueryFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-xquery-file", "my-code.xqy",
            "--read-partitions-xquery-file", "path/my-partitions.xqy",
            "--write-javascript", "fn.currentDate()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_XQUERY_FILE, "my-code.xqy",
            Options.READ_PARTITIONS_XQUERY_FILE, "path/my-partitions.xqy"
        );
    }

    @Test
    void writeJavascriptFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-javascript", "doesn't matter",
            "--write-javascript-file", "my-code.js"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_JAVASCRIPT_FILE, "my-code.js"
        );
    }

    @Test
    void writeXqueryFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connection-string", "user:password@host:8000",
            "--read-javascript", "doesn't matter",
            "--write-xquery-file", "my-code.xqy"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_XQUERY_FILE, "my-code.xqy"
        );
    }
}
