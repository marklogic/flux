/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.tde.TdeBuilder;
import com.marklogic.flux.tde.TdeInputs;
import org.apache.spark.sql.types.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * Contains the Spark-specific bits for generating a TDE.
 */
public class SparkColumnIterator implements Iterator<TdeBuilder.Column> {

    private final Iterator<StructField> fields;
    private final TdeInputs tdeInputs;

    public SparkColumnIterator(StructType schema, TdeInputs tdeInputs) {
        this.fields = Arrays.stream(schema.fields()).iterator();
        this.tdeInputs = tdeInputs;
    }

    @Override
    public boolean hasNext() {
        return fields.hasNext();
    }

    @Override
    public TdeBuilder.Column next() {
        return new SparkColumn(fields.next(), tdeInputs);
    }

    private static class SparkColumn implements TdeBuilder.Column {

        private final StructField field;
        private final TdeInputs tdeInputs;

        public SparkColumn(StructField field, TdeInputs tdeInputs) {
            this.field = field;
            this.tdeInputs = tdeInputs;
        }

        @Override
        public String getName() {
            return field.name();
        }

        @Override
        public String getVal() {
            String name = getName();
            return tdeInputs.getColumnVals() != null && tdeInputs.getColumnVals().containsKey(name) ?
                    tdeInputs.getColumnVals().get(name) : getName();
        }

        @Override
        public String getScalarType() {
            String name = getName();
            if (tdeInputs.getColumnTypes() != null && tdeInputs.getColumnTypes().containsKey(name)) {
                return tdeInputs.getColumnTypes().get(name);
            }
            return convertSparkTypeToTdeType(this.field.dataType());
        }

        @Override
        public boolean isNullable() {
            return tdeInputs.getNullableColumns() != null && tdeInputs.getNullableColumns().contains(getName());
        }

        @Override
        public String getDefaultValue() {
            return tdeInputs.getColumnDefaultValues() != null ? tdeInputs.getColumnDefaultValues().get(getName()) : null;
        }

        @Override
        public String getInvalidValues() {
            return tdeInputs.getColumnInvalidValues() != null ? tdeInputs.getColumnInvalidValues().get(getName()) : null;
        }

        @Override
        public String getReindexing() {
            return tdeInputs.getColumnReindexing() != null ? tdeInputs.getColumnReindexing().get(getName()) : null;
        }

        @Override
        public Set<String> getPermissions() {
            return tdeInputs.getColumnPermissions() != null ? tdeInputs.getColumnPermissions().get(getName()) : null;
        }

        @Override
        public String getCollation() {
            return tdeInputs.getColumnCollations() != null ? tdeInputs.getColumnCollations().get(getName()) : null;
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
