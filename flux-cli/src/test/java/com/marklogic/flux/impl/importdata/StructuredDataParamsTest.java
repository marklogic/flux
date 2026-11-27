/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ColumnNameStrategy;
import com.marklogic.flux.api.FluxException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies how column names containing periods, spaces, and other special characters are handled when transforming
 * a Spark Dataset. These tests only need a local Spark session, not a MarkLogic instance, as the handling of column
 * names is entirely a matter of how Flux refers to columns when calling Spark.
 */
class StructuredDataParamsTest {

    private static final String TRANSACTION = "Transaction Number";
    private static final String CUSTOMER = "Customer Name";
    private static final String ITEM = "Item Name";
    private static final String PERCENT = "Contribution %";
    private static final String AMOUNT = "Contribution Amount (Net Alt. Sales)";

    private static SparkSession sparkSession;

    @BeforeAll
    static void createSparkSession() {
        sparkSession = SparkSession.builder().master("local[1]").config("spark.ui.enabled", "false").getOrCreate();
    }

    @AfterAll
    static void closeSparkSession() {
        sparkSession.stop();
    }

    @Test
    void aggregateMultipleColumns() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, PERCENT, AMOUNT)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, CUSTOMER, "items"}, dataset.schema().names());
        assertArrayEquals(new String[]{ITEM, PERCENT, AMOUNT}, getAggregatedFieldNames(dataset, "items"),
            "Each field in the aggregation should keep the name of the column it was built from.");

        List<Row> rows = dataset.orderBy(ColumnReferences.quote(TRANSACTION)).collectAsList();
        assertEquals(2, rows.size());
        assertEquals(1001, rows.get(0).getInt(0));
        assertEquals("Jane", rows.get(0).getString(1));
        assertEquals(2, rows.get(0).getList(2).size());
        assertEquals(1002, rows.get(1).getInt(0));
        assertEquals(1, rows.get(1).getList(2).size());
    }

    @Test
    void columnWithPeriodIsNotAggregated() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, PERCENT)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, CUSTOMER, AMOUNT, "items"}, dataset.schema().names(),
            "Every column that is not aggregated is still referenced by Flux so that its first value is kept, " +
                "so a period in such a column name must not cause an error.");
        assertEquals(2, dataset.count());
    }

    @Test
    void aggregateSingleColumnWithPeriod() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("amounts", AMOUNT)
            .orderAggregation("amounts", AMOUNT, false)
            .applyTransformations(readFile());

        assertEquals(List.of("18.75", "12.5"), getAmounts(dataset));
    }

    @Test
    void orderSingleColumnAggregation() {
        Dataset<Row> ascending = groupBy(TRANSACTION)
            .aggregateColumns("amounts", AMOUNT)
            .orderAggregation("amounts", AMOUNT, true)
            .applyTransformations(readFile());
        assertEquals(List.of("12.5", "18.75"), getAmounts(ascending));

        Dataset<Row> descending = groupBy(TRANSACTION)
            .aggregateColumns("amounts", AMOUNT)
            .orderAggregation("amounts", AMOUNT, false)
            .applyTransformations(readFile());
        assertEquals(List.of("18.75", "12.5"), getAmounts(descending));
    }

    @Test
    void columnNamesInDifferentCase() {
        Dataset<Row> dataset = groupBy("transaction number")
            .aggregateColumns("items", "ITEM NAME", "contribution amount (net alt. sales)")
            .orderAggregation("items", "item name", false)
            .drop("CUSTOMER NAME")
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, PERCENT, "items"}, dataset.schema().names(),
            "Spark resolves column names regardless of case by default, so Flux must as well. Otherwise, the " +
                "'group by' column is not recognized and appears twice.");
        assertArrayEquals(new String[]{ITEM, AMOUNT}, getAggregatedFieldNames(dataset, "items"));
        assertEquals(List.of("Widget", "Gadget"), getItemNames(dataset, "items"));
    }

    @Test
    void columnNamesInDifferentCaseWithCaseSensitiveSession() {
        StructuredDataParams params = groupBy(TRANSACTION)
            .aggregateColumns("items", "ITEM NAME", AMOUNT);

        Dataset<Row> source = readFile();
        sparkSession.conf().set("spark.sql.caseSensitive", "true");
        try {
            FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
            assertTrue(ex.getMessage().startsWith("Unable to aggregate columns: [ITEM NAME, "),
                "Unexpected message: " + ex.getMessage());
        } finally {
            sparkSession.conf().set("spark.sql.caseSensitive", "false");
        }
    }

    @Test
    void groupByColumnWithPeriod() {
        Dataset<Row> dataset = groupBy(AMOUNT)
            .aggregateColumns("items", ITEM, PERCENT)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{AMOUNT, TRANSACTION, CUSTOMER, "items"}, dataset.schema().names());
        assertEquals(3, dataset.count(), "Each row has a distinct amount.");
    }

    @Test
    void orderAggregationByColumnWithSpace() {
        Dataset<Row> ascending = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, AMOUNT)
            .orderAggregation("items", ITEM, true)
            .applyTransformations(readFile());
        assertEquals(List.of("Gadget", "Widget"), getItemNames(ascending, "items"));

        Dataset<Row> descending = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, AMOUNT)
            .orderAggregation("items", ITEM, false)
            .applyTransformations(readFile());
        assertEquals(List.of("Widget", "Gadget"), getItemNames(descending, "items"));
    }

    @Test
    void orderAggregationByColumnWithPeriod() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, AMOUNT)
            .orderAggregation("items", AMOUNT, false)
            .applyTransformations(readFile());

        assertEquals(List.of("Gadget", "Widget"), getItemNames(dataset, "items"),
            "The gadget has the higher amount and thus should be first.");
    }

    @Test
    void aggregationNameWithSpecialCharacters() {
        final String aggregationName = "Line Items (All.)";
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns(aggregationName, ITEM, AMOUNT)
            .orderAggregation(aggregationName, ITEM, true)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, CUSTOMER, PERCENT, aggregationName}, dataset.schema().names());
        assertEquals(List.of("Gadget", "Widget"), getItemNames(dataset, aggregationName));
    }

    @Test
    void columnNamesQuotedByUser() {
        Dataset<Row> dataset = groupBy("`Transaction Number`")
            .aggregateColumns("items", "`Item Name`", "`Contribution Amount (Net Alt. Sales)`")
            .orderAggregation("items", "`Item Name`", false)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, CUSTOMER, PERCENT, "items"}, dataset.schema().names(),
            "A user may quote a column name with backticks as that is required in a 'where' expression. Flux " +
                "should recognize that as a reference to the column; it previously resulted in the 'group by' " +
                "column appearing twice.");
        assertArrayEquals(new String[]{ITEM, AMOUNT}, getAggregatedFieldNames(dataset, "items"));
        assertEquals(List.of("Widget", "Gadget"), getItemNames(dataset, "items"));
    }

    @Test
    void columnNameWithBacktick() {
        Dataset<Row> source = readFile().withColumnRenamed(ITEM, "Item`Name");

        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", "Item`Name", AMOUNT)
            .orderAggregation("items", "Item`Name", true)
            .applyTransformations(source);

        assertArrayEquals(new String[]{"Item`Name", AMOUNT}, getAggregatedFieldNames(dataset, "items"));
        assertEquals(List.of("Gadget", "Widget"), getItemNames(dataset, "items"));
    }

    @Test
    void whereAndDrop() {
        Dataset<Row> dataset = new StructuredDataParams()
            .where("`Item Name` = 'Widget' and `Contribution Amount (Net Alt. Sales)` = 12.5")
            .drop(AMOUNT, PERCENT)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, CUSTOMER, ITEM}, dataset.schema().names());
        assertEquals(1, dataset.count(), "A 'where' expression is written by the user, so the user must quote " +
            "any column name with special characters in it.");
    }

    @Test
    void dropAfterAggregating() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, PERCENT)
            .drop(AMOUNT, CUSTOMER)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{TRANSACTION, "items"}, dataset.schema().names());
    }

    @Test
    void fieldsInStructColumn() {
        StructType address = new StructType()
            .add("city", DataTypes.StringType)
            .add("zip", DataTypes.StringType);
        StructType schema = new StructType()
            .add("name", DataTypes.StringType)
            .add("address", address);
        Dataset<Row> source = sparkSession.createDataFrame(List.of(
            RowFactory.create("Jane", RowFactory.create("Boston", "02108")),
            RowFactory.create("Raj", RowFactory.create("Boston", "02110")),
            RowFactory.create("Ana", RowFactory.create("Austin", "73301"))
        ), schema);

        Dataset<Row> dataset = groupBy("address.city")
            .aggregateColumns("residents", "name", "address.zip")
            .applyTransformations(source);

        assertArrayEquals(new String[]{"city", "address", "residents"}, dataset.schema().names(),
            "A reference that is not the name of a top-level column must be left for Spark to parse, as it may " +
                "be a path to a field in a struct column, which is common in Avro, ORC, and Parquet files.");
        assertArrayEquals(new String[]{"name", "zip"}, getAggregatedFieldNames(dataset, "residents"));
        assertEquals(2, dataset.count());
    }

    @Test
    void unknownColumn() {
        StructuredDataParams params = groupBy(TRANSACTION)
            .aggregateColumns("items", "Not A Column");

        Dataset<Row> source = readFile();
        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
        assertTrue(ex.getMessage().startsWith("Unable to aggregate columns: [Not A Column]"),
            "Unexpected message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Cause: ") && ex.getMessage().contains("`Not A Column`"),
            "The message from Spark should be included as it identifies the column; message: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("\n"), "The query plan should not be included: " + ex.getMessage());
    }

    @Test
    void unknownGroupByColumn() {
        StructuredDataParams params = groupBy("Transaction Numberr")
            .aggregateColumns("items", ITEM, PERCENT);

        Dataset<Row> source = readFile();
        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
        assertTrue(ex.getMessage().startsWith("Unable to group by 'Transaction Numberr'; cause: "),
            "The error should identify the 'group by' column and not the aggregated columns, which are valid; " +
                "message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("`Transaction Number`"),
            "The message from Spark should be included as it suggests the correct name; message: " + ex.getMessage());
        assertFalse(ex.getMessage().contains("\n"), "The query plan should not be included: " + ex.getMessage());
    }

    @Test
    void orderByColumnWithColon() {
        StructuredDataParams.AggregationOrdering ordering = new StructuredDataParams.AggregationOrdering()
            .convert("items=Item:Name:desc");
        assertEquals("items", ordering.getAggregationName());
        assertEquals("Item:Name", ordering.getColumnName(), "Only a trailing ':asc' or ':desc' is a direction.");
        assertFalse(ordering.isAscending());

        ordering = new StructuredDataParams.AggregationOrdering().convert("items=Item:Name");
        assertEquals("Item:Name", ordering.getColumnName());
        assertTrue(ordering.isAscending());

        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", "Item:Name", AMOUNT)
            .orderAggregation("items", "Item:Name", false)
            .applyTransformations(readFile().withColumnRenamed(ITEM, "Item:Name"));
        assertEquals(List.of("Widget", "Gadget"), getItemNames(dataset, "items"));
    }

    @Test
    void fieldsInStructColumnWithSimpleStrategy() {
        StructType schema = new StructType()
            .add("name", DataTypes.StringType)
            .add("Home Address", new StructType().add("city", DataTypes.StringType));
        Dataset<Row> source = sparkSession.createDataFrame(List.of(
            RowFactory.create("Jane", RowFactory.create("Boston")),
            RowFactory.create("Raj", RowFactory.create("Boston")),
            RowFactory.create("Ana", RowFactory.create("Austin"))
        ), schema);

        for (String reference : List.of("Home_Address.city", "Home Address.city", "home address.city",
            "`Home Address`.city", "`home address`.city", "`Home_Address`.city")) {
            Dataset<Row> dataset = groupBy(reference)
                .columnNameStrategy(ColumnNameStrategy.SIMPLE)
                .aggregateColumns("residents", "name")
                .applyTransformations(source);

            assertArrayEquals(new String[]{"city", "Home_Address", "residents"}, dataset.schema().names(),
                "The struct column should be usable via its sanitized or original name; reference: " + reference);
            assertEquals(2, dataset.count());
        }
    }

    @Test
    void nullColumnNames() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, PERCENT)
            .drop(AMOUNT, null)
            .applyTransformations(readFile());
        assertArrayEquals(new String[]{TRANSACTION, CUSTOMER, "items"}, dataset.schema().names(),
            "A null column to drop is ignored by Spark, so it must not cause an error when Flux resolves it.");

        StructuredDataParams params = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, PERCENT)
            .orderAggregation("items", null, true);
        Dataset<Row> source = readFile();
        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
        assertTrue(ex.getMessage().contains("column 'null' is not in the aggregation"),
            "Unexpected message: " + ex.getMessage());
    }

    @Test
    void sanitizedNamesDifferingOnlyByCase() {
        StructuredDataParams params = groupBy(TRANSACTION)
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .aggregateColumns("items", PERCENT);

        Dataset<Row> source = readFile().withColumnRenamed(CUSTOMER, "item_name");
        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
        assertTrue(ex.getMessage().toLowerCase().contains("`item_name` is ambiguous"), "'Item Name' and 'item_name' are sanitized to names " +
            "that Spark cannot tell apart, as it ignores case by default. The error should identify the " +
            "ambiguous name; message: " + ex.getMessage());
    }

    @Test
    void orderByColumnNotInAggregation() {
        StructuredDataParams params = groupBy(TRANSACTION)
            .aggregateColumns("items", ITEM, PERCENT)
            .orderAggregation("items", AMOUNT, true);

        Dataset<Row> source = readFile();
        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
        assertTrue(ex.getMessage().contains("column '" + AMOUNT + "' is not in the aggregation"),
            "Unexpected message: " + ex.getMessage());
    }

    @Test
    void sanitizedNamesInEveryOption() {
        Dataset<Row> dataset = groupBy("Transaction_Number")
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .where("Customer_Name = 'Jane'")
            .aggregateColumns("items", "Item_Name", "Contribution_Amount_Net_Alt_Sales")
            .orderAggregation("items", "Item_Name", false)
            .drop("Contribution")
            .applyTransformations(readFile());

        verifySanitizedAggregation(dataset);
        assertEquals(1, dataset.count(), "Only the transaction for Jane should be included.");
    }

    @Test
    void originalNamesWithSimpleStrategy() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .aggregateColumns("items", ITEM, AMOUNT)
            .orderAggregation("items", ITEM, false)
            .drop(PERCENT)
            .applyTransformations(readFile());

        verifySanitizedAggregation(dataset);
        assertEquals(2, dataset.count());
    }

    @Test
    void simpleStrategyWithoutGrouping() {
        Dataset<Row> dataset = new StructuredDataParams()
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .where("Item_Name = 'Widget'")
            .drop(PERCENT, "Customer_Name")
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{"Transaction_Number", "Item_Name", "Contribution_Amount_Net_Alt_Sales"},
            dataset.schema().names(), "A column can be dropped via either its original name or its sanitized name.");
        assertEquals(2, dataset.count());
    }

    @Test
    void aggregationNameIsNotSanitized() {
        Dataset<Row> dataset = groupBy(TRANSACTION)
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .aggregateColumns("line items", ITEM, AMOUNT)
            .applyTransformations(readFile());

        assertArrayEquals(new String[]{"Transaction_Number", "Customer_Name", "Contribution", "line items"},
            dataset.schema().names(), "The user chooses the name of an aggregation, so it is used as-is.");
    }

    @Test
    void collidingNamesCannotBeAvoidedByDroppingColumn() {
        Dataset<Row> source = readFile().withColumnRenamed(CUSTOMER, "Contribution");
        StructuredDataParams params = new StructuredDataParams()
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .drop(PERCENT);

        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(source));
        assertEquals("Unable to apply the 'SIMPLE' column name strategy; columns 'Contribution' and " +
                "'Contribution %' both sanitize to 'Contribution'.", ex.getMessage(),
            "Column names are sanitized before any column is dropped.");
    }

    private void verifySanitizedAggregation(Dataset<Row> dataset) {
        assertArrayEquals(new String[]{"Transaction_Number", "Customer_Name", "items"}, dataset.schema().names());
        assertArrayEquals(new String[]{"Item_Name", "Contribution_Amount_Net_Alt_Sales"},
            getAggregatedFieldNames(dataset, "items"),
            "The fields in an aggregation are named after the sanitized columns they are built from.");

        Row row = dataset.where("Transaction_Number = 1001").collectAsList().get(0);
        List<Row> items = row.getList(row.fieldIndex("items"));
        assertEquals(List.of("Widget", "Gadget"), items.stream().map(item -> item.getString(0)).toList());
    }

    private StructuredDataParams groupBy(String columnName) {
        StructuredDataParams params = new StructuredDataParams();
        params.setGroupBy(columnName);
        return params;
    }

    /**
     * Reads the file with the same options that import-delimited-files defaults to.
     */
    private Dataset<Row> readFile() {
        return sparkSession.read()
            .option("header", "true")
            .option("inferSchema", "true")
            .csv("src/test/resources/delimited-files/special-column-names.csv");
    }

    /**
     * @return the amounts, as strings, in the "amounts" aggregation for transaction 1001, which is the only one with
     * multiple items.
     */
    private List<String> getAmounts(Dataset<Row> dataset) {
        Row row = dataset.where(ColumnReferences.quote(TRANSACTION) + " = 1001").collectAsList().get(0);
        return row.getList(row.fieldIndex("amounts")).stream().map(String::valueOf).toList();
    }

    private String[] getAggregatedFieldNames(Dataset<Row> dataset, String aggregationName) {
        ArrayType arrayType = (ArrayType) dataset.schema().apply(aggregationName).dataType();
        return ((StructType) arrayType.elementType()).names();
    }

    /**
     * @return the item names in the given aggregation for transaction 1001, which is the only one with multiple items.
     */
    private List<String> getItemNames(Dataset<Row> dataset, String aggregationName) {
        Row row = dataset.where(ColumnReferences.quote(TRANSACTION) + " = 1001").collectAsList().get(0);
        List<Row> items = row.getList(row.fieldIndex(aggregationName));
        return items.stream().map(item -> item.getString(0)).toList();
    }
}
