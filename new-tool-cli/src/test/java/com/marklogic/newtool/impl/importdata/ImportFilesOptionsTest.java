package com.marklogic.newtool.impl.importdata;

import com.marklogic.newtool.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ImportFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--host", "somehost",
            "--port", "8001",
            "--username", "someuser",
            "--password", "someword",
            "--path", "src/test/resources/mixed-files/hello*",
            "--partitions", "6",
            "--documentType", "XML",
            "--abortOnWriteFailure",
            "--batchSize", "50",
            "--collections", "collection1",
            "--failedDocumentsPath", "/my/failures",
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

        assertOptions(command.getConnectionParams().makeOptions(),
            Options.CLIENT_HOST, "somehost",
            Options.CLIENT_PORT, "8001",
            Options.CLIENT_USERNAME, "someuser",
            Options.CLIENT_PASSWORD, "someword"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "6"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_ABORT_ON_FAILURE, "true",
            Options.WRITE_ARCHIVE_PATH_FOR_FAILED_DOCUMENTS, "/my/failures",
            Options.WRITE_BATCH_SIZE, "50",
            Options.WRITE_COLLECTIONS, "collection1",
            Options.WRITE_DOCUMENT_TYPE, "XML",
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
