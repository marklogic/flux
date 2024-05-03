package com.marklogic.newtool.command.custom;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.newtool.command.S3Params;
import com.marklogic.newtool.command.importdata.WriteDocumentWithTemplateParams;
import com.marklogic.newtool.command.importdata.XmlDocumentParams;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Read data via a custom Spark connector or data source and write JSON or XML documents to MarkLogic.")
public class CustomImportCommand extends AbstractCommand {

    @Parameter(names = "--source", description = "Identifier for the Spark connector that is the source of data to import.")
    private String source;

    @DynamicParameter(
            names = "-P",
            description = "Specify any number of options to be passed to the connector identified by '--source'."
    )
    private Map<String, String> readerParams = new HashMap<>();

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @ParametersDelegate
    private WriteDocumentWithTemplateParams writeDocumentParams = new WriteDocumentWithTemplateParams();

    @Parameter(
            names = "--jsonRootName",
            description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @ParametersDelegate
    private XmlDocumentParams xmlDocumentParams = new XmlDocumentParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format(source)
                .options(readerParams)
                .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
                .options(getConnectionParams().makeOptions())
                .options(writeDocumentParams.makeOptions())
                .options(xmlDocumentParams.makeOptions())
                .options(OptionsUtil.makeOptions(Options.WRITE_JSON_ROOT_NAME, jsonRootName))
                .mode(SaveMode.Append)
                .save();
    }
}
