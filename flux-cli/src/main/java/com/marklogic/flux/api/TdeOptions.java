/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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

    TdeOptions viewVirtual();

    TdeOptions viewLayout(String viewLayout);

    TdeOptions columnVals(Map<String, String> columnVals);

    TdeOptions columnTypes(Map<String, String> columnTypes);

    TdeOptions columnDefaultValues(Map<String, String> columnDefaultValues);

    TdeOptions columnInvalidValues(Map<String, String> columnInvalidValues);

    TdeOptions columnReindexing(Map<String, String> columnReindexing);

    TdeOptions columnPermissions(Map<String, String> columnPermissions);

    TdeOptions nullableColumns(List<String> nullableColumns);

    TdeOptions columnCollations(Map<String, String> columnCollations);

    TdeOptions virtualColumns(List<String> virtualColumns);

    TdeOptions columnDimensions(Map<String, Integer> columnDimensions);

    TdeOptions columnAnnCompressions(Map<String, Float> columnAnnCompressions);

    TdeOptions columnAnnDistances(Map<String, String> columnAnnDistances);

    TdeOptions columnAnnIndexed(Map<String, Boolean> columnAnnIndexed);
}
