/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;
import com.ibm.icu.util.VersionInfo;

final class TdeColumnNameSanitizer {

    private static final VersionInfo UNICODE_8 = VersionInfo.getInstance(8, 0);

    private TdeColumnNameSanitizer() {
    }

    static String sanitize(String name) {
        if (isQuotedName(name)) {
            return name;
        }

        StringBuilder result = new StringBuilder(name.length());
        for (int offset = 0; offset < name.length();) {
            int codePoint = name.codePointAt(offset);
            boolean first = offset == 0;
            int category = unicode8Category(codePoint);
            if (first && (category == UCharacterCategory.DECIMAL_DIGIT_NUMBER || isSpecial(codePoint))) {
                result.append('_');
            }
            if (isLetter(category) || codePoint == '_' || (!first && (isNumber(category) || isSpecial(codePoint)))
                || (first && (category == UCharacterCategory.DECIMAL_DIGIT_NUMBER || isSpecial(codePoint)))) {
                result.appendCodePoint(codePoint);
            } else {
                result.append('_');
            }
            offset += Character.charCount(codePoint);
        }
        return result.toString();
    }

    private static boolean isQuotedName(String name) {
        return name.length() >= 3 && name.charAt(0) == '"' && name.charAt(name.length() - 1) == '"'
            && name.indexOf('"', 1) == name.length() - 1;
    }

    private static int unicode8Category(int codePoint) {
        if (codePoint > Character.MAX_VALUE) {
            return UCharacterCategory.UNASSIGNED;
        }
        VersionInfo age = UCharacter.getAge(codePoint);
        if (age.getMajor() == 0 || age.compareTo(UNICODE_8) > 0) {
            return UCharacterCategory.UNASSIGNED;
        }
        // These BMP characters changed between letter and mark after Unicode 8.
        if (codePoint == 0x1885 || codePoint == 0x1886) {
            return UCharacterCategory.OTHER_LETTER;
        }
        if (codePoint == 0x1CF2 || codePoint == 0x1CF3) {
            return UCharacterCategory.COMBINING_SPACING_MARK;
        }
        return UCharacter.getType(codePoint);
    }

    private static boolean isLetter(int category) {
        return category >= UCharacterCategory.UPPERCASE_LETTER && category <= UCharacterCategory.OTHER_LETTER;
    }

    private static boolean isNumber(int category) {
        return category >= UCharacterCategory.DECIMAL_DIGIT_NUMBER && category <= UCharacterCategory.OTHER_NUMBER;
    }

    private static boolean isSpecial(int codePoint) {
        return codePoint == '$' || codePoint == '#' || codePoint == '@';
    }
}
