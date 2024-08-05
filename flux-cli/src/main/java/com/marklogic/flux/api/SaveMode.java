/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
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
