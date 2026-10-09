/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.flux.AbstractTest;
import com.marklogic.junit5.PermissionsTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SidecarInheritanceE2ETest extends AbstractTest {

    private final String HR_SOURCE_URI = "/e2e/policy-hr.json";
    private final String FINANCE_SOURCE_URI = "/e2e/report-finance.json";

    @AfterEach
    void cleanupE2eDocuments() {
        deleteDocuments("/e2e/*", "/e2e-processed/*", "/e2e-import/*");
    }

    private void deleteDocuments(String... uriPatterns) {
        for (String pattern : uriPatterns) {
            getDatabaseClient().newServerEval()
                .javascript("declareUpdate(); for (var uri of cts.uriMatch('" + pattern + "')) { xdmp.documentDelete(uri); }")
                .eval();
        }
    }

    @Test
    void endToEndCopyMultiTenantDocumentsWithInheritedCollectionsAndPermissions() {
        JSONDocumentManager docMgr = getDatabaseClient().newJSONDocumentManager();

        // 1. Inject HR source document with HR-specific collections & permissions
        DocumentMetadataHandle hrMeta = new DocumentMetadataHandle();
        hrMeta.getCollections().addAll("e2e-source", "department-hr", "confidential");
        hrMeta.getPermissions().add("flux-test-role", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE);
        docMgr.write(HR_SOURCE_URI, hrMeta, new StringHandle("{\"title\": \"HR Benefits Policy\", \"body\": \"All employees receive healthcare and 401k match.\"}"));

        // 2. Inject Finance source document with Finance-specific collections & permissions
        // Note: flux-test-role must have READ so the copy job user can read the document to copy it
        DocumentMetadataHandle financeMeta = new DocumentMetadataHandle();
        financeMeta.getCollections().addAll("e2e-source", "department-finance", "restricted");
        financeMeta.getPermissions().add("flux-test-role", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE);
        financeMeta.getPermissions().add("qconsole-user", DocumentMetadataHandle.Capability.READ);
        docMgr.write(FINANCE_SOURCE_URI, financeMeta, new StringHandle("{\"title\": \"Q3 Financial Audit\", \"body\": \"Q3 revenue increased by 14% year over year.\"}"));

        try {
            // 3. Execute End-to-End Flux Copy with both inheritance flags and explicit sidecar options
            run(
                "copy",
                "--collections", "e2e-source",
                "--connection-string", makeConnectionString(),
                "--output-uri-prefix", "/e2e-processed",
                "--splitter-json-pointer", "/body",
                "--splitter-sidecar-max-chunks", "1",
                "--splitter-sidecar-inherit-collections",
                "--splitter-sidecar-collections", "rag-index",
                "--splitter-sidecar-inherit-permissions",
                "--splitter-sidecar-permissions", "manage-user,read,flux-test-role,update"
            );

            // 4. Verify HR sidecar chunk document metadata
            String hrChunkUri = "/e2e-processed/e2e/policy-hr.json-chunks-1.json";
            DocumentMetadataHandle hrChunkMeta = docMgr.readMetadata(hrChunkUri, new DocumentMetadataHandle());

            // Collections: Union of HR collections + explicit rag-index (4 collections)
            assertEquals(4, hrChunkMeta.getCollections().size());
            assertTrue(hrChunkMeta.getCollections().contains("department-hr"));
            assertTrue(hrChunkMeta.getCollections().contains("confidential"));
            assertTrue(hrChunkMeta.getCollections().contains("e2e-source"));
            assertTrue(hrChunkMeta.getCollections().contains("rag-index"));
            assertFalse(hrChunkMeta.getCollections().contains("department-finance"), "HR chunk must not have finance collection");

            // Permissions: Union of HR capabilities + explicit manage-user
            PermissionsTester hrTester = readDocumentPermissions(hrChunkUri);
            hrTester.assertReadPermissionExists("flux-test-role");
            hrTester.assertUpdatePermissionExists("flux-test-role");
            hrTester.assertReadPermissionExists("manage-user");

            // 5. Verify Finance sidecar chunk document metadata
            String financeChunkUri = "/e2e-processed/e2e/report-finance.json-chunks-1.json";
            DocumentMetadataHandle financeChunkMeta = docMgr.readMetadata(financeChunkUri, new DocumentMetadataHandle());

            // Collections: Union of Finance collections + explicit rag-index (4 collections)
            assertEquals(4, financeChunkMeta.getCollections().size());
            assertTrue(financeChunkMeta.getCollections().contains("department-finance"));
            assertTrue(financeChunkMeta.getCollections().contains("restricted"));
            assertTrue(financeChunkMeta.getCollections().contains("e2e-source"));
            assertTrue(financeChunkMeta.getCollections().contains("rag-index"));
            assertFalse(financeChunkMeta.getCollections().contains("department-hr"), "Finance chunk must not have HR collection");

            // Permissions: Union of Finance capabilities + explicit manage-user
            PermissionsTester financeTester = readDocumentPermissions(financeChunkUri);
            financeTester.assertReadPermissionExists("flux-test-role");
            financeTester.assertReadPermissionExists("qconsole-user");
            financeTester.assertReadPermissionExists("manage-user");
            financeTester.assertUpdatePermissionExists("flux-test-role");

            // 6. Verify Semantic Collection Partitioning across the entire database
            // rag-index has exactly the 2 generated sidecar chunks
            assertCollectionSize("rag-index", 2);
            // department-hr has 1 source doc + 1 copied doc + 1 chunk doc = 3 docs
            assertCollectionSize("department-hr", 3);
            // department-finance has 1 source doc + 1 copied doc + 1 chunk doc = 3 docs
            assertCollectionSize("department-finance", 3);

        } finally {
            cleanupE2eDocuments();
        }
    }

    @Test
    void endToEndImportFilesWithInheritedCollectionsAndPermissions() {
        try {
            // Execute End-to-End File Ingest with splitting and metadata inheritance
            run(
                "import-files",
                "--path", "../flux-cli/src/test/resources/json-files/java-client-intro.json",
                "--connection-string", makeConnectionString(),
                "--collections", "e2e-import-docs,manuals",
                "--permissions", "flux-test-role,read,flux-test-role,update",
                "--uri-replace", ".*/json-files,''",
                "--uri-prefix", "/e2e-import",
                "--splitter-json-pointer", "/text",
                "--splitter-max-chunk-size", "500",
                "--splitter-sidecar-max-chunks", "2",
                "--splitter-sidecar-inherit-collections",
                "--splitter-sidecar-collections", "e2e-chunks",
                "--splitter-sidecar-inherit-permissions",
                "--splitter-sidecar-permissions", "qconsole-user,read"
            );

            // Verify chunk 1 metadata
            String chunkUri = "/e2e-import/java-client-intro.json-chunks-1.json";
            DocumentMetadataHandle chunkMeta = getDatabaseClient().newJSONDocumentManager().readMetadata(chunkUri, new DocumentMetadataHandle());

            // Collections union: 2 source collections + 1 explicit = 3 collections
            assertEquals(3, chunkMeta.getCollections().size());
            assertTrue(chunkMeta.getCollections().contains("e2e-import-docs"));
            assertTrue(chunkMeta.getCollections().contains("manuals"));
            assertTrue(chunkMeta.getCollections().contains("e2e-chunks"));

            // Permissions capability union
            PermissionsTester tester = readDocumentPermissions(chunkUri);
            tester.assertReadPermissionExists("flux-test-role");
            tester.assertUpdatePermissionExists("flux-test-role");
            tester.assertReadPermissionExists("qconsole-user");

            assertCollectionSize("e2e-chunks", 2);

        } finally {
            cleanupE2eDocuments();
        }
    }
}
