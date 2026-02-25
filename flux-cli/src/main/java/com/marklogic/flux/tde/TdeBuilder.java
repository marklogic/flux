/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import java.util.Iterator;
import java.util.Set;

public interface TdeBuilder {

    TdeTemplate buildTde(TdeInputs inputs, Iterator<Column> columns);

    interface Column {
        String getName();

        String getVal();

        String getScalarType();

        default boolean isNullable() { return true; }

        default boolean isVirtual() { return false; }

        default String getDefaultValue() { return null; }

        default String getInvalidValues() { return null; }

        default String getReindexing() { return null; }

        /**
         * @return a set of role names that define read permissions for the column. A MarkLogic TDE uses the term
         * "permissions" even though it's really a set of role names (while "permission" normally refers to a role
         * name joined with a capability in MarkLogic).
         */
        default Set<String> getPermissions() { return null; }

        default String getCollation() { return null; }

        default Integer getDimension() { return null; }

        default Float getAnnCompression() { return null; }

        default String getAnnDistance() { return null; }

        default Boolean isAnnIndexed() { return null; }
    }
}
