/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.List;
import java.util.Map;

/**
 * @since 1.4.0
 */
public interface TdeOptions {

    TdeOptions schemaName(String schemaName);

    TdeOptions viewName(String viewName);

    TdeOptions documentType(String documentType);

    TdeOptions preview();

    TdeOptions permissions(String permissions);

    TdeOptions collections(String collections);

    TdeOptions directories(List<String> directories);

    TdeOptions context(String context);

    TdeOptions uri(String uri);

    TdeOptions templateDisabled();

    TdeOptions viewLayout(String viewLayout);

    TdeOptions columnVals(Map<String, String> columnVals);

    TdeOptions columnTypes(Map<String, String> columnTypes);

    TdeOptions columnDefaultValues(Map<String, String> columnDefaultValues);

    TdeOptions columnInvalidValues(Map<String, String> columnInvalidValues);

    TdeOptions columnReindexing(Map<String, String> columnReindexing);

    TdeOptions columnPermissions(Map<String, String> columnPermissions);

    TdeOptions nullableColumns(List<String> nullableColumns);

    TdeOptions columnCollations(Map<String, String> columnCollations);
}
