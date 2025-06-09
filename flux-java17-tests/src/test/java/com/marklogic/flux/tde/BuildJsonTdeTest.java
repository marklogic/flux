/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.flux.impl.importdata.SparkColumnIterator;
import marklogicspark.marklogic.client.io.JacksonHandle;
import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is in this Java17-dependent project solely for using triple quotes for easily putting the
 * expected TDE template in the test.
 */
class BuildJsonTdeTest extends AbstractTdeTest {

    private static final StructType DEFAULT_SCHEMA = new StructType().add("myString", DataTypes.StringType);

    @Test
    void allTypes() {
        final String expectedTemplate = """
            {
              "template" : {
                "context" : "/some-custom-context",
                "collections" : [ "customer" ],
                "rows" : [ {
                  "schemaName" : "myschema",
                  "viewName" : "myview",
                  "columns" : [ {
                    "name" : "myInt",
                    "val" : "myInt",
                    "scalarType" : "int"
                  }, {
                    "name" : "myShort",
                    "val" : "myShort",
                    "scalarType" : "short"
                  }, {
                    "name" : "myString",
                    "val" : "myString",
                    "scalarType" : "string"
                  }, {
                    "name" : "myBoolean",
                    "val" : "myBoolean",
                    "scalarType" : "boolean"
                  }, {
                    "name" : "myDouble",
                    "val" : "myDouble",
                    "scalarType" : "double"
                  }, {
                    "name" : "myFloat",
                    "val" : "myFloat",
                    "scalarType" : "float"
                  }, {
                    "name" : "myLong",
                    "val" : "myLong",
                    "scalarType" : "long"
                  }, {
                    "name" : "myDecimal",
                    "val" : "myDecimal",
                    "scalarType" : "string"
                  }, {
                    "name" : "myBinary",
                    "val" : "myBinary",
                    "scalarType" : "base64Binary"
                  }, {
                    "name" : "myDate",
                    "val" : "myDate",
                    "scalarType" : "date"
                  }, {
                    "name" : "myByte",
                    "val" : "myByte",
                    "scalarType" : "byte"
                  }, {
                    "name" : "myTimestamp",
                    "val" : "myTimestamp",
                    "scalarType" : "dateTime"
                  } ]
                } ]
              }
            }""";

        StructType schema = new StructType()
            .add("myInt", DataTypes.IntegerType)
            .add("myShort", DataTypes.ShortType)
            .add("myString", DataTypes.StringType)
            .add("myBoolean", DataTypes.BooleanType)
            .add("myDouble", DataTypes.DoubleType)
            .add("myFloat", DataTypes.FloatType)
            .add("myLong", DataTypes.LongType)
            .add("myDecimal", DataTypes.createDecimalType(10, 2))
            .add("myBinary", DataTypes.BinaryType)
            .add("myDate", DataTypes.DateType)
            .add("myByte", DataTypes.ByteType)
            .add("myTimestamp", DataTypes.TimestampType)
            .add("myArray", DataTypes.createArrayType(DataTypes.StringType))
            .add("myMap", DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType))
            .add("myStruct", new StructType()
                .add("myInnerInt", DataTypes.IntegerType)
                .add("myInnerString", DataTypes.StringType))
            .add("myNull", DataTypes.NullType);

