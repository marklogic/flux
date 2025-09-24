/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.flux.AbstractJava17Test;

import java.util.Properties;

abstract class AbstractTdeTest extends AbstractJava17Test {

    protected final void verifyTdeCanBeLoaded(TdeTemplate template) {
        Properties props = loadTestProperties();
        final String uri = template.getUri();
        try (DatabaseClient client = DatabaseClientFactory.newClient(props::get)) {
            // Verify that the TDE can be loaded without errors.
            new TdeLoader(client).loadTde(template);
        } finally {
            try (DatabaseClient schemasClient = newDatabaseClient("flux-test-schemas")) {
                schemasClient.newDocumentManager().delete(uri);
            }
        }
    }
}
