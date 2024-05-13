package com.marklogic.newtool.impl.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.DelimitedFilesExporter;
import com.marklogic.newtool.api.ReadRowsOptions;
import com.marklogic.newtool.api.WriteSparkFilesOptions;
import com.marklogic.newtool.impl.OptionsUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to delimited text files on a " +
    "local filesystem, HDFS, or S3.")
public class ExportDelimitedFilesCommand extends AbstractExportRowsToFilesCommand<DelimitedFilesExporter> implements DelimitedFilesExporter {

    @ParametersDelegate
    private WriteDelimitedFilesParams writeParams = new WriteDelimitedFilesParams();

    @Override
    protected String getWriteFormat() {
        return "csv";
    }

    @Override
    protected WriteStructuredFilesParams getWriteFilesParams() {
        return writeParams;
    }

    public static class WriteDelimitedFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark CSV option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-csv.html; e.g. -PquoteAll=true.")
        private Map<String, String> additionalOptions = new HashMap<>();

        @Override
        public Map<String, String> get() {
            Map<String, String> options = OptionsUtil.makeOptions("header", "true");
            options.putAll(additionalOptions);
            return options;
        }

        @Override
        public WriteSparkFilesOptions additionalOptions(Map<String, String> options) {
            this.additionalOptions = options;
            return this;
        }
    }

    @Override
    public DelimitedFilesExporter readRows(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public DelimitedFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
