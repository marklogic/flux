package com.marklogic.newtool.command.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.ParquetFilesExporter;
import com.marklogic.newtool.api.ReadRowsOptions;
import com.marklogic.newtool.api.WriteSparkFilesOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to Parquet files on a local filesystem, HDFS, or S3.")
public class ExportParquetFilesCommand extends AbstractExportRowsToFilesCommand<ParquetFilesExporter> implements ParquetFilesExporter {

    @ParametersDelegate
    private WriteParquetFilesParams writeParams = new WriteParquetFilesParams();

    @Override
    protected String getWriteFormat() {
        return "parquet";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteParquetFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Parquet option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -Pcompression=gzip."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> get() {
            return additionalOptions;
        }

        @Override
        public WriteSparkFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }
    }

    @Override
    public ParquetFilesExporter readRows(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public ParquetFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
