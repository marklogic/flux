/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Defines how data should be written to an external source if data already exists at the given location.
 */
public enum SaveMode {

    APPEND,
    OVERWRITE,
    ERRORIFEXISTS,
    IGNORE;
}
