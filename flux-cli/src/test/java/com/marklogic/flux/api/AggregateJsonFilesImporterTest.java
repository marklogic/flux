/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregateJsonFilesImporterTest extends AbstractTest {

    @Test
    void test() {
        Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/custom-delimiter-json.txt")
                .jsonLines()
                .additionalOptions(Map.of("lineSep", ":\n")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("custom-delimiter")
                .uriTemplate("/custom-delimiter/{firstName}.json"))
            .execute();

        assertCollectionSize("custom-delimiter", 3);
        Stream.of(
            "/custom-delimiter/firstName-1.json", "/custom-delimiter/firstName-2.json", "/custom-delimiter/firstName-3.json"
        ).forEach(uri -> {
            JsonNode doc = readJsonDocument(uri);
            assertTrue(doc.has("firstName"));
            assertTrue(doc.has("lastName"));
        });
    }

    @Test
    void missingFieldInUriTemplate() {
        Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/custom-delimiter-json.txt")
                .jsonLines()
                .additionalOptions(Map.of("lineSep", ":\n")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("custom-delimiter")
                .uriTemplate("/custom-delimiter/{doesntExist}.json")
                .uriTemplateWarnOnMissingField())
            .execute();

        List<String> uris = getUrisInCollection("custom-delimiter", 3);
        uris.forEach(uri -> assertTrue(uri.startsWith("/custom-delimiter/UNRESOLVED-"),
            "When a field cannot be resolved in a URI template, it should be replaced with UNRESOLVED-<UUID>. " +
                "Actual URI: " + uri));
    }

    @Test
    void missingPath() {
        AggregateJsonFilesImporter importer = Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString());

        FluxException ex = assertThrowsFluxException(importer::execute);
        assertEquals("Must specify one or more file paths", ex.getMessage());
    }

    @Test
    void jsonLinesRaw() {
        Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/different-objects.txt")
                .jsonLinesRaw()
            )
            .to(options -> options
                .collections("json-lines")
                .permissionsString(DEFAULT_PERMISSIONS)
            ).execute();

        assertCollectionSize("json-lines", 2);
    }

    @Test
    void uriTemplateHashValues() {
        Flux.importAggregateJsonFiles()
            .connectionString(makeConnectionString())
            .from(options -> options
                .paths("src/test/resources/delimited-files/custom-delimiter-json.txt")
                .jsonLines()
                .additionalOptions(Map.of("lineSep", ":\n")))
            .to(options -> options
                .permissionsString(DEFAULT_PERMISSIONS)
                .collections("custom-delimiter-hashed")
                .uriTemplate("/custom-delimiter/{firstName}.json")
                .uriTemplateHashValues())
            .execute();

        assertCollectionSize("custom-delimiter-hashed", 3);
        Stream.of("firstName-1", "firstName-2", "firstName-3").forEach(name -> {
            byte[] hash = sha256Hex(name);
            String expectedUri = String.format("/custom-delimiter/%s.json", HexFormat.of().formatHex(hash));
            JsonNode doc = readJsonDocument(expectedUri);
            assertEquals(name, doc.get("firstName").asText());
            assertTrue(doc.has("lastName"));
        });
    }

    private static byte[] sha256Hex(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
