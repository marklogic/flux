/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates all parameters related to incremental writing of documents.
 */
class IncrementalWriteParams {

    @CommandLine.Option(
        names = "--incremental-write",
        description = "Enables incremental writing, which avoids writing documents that have not changed since " +
            "they were last written to MarkLogic."
    )
    private boolean enabled;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1")
    private SchemaViewGroup schemaView;

    static class SchemaViewGroup {
        @CommandLine.Option(
            names = "--incremental-write-schema",
            description = "Name of the schema to use for detecting changes when performing incremental writes; must " +
                "specify a view name as well.",
            required = true
        )
        String schema;

        @CommandLine.Option(
            names = "--incremental-write-view",
            description = "Name of the view to use for detecting changes when performing incremental writes; must " +
                "specify a schema name as well.",
            required = true
        )
        String view;
    }

    @CommandLine.Option(
        names = "--incremental-write-hash-name",
        description = "Name of the metadata key in which to store the hash value used for detecting changes during incremental writes."
    )
    private String hashKeyName;

    @CommandLine.Option(
        names = "--incremental-write-timestamp-name",
        description = "Name of the metadata key in which to store the timestamp for when the document was written."
    )
    private String timestampKeyName;

    @CommandLine.Option(
        names = "--incremental-write-dont-canonicalize-json",
        description = "Disables JSON canonicalization during incremental write."
    )
    private Boolean dontCanonicalizeJson;

    @CommandLine.Option(
        names = "--incremental-write-json-exclusion",
        description = "JSON Pointer expression for JSON to exclude when calculating the hash value for incremental writes. " +
            "Can be specified multiple times to exclude multiple paths."
    )
    private List<String> jsonExclusions = new ArrayList<>();

    @CommandLine.Option(
        names = "--incremental-write-xml-exclusion",
        description = "XPath expression for XML to exclude when calculating the hash value for incremental writes. " +
            "Can be specified multiple times to exclude multiple paths."
    )
    private List<String> xmlExclusions = new ArrayList<>();

    @CommandLine.Option(
        names = "--log-skipped",
        description = "Log a count of skipped documents every time this many documents are skipped when incremental writes are enabled."
    )
    private Integer skippedInterval;

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.WRITE_INCREMENTAL, enabled ? "true" : null,
            Options.WRITE_INCREMENTAL_SCHEMA, schemaView != null ? schemaView.schema : null,
            Options.WRITE_INCREMENTAL_VIEW, schemaView != null ? schemaView.view : null,
            Options.WRITE_INCREMENTAL_HASH_KEY_NAME, hashKeyName,
            Options.WRITE_INCREMENTAL_TIMESTAMP_KEY_NAME, timestampKeyName,
            Options.WRITE_INCREMENTAL_CANONICALIZE_JSON, dontCanonicalizeJson != null ? (!dontCanonicalizeJson ? "true" : "false") : null,
            Options.WRITE_INCREMENTAL_JSON_EXCLUSIONS, jsonExclusions.isEmpty() ? null : String.join("\n", jsonExclusions),
            Options.WRITE_INCREMENTAL_XML_EXCLUSIONS, xmlExclusions.isEmpty() ? null : String.join("\n", xmlExclusions),
            Options.WRITE_LOG_SKIPPED_DOCUMENTS, OptionsUtil.integerOption(skippedInterval)
        );
    }
}
