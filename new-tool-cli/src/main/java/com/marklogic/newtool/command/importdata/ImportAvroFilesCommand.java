package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.AvroFilesImporter;
import com.marklogic.newtool.api.ReadSparkFilesOptions;
import com.marklogic.newtool.api.WriteStructuredDocumentsOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "as a JSON or XML document in MarkLogic.")
public class ImportAvroFilesCommand extends AbstractImportFilesCommand<AvroFilesImporter> implements AvroFilesImporter {

    @ParametersDelegate
    private ReadAvroFilesParams readParams = new ReadAvroFilesParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeDocumentParams = new WriteStructuredDocumentParams();

    @Override
    protected String getReadFormat() {
        return "avro";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeDocumentParams;
    }

    public static class ReadAvroFilesParams extends ReadFilesParams<ReadSparkFilesOptions> implements ReadSparkFilesOptions {

        @DynamicParameter(
            names = "-P",
            description = "Specify any Spark Avro data source option defined at " +
                "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true. " +
                "Spark configuration options must be defined via '-C'."
        )
        private Map<String, String> dynamicParams = new HashMap<>();

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            options.putAll(dynamicParams);
            return options;
        }

        @Override
        public ReadSparkFilesOptions additionalOptions(Map<String, String> options) {
            this.dynamicParams = options;
            return this;
        }
    }

    @Override
    public AvroFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AvroFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeDocumentParams);
        return this;
    }
}
