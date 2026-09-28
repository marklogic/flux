/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ColumnNameStrategy;
import com.marklogic.flux.api.FluxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleColumnNameSanitizerTest {

    private final ColumnNameSanitizer sanitizer = new SimpleColumnNameSanitizer();

    private void verify(String[] expected, String... columnNames) {
        assertArrayEquals(expected, sanitizer.sanitize(columnNames));
    }

    @Test
    void forStrategy() {
        assertInstanceOf(SimpleColumnNameSanitizer.class, ColumnNameSanitizer.forStrategy(ColumnNameStrategy.SIMPLE));
        assertNull(ColumnNameSanitizer.forStrategy(ColumnNameStrategy.NONE));
        assertNull(ColumnNameSanitizer.forStrategy(null));
    }

    @Test
    void replacesNonAlphanumericRunsWithUnderscore() {
        verify(new String[]{"first_name", "c_d", "e_f"}, "first. name", "c...d", "e - f");
    }

    @Test
    void preservesOriginalUnderscores() {
        verify(new String[]{"_id_", "a__b", "___", "x__y", "_z"}, "_id_", "a__b", "___", "x_.y", "._z");
    }

    @Test
    void removesLeadingAndTrailingUnsupportedCharacters() {
        verify(new String[]{"spaced", "name"}, "  spaced  ", "(name)");
    }

    @Test
    void leavesAlreadyValidNamesUnchanged() {
        verify(new String[]{"validName1", "valid_name_2"}, "validName1", "valid_name_2");
    }

    @Test
    void foldsPrecomposedAccentedLetters() {
        verify(new String[]{"prenom", "Muller", "nino"}, "prénom", "Müller", "niño");
    }

    @Test
    void foldsDecomposedAccentedLetters() {
        // "e" followed by the combining acute accent U+0301, i.e. the NFD form of "é".
        verify(new String[]{"prenom"}, "pr\u0065\u0301nom");
    }

    @Test
    void foldsNonDecomposableLatinLetters() {
        verify(new String[]{"strasse", "AEther", "coeur", "Ost", "Dord", "Thor"},
            "straße", "Æther", "cœur", "Øst", "Ðord", "Þor");
    }

    @Test
    void foldsAccentedNonDecomposableLatinLettersInBothForms() {
        verify(new String[]{"ore", "ore2", "ae", "aex", "aey", "aez", "O", "AEb", "AEc"},
            "\u01FFre", "\u00F8\u0301re2",
            "\u01FD", "\u00E6\u0301x",
            "\u01E3y", "\u00E6\u0304z",
            "\u01FE", "\u01FCb", "\u01E2c");
    }

    @Test
    void foldsLettersWhilePreservingUnderscores() {
        verify(new String[]{"_prenom_", "strasse__nr", "AE_o", "_Muller_name"},
            "_prénom_", "straße__nr", "Æ_ø", "_Müller.name");
    }

    @Test
    void replacesUnmappableNonLatinCharactersWithUnderscore() {
        verify(new String[]{"na_me", "name2"}, "na\u4e2d\u6587me", "name2");
    }

    @Test
    void collidingSanitizedNames() {
        FluxException ex = assertThrows(FluxException.class, () -> sanitizer.sanitize(new String[]{"first.name", "first-name"}));
        assertEquals("Unable to apply the 'SIMPLE' column name strategy; columns 'first.name' and 'first-name' " +
            "both sanitize to 'first_name'.", ex.getMessage());
    }

    @Test
    void emptySanitizedName() {
        FluxException ex = assertThrows(FluxException.class, () -> sanitizer.sanitize(new String[]{"...", "id"}));
        assertEquals("Unable to apply the 'SIMPLE' column name strategy to column '...'; the resulting column " +
            "name is empty.", ex.getMessage());
    }
}
