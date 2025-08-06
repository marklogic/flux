/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package org.example;

import com.marklogic.flux.api.Flux;

public class CustomProgram {

    public static void main(String[] args) {
        // Depends on the example application created in the Getting Started guide.
        Flux.importGenericFiles()
            .connectionString("flux-example-user:password@localhost:8004")
            .from(options -> options
                .paths("../../flux-cli/src/test/resources/mixed-files/hello.txt"))
            .to(options -> options
                .collections("embedder-example")
                .permissionsString("flux-example-role,read,flux-example-role,update")
                .splitter(splitterOptions -> splitterOptions.text())
                .embedder(embedderOptions -> embedderOptions.embedder("minilm"))
            )
            .execute();
    }

}
