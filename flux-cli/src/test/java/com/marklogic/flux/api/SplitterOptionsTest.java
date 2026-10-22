/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.impl.importdata.SplitterParams;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class SplitterOptionsTest extends AbstractOptionsTest {

    @Test
    void inheritMetadata() {
        AtomicReference<SplitterOptions> reference = new AtomicReference<>();

        Flux.importGenericFiles()
            .to(options -> options.splitter(splitterOptions -> {
                splitterOptions
                    .jsonPointers("/text")
                    .outputInheritCollections()
                    .outputInheritPermissions();
                reference.set(splitterOptions);
            }));

        SplitterParams params = (SplitterParams) reference.get();
        assertOptions(params.makeOptions(),
            Options.WRITE_SPLITTER_JSON_POINTERS, "/text",
            Options.WRITE_SPLITTER_SIDECAR_INHERIT_COLLECTIONS, "true",
            Options.WRITE_SPLITTER_SIDECAR_INHERIT_PERMISSIONS, "true"
        );
    }
}
