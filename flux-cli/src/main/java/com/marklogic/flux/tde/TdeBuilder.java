/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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

        boolean isNullable();

        String getDefaultValue();

        String getInvalidValues();

        String getReindexing();

        /**
         * @return a set of role names that define read permissions for the column. A MarkLogic TDE uses the term
         * "permissions" even though it's really a set of role names (while "permission" normally refers to a role
         * name joined with a capability in MarkLogic).
         */
        Set<String> getPermissions();

        String getCollation();
    }
}
