/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a hack purely to support a "--tde" prototype. For the real future, we'd reuse the mapping
 * in SchemaInferrer in the Spark connector.
 */
public class TdeUtil {

    public static final Map<String, DataType> COLUMN_INFO_TYPES_TO_SPARK_TYPES = new HashMap<>();

    static {
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("int", DataTypes.IntegerType);
        // Including "short" here, but a TDE column of type "short" reports "int" as its type in column info. So not
        // yet able to test this, but including it here in case the server does report "short" in the future.
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("short", DataTypes.ShortType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("unsignedInt", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("long", DataTypes.LongType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("unsignedLong", DataTypes.LongType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("float", DataTypes.FloatType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("double", DataTypes.DoubleType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("decimal", DataTypes.DoubleType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("dateTime", DataTypes.TimestampType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("date", DataTypes.DateType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("gYearMonth", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("gYear", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("gMonth", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("gDay", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("yearMonthDuration", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("dayTimeDuration", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("string", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("collatedString", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("anyUri", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("point", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("boolean", DataTypes.BooleanType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("none", DataTypes.StringType); // See DBQ-296, this is intentional for some column types.
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("value", DataTypes.StringType); // In MarkLogic 10, "value" is returned for a column containing a JSON object.
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("integer", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("unsignedInt", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("iri", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("time", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("unknown", DataTypes.StringType);

        // New MarkLogic 12 types.
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("unsignedByte", DataTypes.ByteType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("unsignedShort", DataTypes.ShortType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("base64Binary", DataTypes.BinaryType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("hexBinary", DataTypes.BinaryType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("byte", DataTypes.ByteType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("duration", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("gMonthDay", DataTypes.StringType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("positiveInteger", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("nonPositiveInteger", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("negativeInteger", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("nonNegativeInteger", DataTypes.IntegerType);
        COLUMN_INFO_TYPES_TO_SPARK_TYPES.put("longLatPoint", DataTypes.StringType);
    }

    public static ObjectNode buildTde(StructType schema) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode tde = mapper.createObjectNode();
        ObjectNode template = tde.putObject("template");
        template.put("context", "/");
        template.putArray("collections").add("testing");
        ObjectNode row = template.putArray("rows").addObject();
        row.put("schemaName", "todo");
        row.put("viewName", "changeme");
        ArrayNode columns = row.putArray("columns");
        for (StructField field : schema.fields()) {
            ObjectNode column = columns.addObject();
            column.put("name", field.name());
            column.put("val", field.name());
            String type = getTdeType(field.dataType());
            column.put("scalarType", type);
        }
        return tde;
    }

    private static String getTdeType(DataType dataType) {
        for (Map.Entry<String, DataType> entry : COLUMN_INFO_TYPES_TO_SPARK_TYPES.entrySet()) {
            if (entry.getValue().sameType(dataType)) {
                return entry.getKey();
            }
        }
        // Could log a warning here.
        return "string";
    }
}
