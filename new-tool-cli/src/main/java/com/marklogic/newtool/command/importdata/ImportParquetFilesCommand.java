package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.api.ParquetFilesImporter;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Parquet files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-parquet.html, with each row being written " +
    "as a JSON or XML document in MarkLogic.")
public class ImportParquetFilesCommand extends AbstractImportStructuredFilesCommand<ParquetFilesImporter> implements ParquetFilesImporter {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Parquet data source option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-parquet.html; e.g. -PmergeSchema=true. " +
            "Spark configuration options must be defined via '-C'."
    )
    private Map<String, String> parquetParams;

    public ImportParquetFilesCommand() {
        super("parquet");
        this.parquetParams = new HashMap<>();
        setDynamicParams(this.parquetParams);
    }
}
