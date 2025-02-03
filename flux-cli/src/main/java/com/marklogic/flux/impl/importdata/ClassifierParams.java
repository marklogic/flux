/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ClassifierOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import com.marklogic.spark.udf.TextClassifierUdf;
import org.apache.spark.sql.expressions.UserDefinedFunction;
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
    private String port;

    @CommandLine.Option(
        names = "--classifier-https",
        description = "Specifies the https protocol for the classifier service."
    )
    private boolean https = false;

    @CommandLine.Option(
        names = "--classifier-endpoint",
        description = "Endpoint of the classifier service."
    )
    private String endpoint;

    @CommandLine.Option(
        names = "--classifier-api-key",
        description = "API key granting access to the classifier service."
    )
    private String apikey;

    @CommandLine.Option(
        names = "--classifier-token-endpoint",
        description = "Endpoint of the token generator for the classifier service."
    )
    private String tokenEndpoint;

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (host != null) {
            OptionsUtil.addOptions(options,
                Options.WRITE_CLASSIFIER_HOST, host,
                Options.WRITE_CLASSIFIER_PORT, port,
                Options.WRITE_CLASSIFIER_HTTPS, Boolean.toString(https),
                Options.WRITE_CLASSIFIER_ENDPOINT, endpoint,
                Options.WRITE_CLASSIFIER_APIKEY, apikey,
                Options.WRITE_CLASSIFIER_TOKEN_ENDPOINT, tokenEndpoint
            );
        }
        return options;
    }

    public UserDefinedFunction buildTextClassifier() {
        return TextClassifierUdf.build(host, https, port, endpoint, apikey, tokenEndpoint);
    }

    @Override
    public ClassifierOptions host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public ClassifierOptions port(String port) {
        this.port = port;
        return this;
    }

    @Override
    public ClassifierOptions https() {
        this.https = true;
        return this;
    }

    @Override
    public ClassifierOptions endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    @Override
    public ClassifierOptions apiKey(String apikey) {
        this.apikey = apikey;
        return this;
    }

    @Override
    public ClassifierOptions tokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
        return this;
    }
}
