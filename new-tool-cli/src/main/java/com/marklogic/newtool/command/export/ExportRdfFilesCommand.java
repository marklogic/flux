package com.marklogic.newtool.command.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.AtLeastOneValidator;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.newtool.command.S3Params;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.Map;

@Parameters(
    commandDescription = "Read triples from MarkLogic and write them to a local filesystem, HDFS, or S3.",
    parametersValidators = ExportRdfFilesCommand.Validator.class
)
public class ExportRdfFilesCommand extends AbstractCommand {

    public static class Validator extends AtLeastOneValidator {
        public Validator() {
            super("--graphs", "--query", "--uris", "--stringQuery", "--collections", "--directory");
        }
    }

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @Parameter(names = "--mode", converter = SaveModeConverter.class,
        description = "Specifies how data is written if the path already exists. " +
            "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
    private SaveMode saveMode = SaveMode.Append;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Parameter(names = "--uris", description = "Newline-delimited sequence of document URIs to retrieve. Can be combined " +
        "with --collections, --directory, and --stringQuery. If specified, --query will be ignored.")
    private String uris;

    @Parameter(names = "--stringQuery", description = "A query utilizing the MarkLogic search grammar; " +
        "see https://docs.marklogic.com/guide/search-dev/string-query for more information.")
    private String stringQuery;

    @Parameter(names = "--query", description = "A JSON or XML representation of a structured query, serialized CTS query, or combined query. " +
        "See https://docs.marklogic.com/guide/rest-dev/search#id_49329 for more information.")
    private String query;

    @Parameter(names = "--graphs", description = "Comma-delimited sequence of MarkLogic graph names by which to constrain the query.")
    private String graphs;

    @Parameter(names = "--collections", description = "Comma-delimited sequence of collection names by which to constrain the query.")
    private String collections;

    @Parameter(names = "--directory", description = "Database directory by which to constrain the query.")
    private String directory;

    @Parameter(names = "--options", description = "Name of a set of MarkLogic REST API search options.")
    private String options;

    @Parameter(names = "--batchSize", description = "Number of documents to retrieve in each call to MarkLogic.")
    private Integer batchSize = 100;

    @Parameter(names = "--partitionsPerForest", description = "Number of partition readers to create for each forest.")
    private Integer partitionsPerForest = 4;

    @Parameter(names = "--format", description = "RDF file format; supported values are 'nq', 'nt', 'rdfthrift', 'trig', 'trix', and 'ttl'.")
    private String format = "ttl";

    @Parameter(names = "--graphOverride", description = "Semantic graph to include in each file. Only allowed when '--format' is 'nq' or 'trig'.")
    private String graphOverride;

    @Parameter(names = "--fileCount", description = "Specifies how many RDF files should be written.")
    private Integer fileCount = 1;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (fileCount != null && fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(makeReadOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteOptions())
            .mode(saveMode)
            .save(path);
    }

    protected Map<String, String> makeReadOptions() {
        return OptionsUtil.makeOptions(
            Options.READ_TRIPLES_GRAPHS, graphs,
            Options.READ_TRIPLES_COLLECTIONS, collections,
            Options.READ_TRIPLES_OPTIONS, options,
            Options.READ_TRIPLES_DIRECTORY, directory,
            Options.READ_TRIPLES_QUERY, query,
            Options.READ_TRIPLES_STRING_QUERY, stringQuery,
            Options.READ_TRIPLES_URIS, uris,
            Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST, partitionsPerForest.toString(),
            Options.READ_BATCH_SIZE, batchSize.toString()
        );
    }

    protected Map<String, String> makeWriteOptions() {
        return OptionsUtil.makeOptions(
            Options.WRITE_RDF_FILES_FORMAT, format,
            Options.WRITE_RDF_FILES_GRAPH, graphOverride
        );
    }
}
