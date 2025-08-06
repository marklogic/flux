/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.AzureStorageType;
import com.marklogic.flux.api.ReadFilesOptions;
import com.marklogic.flux.impl.importdata.ReadFilesParams;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AzureStorageParamsTest {

    private AzureStorageParams params;
    private ReadFilesOptions<?> options;
    private Configuration conf;

    @BeforeEach
    void setUp() {
        ReadFilesParams<?> readFilesParams = new ReadFilesParams<>();
        options = readFilesParams;
        params = readFilesParams.getAzureStorageParams();
        conf = new Configuration();
    }

    @Test
    void blobStorageWithAccessKey() {
        options.azureStorage(azure -> azure
            .storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .accessKey("test-access-key"));

        params.addToHadoopConfiguration(conf);

        assertEquals("test-access-key",
            conf.get("fs.azure.account.key.teststorage.blob.core.windows.net"));
    }

    @Test
    void blobStorageWithSasToken() {
        options.azureStorage(azure -> azure
            .storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .sasToken("test-sas-token")
            .containerName("testcontainer"));

        params.addToHadoopConfiguration(conf);

        assertEquals("test-sas-token",
            conf.get("fs.azure.sas.testcontainer.teststorage.blob.core.windows.net"));
    }

    @Test
    void blobStorageWithSasTokenMissingContainerName() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .sasToken("test-sas-token"));
        // Missing container name

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Container name must be provided when using SAS token for Azure Blob Storage.",
            exception.getMessage());
    }

    @Test
    void blobStorageWithoutAccessKeyOrSasToken() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB));
        // Missing both access key and SAS token

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Either access key or SAS token must be provided for Azure Blob Storage.",
            exception.getMessage());
    }

    @Test
    void dataLakeStorageWithSharedKey() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.DATA_LAKE)
            .sharedKey("test-shared-key"));

        params.addToHadoopConfiguration(conf);

        assertEquals("SharedKey",
            conf.get("fs.azure.account.auth.type.teststorage.dfs.core.windows.net"));
        assertEquals("test-shared-key",
            conf.get("fs.azure.account.key.teststorage.dfs.core.windows.net"));
    }

    @Test
    void dataLakeStorageWithoutSharedKey() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.DATA_LAKE));
        // Missing shared key

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Shared key must be provided for Azure Data Lake Storage.",
            exception.getMessage());
    }

    @Test
    void noStorageAccountProvided() {
        options.azureStorage(azure -> azure.storageType(AzureStorageType.BLOB)
            .accessKey("test-access-key"));
        // Missing storage account

        params.addToHadoopConfiguration(conf);

        // Should not add any configuration if storage account is not provided
        assertNull(conf.get("fs.azure.account.key.teststorage.blob.core.windows.net"));
    }

    @Test
    void emptyStorageAccount() {
        options.azureStorage(azure -> azure.storageAccount("")
            .storageType(AzureStorageType.BLOB)
            .accessKey("test-access-key"));

        params.addToHadoopConfiguration(conf);

        // Should not add any configuration if storage account is empty
        assertNull(conf.get("fs.azure.account.key..blob.core.windows.net"));
    }

    @Test
    void nullStorageAccount() {
        options.azureStorage(azure -> azure.storageAccount(null)
            .storageType(AzureStorageType.BLOB)
            .accessKey("test-access-key"));

        params.addToHadoopConfiguration(conf);

        // Should not add any configuration if storage account is null
        assertNull(conf.get("fs.azure.account.key.null.blob.core.windows.net"));
    }

    @Test
    void blobStorageWithBothAccessKeyAndSasToken() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .accessKey("test-access-key")
            .sasToken("test-sas-token")
            .containerName("testcontainer"));

        params.addToHadoopConfiguration(conf);

        // Should prioritize access key over SAS token
        assertEquals("test-access-key",
            conf.get("fs.azure.account.key.teststorage.blob.core.windows.net"));
        assertNull(conf.get("fs.azure.sas.testcontainer.teststorage.blob.core.windows.net"));
    }

    @Test
    void dataLakeStorageWithAccessKeyInsteadOfSharedKey() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.DATA_LAKE)
            .accessKey("test-access-key"));
        // Using access key instead of shared key

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Shared key must be provided for Azure Data Lake Storage.",
            exception.getMessage());
    }

    @Test
    void multipleStorageAccountsConfiguration() {
        // Test first storage account
        options.azureStorage(azure -> azure.storageAccount("storage1")
            .storageType(AzureStorageType.BLOB)
            .accessKey("key1"));
        params.addToHadoopConfiguration(conf);

        // Test second storage account with different params
        AzureStorageParams secondParams = new AzureStorageParams();
        secondParams.storageAccount("storage2");
        secondParams.storageType(AzureStorageType.DATA_LAKE);
        secondParams.sharedKey("key2");
        secondParams.addToHadoopConfiguration(conf);

        // Both configurations should be present
        assertEquals("key1", conf.get("fs.azure.account.key.storage1.blob.core.windows.net"));
        assertEquals("SharedKey", conf.get("fs.azure.account.auth.type.storage2.dfs.core.windows.net"));
        assertEquals("key2", conf.get("fs.azure.account.key.storage2.dfs.core.windows.net"));
    }

    @Test
    void specialCharactersInStorageAccount() {
        options.azureStorage(azure -> azure.storageAccount("test-storage_123")
            .storageType(AzureStorageType.BLOB)
            .accessKey("test-access-key"));

        params.addToHadoopConfiguration(conf);

        assertEquals("test-access-key",
            conf.get("fs.azure.account.key.test-storage_123.blob.core.windows.net"));
    }

    @Test
    void specialCharactersInContainerName() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .sasToken("test-sas-token")
            .containerName("test-container_123"));

        params.addToHadoopConfiguration(conf);

        assertEquals("test-sas-token",
            conf.get("fs.azure.sas.test-container_123.teststorage.blob.core.windows.net"));
    }

    @Test
    void defaultStorageTypeIsBLOB() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .accessKey("test-access-key"));
        // Not explicitly setting storage type - should default to BLOB

        params.addToHadoopConfiguration(conf);

        assertEquals("test-access-key",
            conf.get("fs.azure.account.key.teststorage.blob.core.windows.net"));
    }

    @Test
    void configurationOverwrite() {
        // Set initial configuration
        String configKey = "fs.azure.account.key.teststorage.blob.core.windows.net";
        conf.set(configKey, "old-value");

        // Add new configuration
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .accessKey("new-value"));
        params.addToHadoopConfiguration(conf);

        // Should overwrite the old value
        assertEquals("new-value", conf.get(configKey));
    }

    @Test
    void emptyAccessKey() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .accessKey(""));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Either access key or SAS token must be provided for Azure Blob Storage.",
            exception.getMessage());
    }

    @Test
    void emptySasToken() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .sasToken(""));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Either access key or SAS token must be provided for Azure Blob Storage.",
            exception.getMessage());
    }

    @Test
    void emptySharedKey() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.DATA_LAKE)
            .sharedKey(""));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Shared key must be provided for Azure Data Lake Storage.",
            exception.getMessage());
    }

    @Test
    void emptyContainerName() {
        options.azureStorage(azure -> azure.storageAccount("teststorage")
            .storageType(AzureStorageType.BLOB)
            .sasToken("test-sas-token")
            .containerName(""));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> params.addToHadoopConfiguration(conf));
        assertEquals("Container name must be provided when using SAS token for Azure Blob Storage.",
            exception.getMessage());
    }

    @Test
    void transformPathsNoAzureConfig() {
        // No Azure storage configured
        List<String> paths = Arrays.asList("Hogwarts.csv", "Dumbledore.json");
        List<String> result = params.transformPathsIfNecessary(paths);

        // Should return unchanged
        assertEquals(paths, result);
    }

    @Test
    void transformPathsBlobStorage() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList("Hogwarts.csv", "spells/Expelliarmus.txt");
        List<String> result = params.transformPathsIfNecessary(paths);

        assertEquals(2, result.size());
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/Hogwarts.csv", result.get(0));
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/spells/Expelliarmus.txt", result.get(1));
    }

    @Test
    void transformPathsDataLakeStorage() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.DATA_LAKE)
            .containerName("filesystem1"));

        List<String> paths = Arrays.asList("Hogwarts.csv", "potions/Felix_Felicis.json");
        List<String> result = params.transformPathsIfNecessary(paths);

        assertEquals(2, result.size());
        assertEquals("abfss://filesystem1@realgenius1.dfs.core.windows.net/Hogwarts.csv", result.get(0));
        assertEquals("abfss://filesystem1@realgenius1.dfs.core.windows.net/potions/Felix_Felicis.json", result.get(1));
    }

    @Test
    void transformPathsWithLeadingSlash() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList("/Hogwarts.csv", "Dumbledore.json");
        List<String> result = params.transformPathsIfNecessary(paths);

        assertEquals(2, result.size());
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/Hogwarts.csv", result.get(0));
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/Dumbledore.json", result.get(1));
    }

    @Test
    void transformPathsLeavesFullUrlsUnchangedInMixedMode() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "Hogwarts.csv",
            "wasbs://other@different.blob.core.windows.net/existing.csv",
            "https://example.com/web.csv",
            "file:///local/path.csv"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // In mixed mode (any path has ://), ALL paths should be left unchanged
        assertEquals(4, result.size());
        assertEquals("Hogwarts.csv", result.get(0));  // NOT transformed because of mixed mode
        assertEquals("wasbs://other@different.blob.core.windows.net/existing.csv", result.get(1));
        assertEquals("https://example.com/web.csv", result.get(2));
        assertEquals("file:///local/path.csv", result.get(3));
    }

    @Test
    void transformPathsWithoutContainerName() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB));

        List<String> result = params.transformPathsIfNecessary(Arrays.asList("Hogwarts.csv"));

        assertEquals(1, result.size());
        assertEquals("Hogwarts.csv", result.get(0), "If no container name is set, paths should not be transformed");
    }

    @Test
    void transformPathsEmptyList() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList();
        List<String> result = params.transformPathsIfNecessary(paths);

        assertEquals(0, result.size());
    }

    @Test
    void transformPathsSpecialCharacters() {
        options.azureStorage(azure -> azure.storageAccount("real-genius_123")
            .storageType(AzureStorageType.BLOB)
            .containerName("container-name_test"));

        List<String> paths = Arrays.asList("folder/file-name_test.csv", "special chars & symbols.json");
        List<String> result = params.transformPathsIfNecessary(paths);

        assertEquals(2, result.size());
        assertEquals("wasbs://container-name_test@real-genius_123.blob.core.windows.net/folder/file-name_test.csv", result.get(0));
        assertEquals("wasbs://container-name_test@real-genius_123.blob.core.windows.net/special chars & symbols.json", result.get(1));
    }

    @Test
    void transformPathsMixedModeWithOneFullUrl() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "Hogwarts.csv",
            "https://example.com/web.csv",
            "simple-file.txt"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Mixed mode: ANY path with :// means NO transformation
        assertEquals(3, result.size());
        assertEquals("Hogwarts.csv", result.get(0));  // NOT transformed
        assertEquals("https://example.com/web.csv", result.get(1));
        assertEquals("simple-file.txt", result.get(2));  // NOT transformed
    }

    @Test
    void transformPathsMixedModeWithS3Path() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "azure-file.csv",
            "s3://bucket/file.csv"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Mixed mode: S3 path triggers no transformation
        assertEquals(2, result.size());
        assertEquals("azure-file.csv", result.get(0));  // NOT transformed
        assertEquals("s3://bucket/file.csv", result.get(1));
    }

    @Test
    void transformPathsMixedModeWithLocalPath() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "azure-file.csv",
            "file:///tmp/local.csv"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Mixed mode: Local file path triggers no transformation
        assertEquals(2, result.size());
        assertEquals("azure-file.csv", result.get(0));  // NOT transformed
        assertEquals("file:///tmp/local.csv", result.get(1));
    }

    @Test
    void transformPathsMixedModeWithExistingAzurePath() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "simple-file.csv",
            "wasbs://other@different.blob.core.windows.net/existing.csv"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Mixed mode: Existing Azure path triggers no transformation
        assertEquals(2, result.size());
        assertEquals("simple-file.csv", result.get(0));  // NOT transformed
        assertEquals("wasbs://other@different.blob.core.windows.net/existing.csv", result.get(1));
    }

    @Test
    void transformPathsSimpleModeAllRelativePaths() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "Hogwarts.csv",
            "spells/Expelliarmus.txt",
            "potions/Felix_Felicis.json"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Simple mode: ALL relative paths get transformed
        assertEquals(3, result.size());
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/Hogwarts.csv", result.get(0));
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/spells/Expelliarmus.txt", result.get(1));
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/potions/Felix_Felicis.json", result.get(2));
    }

    @Test
    void transformPathsSimpleModeWithLeadingSlashes() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB)
            .containerName("container1"));

        List<String> paths = Arrays.asList(
            "/Hogwarts.csv",
            "/spells/Expelliarmus.txt",
            "no-slash.json"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Simple mode: ALL relative paths get transformed (leading slashes removed)
        assertEquals(3, result.size());
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/Hogwarts.csv", result.get(0));
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/spells/Expelliarmus.txt", result.get(1));
        assertEquals("wasbs://container1@realgenius1.blob.core.windows.net/no-slash.json", result.get(2));
    }

    @Test
    void transformPathsMixedModeNoContainerValidation() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.BLOB));
        // No container name set

        List<String> paths = Arrays.asList(
            "simple-file.csv",
            "https://example.com/web.csv"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        assertEquals(2, result.size());
        assertEquals("simple-file.csv", result.get(0));
        assertEquals("https://example.com/web.csv", result.get(1));
    }

    @Test
    void transformPathsDataLakeSimpleMode() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.DATA_LAKE)
            .containerName("filesystem1"));

        List<String> paths = Arrays.asList(
            "data/big-file.parquet",
            "analytics/results.csv"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Simple mode for Data Lake: All paths transformed with abfss://
        assertEquals(2, result.size());
        assertEquals("abfss://filesystem1@realgenius1.dfs.core.windows.net/data/big-file.parquet", result.get(0));
        assertEquals("abfss://filesystem1@realgenius1.dfs.core.windows.net/analytics/results.csv", result.get(1));
    }

    @Test
    void transformPathsDataLakeMixedMode() {
        options.azureStorage(azure -> azure.storageAccount("realgenius1")
            .storageType(AzureStorageType.DATA_LAKE)
            .containerName("filesystem1"));

        List<String> paths = Arrays.asList(
            "data/big-file.parquet",
            "abfss://other@different.dfs.core.windows.net/existing.parquet"
        );
        List<String> result = params.transformPathsIfNecessary(paths);

        // Mixed mode for Data Lake: No transformation when any path has ://
        assertEquals(2, result.size());
        assertEquals("data/big-file.parquet", result.get(0));  // NOT transformed
        assertEquals("abfss://other@different.dfs.core.windows.net/existing.parquet", result.get(1));
    }
}
