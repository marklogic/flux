package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.api.AvroFilesImporter;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read Avro files from local, HDFS, and S3 locations using Spark's support " +
    "defined at https://spark.apache.org/docs/latest/sql-data-sources-avro.html, with each row being written " +
    "as a JSON or XML document in MarkLogic.")
public class ImportAvroFilesCommand extends AbstractImportStructuredFilesCommand<AvroFilesImporter> implements AvroFilesImporter {

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark Avro data source option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-avro.html; e.g. -PignoreExtension=true. " +
            "Spark configuration options must be defined via '-C'."
    )
    private Map<String, String> avroParams;

    public ImportAvroFilesCommand() {
        super("avro");
        this.avroParams = new HashMap<>();
        setDynamicParams(this.avroParams);
    }
}
