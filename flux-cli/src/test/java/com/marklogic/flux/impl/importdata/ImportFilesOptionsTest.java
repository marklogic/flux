/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.AbstractOptionsTest;
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
            "--document-type", "XML",
            "--abort-on-write-failure",
            "--batch-size", "50",
            "--collections", "collection1",
            "--encoding", "UTF-16",
            "--failed-documents-path", "/my/failures",
            "--permissions", "role1,read,role2,update",
            "--temporal-collection", "temporal1",
            "--thread-count", "17",
            "--transform", "transform1",
            "--transform-params", "param1;value1",
            "--transform-params-delimiter", ";",
            "--uri-prefix", "/prefix",
            "--uri-replace", ".*value,''",
            "--uri-suffix", ".suffix",
            "--uri-template", "/test/{value}.json",
            "--streaming"
        );

        assertOptions(command.getConnectionParams().makeOptions(),
            Options.CLIENT_HOST, "somehost",
            Options.CLIENT_PORT, "8001",
            Options.CLIENT_USERNAME, "someuser",
            Options.CLIENT_PASSWORD, "someword"
        );

        assertOptions(command.getReadParams().makeOptions(),
            Options.READ_NUM_PARTITIONS, "6",
            Options.READ_FILES_ENCODING, "UTF-16",
            Options.STREAM_FILES, "true"
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
            Options.WRITE_URI_TEMPLATE, "/test/{value}.json",
            Options.STREAM_FILES, "true"
        );
    }

    @Test
    void splitterOptions() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--connection-string", "user:password@host:8001",
            "--path", "anywhere",
            "--splitter-json-pointer", "/path1\n/path2",
            "--splitter-xpath", "/some/path",
            "-Xex=org:example",
            "--splitter-max-chunk-size", "100",
            "--splitter-max-overlap-size", "50",
            "--splitter-regex", "word",
            "--splitter-join-delimiter", "aaa",
            "--splitter-text",
            "--splitter-sidecar-max-chunks", "10",
            "--splitter-sidecar-document-type", "xml",
            "--splitter-sidecar-collections", "c1,c2",
            "--splitter-sidecar-permissions", "role1,read,role2,update",
            "--splitter-sidecar-root-name", "root1",
            "--splitter-sidecar-uri-prefix", "/prefix",
            "--splitter-sidecar-uri-suffix", ".json",
            "--splitter-sidecar-xml-namespace", "org:example"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_SPLITTER_JSON_POINTERS, "/path1\n/path2",
            Options.WRITE_SPLITTER_XPATH, "/some/path",
            Options.XPATH_NAMESPACE_PREFIX + "ex", "org:example",
            Options.WRITE_SPLITTER_MAX_CHUNK_SIZE, "100",
            Options.WRITE_SPLITTER_MAX_OVERLAP_SIZE, "50",
            Options.WRITE_SPLITTER_REGEX, "word",
            Options.WRITE_SPLITTER_JOIN_DELIMITER, "aaa",
            Options.WRITE_SPLITTER_TEXT, "true",
            Options.WRITE_SPLITTER_SIDECAR_MAX_CHUNKS, "10",
            Options.WRITE_SPLITTER_SIDECAR_DOCUMENT_TYPE, "XML",
            Options.WRITE_SPLITTER_SIDECAR_COLLECTIONS, "c1,c2",
            Options.WRITE_SPLITTER_SIDECAR_PERMISSIONS, "role1,read,role2,update",
            Options.WRITE_SPLITTER_SIDECAR_ROOT_NAME, "root1",
            Options.WRITE_SPLITTER_SIDECAR_URI_PREFIX, "/prefix",
            Options.WRITE_SPLITTER_SIDECAR_URI_SUFFIX, ".json",
            Options.WRITE_SPLITTER_SIDECAR_XML_NAMESPACE, "org:example"
        );
    }

    @Test
    void embedderOptions() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--connection-string", "user:password@host:8001",
            "--path", "anywhere",
            "--embedder", "azure",
            "-Ekey=value",
            "--embedder-chunks-json-pointer", "/some/chunks",
            "--embedder-text-json-pointer", "/my-text",
            "--embedder-embedding-name", "stuff"
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, "com.marklogic.flux.langchain4j.embedding.AzureOpenAiEmbeddingModelFunction",
            Options.WRITE_EMBEDDER_CHUNKS_JSON_POINTER, "/some/chunks",
            Options.WRITE_EMBEDDER_TEXT_JSON_POINTER, "/my-text",
            Options.WRITE_EMBEDDER_EMBEDDING_NAME, "stuff",
            Options.WRITE_EMBEDDER_MODEL_FUNCTION_OPTION_PREFIX + "key", "value"
        );
    }

    @Test
    void emptyJsonPointerPath() {
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--connection-string", "user:password@host:8001",
            "--path", "anywhere",
            // This works on the command line too - i.e. a user can punch in "" and the value will be picked up correctly.
            "--splitter-json-pointer", ""
        );

        assertOptions(command.getWriteParams().makeOptions(),
            Options.WRITE_SPLITTER_JSON_POINTERS, ""
        );
    }
}
