/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.AzureStorageType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportFilesAzureTest {

    private ExportFilesCommand command = new ExportFilesCommand();

    @Test
    void testAzurePathTransformation() {
        command.to(writeOptions -> writeOptions
            .azureStorage(azure -> azure
                .storageAccount("testaccount")
                .storageType(AzureStorageType.BLOB)
                .containerName("testcontainer")
                .accessKey("testkey")
            )
        );

        List<String> transformedPaths = command.writeParams.getAzureStorageParams().transformPathsIfNecessary(Arrays.asList("output-files"));
        assertEquals(1, transformedPaths.size());
        assertEquals("abfss://testcontainer@testaccount.blob.core.windows.net/output-files", transformedPaths.get(0),
            "Simple path should be transformed to full Azure blob URL. Note that after Hadoop 3.4.2 was upgraded to " +
                "Hadoop 3.5.0, support for 'wasbs' was removed and 'abfss' should now be used for both Blob and " +
                "Data Lake storage. See https://issues.apache.org/jira/browse/HADOOP-19778 for more information.");
    }

    @Test
    void testDataLakePathTransformation() {
        command.to(writeOptions -> writeOptions
            .azureStorage(azure -> azure
                .storageAccount("datalake")
                .storageType(AzureStorageType.DATA_LAKE)
                .containerName("analytics")
                .sharedKey("sharedkey")
            )
        );

        List<String> transformedPaths = command.writeParams.getAzureStorageParams().transformPathsIfNecessary(Arrays.asList("analytics/results"));
        assertEquals(1, transformedPaths.size());
        assertEquals("abfss://analytics@datalake.dfs.core.windows.net/analytics/results", transformedPaths.get(0),
            "Simple path should be transformed to full Azure Data Lake URL");
    }

    @Test
    void testNoTransformationWithoutAzureConfig() {
        List<String> transformedPaths = command.writeParams.getAzureStorageParams().transformPathsIfNecessary(Arrays.asList("local-output"));
        assertEquals(1, transformedPaths.size());
        assertEquals("local-output", transformedPaths.get(0), "Path should remain unchanged when no Azure config is provided");
    }

    @Test
    void testNoTransformationWithFullURL() {
        // Configure Azure storage but use full URL path
        command.to(writeOptions -> writeOptions
            .azureStorage(azure -> azure
                .storageAccount("testaccount")
                .storageType(AzureStorageType.BLOB)
                .containerName("testcontainer")
                .accessKey("testkey")
            )
        );

        List<String> transformedPaths = command.writeParams.getAzureStorageParams().transformPathsIfNecessary(
            Arrays.asList("wasbs://other@different.blob.core.windows.net/full-path"));
        assertEquals(1, transformedPaths.size());
        assertEquals("wasbs://other@different.blob.core.windows.net/full-path", transformedPaths.get(0),
            "Full URL should remain unchanged (mixed mode protection)");
    }
}
