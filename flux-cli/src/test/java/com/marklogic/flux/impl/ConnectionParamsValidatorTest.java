/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.AuthenticationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConnectionParamsValidatorTest {

    ConnectionParamsValidator validator = new ConnectionParamsValidator(false);
    ConnectionInputs inputs = new ConnectionParams();

    @Test
    void cloudAuth() {
        inputs.host = "anyhost";
        inputs.cloudApiKey = "anything";

        assertEquals(0, inputs.port);
        assertNull(inputs.authType);

        validator.validate(inputs);

        assertEquals(443, inputs.port, "If a cloud API key is specified, we can assume " +
            "that the port should be 443.");
        assertEquals(AuthenticationType.CLOUD, inputs.authType, "If a cloud API key is " +
            "specified, we can assume that the auth type should be CLOUD.");
    }

    @Test
    void cloudAuthAndPortAlreadySet() {
        inputs.host = "anyhost";
        inputs.cloudApiKey = "anything";
        inputs.port = 8443;

        validator.validate(inputs);

        assertEquals(8443, inputs.port,
            "There may be a use case in the future where a port other than 443 " +
                "should be used. So we allow the user to override the default of 443 " +
                "when a cloud API key is specified.");
        assertEquals(AuthenticationType.CLOUD, inputs.authType);
    }
}
