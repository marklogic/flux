/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.AzureStorageOptions;
import com.marklogic.flux.api.AzureStorageType;
import org.apache.hadoop.conf.Configuration;
import picocli.CommandLine;

import java.util.List;

public class AzureStorageParams implements AzureStorageOptions {

    @CommandLine.Option(
        names = "--azure-storage-account",
        description = "Azure Storage account name; required in order to access Azure Blob Storage or Azure Data Lake Storage."
    )
    private String storageAccount;

    @CommandLine.Option(
        names = "--azure-storage-type",
        defaultValue = "BLOB",
        description = "Type of Azure Storage. Defaults to Blob Storage; set to DATA_LAKE for Azure Data Lake Storage."
    )
    private AzureStorageType storageType = AzureStorageType.BLOB;

    @CommandLine.Option(
        names = "--azure-access-key",
        description = "Access key for Blob Storage authentication."
    )
    private String accessKey;

    @CommandLine.Option(
        names = "--azure-sas-token",
        description = "Azure SAS token for Blob Storage authentication."
    )
    private String sasToken;

    @CommandLine.Option(
        names = "--azure-shared-key",
        description = "Azure shared key for Data Lake Storage authentication."
    )
    private String sharedKey;

    @CommandLine.Option(
        names = "--azure-container-name",
        description = "Azure container name."
    )
    private String containerName;

    public void addToHadoopConfiguration(Configuration conf) {
        if (OptionsUtil.hasText(storageAccount)) {
            if (AzureStorageType.BLOB.equals(storageType)) {
                configureBlobStorage(conf);
            } else if (AzureStorageType.DATA_LAKE.equals(storageType)) {
                configureDataLakeStorage(conf);
            }
        }
    }

    private void configureBlobStorage(Configuration conf) {
        if (OptionsUtil.hasText(accessKey)) {
            conf.set(String.format("fs.azure.account.key.%s.blob.core.windows.net", storageAccount), accessKey);
        } else if (OptionsUtil.hasText(sasToken)) {
            configureBlobStorageWithSasToken(conf);
        } else {
            throw new IllegalArgumentException("Either access key or SAS token must be provided for Azure Blob Storage.");
        }
    }

    private void configureBlobStorageWithSasToken(Configuration conf) {
        if (OptionsUtil.hasText(containerName)) {
            conf.set(String.format("fs.azure.sas.%s.%s.blob.core.windows.net", containerName, storageAccount), sasToken);
        } else {
            throw new IllegalArgumentException("Container name must be provided when using SAS token for Azure Blob Storage.");
        }
    }

    private void configureDataLakeStorage(Configuration conf) {
        if (!OptionsUtil.hasText(sharedKey)) {
            throw new IllegalArgumentException("Shared key must be provided for Azure Data Lake Storage.");
        }
        conf.set(String.format("fs.azure.account.auth.type.%s.dfs.core.windows.net", storageAccount), "SharedKey");
        conf.set(String.format("fs.azure.account.key.%s.dfs.core.windows.net", storageAccount), sharedKey);
    }

    /**
     * Transforms simple file paths into full Azure Storage URLs when Azure storage is configured.
     * Uses an "all-or-nothing" approach: if ANY path contains a protocol (://), then NO paths are transformed.
     * This allows users to either use all simple paths (auto-transformed) or all full URLs (user-controlled).
     * For example: "Hogwarts.csv" becomes "wasbs://container1@realgenius1.blob.core.windows.net/Hogwarts.csv"
     *
     * @param paths List of file paths to transform
     * @return List of transformed paths (full URLs for Azure, unchanged for non-Azure or mixed scenarios)
     */
    public List<String> transformPathsIfNecessary(List<String> paths) {
        if (!OptionsUtil.hasText(storageAccount) || !OptionsUtil.hasText(containerName) || atLeastOnePathHasProtocol(paths)) {
            return paths;
        }

        return paths.stream()
            .map(this::transformSinglePath)
            .collect(java.util.stream.Collectors.toList());
    }

    private boolean atLeastOnePathHasProtocol(List<String> paths) {
        return paths.stream().anyMatch(path -> path.contains("://"));
    }

    private String transformSinglePath(String path) {
        // Remove leading slash if present for consistency
        final String cleanPath = path.startsWith("/") ? path.substring(1) : path;

        return AzureStorageType.DATA_LAKE.equals(storageType) ?
            String.format("abfss://%s@%s.dfs.core.windows.net/%s", containerName, storageAccount, cleanPath) :
            String.format("wasbs://%s@%s.blob.core.windows.net/%s", containerName, storageAccount, cleanPath);
    }

    @Override
    public AzureStorageOptions storageAccount(String storageAccount) {
        this.storageAccount = storageAccount;
        return this;
    }

    @Override
    public AzureStorageOptions storageType(AzureStorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    @Override
    public AzureStorageOptions accessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    @Override
    public AzureStorageOptions sasToken(String sasToken) {
        this.sasToken = sasToken;
        return this;
    }

    @Override
    public AzureStorageOptions sharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return this;
    }

    @Override
    public AzureStorageOptions containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }
}

