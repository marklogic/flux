/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;

public interface TdeTemplate {

    AbstractWriteHandle toWriteHandle();

    String toPrettyString();
}
