/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.marklogic.flux.AbstractJava17Test;
import marklogicspark.marklogic.client.DatabaseClient;
import marklogicspark.marklogic.client.DatabaseClientFactory;

import java.util.Properties;

abstract class AbstractTdeTest extends AbstractJava17Test {

    protected final void verifyTdeCanBeLoaded(TdeTemplate template) {
        Properties props = loadTestProperties();
        final String uri = template.getUri();
        DatabaseClient client = null;
        try {
            client = DatabaseClientFactory.newClient(props::get);
            // Verify that the TDE can be loaded without errors.
            new TdeLoader(client).loadTde(template);
        } finally {
            if (client != null) {
                client.newDocumentManager().delete(uri);
                client.release();
            }
        }
    }
}
