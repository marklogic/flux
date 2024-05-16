package com.marklogic.newtool.impl.export;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.JsonLinesFilesExporter;
import com.marklogic.newtool.api.ReadRowsOptions;
import com.marklogic.newtool.api.WriteFilesOptions;
import com.marklogic.newtool.api.WriteSparkFilesOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ExportJsonLinesFilesCommand extends AbstractExportRowsToFilesCommand<JsonLinesFilesExporter> implements JsonLinesFilesExporter {

    @ParametersDelegate
    private WriteJsonFilesParams writeParams = new WriteJsonFilesParams();

    public static class WriteJsonFilesParams extends WriteStructuredFilesParams<WriteSparkFilesOptions> implements WriteSparkFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark JSON option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-json.html; e.g. -Pcompression=bzip2."
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
    protected WriteStructuredFilesParams<? extends WriteFilesOptions> getWriteFilesParams() {
        return writeParams;
    }

    @Override
    protected String getWriteFormat() {
        return "json";
    }

    @Override
    public JsonLinesFilesExporter readRows(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public JsonLinesFilesExporter readRows(String opticQuery) {
        return this.readRows(options -> options.opticQuery(opticQuery));
    }

    @Override
    public JsonLinesFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    @Override
    public JsonLinesFilesExporter writeFiles(String path) {
        return this.writeFiles(options -> options.path(path));
    }
}
