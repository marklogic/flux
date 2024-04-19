package com.marklogic.newtool;

import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorMessagesTest {

    @Test
    void verifyEachKeyIsOverridden() {
        ResourceBundle bundle = ResourceBundle.getBundle("marklogic-spark-messages");
        assertEquals(13, bundle.keySet().size(),
            "Expecting 13 keys as of the upcoming 2.3.0 release. Bump this up as more keys are added. Each key should " +
                "also be verified in an assertion below.");

        assertEquals("--connectionString", bundle.getString(Options.CLIENT_URI));
        assertEquals("--batchSize", bundle.getString(Options.READ_BATCH_SIZE));
        assertEquals("--partitionsPerForest", bundle.getString(Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST));
        assertEquals("--partitions", bundle.getString(Options.READ_NUM_PARTITIONS));
        assertEquals("--batchSize", bundle.getString(Options.WRITE_BATCH_SIZE));
        assertEquals("--documentType", bundle.getString(Options.WRITE_FILE_ROWS_DOCUMENT_TYPE));
        assertEquals("--graph", bundle.getString(Options.WRITE_GRAPH));
        assertEquals("--graphOverride", bundle.getString(Options.WRITE_GRAPH_OVERRIDE));
        assertEquals("--jsonRootName", bundle.getString(Options.WRITE_JSON_ROOT_NAME));
        assertEquals("--threadCount", bundle.getString(Options.WRITE_THREAD_COUNT));
        assertEquals("--transformParams", bundle.getString(Options.WRITE_TRANSFORM_PARAMS));
        assertEquals("--uriTemplate", bundle.getString(Options.WRITE_URI_TEMPLATE));
        assertEquals("--xmlRootName", bundle.getString(Options.WRITE_XML_ROOT_NAME));
    }
}
