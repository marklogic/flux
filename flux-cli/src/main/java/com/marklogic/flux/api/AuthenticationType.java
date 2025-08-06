/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public enum AuthenticationType {
    BASIC,
    DIGEST,
    CLOUD,
    KERBEROS,
    CERTIFICATE,
    /**
     * @since 1.2.0
     */
    OAUTH,
    SAML
}
