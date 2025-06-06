/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;

public interface TdeTemplate {

    String getUri();

    AbstractWriteHandle getWriteHandle();

    /**
     * @return a comma-delimited string of permissions in the format of role1,capability1,role2,capability2,...
     */
    String getPermissions();

    String toPrettyString();
}
