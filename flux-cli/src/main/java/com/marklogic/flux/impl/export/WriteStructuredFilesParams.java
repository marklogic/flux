package com.marklogic.flux.impl.export;

import com.beust.jcommander.Parameter;
import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.api.WriteFilesOptions;

/**
 * Structured = reuses a Spark data source, where saveMode can vary.
 */
public abstract class WriteStructuredFilesParams<T extends WriteFilesOptions> extends WriteFilesParams<T> {

    @Parameter(names = "--mode",
        description = "Specifies how data is written if the path already exists. " +
            "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
    private SaveMode saveMode = SaveMode.OVERWRITE;

    protected WriteStructuredFilesParams() {
        // For Avro/Parquet/etc files, writing many rows to a single file is acceptable and expected.
        this.fileCount = 1;
    }

    public SaveMode getSaveMode() {
        return saveMode;
    }
}
