/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Options for processing documents via the Nuclia RAG API.
 *
 * @since 2.1.0
 */
public interface NucliaOptions {

    /**
     * @param nuaKey Nuclia NUA key for authentication. See https://docs.rag.progress.cloud/docs/develop/python-sdk/nua/ for more information.
     * @return this instance for method chaining
     */
    NucliaOptions nuaKey(String nuaKey);

    /**
     * @param apiUrl Nuclia API URL. Defaults to 'https://aws-us-east-2-1.rag.progress.cloud/api/v1' if not specified.
     * @return this instance for method chaining
     */
    NucliaOptions apiUrl(String apiUrl);

    /**
     * @param timeout Maximum number of seconds to wait for Nuclia processing to complete
     * @return this instance for method chaining
     */
    NucliaOptions timeout(int timeout);
}