        ObjectNode tde = buildTde(new TdeInputs("myschema", "myview")
            .withCollections("customer")
            .withDirectories()
            .withContext("/some-custom-context"), schema
        );
        JsonAssertions.assertThatJson(tde)
            .describedAs("For now, we're ignoring array/map/struct types in the TDE template, " +
                "we may add support for them later.")
            .isEqualTo(expectedTemplate);
    }

    @Test
    void jsonRootNameWithViewLayout() {
        final String expectedTemplate = """
            {
              "template" : {
                "context" : "/my-root",
                "collections" : [ "customer", "customer2" ],
                "directories" : [ "dir1/", "/dir2/" ],
                "rows" : [ {
                  "schemaName" : "myschema",
                  "viewName" : "myview",
                  "viewLayout": "identical",
                  "columns" : [ {
                    "name" : "myString",
                    "val" : "myString",
                    "scalarType" : "string"
                  } ]
                } ]
              }
            }""";

        ObjectNode tde = buildTde(new TdeInputs("myschema", "myview")
                .withCollections("customer", "customer2")
                .withDirectories("dir1/", "/dir2/")
                .withJsonRootName("my-root")
                .withViewLayout("identical"),
            DEFAULT_SCHEMA
        );

        JsonAssertions.assertThatJson(tde).isEqualTo(expectedTemplate);
    }

    @Test
    void jsonRootNameWithCustomContextAndDisabled() {
        final String expectedTemplate = """
            {
              "template" : {
                "context" : "/some-custom-context",
                "enabled": false,
                "rows" : [ {
                  "schemaName" : "myschema",
                  "viewName" : "myview",
                  "columns" : [ {
                    "name" : "myString",
                    "val" : "myString",
                    "scalarType" : "string"
                  } ]
                } ]
              }
            }""";

        ObjectNode tde = buildTde(new TdeInputs("myschema", "myview")
                .withContext("/some-custom-context")
                .withJsonRootName("my-root")
                .withDisabled(true),
            DEFAULT_SCHEMA
        );
        JsonAssertions.assertThatJson(tde).isEqualTo(expectedTemplate);

        tde = buildTde(new TdeInputs("myschema", "myview")
                .withJsonRootName("my-root")
                .withContext("/some-custom-context")
                .withDisabled(true),
            DEFAULT_SCHEMA
        );
        JsonAssertions.assertThatJson(tde).isEqualTo(expectedTemplate);
    }

    @Test
    void customizedColumns() {
        final String expectedTemplate = """
            {
              "template" : {
                "context" : "/some-custom-context",
                "enabled": false,
                "rows" : [ {
                  "schemaName" : "myschema",
                  "viewName" : "myview",
                  "columns" : [ {
                    "name" : "myString",
                    "val" : "myStringValue",
                    "scalarType" : "string",
                    "nullable" : true,
                    "default" : "0",
                    "invalidValues" : "reject",
                    "reindexing" : "visible",
                    "permissions" : ["rest-reader", "rest-writer"],
                    "collation" : "http://marklogic.com/collation/codepoint"
                  } ]
                } ]
              }
            }""";

        TdeInputs inputs = new TdeInputs("myschema", "myview")
            .withContext("/some-custom-context")
            .withJsonRootName("my-root")
            .withDisabled(true)
            .withColumnVals(Map.of("myString", "myStringValue"))
            .withColumnTypes(Map.of("myString", "string"))
            .withNullableColumns(List.of("myString"))
            .withColumnDefaultValues(Map.of("myString", "0"))
            .withColumnInvalidValues(Map.of("myString", "reject"))
            .withColumnReindexing(Map.of("myString", "visible"))
            .withColumnPermissions(Map.of("myString", Set.of("rest-reader", "rest-writer")))
            .withColumnCollations(Map.of("myString", "http://marklogic.com/collation/codepoint"));

        ObjectNode tde = buildTde(inputs, DEFAULT_SCHEMA);
        JsonAssertions.assertThatJson(tde)
            // The Set of permission role names is unordered, and order doesn't matter in the TDE template.
            .withOptions(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(expectedTemplate);
    }

    @Test
    void customUri() {
        TdeInputs inputs = new TdeInputs("myschema", "myview")
            .withUri("/my/uri.json");

        TdeTemplate template = new JsonTdeBuilder().buildTde(inputs, new SparkColumnIterator(DEFAULT_SCHEMA, inputs));
        assertEquals("/my/uri.json", template.getUri());
    }

    private ObjectNode buildTde(TdeInputs inputs, StructType schema) {
        TdeTemplate template = new JsonTdeBuilder().buildTde(inputs, new SparkColumnIterator(schema, inputs));
        assertEquals("/tde/%s/%s.json".formatted(inputs.getSchemaName(), inputs.getViewName()), template.getUri());

        verifyTdeCanBeLoaded(template);

        AbstractWriteHandle handle = template.getWriteHandle();
        return (ObjectNode) ((JacksonHandle) handle).get();
    }
}
