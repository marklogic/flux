/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import org.apache.spark.sql.types.*;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Contains the Spark-specific bits for generating a TDE.
 */
public class SparkColumnIterator implements Iterator<TdeInputs.Column> {

    private final Iterator<StructField> fields;

    public SparkColumnIterator(StructType structType) {
        fields = Arrays.stream(structType.fields()).iterator();
    }

    @Override
    public boolean hasNext() {
        return fields.hasNext();
    }

    @Override
    public TdeInputs.Column next() {
        return new SparkColumn(fields.next());
    }

    private static class SparkColumn implements TdeInputs.Column {

        private final StructField field;

        public SparkColumn(StructField field) {
            this.field = field;
        }

        @Override
        public String getName() {
            return field.name();
        }

        @Override
        public String getScalarType() {
            return convertSparkTypeToTdeType(this.field.dataType());
        }

        private String convertSparkTypeToTdeType(DataType sparkType) {
            if (DataTypes.IntegerType.equals(sparkType)) {
                return "int";
            } else if (DataTypes.LongType.equals(sparkType)) {
                return "long";
            } else if (DataTypes.FloatType.equals(sparkType)) {
                return "float";
            } else if (DataTypes.DoubleType.equals(sparkType)) {
                return "double";
            } else if (DataTypes.DateType.equals(sparkType)) {
                return "date";
            } else if (DataTypes.TimestampType.equals(sparkType)) {
                return "dateTime";
            } else if (DataTypes.BooleanType.equals(sparkType)) {
                return "boolean";
            } else if (DataTypes.BinaryType.equals(sparkType)) {
                return "base64Binary";
            } else if (DataTypes.ByteType.equals(sparkType)) {
                return "byte";
            } else if (DataTypes.ShortType.equals(sparkType)) {
                return "short";
            } else if (sparkType instanceof ArrayType) {
                return null;
            } else if (sparkType instanceof MapType) {
                return null;
            } else if (sparkType instanceof StructType) {
                return null;
            } else if (DataTypes.NullType.equals(sparkType)) {
                return null;
            }
            return "string";
        }
    }
}
