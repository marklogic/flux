package com.marklogic.flux.impl.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.AvroFilesExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.WriteSparkFilesOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to Avro files on a local filesystem, HDFS, or S3.")
public class ExportAvroFilesCommand extends AbstractExportRowsToFilesCommand<AvroFilesExporter> implements AvroFilesExporter {

    @ParametersDelegate
    private WriteAvroFilesParams writeParams = new WriteAvroFilesParams();

    @Override
    protected String getWriteFormat() {
        return "avro";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteAvroFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Avro option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -Pcompression=bzip2."
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
    public AvroFilesExporter readRows(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AvroFilesExporter readRows(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public AvroFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public AvroFilesExporter writeFiles(String path) {
        writeParams.path(path);
        return this;
    }
}
