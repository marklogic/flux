package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import_files",
            "--host", "somehost",
            "--port", "8001",
            "--username", "someuser",
            "--password", "someword",
            "--path", "src/test/resources/mixed-files/hello*",
            "--documentType", "XML",
            "--abortOnFailure", "false",
            "--batchSize", "50",
            "--collections", "collection1",
            "--permissions", "role1,read,role2,update",
            "--temporalCollection", "temporal1",
            "--threadCount", "17",
            "--transform", "transform1",
            "--transformParams", "param1;value1",
            "--transformParamsDelimiter", ";",
            "--uriPrefix", "/prefix",
            "--uriReplace", ".*value,''",
            "--uriSuffix", ".suffix",
            "--uriTemplate", "/test/{value}.json"
        );

        assertOptions(command.makeWriteOptions(),
            Options.CLIENT_HOST, "somehost",
            Options.CLIENT_PORT, "8001",
            Options.CLIENT_USERNAME, "someuser",
            Options.CLIENT_PASSWORD, "someword",
            Options.WRITE_ABORT_ON_FAILURE, "false",
            Options.WRITE_BATCH_SIZE, "50",
            Options.WRITE_COLLECTIONS, "collection1",
            Options.WRITE_FILE_ROWS_DOCUMENT_TYPE, "XML",
            Options.WRITE_PERMISSIONS, "role1,read,role2,update",
            Options.WRITE_TEMPORAL_COLLECTION, "temporal1",
            Options.WRITE_THREAD_COUNT, "17",
            Options.WRITE_TRANSFORM_NAME, "transform1",
            Options.WRITE_TRANSFORM_PARAMS, "param1;value1",
            Options.WRITE_TRANSFORM_PARAMS_DELIMITER, ";",
            Options.WRITE_URI_PREFIX, "/prefix",
            Options.WRITE_URI_REPLACE, ".*value,''",
            Options.WRITE_URI_SUFFIX, ".suffix",
            Options.WRITE_URI_TEMPLATE, "/test/{value}.json"
        );
    }
}
