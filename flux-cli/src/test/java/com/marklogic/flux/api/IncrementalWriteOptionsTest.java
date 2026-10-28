/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.impl.importdata.WriteDocumentParams;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;

class IncrementalWriteOptionsTest extends AbstractOptionsTest {

    @Test
    void allOptions() {
        Map<String, String> options = makeWriteOptions(writeOptions -> writeOptions
            .incrementalWrite(iw -> iw
                .enable()
                .schemaAndView("mySchema", "myView")
                .hashName("customHash")
                .timestampName("customTimestamp")
                .disableJsonCanonicalization()
                .jsonExclusions("/json1", "/json2")
                .xmlExclusions("/ex:xml1", "/xml2")
                .logSkipped(789))
            .splitter(splitter -> splitter.xmlNamespaces("ex", "org:example")));

        assertOptions(options,
            Options.WRITE_INCREMENTAL, "true",
            Options.WRITE_INCREMENTAL_SCHEMA, "mySchema",
            Options.WRITE_INCREMENTAL_VIEW, "myView",
            Options.WRITE_INCREMENTAL_HASH_KEY_NAME, "customHash",
            Options.WRITE_INCREMENTAL_TIMESTAMP_KEY_NAME, "customTimestamp",
            Options.WRITE_INCREMENTAL_CANONICALIZE_JSON, "false",
            Options.WRITE_INCREMENTAL_JSON_EXCLUSIONS, "/json1\n/json2",
            Options.WRITE_INCREMENTAL_XML_EXCLUSIONS, "/ex:xml1\n/xml2",
            Options.XPATH_NAMESPACE_PREFIX + "ex", "org:example",
            Options.WRITE_LOG_SKIPPED_DOCUMENTS, "789"
        );
    }

    @Test
    void defaults() {
        Map<String, String> options = makeWriteOptions(writeOptions -> writeOptions.incrementalWrite(IncrementalWriteOptions::enable));

        assertOptions(options,
            Options.WRITE_INCREMENTAL, "true",
            Options.WRITE_INCREMENTAL_HASH_KEY_NAME, "incrementalWriteHash",
            Options.WRITE_LOG_SKIPPED_DOCUMENTS, "10000"
        );
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL_SCHEMA));
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL_VIEW));
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL_TIMESTAMP_KEY_NAME));
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL_CANONICALIZE_JSON));
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL_JSON_EXCLUSIONS));
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL_XML_EXCLUSIONS));
    }

    @Test
    void notEnabledByDefault() {
        Map<String, String> options = makeWriteOptions(writeOptions -> writeOptions.collections("anything"));
        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL));
    }

    @Test
    void notEnabledWithoutCallingEnable() {
        Map<String, String> options = makeWriteOptions(writeOptions -> writeOptions
            .incrementalWrite(iw -> iw.hashName("customHash")));

        assertFalse(options.containsKey(Options.WRITE_INCREMENTAL),
            "Mirroring the CLI, incremental write should only be enabled when enable() is called, just like " +
                "--incremental-write-hash-name has no effect without --incremental-write.");
        assertOptions(options, Options.WRITE_INCREMENTAL_HASH_KEY_NAME, "customHash");
    }

    private Map<String, String> makeWriteOptions(Consumer<GenericFilesImporter.WriteGenericDocumentsOptions> consumer) {
        AtomicReference<WriteDocumentParams<?>> reference = new AtomicReference<>();
        Flux.importGenericFiles().to(options -> {
            consumer.accept(options);
            reference.set((WriteDocumentParams<?>) options);
        });
        return reference.get().makeOptions();
    }
}
