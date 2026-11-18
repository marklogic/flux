/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnReferencesTest {

    private final ColumnReferences references = new ColumnReferences(new String[]{"id", "Item Name", "a.b", "tick`ed"});

    @Test
    void quote() {
        assertEquals("`id`", ColumnReferences.quote("id"));
        assertEquals("`Item Name`", ColumnReferences.quote("Item Name"));
        assertEquals("`tick``ed`", ColumnReferences.quote("tick`ed"), "A backtick is escaped by doubling it.");
    }

    @Test
    void topLevelColumn() {
        assertEquals("Item Name", references.resolve("Item Name"));
        assertEquals("`Item Name`", references.toExpression("Item Name"));
        assertEquals("`a.b`", references.toExpression("a.b"),
            "Must be quoted so that Spark does not parse this as field 'b' in column 'a'.");
        assertEquals("`tick``ed`", references.toExpression("tick`ed"));
    }

    @Test
    void columnQuotedByUser() {
        assertEquals("Item Name", references.resolve("`Item Name`"));
        assertEquals("`Item Name`", references.toExpression("`Item Name`"));
        assertEquals("tick`ed", references.resolve("`tick``ed`"));
    }

    @Test
    void originalNameOfSanitizedColumn() {
        ColumnReferences sanitized = new ColumnReferences(
            new String[]{"id", "Item_Name", "a_b"},
            new String[]{"id", "Item Name", "a.b"},
            false
        );

        assertEquals("Item_Name", sanitized.resolve("Item_Name"));
        assertEquals("Item_Name", sanitized.resolve("Item Name"));
        assertEquals("Item_Name", sanitized.resolve("`Item Name`"));
        assertEquals("`Item_Name`", sanitized.toExpression("Item Name"));
        assertEquals("`a_b`", sanitized.toExpression("a.b"));
        assertEquals("id.nested", sanitized.toExpression("id.nested"));
    }

    @Test
    void differentCase() {
        assertEquals("Item Name", references.resolve("item name"));
        assertEquals("Item Name", references.resolve("`ITEM NAME`"));
        assertEquals("`Item Name`", references.toExpression("ITEM name"),
            "The expression should use the actual name of the column.");
        assertEquals("`a.b`", references.toExpression("A.B"));
    }

    @Test
    void differentCaseWhenCaseSensitive() {
        ColumnReferences caseSensitive = new ColumnReferences(
            new String[]{"id", "Item_Name"},
            new String[]{"id", "Item Name"},
            true
        );

        assertEquals("`Item_Name`", caseSensitive.toExpression("Item_Name"));
        assertEquals("`Item_Name`", caseSensitive.toExpression("Item Name"));
        assertEquals("item_name", caseSensitive.toExpression("item_name"),
            "Not a match when Spark is case-sensitive, so it is left as-is for Spark to report as unknown.");
        assertEquals("item name", caseSensitive.resolve("item name"));
    }

    @Test
    void columnNamesDifferingOnlyByCase() {
        ColumnReferences ambiguous = new ColumnReferences(new String[]{"ID", "id"});
        assertEquals("ID", ambiguous.resolve("ID"), "An exact match always wins.");
        assertEquals("id", ambiguous.resolve("id"));
    }

    @Test
    void notATopLevelColumn() {
        assertEquals("id.nested", references.resolve("id.nested"));
        assertEquals("id.nested", references.toExpression("id.nested"),
            "Left as-is so that Spark can parse it as a path to a nested field.");
        assertEquals("`unknown`", references.toExpression("`unknown`"));
        assertEquals("`", references.toExpression("`"));
        assertEquals("``", references.toExpression("``"));
    }
}
