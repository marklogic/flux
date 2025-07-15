/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.expression.PlanBuilder;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.row.RowManager;
import com.marklogic.client.row.RowRecord;
import com.marklogic.client.row.RowSet;
import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.PostgresUtil;
import com.marklogic.junit5.XmlNode;
import org.jdom2.Namespace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportJdbcWithTdeTest extends AbstractTest {

    private static final String JSON_TDE_URI = "/tde/junit/customer.json";
    private static final String XML_TDE_URI = "/tde/junit/customer.xml";

    private DatabaseClient schemasDatabaseClient;

    @BeforeEach
    void setup() {
        schemasDatabaseClient = newDatabaseClient("flux-test-schemas");
    }

    @AfterEach
    void deleteCustomerTemplate() {
        // This needs to be deleted after a test so as not to impact other tests that aren't aware that this might
        // exist.
        schemasDatabaseClient.newDocumentManager().delete(JSON_TDE_URI, XML_TDE_URI, "/tde/custom-uri.json");
        schemasDatabaseClient.release();
    }

    @Test
    void buildAndLogJsonTde() {
        importJdbcWithArgs(
            "--tde-schema", "demo",
            "--tde-view", "example",
            "--json-root-name", "customer",
            "--tde-preview"
        );

        assertCollectionSize(
            "Just verifying that for now, no documents were imported because a TDE should have been generated and " +
                "logged due to the user of tde-preview.", "customer", 0);
    }

    @Test
    void buildAndLogXmlTde() {
        importJdbcWithArgs(
            "--tde-schema", "demo",
            "--tde-view", "example",
            "--xml-root-name", "customer",
            "--xml-namespace", "http://example.com/customer",
            "--tde-preview"
        );

        assertCollectionSize(
            "Just verifying that for now, no documents were imported because a TDE should have been generated and " +
                "logged due to the user of tde-preview.", "customer", 0);
    }

    @Test
    void tdeWithCollections() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "customer"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();

        assertNotNull(schemasDatabaseClient.newDocumentManager().exists(JSON_TDE_URI));
        assertNull(schemasDatabaseClient.newDocumentManager().exists(XML_TDE_URI));
    }

    @Test
    void xmlTde() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-document-type", "xml",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "customer"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();

        assertNotNull(schemasDatabaseClient.newDocumentManager().exists(XML_TDE_URI));
        assertNull(schemasDatabaseClient.newDocumentManager().exists(JSON_TDE_URI));
    }

    @Test
    void tdeWithDirectories() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-directory", "/customer/",
            // Including a second directory to ensure that it doesn't cause an error.
            "--tde-directory", "/doesnt-matter/"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();
    }

    @Test
    void tdeWithJsonRootName() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--json-root-name", "customRootName"
        );

        JsonNode tdeTemplate = schemasDatabaseClient.newJSONDocumentManager()
            .read(JSON_TDE_URI, new JacksonHandle()).get();
        assertEquals("/customRootName", tdeTemplate.get("template").get("context").asText());
    }

    @Test
    void tdeWithXmlRootNameAndNamespace() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--xml-root-name", "customRootName",
            "--xml-namespace", "http://example.org",
            "--uri-template", "/customer/{customer_id}.xml"
        );

        JsonNode tdeTemplate = schemasDatabaseClient.newJSONDocumentManager().read(JSON_TDE_URI, new JacksonHandle()).get();
        assertEquals("/ns1:customRootName", tdeTemplate.get("template").get("context").asText());

        ArrayNode namespaces = tdeTemplate.get("template").withArray("pathNamespace");
        assertEquals(1, namespaces.size());

        JsonNode namespace = namespaces.get(0);
        assertEquals("http://example.org", namespace.get("namespaceUri").asText());
        assertEquals("ns1", namespace.get("prefix").asText(),
            "For now, we're just using a default namspace prefix of 'ns1', knowing that we only ever care about a " +
                "single namespace, which is the one that a user can specify via --xml-namespace.");

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();
    }

    @Test
    void xmlTdeWithXmlRootNameAndNamespace() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-document-type", "xml",
            "--xml-root-name", "customRootName",
            "--xml-namespace", "http://example.org",
            "--uri-template", "/customer/{customer_id}.xml"
        );

        String xml = schemasDatabaseClient.newJSONDocumentManager().read(XML_TDE_URI, new StringHandle()).get();
        XmlNode tdeTemplate = new XmlNode(xml, Namespace.getNamespace("tde", "http://marklogic.com/xdmp/tde"));
        tdeTemplate.assertElementValue("/tde:template/tde:context", "/ns1:customRootName");
        tdeTemplate.assertElementValue(
            "When a user specifies an XML root name and namespace, the TDE should use the namespace prefix 'ns1' " +
                "for the column values, which is the default prefix for a single namespace in a TDE.",
            "/tde:template/tde:rows/tde:row/tde:columns/tde:column[1]/tde:val", "ns1:customer_id");

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();
    }

    @Test
    void tdeWithCustomUri() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "customer",
            "--tde-uri", "/tde/custom-uri.json",

            // Make sure this option works, even though it's set to the default value.
            "--tde-view-layout", "sparse"
        );

        assertCollectionSize("customer", 10);
        verifyOpticQueryUsingTdeViewReturnsCustomers();

        assertNotNull(schemasDatabaseClient.newDocumentManager().exists("/tde/custom-uri.json"));
        assertNull(schemasDatabaseClient.newDocumentManager().exists(JSON_TDE_URI));
    }

    @Test
    void tdeCollectionDoesntMatchAnything() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "not-customer"
        );

        assertCollectionSize("customer", 10);

        RowManager rm = getDatabaseClient().newRowManager();
        PlanBuilder op = rm.newPlanBuilder();
        RowSet<RowRecord> rows = rm.resultRows(op.fromView("junit", "customer", ""));
        assertFalse(rows.iterator().hasNext(), "The TDE is valid, but there are no docs in the 'not-customer' " +
            "collection, so no rows should be returned.");
    }

    @Test
    void invalidPermissions() {
        String stderr = runAndReturnStderr(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS,

            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role"
        );

        assertTrue(stderr.contains("Unable to parse permissions string"), "Actual stderr: " + stderr);
        assertCollectionSize(
            "No data should be loaded if an error occurs while loading the TDE template.",
            "customer", 0
        );
    }

    @Test
    void invalidViewLayout() {
        String stderr = runAndReturnStderr(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS,

            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-view-layout", "invalid-layout"
        );

        assertTrue(stderr.contains("TDE-INVALIDTEMPLATENODEVAL"), "Actual stderr: " + stderr);
    }

    @Test
    void tdeWithAllColumnCustomizations() {
        importJdbcWithArgs(
            "--tde-schema", "junit",
            "--tde-view", "customer",
            "--tde-permissions", "flux-test-role,read,flux-test-role,update",
            "--tde-collections", "customer",

            "--tde-column-val", "customer_id=customerId",
            "--tde-column-val", "first_name=firstName",
            "--tde-column-type", "customer_id=int",
            "--tde-column-default", "customer_id=0",
            "--tde-column-type", "first_name=string",
            "--tde-column-reindexing", "customer_id=visible",
            "--tde-column-reindexing", "first_name=hidden",
            "--tde-column-invalid-values", "customer_id=reject",
            "--tde-column-invalid-values", "first_name=ignore",
            "--tde-column-permissions", "customer_id=flux-test-role",
            "--tde-column-permissions", "first_name=flux-test-role,qconsole-user",
            "--tde-column-nullable", "customer_id",
            "--tde-column-nullable", "first_name",
            "--tde-column-collation", "first_name=http://marklogic.com/collation/codepoint"
        );

        assertCollectionSize("customer", 10);
        verifyTdeContainsColumnCustomizations();
    }

    private void importJdbcWithArgs(String... options) {
        List<String> defaultArgs = List.of(
            "import-jdbc",
            "--jdbc-url", PostgresUtil.URL,
            "--jdbc-user", PostgresUtil.USER,
            "--jdbc-password", PostgresUtil.PASSWORD,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--query", "select * from customer where customer_id < 11",
            "--connection-string", makeConnectionString(),
            "--collections", "customer",
            "--permissions", DEFAULT_PERMISSIONS
        );

        List<String> args = new ArrayList<>(defaultArgs);
        for (String option : options) {
            args.add(option);
        }

        if (!args.contains("--uri-template")) {
            args.add("--uri-template");
            args.add("/customer/{customer_id}.json");
        }

        run(args.toArray(new String[0]));
    }

    private void verifyOpticQueryUsingTdeViewReturnsCustomers() {
        RowManager rm = getDatabaseClient().newRowManager();
        PlanBuilder op = rm.newPlanBuilder();
        JsonNode result = rm.resultDoc(op.fromView("junit", "customer", "")
                .select(op.col("customer_id"), op.col("first_name"))
                .orderBy(op.col("customer_id")),
            new JacksonHandle()).get();

        JsonNode rows = result.get("rows");
        assertEquals(10, rows.size());
        assertEquals("Mary", rows.get(0).get("first_name").get("value").asText());
    }

    private void verifyTdeContainsColumnCustomizations() {
        JsonNode tdeTemplate = schemasDatabaseClient.newJSONDocumentManager()
            .read(JSON_TDE_URI, new JacksonHandle()).get();

        JsonNode columns = tdeTemplate.get("template").get("rows").get(0).get("columns");

        // Find the customer_id column and verify its customizations
        JsonNode customerIdColumn = null;
        JsonNode firstNameColumn = null;

        for (JsonNode column : columns) {
            String name = column.get("name").asText();
            if ("customer_id".equals(name)) {
                customerIdColumn = column;
            } else if ("first_name".equals(name)) {
                firstNameColumn = column;
            }
        }

        assertNotNull(customerIdColumn, "customer_id column should exist in TDE");
        assertNotNull(firstNameColumn, "first_name column should exist in TDE");

        // Verify customer_id customizations
        assertEquals("customerId", customerIdColumn.get("val").asText());
        assertEquals("int", customerIdColumn.get("scalarType").asText());
        assertEquals("visible", customerIdColumn.get("reindexing").asText());
        assertEquals("flux-test-role", customerIdColumn.get("permissions").get(0).asText());
        assertTrue(customerIdColumn.get("nullable").asBoolean());
        assertEquals("reject", customerIdColumn.get("invalidValues").asText());
        assertEquals(0, customerIdColumn.get("default").asInt());

        // Verify first_name customizations
        assertEquals("firstName", firstNameColumn.get("val").asText());
        assertEquals("string", firstNameColumn.get("scalarType").asText());
        assertEquals("hidden", firstNameColumn.get("reindexing").asText());
        assertEquals("flux-test-role", firstNameColumn.get("permissions").get(0).asText());
        assertEquals("qconsole-user", firstNameColumn.get("permissions").get(1).asText());
        assertTrue(firstNameColumn.get("nullable").asBoolean());
        assertEquals("http://marklogic.com/collation/codepoint", firstNameColumn.get("collation").asText());
        assertEquals("ignore", firstNameColumn.get("invalidValues").asText());
    }
}
