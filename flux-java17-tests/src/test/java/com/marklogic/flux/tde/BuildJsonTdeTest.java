/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;


import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

/**
 * This test is in this Java17-dependent project solely for using triple quotes for easily putting the
 * expected TDE template in the test.
 */
class BuildJsonTdeTest {

    @Test
    void allTypes() {
        final String expectedTemplate = """
            {
              "template" : {
                "context" : "/",
                "collections" : [ "changeme" ],
                "rows" : [ {
                  "schemaName" : "my-schema",
                  "viewName" : "my-view",
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

        TdeInputs inputs = new TdeInputs("my-schema", "my-view", new SparkColumnIterator(schema));
        String tde = TdeBuilder.newTdeBuilder(inputs).buildTde(inputs);

        JsonAssertions.assertThatJson(tde)
            .describedAs("For now, we're ignoring array/map/struct types in the TDE template, " +
                "we may add support for them later.")
            .isEqualTo(expectedTemplate);
    }

    @Test
    void jsonRootName() {
        final String expectedTemplate = """
            {
              "template" : {
                "context" : "/my-root",
                "collections" : [ "changeme" ],
                "rows" : [ {
                  "schemaName" : "my-schema",
                  "viewName" : "my-view",
                  "columns" : [ {
                    "name" : "myString",
                    "val" : "myString",
                    "scalarType" : "string"
                  } ]
                } ]
              }
            }""";

        StructType schema = new StructType().add("myString", DataTypes.StringType);

        TdeInputs inputs = new TdeInputs("my-schema", "my-view", new SparkColumnIterator(schema))
            .withJsonRootName("my-root");

        String tde = TdeBuilder.newTdeBuilder(inputs).buildTde(inputs);

        JsonAssertions.assertThatJson(tde).isEqualTo(expectedTemplate);
    }
}
