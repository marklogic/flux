/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.impl.CloudStorageParams;

import java.util.List;
import java.util.Map;

/**
 * Temporary interface name to avoid a bunch of compiler warnings.
 */
public interface IReadFilesParams extends CloudStorageParams {

    List<String> getPaths();

    Map<String, String> makeOptions();
}
