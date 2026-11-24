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
    private final boolean sanitizeColumnNames;

    public SparkColumnIterator(StructType schema, TdeInputs tdeInputs) {
        this(schema, tdeInputs, false);
    }

    public SparkColumnIterator(StructType schema, TdeInputs tdeInputs, boolean sanitizeColumnNames) {
        this.fields = Arrays.stream(schema.fields()).iterator();
        this.tdeInputs = tdeInputs;
        this.sanitizeColumnNames = sanitizeColumnNames;
    }

    @Override
    public boolean hasNext() {
        return fields.hasNext();
    }

    @Override
    public TdeBuilder.Column next() {
        return new SparkColumn(fields.next(), tdeInputs, sanitizeColumnNames);
    }

    private static class SparkColumn implements TdeBuilder.Column {

        private final StructField field;
        private final TdeInputs tdeInputs;
        private final boolean sanitizeColumnNames;

        public SparkColumn(StructField field, TdeInputs tdeInputs, boolean sanitizeColumnNames) {
            this.field = field;
            this.tdeInputs = tdeInputs;
            this.sanitizeColumnNames = sanitizeColumnNames;
        }

        @Override
        public String getName() {
            return sanitizeColumnNames ? TdeColumnNameSanitizer.sanitize(field.name()) : field.name();
        }

        @Override
        public String getVal() {
            String name = field.name();
            return tdeInputs.getColumnVals() != null && tdeInputs.getColumnVals().containsKey(name) ?
                tdeInputs.getColumnVals().get(name) : name;
        }

        @Override
        public String getScalarType() {
            String name = field.name();
            if (tdeInputs.getColumnTypes() != null && tdeInputs.getColumnTypes().containsKey(name)) {
                return tdeInputs.getColumnTypes().get(name);
            }
            return convertSparkTypeToTdeType(this.field.dataType());
        }

        @Override
        public boolean isNullable() {
            return tdeInputs.getNullableColumns() != null && tdeInputs.getNullableColumns().contains(field.name());
        }

        @Override
        public String getDefaultValue() {
            return tdeInputs.getColumnDefaultValues() != null ? tdeInputs.getColumnDefaultValues().get(field.name()) : null;
        }

        @Override
        public String getInvalidValues() {
            return tdeInputs.getColumnInvalidValues() != null ? tdeInputs.getColumnInvalidValues().get(field.name()) : null;
        }

        @Override
        public String getReindexing() {
            return tdeInputs.getColumnReindexing() != null ? tdeInputs.getColumnReindexing().get(field.name()) : null;
        }

        @Override
        public Set<String> getPermissions() {
            return tdeInputs.getColumnPermissions() != null ? tdeInputs.getColumnPermissions().get(field.name()) : null;
        }

        @Override
        public String getCollation() {
            return tdeInputs.getColumnCollations() != null ? tdeInputs.getColumnCollations().get(field.name()) : null;
        }

        @Override
        public boolean isVirtual() {
            return tdeInputs.getVirtualColumns() != null && tdeInputs.getVirtualColumns().contains(field.name());
        }

        @Override
        public Integer getDimension() {
            return tdeInputs.getColumnDimensions() != null ? tdeInputs.getColumnDimensions().get(field.name()) : null;
        }

        @Override
        public Float getAnnCompression() {
            return tdeInputs.getColumnAnnCompressions() != null ? tdeInputs.getColumnAnnCompressions().get(field.name()) : null;
        }

        @Override
        public String getAnnDistance() {
            return tdeInputs.getColumnAnnDistances() != null ? tdeInputs.getColumnAnnDistances().get(field.name()) : null;
        }

        @Override
        public Boolean isAnnIndexed() {
            return tdeInputs.getColumnAnnIndexed() != null ? tdeInputs.getColumnAnnIndexed().get(field.name()) : null;
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
