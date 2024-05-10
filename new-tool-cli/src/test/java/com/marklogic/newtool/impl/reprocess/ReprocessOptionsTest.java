package com.marklogic.newtool.impl.reprocess;

import com.marklogic.newtool.impl.AbstractOptionsTest;
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
            "--connectionString", "user:password@host:8000",
            "--readInvoke", "/my/invoke.sjs",
            "--readPartitionsInvoke", "/my/other-invoke.sjs",
            "--readVar", "param1=value1",
            "--readVar", "param2=spaces work!",
            "--writeInvoke", "/my/invoke.sjs"
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
            "--connectionString", "user:password@host:8000",
            "--readInvoke", "/my/invoke.sjs",
            "--writeInvoke", "/my/invoke.sjs",
            "--externalVariableName", "MY_VAR",
            "--externalVariableDelimiter", ";",
            "--abortOnWriteFailure",
            "--batchSize", "123",
            "--writeVar", "param1=value1",
            "--writeVar", "param2=spaces work!"
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
            "--connectionString", "user:password@host:8000",
            "--readJavascript", "fn.currentDate()",
            "--readPartitionsJavascript", "console.log('')",
            "--writeJavascript", "fn.currentDate()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_JAVASCRIPT, "fn.currentDate()",
            Options.READ_PARTITIONS_JAVASCRIPT, "console.log('')"
        );
    }

    @Test
    void writeJavascript() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readJavascript", "fn.currentDate()",
            "--writeJavascript", "fn.currentDate()"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_JAVASCRIPT, "fn.currentDate()"
        );
    }

    @Test
    void readXquery() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readXquery", "fn:current-date()",
            "--readPartitionsXquery", "xdmp:log('')",
            "--writeXquery", "fn:current-date()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_XQUERY, "fn:current-date()",
            Options.READ_PARTITIONS_XQUERY, "xdmp:log('')"
        );
    }

    @Test
    void writeXquery() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readXquery", "fn:current-date()",
            "--writeXquery", "fn:current-date()"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_XQUERY, "fn:current-date()"
        );
    }

    @Test
    void readJavascriptFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readJavascriptFile", "my-code.js",
            "--readPartitionsJavascriptFile", "path/my-partitions.js",
            "--writeJavascript", "fn.currentDate()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_JAVASCRIPT_FILE, "my-code.js",
            Options.READ_PARTITIONS_JAVASCRIPT_FILE, "path/my-partitions.js"
        );
    }

    @Test
    void readXqueryFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readXqueryFile", "my-code.xqy",
            "--readPartitionsXqueryFile", "path/my-partitions.xqy",
            "--writeJavascript", "fn.currentDate()"
        );

        assertOptions(command.readParams.get(),
            Options.READ_XQUERY_FILE, "my-code.xqy",
            Options.READ_PARTITIONS_XQUERY_FILE, "path/my-partitions.xqy"
        );
    }

    @Test
    void writeJavascriptFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readJavascript", "doesn't matter",
            "--writeJavascriptFile", "my-code.js"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_JAVASCRIPT_FILE, "my-code.js"
        );
    }

    @Test
    void writeXqueryFile() {
        ReprocessCommand command = (ReprocessCommand) getCommand("reprocess",
            "--connectionString", "user:password@host:8000",
            "--readJavascript", "doesn't matter",
            "--writeXqueryFile", "my-code.xqy"
        );

        assertOptions(command.writeParams.get(),
            Options.WRITE_XQUERY_FILE, "my-code.xqy"
        );
    }
}
