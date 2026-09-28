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
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises {@link StructuredDataParams#applyTransformations(Dataset)} directly against a local, MarkLogic-free
 * Spark session so that the column name sanitization algorithm, its failure modes, and its ordering relative to
 * the other structured data options can be verified without requiring a live MarkLogic or JDBC data source.
 */
class StructuredDataParamsTest {

    private static SparkSession session;

    @BeforeAll
    static void createSparkSession() {
        session = SparkSession.builder().master("local[*]").appName("StructuredDataParamsTest").getOrCreate();
    }

    @AfterAll
    static void stopSparkSession() {
        if (session != null) {
            session.stop();
        }
    }

    private Dataset<Row> newDataset(String... columnNames) {
        StructField[] fields = Arrays.stream(columnNames)
            .map(name -> DataTypes.createStructField(name, DataTypes.StringType, true))
            .toArray(StructField[]::new);
        StructType schema = DataTypes.createStructType(fields);
        Row row = RowFactory.create(Arrays.stream(columnNames).map(name -> "value-of-" + name).toArray());
        return session.createDataFrame(List.of(row), schema);
    }

    @Test
    void defaultStrategyPreservesColumnNames() {
        StructuredDataParams params = new StructuredDataParams();
        Dataset<Row> dataset = newDataset("first. name", "last-name");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"first. name", "last-name"}, result.schema().names(),
            "The default 'none' strategy must leave column names untouched.");
    }

    @Test
    void simpleStrategyReplacesNonAlphanumericRunsWithUnderscore() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("first. name");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"first_name"}, result.schema().names(),
            "A single run of non-alphanumeric characters, including a pre-existing space, should collapse to one underscore.");
    }

    @Test
    void simpleStrategyCollapsesConsecutivePunctuationAndUnderscores() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("a__b", "c...d", "e - f");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"a_b", "c_d", "e_f"}, result.schema().names());
    }

    @Test
    void simpleStrategyTrimsLeadingAndTrailingUnderscores() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("_id_", "  spaced  ");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"id", "spaced"}, result.schema().names());
    }

    @Test
    void simpleStrategyLeavesAlreadyValidNamesUnchanged() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("validName1", "valid_name_2");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"validName1", "valid_name_2"}, result.schema().names());
    }

    @Test
    void simpleStrategyFoldsPrecomposedAccentedLetters() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("prénom", "Müller", "niño");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"prenom", "Muller", "nino"}, result.schema().names(),
            "Accented Western European letters should be folded to their base ASCII letter instead of being " +
                "replaced with an underscore.");
    }

    @Test
    void simpleStrategyFoldsDecomposedAccentedLetters() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        // "e" followed by the combining acute accent U+0301, i.e. the NFD form of "é".
        String decomposedEAcute = "pr\u0065\u0301nom";
        Dataset<Row> dataset = newDataset(decomposedEAcute);

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"prenom"}, result.schema().names(),
            "Accented letters already expressed as a base letter plus a combining mark should fold the same way " +
                "as their precomposed equivalent.");
    }

    @Test
    void simpleStrategyFoldsNonDecomposableLatinLetters() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("straße", "Æther", "cœur", "Øst", "Ðord", "Þor");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"strasse", "AEther", "coeur", "Ost", "Dord", "Thor"}, result.schema().names(),
            "Letters without a single-letter ASCII equivalent should be folded via explicit mappings, e.g. 'ß' " +
                "becomes 'ss' and 'æ' becomes 'ae', rather than a German-specific digraph convention.");
    }

    @Test
    void simpleStrategyFoldsAccentedNonDecomposableLatinLettersInBothForms() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset(
            "\u01FFre", "\u00F8\u0301re2",
            "\u01FD", "\u00E6\u0301x",
            "\u01E3y", "\u00E6\u0304z",
            "\u01FE", "\u01FCb", "\u01E2c"
        );

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"ore", "ore2", "ae", "aex", "aey", "aez", "O", "AEb", "AEc"},
            result.schema().names(),
            "Accented forms of 'ø' and 'æ' should fold identically whether precomposed or decomposed.");
    }

    @Test
    void simpleStrategyReplacesUnmappableNonLatinCharactersWithUnderscore() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("na\u4e2d\u6587me", "name2");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"na_me", "name2"}, result.schema().names(),
            "Characters with no ASCII equivalent, such as non-Latin scripts, should still be replaced with an " +
                "underscore rather than left as-is or dropped.");
    }

    @Test
    void collidingSanitizedNamesThrowFluxException() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("first.name", "first-name");

        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(dataset));
        assertTrue(ex.getMessage().contains("first.name"), ex.getMessage());
        assertTrue(ex.getMessage().contains("first-name"), ex.getMessage());
        assertTrue(ex.getMessage().contains("first_name"), ex.getMessage());
    }

    @Test
    void emptySanitizedNameThrowsFluxException() {
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        Dataset<Row> dataset = newDataset("...", "id");

        FluxException ex = assertThrows(FluxException.class, () -> params.applyTransformations(dataset));
        assertTrue(ex.getMessage().contains("..."), ex.getMessage());
        assertTrue(ex.getMessage().contains("empty"), ex.getMessage());
    }

    @Test
    void dropAppliesBeforeSanitizationAndReferencesOriginalNames() {
        StructuredDataParams params = new StructuredDataParams()
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .drop("first. name");
        Dataset<Row> dataset = newDataset("first. name", "last-name");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"last_name"}, result.schema().names(),
            "--drop must reference the original column name, and the dropped column must not participate in " +
                "sanitization or collision checks.");
    }

    @Test
    void whereAppliesBeforeSanitizationAndReferencesOriginalNames() {
        StructuredDataParams params = new StructuredDataParams()
            .columnNameStrategy(ColumnNameStrategy.SIMPLE)
            .where("`first. name` = 'value-of-first. name'");
        Dataset<Row> dataset = newDataset("first. name", "last-name");

        Dataset<Row> result = params.applyTransformations(dataset);

        assertEquals(1, result.count());
        assertArrayEquals(new String[]{"first_name", "last_name"}, result.schema().names());
    }

    @Test
    void groupByAndAggregateApplyBeforeSanitizationAndReferenceOriginalNames() {
        // Note that column names with a "." cannot be used here, as Spark's groupBy(String) resolves the string as
        // a SQL expression, where a "." denotes a nested field reference rather than a literal character in the name.
        StructuredDataParams params = new StructuredDataParams().columnNameStrategy(ColumnNameStrategy.SIMPLE);
        params.setGroupBy("group id");
        params.aggregateColumns("child items", "child name");

        StructType schema = DataTypes.createStructType(new StructField[]{
            DataTypes.createStructField("group id", DataTypes.StringType, true),
            DataTypes.createStructField("child name", DataTypes.StringType, true)
        });
        Dataset<Row> dataset = session.createDataFrame(List.of(
            RowFactory.create("g1", "child-a"),
            RowFactory.create("g1", "child-b")
        ), schema);

        Dataset<Row> result = params.applyTransformations(dataset);

        assertArrayEquals(new String[]{"group_id", "child_items"}, result.schema().names(),
            "--group-by and --aggregate must reference original names, and their resulting top-level column " +
                "names are still subject to sanitization.");
    }
}
