/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.flux.tde.JsonTdeBuilder;
import com.marklogic.flux.tde.TdeInputs;
import com.marklogic.flux.tde.TdeTemplate;
import com.marklogic.flux.tde.XmlTdeBuilder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SanitizeTdeColumnNamesTest {

    @ParameterizedTest
    @MethodSource("columnNames")
    void sanitizesNames(String input, String expected) {
        assertEquals(expected, TdeColumnNameSanitizer.sanitize(input));
    }

    private static Stream<Arguments> columnNames() {
        return Stream.of(
            arguments("First/Last Name", "First_Last_Name"),
            arguments("it's-valid", "it_s_valid"),
            arguments("1stPlace", "_1stPlace"),
            arguments("$foo", "_$foo"),
            arguments("#foo", "_#foo"),
            arguments("@foo", "_@foo"),
            arguments("a$#@9", "a$#@9"),
            arguments("\"Mixed Name\"", "\"Mixed Name\""),
            arguments("\"a\"b\"", "_a_b_"),
            arguments("\"\"", "__"),
            arguments("a😀b", "a_b"),
            arguments("é١", "é١"),
            arguments("\u1885", "\u1885"),
            arguments("a\u1cf2", "a_"),
            arguments("\u1c90", "_"),
            arguments("a\u0301", "a_"),
            arguments("", ""),
            arguments("a".repeat(128), "a".repeat(128)),
            arguments("a".repeat(129), "a".repeat(129)),
            arguments("1" + "a".repeat(127), "_1" + "a".repeat(127)),
            arguments("a/b", "a_b"),
            arguments("a-b", "a_b")
        );
    }

    @Test
    void flagIsOptIn() {
        TdeParams defaults = new TdeParams();
        new CommandLine(defaults).parseArgs();
        assertFalse(defaults.isSanitizeColumnNames());

        TdeParams enabled = new TdeParams();
        new CommandLine(enabled).parseArgs("--sanitize-tde-column-names");
        assertTrue(enabled.isSanitizeColumnNames());

        StructType schema = new StructType().add("first/name", DataTypes.StringType);
        TdeInputs inputs = new TdeInputs("schema", "view");
        assertEquals("first/name", new SparkColumnIterator(schema, inputs, defaults.isSanitizeColumnNames()).next().getName());
        assertEquals("first_name", new SparkColumnIterator(schema, inputs, enabled.isSanitizeColumnNames()).next().getName());
    }

    @Test
    void jsonTemplatePreservesSourceValuesAndColumnOptions() {
        StructType schema = new StructType()
            .add("first/name", DataTypes.StringType)
            .add("it's-valid", DataTypes.StringType);
        TdeInputs inputs = new TdeInputs("schema", "view")
            .withColumnVals(Map.of("first/name", "custom-expression"))
            .withColumnTypes(Map.of("first/name", "int"))
            .withNullableColumns(List.of("first/name"))
            .withColumnDefaultValues(Map.of("first/name", "5"));

        TdeTemplate template = new JsonTdeBuilder().buildTde(inputs, new SparkColumnIterator(schema, inputs, true));
        JsonNode columns = ((JacksonHandle) template.getWriteHandle()).get()
            .path("template").path("rows").get(0).path("columns");
        assertEquals("first_name", columns.get(0).path("name").asText());
        assertEquals("custom-expression", columns.get(0).path("val").asText());
        assertEquals("int", columns.get(0).path("scalarType").asText());
        assertTrue(columns.get(0).path("nullable").asBoolean());
        assertEquals("5", columns.get(0).path("default").asText());
        assertEquals("it_s_valid", columns.get(1).path("name").asText());
        assertEquals("it's-valid", columns.get(1).path("val").asText());
    }

    @Test
    void xmlTemplatePreservesNamespacedValueExpression() {
        StructType schema = new StructType().add("first-name", DataTypes.StringType);
        TdeInputs inputs = new TdeInputs("schema", "view").withXmlRootName("root", "urn:test");
        TdeTemplate template = new XmlTdeBuilder().buildTde(inputs, new SparkColumnIterator(schema, inputs, true));
        Document doc = ((DOMHandle) template.getWriteHandle()).get();
        assertEquals("first_name", doc.getElementsByTagNameNS("http://marklogic.com/xdmp/tde", "name").item(0).getTextContent());
        assertEquals("ns1:first-name", doc.getElementsByTagNameNS("http://marklogic.com/xdmp/tde", "val").item(0).getTextContent());
    }
}
