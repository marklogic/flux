package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Parquet files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-parquet.html, with each row being written " +
    "as a JSON document in MarkLogic.")
public class ImportParquetFilesCommand extends AbstractImportFilesCommand {

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @ParametersDelegate
    private XmlDocumentParams xmlDocumentParams = new XmlDocumentParams();

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Parquet option or configuration item defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -PmergeSchema=true or " +
            "-Pspark.sql.parquet.filterPushdown=false."
    )
    private Map<String, String> parquetParams = new HashMap<>();

    @Override
    protected String getReadFormat() {
        return "parquet";
    }

    @Override
    protected void modifySparkSession(SparkSession session) {
        parquetParams.entrySet().stream()
            .filter(OptionsUtil::isSparkConfigurationOption)
            .forEach(entry -> session.conf().set(entry.getKey(), entry.getValue()));
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        parquetParams.entrySet().stream()
            .filter(OptionsUtil::isSparkDataSourceOption)
            .forEach(entry -> options.put(entry.getKey(), entry.getValue()));
        return options;
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = OptionsUtil.addOptions(super.makeWriteOptions(),
            Options.WRITE_JSON_ROOT_NAME, jsonRootName
        );
        options.putAll(xmlDocumentParams.makeOptions());
        return options;
    }
}
