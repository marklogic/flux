/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.api.WriteFilesOptions;
import com.marklogic.flux.impl.OptionsUtil;
import picocli.CommandLine;

/**
 * Structured = reuses a Spark data source, where saveMode can vary.
 */
public abstract class WriteStructuredFilesParams<T extends WriteFilesOptions> extends WriteFilesParams<T> {

    @CommandLine.Option(names = "--mode",
        description = "Specifies how data is written if the path already exists. " +
            "See %nhttps://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information. "
            + OptionsUtil.VALID_VALUES_DESCRIPTION)
    private SaveMode saveMode = SaveMode.APPEND;

    protected WriteStructuredFilesParams() {
        // For Avro/Parquet/etc files, writing many rows to a single file is acceptable and expected.
        this.fileCount = 1;
    }

    public SaveMode getSaveMode() {
        return saveMode;
    }
}
