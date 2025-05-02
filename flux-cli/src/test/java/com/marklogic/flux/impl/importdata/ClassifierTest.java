/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */

package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassifierTest extends AbstractTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "SEMAPHORE_API_KEY", matches = ".*")
    void testJsonClassifier() {
        final String collection = "classification-test";
        final String API_KEY = System.getenv("SEMAPHORE_API_KEY");

        run(
            "import-files",
            "--path", "src/test/resources/json-files/java-client-intro.json",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", collection,
            "--uri-replace", ".*/resources,''",
            "--classifier-host", "demo.data.progress.cloud",
            "--classifier-port", "443",
            "--classifier-path", "/cls/dev/cs1/",
            "--classifier-api-key", API_KEY,
            "--classifier-token-path", "token/"
        );

        JsonNode jsonDoc = readJsonDocument("/json-files/java-client-intro.json");
        assertTrue(jsonDoc.get("classification").has("SYSTEM"));
        assertTrue(jsonDoc.get("classification").has("META"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SEMAPHORE_API_KEY", matches = ".*")
    void testXmlClassifier() {
        final String collection = "classification-test";
        final String API_KEY = System.getenv("SEMAPHORE_API_KEY");

        run(
            "import-files",
            "--path", "src/test/resources/xml-file/namespaced-java-client-intro.xml",
            "--connection-string", makeConnectionString(),
            "--permissions", DEFAULT_PERMISSIONS,
            "--collections", collection,
            "--uri-replace", ".*/resources,''",
            "--classifier-host", "demo.data.progress.cloud",
            "--classifier-port", "443",
            "--classifier-path", "/cls/dev/cs1/",
            "--classifier-api-key", API_KEY,
            "--classifier-token-path", "token/"
        );

        XmlNode xmlDoc = readXmlDocument("/xml-file/namespaced-java-client-intro.xml");
        xmlDoc.assertElementExists("/ex:root/model:classification/model:SYSTEM[1]");
        xmlDoc.assertElementExists("/ex:root/model:classification/model:META[1]");
    }
}
