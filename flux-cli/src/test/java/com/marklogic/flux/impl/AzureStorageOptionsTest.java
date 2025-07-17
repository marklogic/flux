/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.AzureStorageType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AzureStorageOptionsTest {

    @Test
    void testAzureStorageOptions() {
        AzureStorageParams params = new AzureStorageParams();

        // Test the fluent interface methods return the same instance
        assertSame(params, params.storageAccount("teststorage"));
        assertSame(params, params.storageType(AzureStorageType.BLOB));
        assertSame(params, params.accessKey("testkey"));
        assertSame(params, params.sasToken("testtoken"));
        assertSame(params, params.sharedKey("testsharedkey"));
        assertSame(params, params.containerName("testcontainer"));
    }
}
