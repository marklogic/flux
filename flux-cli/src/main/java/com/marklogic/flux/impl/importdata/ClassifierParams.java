/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ClassifierOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class ClassifierParams implements ClassifierOptions {

    @CommandLine.Option(
        names = "--classifier-host",
        description = "Hostname of the classifier service."
    )
    private String host;

    @CommandLine.Option(
        names = "--classifier-port",
        description = "Port number of the classifier service."
    )
    private int port;

    @CommandLine.Option(
        names = "--classifier-http",
        description = "Specifies the use of HTTP instead of HTTPS for the classifier service."
    )
    private boolean useHttp;

    @CommandLine.Option(
        names = "--classifier-path",
        description = "Path of the classifier service."
    )
    private String path;

    @CommandLine.Option(
        names = "--classifier-api-key",
        description = "API key granting access to the classifier service when hosted in Progress Data Cloud."
    )
    private String apikey;

    @CommandLine.Option(
        names = "--classifier-token-path",
        description = "Path of the token generator for the classifier service when hosted in Progress Data Cloud."
    )
    private String tokenPath;

    @CommandLine.Option(
        names = "--classifier-batch-size",
        description = "Number of documents and/or chunks of text to send to the classifier service in a single request."
    )
    private Integer batchSize = 20;

    @CommandLine.Option(
        names = "--classifier-prop",
        description = "Specify additional options for configuring the behavior of the classifier service."
    )
    private Map<String, String> additionalOptions = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (host != null) {
            OptionsUtil.addOptions(options,
                Options.WRITE_CLASSIFIER_HOST, host,
                Options.WRITE_CLASSIFIER_PORT, OptionsUtil.intOption(port),
                Options.WRITE_CLASSIFIER_HTTP, Boolean.toString(useHttp),
                Options.WRITE_CLASSIFIER_PATH, path,
                Options.WRITE_CLASSIFIER_APIKEY, apikey,
                Options.WRITE_CLASSIFIER_TOKEN_PATH, tokenPath,
                Options.WRITE_CLASSIFIER_BATCH_SIZE, OptionsUtil.integerOption(batchSize)
            );
            if (additionalOptions != null) {
                additionalOptions.entrySet().forEach(entry -> options.put(
                    Options.WRITE_CLASSIFIER_OPTION_PREFIX + entry.getKey(), entry.getValue()));
            }
        }
        return options;
    }

    @Override
    public ClassifierOptions host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public ClassifierOptions port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ClassifierOptions http() {
        this.useHttp = true;
        return this;
    }

    @Override
    public ClassifierOptions path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public ClassifierOptions apiKey(String apikey) {
        this.apikey = apikey;
        return this;
    }

    @Override
    public ClassifierOptions tokenPath(String tokenPath) {
        this.tokenPath = tokenPath;
        return this;
    }

    @Override
    public ClassifierOptions batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public ClassifierOptions additionalOptions(Map<String, String> additionalOptions) {
        this.additionalOptions = additionalOptions;
        return this;
    }
}
