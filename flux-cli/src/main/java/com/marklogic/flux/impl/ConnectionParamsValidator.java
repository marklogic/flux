/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.beust.jcommander.ParameterException;
import com.marklogic.flux.api.AuthenticationType;
import com.marklogic.flux.api.FluxException;

public class ConnectionParamsValidator {

    private final ParamNames paramNames;

    public ConnectionParamsValidator(boolean isOutput) {
        this.paramNames = new ParamNames(isOutput);
    }

    public void validate(ConnectionInputs connectionInputs, CommonParams commonParams) throws ParameterException {
        if (connectionInputs.connectionString != null || (commonParams != null && commonParams.isPreviewRequested())) {
            return;
        }

        if (connectionInputs.host == null) {
            throw new FluxException(String.format("Must specify a MarkLogic host via %s or %s.",
                paramNames.host, paramNames.connectionString));
        }
        if (connectionInputs.port == null) {
            throw new FluxException(String.format("Must specify a MarkLogic app server port via %s or %s.",
                paramNames.port, paramNames.connectionString));
        }

        AuthenticationType authType = connectionInputs.authType;
        boolean isDigestOrBasicAuth = authType == null || (AuthenticationType.DIGEST.equals(authType) || AuthenticationType.BASIC.equals(authType));
        if (isDigestOrBasicAuth) {
            if (connectionInputs.username == null) {
                throw new FluxException(String.format("Must specify a MarkLogic user via %s when using 'BASIC' or 'DIGEST' authentication.",
                    paramNames.username));
            }
            if (connectionInputs.password == null) {
                throw new FluxException(String.format("Must specify a password via %s when using 'BASIC' or 'DIGEST' authentication.",
                    paramNames.password));
            }
        }
    }

    private static class ParamNames {
        final String connectionString;
        final String host;
        final String port;
        final String authType;
        final String username;
        final String password;

        ParamNames(boolean isOutput) {
            connectionString = isOutput ? "--output-connection-string" : "--connection-string";
            host = isOutput ? "--output-host" : "--host";
            port = isOutput ? "--output-port" : "--port";
            authType = isOutput ? "--output-auth-type" : "--auth-type";
            username = isOutput ? "--output-username" : "--username";
            password = isOutput ? "--output-password" : "--password";
        }
    }
}
