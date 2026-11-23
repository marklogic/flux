/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    }

    @Test
    void pathToFieldInStructColumn() {
        ColumnReferences sanitized = new ColumnReferences(
            new String[]{"id", "Home_Address", "a_b"},
            new String[]{"id", "Home Address", "a.b"},
            false
        );

        assertEquals("`Home_Address`.city", sanitized.toExpression("Home_Address.city"));
        assertEquals("`Home_Address`.city", sanitized.toExpression("Home Address.city"),
            "The struct column can be referred to by its original name.");
        assertEquals("`Home_Address`.city.name", sanitized.toExpression("home address.city.name"));
        assertEquals("`a_b`.c", sanitized.toExpression("a.b.c"),
            "The original name of the column has a period in it, so the longest matching name is the column.");
        assertEquals("Home Address.city", sanitized.resolve("Home Address.city"),
            "A path does not identify a top-level column.");
        assertEquals("other.city", sanitized.toExpression("other.city"));
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
    void pathWithQuotedStructColumn() {
        ColumnReferences sanitized = new ColumnReferences(
            new String[]{"id", "Home_Address", "a_b"},
            new String[]{"id", "Home Address", "a.b"},
            false
        );

        assertEquals("`Home_Address`.city", sanitized.toExpression("`Home Address`.city"),
            "A quoted struct column can be referred to by its original name.");
        assertEquals("`Home_Address`.city", sanitized.toExpression("`home address`.city"));
        assertEquals("`Home_Address`.city", sanitized.toExpression("`Home_Address`.city"));
        assertEquals("`Home_Address`.`Zip Code`.x", sanitized.toExpression("`Home Address`.`Zip Code`.x"),
            "The rest of the path keeps the quoting that the user gave it.");
        assertEquals("`a_b`.c", sanitized.toExpression("`a.b`.c"));
        assertEquals("`unknown`.city", sanitized.toExpression("`unknown`.city"));
        assertEquals("`Home Address.city", sanitized.toExpression("`Home Address.city"),
            "Not a valid reference, so it is left as-is for Spark to report.");
        assertEquals("`Home Address`city", sanitized.toExpression("`Home Address`city"));

        ColumnReferences withBacktick = new ColumnReferences(new String[]{"tick`ed"});
        assertEquals("`tick``ed`.city", withBacktick.toExpression("`tick``ed`.city"),
            "A doubled backtick is part of the name and does not end it.");
    }

    @Test
    void nullReference() {
        assertNull(references.resolve(null));
        assertNull(references.toExpression(null));
    }

    @Test
    void notATopLevelColumn() {
        assertEquals("id.nested", references.resolve("id.nested"));
        assertEquals("`id`.nested", references.toExpression("id.nested"),
            "Only the column is quoted; the rest is left for Spark to parse as a path to a nested field.");
        assertEquals("unknown.nested", references.toExpression("unknown.nested"));
        assertEquals("`unknown`", references.toExpression("`unknown`"));
        assertEquals("`", references.toExpression("`"));
        assertEquals("``", references.toExpression("``"));
    }
}
