/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorMessagesTest {

    // Don't need Sonar to complain about WRITE_FILE_ROWS_DOCUMENT_TYPE being deprecated, we are aware of that.
    @SuppressWarnings("java:S5738")
    @Test
    void verifyEachKeyIsOverridden() {
        ResourceBundle bundle = ResourceBundle.getBundle("marklogic-spark-messages");
        assertEquals(22, bundle.keySet().size(),
            "Expecting 22 keys as of the upcoming 1.2.0 release. Bump this up as more keys are added. Each key should " +
                "also be verified in an assertion below.");

        assertEquals("--connection-string", bundle.getString(Options.CLIENT_URI));
        assertEquals("--batch-size", bundle.getString(Options.READ_BATCH_SIZE));
        assertEquals("--partitions-per-forest", bundle.getString(Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST));
        assertEquals("--partitions", bundle.getString(Options.READ_NUM_PARTITIONS));
        assertEquals("--batch-size", bundle.getString(Options.WRITE_BATCH_SIZE));
        assertEquals("--document-type", bundle.getString(Options.WRITE_DOCUMENT_TYPE));
        assertEquals("--document-type", bundle.getString(Options.WRITE_FILE_ROWS_DOCUMENT_TYPE));
        assertEquals("--graph", bundle.getString(Options.WRITE_GRAPH));
        assertEquals("--graph-override", bundle.getString(Options.WRITE_GRAPH_OVERRIDE));
        assertEquals("--json-root-name", bundle.getString(Options.WRITE_JSON_ROOT_NAME));
        assertEquals("--thread-count", bundle.getString(Options.WRITE_THREAD_COUNT));
        assertEquals("--thread-count-per-partition", bundle.getString(Options.WRITE_THREAD_COUNT_PER_PARTITION));
        assertEquals("--transform-params", bundle.getString(Options.WRITE_TRANSFORM_PARAMS));
        assertEquals("--uri-template", bundle.getString(Options.WRITE_URI_TEMPLATE));
        assertEquals("--xml-root-name", bundle.getString(Options.WRITE_XML_ROOT_NAME));
        assertEquals("Must define an Optic query", bundle.getString("spark.marklogic.read.noOpticQuery"));
        assertEquals("--splitter-max-chunk-size", bundle.getString(Options.WRITE_SPLITTER_MAX_CHUNK_SIZE));
        assertEquals("--splitter-max-overlap-size", bundle.getString(Options.WRITE_SPLITTER_MAX_OVERLAP_SIZE));
        assertEquals("--splitter-sidecar-max-chunks", bundle.getString(Options.WRITE_SPLITTER_SIDECAR_MAX_CHUNKS));
        assertEquals("--embedder-chunks-json-pointer", bundle.getString(Options.WRITE_EMBEDDER_CHUNKS_JSON_POINTER));
        assertEquals("--embedder-chunks-xpath", bundle.getString(Options.WRITE_EMBEDDER_CHUNKS_XPATH));
        assertEquals("--embedder-batch-size", bundle.getString(Options.WRITE_EMBEDDER_BATCH_SIZE));
    }
}
