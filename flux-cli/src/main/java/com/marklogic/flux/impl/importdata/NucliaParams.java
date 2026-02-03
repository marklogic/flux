/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.NucliaOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

/**
 * Captures all Nuclia-related options for document processing via the Nuclia RAG API.
 */
public class NucliaParams implements NucliaOptions {

    @CommandLine.Option(
        names = "--nuclia-nua-key",
        description = "Nuclia NUA key for authentication. Required if any Nuclia options are used. See https://docs.rag.progress.cloud/docs/develop/python-sdk/nua/ for more information."
    )
    private String nuaKey;

    @CommandLine.Option(
        names = "--nuclia-api-url",
        description = "Nuclia API URL. Defaults to 'https://aws-us-east-2-1.rag.progress.cloud/api/v1' if not specified."
    )
    private String apiUrl;

    @CommandLine.Option(
        names = "--nuclia-timeout",
        description = "Maximum number of seconds to wait for Nuclia processing to complete."
    )
    private Integer timeout = 120;

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (nuaKey != null) {
            OptionsUtil.addOptions(options,
                Options.WRITE_NUCLIA_NUA_KEY, nuaKey,
                Options.WRITE_NUCLIA_API_URL, apiUrl,
                Options.WRITE_NUCLIA_TIMEOUT, OptionsUtil.integerOption(timeout)
            );
        }
        return options;
    }

    @Override
    public NucliaOptions nuaKey(String nuaKey) {
        this.nuaKey = nuaKey;
        return this;
    }

    @Override
    public NucliaOptions apiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    @Override
    public NucliaOptions timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
}
