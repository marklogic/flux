/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Defines the document types that can be used to force a type when importing files with URI extensions unrecognized by
 * MarkLogic. "binary" is not provided as an option because if MarkLogic recognizes the URI extension, it will ignore
 * the "format" parameter. And if MarkLogic does not recognize the URI extension, it will treat the document as binary
 * by default.
 *
 * @since 1.4.0
 */
public enum DocumentType {
    JSON, TEXT, XML
}
