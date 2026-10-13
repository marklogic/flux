/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ColumnNameStrategy;
import com.marklogic.flux.api.FluxException;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link ColumnNameStrategy#SIMPLE}. First, Western European Latin letters are folded to their closest
 * ASCII letter(s) - e.g. "é" becomes "e" and "ß" becomes "ss". Then, any leading or trailing run of characters that
 * is not an ASCII letter, digit, or underscore is removed, and each remaining such run is replaced with a single
 * underscore - e.g. "first. name" becomes "first_name". Underscores in the original name are always preserved, as
 * they may be meaningful - e.g. "_id" and "a__b" are left unchanged.
 */
class SimpleColumnNameSanitizer implements ColumnNameSanitizer {

    /**
     * Explicit mappings for Western European Latin letters that Unicode does not decompose into a base letter plus
     * a combining mark, so they must be folded to ASCII by hand. This is language-neutral folding - e.g. "ö"
     * becomes "o", not the German digraph "oe" - to keep behavior predictable across languages that use these
     * letters differently.
     */
    private static final Map<Integer, String> NON_DECOMPOSABLE_LATIN_LETTERS = Map.ofEntries(
        Map.entry((int) 'æ', "ae"), Map.entry((int) 'Æ', "AE"),
        Map.entry((int) 'œ', "oe"), Map.entry((int) 'Œ', "OE"),
        Map.entry((int) 'ß', "ss"), Map.entry((int) 'ẞ', "SS"),
        Map.entry((int) 'ø', "o"), Map.entry((int) 'Ø', "O"),
        Map.entry((int) 'ð', "d"), Map.entry((int) 'Ð', "D"),
        Map.entry((int) 'þ', "th"), Map.entry((int) 'Þ', "Th"),
        // Precomposed accented forms of the above; NFD would decompose these into a mapped letter plus a mark, but
        // only after the map lookup has already run, so they need their own entries.
        Map.entry((int) 'ǿ', "o"), Map.entry((int) 'Ǿ', "O"),
        Map.entry((int) 'ǽ', "ae"), Map.entry((int) 'Ǽ', "AE"),
        Map.entry((int) 'ǣ', "ae"), Map.entry((int) 'Ǣ', "AE")
    );

    @Override
    public String[] sanitize(String[] columnNames) {
        String[] newNames = new String[columnNames.length];
        Map<String, String> originalNameByNewName = new HashMap<>();

        for (int i = 0; i < columnNames.length; i++) {
            String originalName = columnNames[i];
            String newName = sanitizeColumnName(originalName);
            if (newName.isEmpty()) {
                throw new FluxException(String.format(
                    "Unable to apply the '%s' column name strategy to column '%s'; the resulting column name is empty.",
                    ColumnNameStrategy.SIMPLE, originalName));
            }
            String conflictingOriginalName = originalNameByNewName.putIfAbsent(newName, originalName);
            if (conflictingOriginalName != null && !conflictingOriginalName.equals(originalName)) {
                throw new FluxException(String.format(
                    "Unable to apply the '%s' column name strategy; columns '%s' and '%s' both sanitize to '%s'.",
                    ColumnNameStrategy.SIMPLE, conflictingOriginalName, originalName, newName));
            }
            newNames[i] = newName;
        }
        return newNames;
    }

    private String sanitizeColumnName(String name) {
        return foldWesternEuropeanLetters(name)
            .replaceAll("^[^A-Za-z0-9_]+|[^A-Za-z0-9_]+$", "")
            .replaceAll("[^A-Za-z0-9_]+", "_");
    }

    /**
     * Folds Western European Latin letters to their closest ASCII equivalent(s). Uses Unicode NFD normalization to
     * split precomposed accented letters - such as "é" - into a base letter plus one or more combining marks, which
     * are then stripped; this handles accented letters regardless of whether the source text used the precomposed
     * or already-decomposed form. Letters that do not decompose this way - such as "æ" and "ß" - are handled via
     * {@link #NON_DECOMPOSABLE_LATIN_LETTERS}. Characters with no ASCII equivalent, including non-Latin scripts,
     * are left as-is so that the subsequent underscore replacement applies to them.
     */
    private String foldWesternEuropeanLetters(String name) {
        StringBuilder result = new StringBuilder(name.length());
        name.codePoints().forEach(codePoint -> {
            String mapped = NON_DECOMPOSABLE_LATIN_LETTERS.get(codePoint);
            if (mapped != null) {
                result.append(mapped);
            } else {
                result.appendCodePoint(codePoint);
            }
        });

        String normalized = Normalizer.normalize(result, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }
}
