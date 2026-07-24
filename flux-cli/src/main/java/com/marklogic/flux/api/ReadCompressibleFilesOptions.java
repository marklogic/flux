/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * Options shared by all import commands that support reading zip-compressed files.
 * Provides the {@code partitions}, {@code zipMaxUncompressedEntryBytes}, and
 * {@code zipMaxEntryCount} options, which are common to all five zip-capable import commands.
 *
 * @since 2.1.2
 */
@SuppressWarnings("unchecked")
public interface ReadCompressibleFilesOptions<T extends ReadCompressibleFilesOptions> extends ReadFilesOptions<T> {

    /**
     * Specifies the number of Spark partitions used for reading files. More partitions increase parallelism
     * but also increase overhead. When not set, the connector determines the number of partitions automatically
     * based on the number of files found.
     */
    T partitions(int partitions);

    /**
     * Set to a positive integer to enable zip bomb protection. Any value less than 1 (including 0)
     * disables the limit. When this option is not set, the connector default applies.
     *
     * @since 2.1.2
     */
    T zipMaxUncompressedEntryBytes(long bytes);

    /**
     * Set to a positive integer to enable zip bomb protection. Any value less than 1 (including 0)
     * disables the limit. When this option is not set, the connector default applies.
     *
     * @since 2.1.2
     */
    T zipMaxEntryCount(int count);
}
