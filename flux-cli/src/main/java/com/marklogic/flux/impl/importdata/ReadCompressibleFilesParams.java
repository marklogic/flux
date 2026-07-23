/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ReadCompressibleFilesOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;

/**
 * Base params class for all import commands that support reading zip-compressed files.
 * Declares the shared {@code --partitions}, {@code --zip-max-entry-bytes}, and
 * {@code --zip-max-entry-count} CLI options and handles them in {@code makeOptions()}.
 */
@SuppressWarnings("unchecked")
public abstract class ReadCompressibleFilesParams<T extends ReadCompressibleFilesOptions>
    extends ReadFilesParams<T>
    implements ReadCompressibleFilesOptions<T> {

    @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
    private int partitions;

    @CommandLine.Option(
        names = "--zip-max-entry-bytes",
        description = "Maximum number of uncompressed bytes to read from a single zip entry. " +
            "Set to a positive integer to enable protection. Any value less than 1 (including 0) disables the limit."
    )
    private Long zipMaxEntryBytes;

    @CommandLine.Option(
        names = "--zip-max-entry-count",
        description = "Maximum number of entries to process from a single zip archive. " +
            "Set to a positive integer to enable protection. Any value less than 1 (including 0) disables the limit."
    )
    private Integer zipMaxEntryCount;

    @Override
    public Map<String, String> makeOptions() {
        Map<String, String> options = super.makeOptions();
        OptionsUtil.addOptions(options, Options.READ_NUM_PARTITIONS, OptionsUtil.intOption(partitions));
        // Forward any explicitly set value, including 0 and negatives.
        // The connector treats any value < 1 as "disabled", so forwarding these allows operators
        // to explicitly override future connector defaults rather than relying on omission.
        if (zipMaxEntryBytes != null) {
            options.put(Options.READ_ZIP_MAX_UNCOMPRESSED_ENTRY_BYTES, String.valueOf(zipMaxEntryBytes));
        }
        if (zipMaxEntryCount != null) {
            options.put(Options.READ_ZIP_MAX_ENTRY_COUNT, String.valueOf(zipMaxEntryCount));
        }
        return options;
    }

    @Override
    public T partitions(int partitions) {
        this.partitions = partitions;
        return (T) this;
    }

    @Override
    public T zipMaxUncompressedEntryBytes(long bytes) {
        this.zipMaxEntryBytes = bytes;
        return (T) this;
    }

    @Override
    public T zipMaxEntryCount(int count) {
        this.zipMaxEntryCount = count;
        return (T) this;
    }
}
