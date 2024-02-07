package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.Map;

/**
 * Not able to reuse {@code WriteDocumentParams} as we need alternate annotations for each of the fields so that
 * each "write" param can be prefixed with "--output". This therefore duplicates all the "write" params, as well as
 * all the connection params for writing as well. It relies on unit tests to ensure that the counts of these params
 * are the same to avoid a situation where e.g. a new param is added to {@code WriteDocumentParams} but not added here.
 */
@Parameters(commandDescription = "Copy documents from one to another database, which includes the originating database.")
public class CopyCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadDocumentParams readDocumentParams = new ReadDocumentParams();

    @Parameter(names = {"--outputClientUri"},
        description = "Defines a connection string as user:password@host:port; only usable when using digest or basic authentication."
    )
    private String clientUri;

    @Parameter(names = {"--outputHost"}, description = "The MarkLogic host to connect to.")
    private String host;

    @Parameter(names = "--outputPort", description = "Port of a MarkLogic REST API app server to connect to.")
    private Integer port;

    @Parameter(names = "--outputUsername", description = "Username when using 'digest' or 'basic' authentication.")
    private String username;

    @Parameter(names = "--outputPassword", description = "Password when using 'digest' or 'basic' authentication.", password = true)
    private String password;

    @Parameter(
        names = "--outputAbortOnFailure", arity = 1,
        description = "Set to true to cause an import to abort when a batch of documents cannot be written to MarkLogic."
    )
    private boolean abortOnFailure = true;

    @Parameter(
        names = "--outputBatchSize",
        description = "The number of documents written in a call to MarkLogic."
    )
    private Integer batchSize = 100;

    @Parameter(
        names = "--outputCollections",
        description = "Comma-delimited string of collection names to add to each document."
    )
    private String collections;

    @Parameter(
        names = "--outputPermissions",
        description = "Comma-delimited string of role names and capabilities to add to each document - e.g. role1,read,role2,update,role3,execute."
    )
    private String permissions;

    @Parameter(
        names = "--outputTemporalCollection",
        description = "Name of a temporal collection to assign to each document."
    )
    private String temporalCollection;

    @Parameter(
        names = "--outputThreadCount",
        description = "The number of threads used by each partition worker when writing batches of documents to MarkLogic."
    )
    private Integer threadCount = 4;

    @Parameter(
        names = "--outputTransform",
        description = "Name of a MarkLogic REST API transform to apply to each document."
    )
    private String transform;

    @Parameter(
        names = "--outputTransformParams",
        description = "Comma-delimited string of REST API transform parameter names and values - e.g. param1,value1,param2,value2."
    )
    private String transformParams;

    @Parameter(
        names = "--outputTransformParamsDelimiter",
        description = "Delimiter to use instead of a comma for the '--transformParams' parameter."
    )
    private String transformParamsDelimiter;

    @Parameter(
        names = "--outputUriPrefix",
        description = "String to prepend to each document URI."
    )
    private String uriPrefix;

    @Parameter(
        names = "--outputUriReplace",
        description = "Modify the URI for a document via a comma-delimited list of regular expression \n" +
            "and replacement string pairs - e.g. regex,'value',regex,'value'. Each replacement string must be enclosed by single quotes."
    )
    private String uriReplace;

    @Parameter(
        names = "--outputUriSuffix",
        description = "String to append to each document URI."
    )
    private String uriSuffix;

    @Parameter(
        names = "--outputUriTemplate",
        description = "String defining a template for constructing each document URI. " +
            "See https://marklogic.github.io/marklogic-spark-connector/writing.html for more information."
    )
    private String uriTemplate;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readDocumentParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteConnectionOptions())
            .options(makeWriteOptions())
            .mode(SaveMode.Append)
            .save();
    }

    protected Map<String, String> makeWriteConnectionOptions() {
        Map<String, String> writeConnectionOptions;
        if (clientUri != null && !clientUri.isEmpty()) {
            writeConnectionOptions = OptionsUtil.makeOptions(Options.CLIENT_URI, clientUri);
        } else {
            writeConnectionOptions = OptionsUtil.makeOptions(
                Options.CLIENT_HOST, host,
                Options.CLIENT_PORT, port != null ? port.toString() : null,
                Options.CLIENT_USERNAME, username,
                Options.CLIENT_PASSWORD, password
            );
        }

        // If user doesn't specify any "--output" connection options, then reuse the connection for reading data.
        if (writeConnectionOptions.isEmpty()) {
            writeConnectionOptions = getConnectionParams().makeOptions();
        }

        return writeConnectionOptions;
    }

    protected Map<String, String> makeWriteOptions() {
        return OptionsUtil.makeOptions(
            Options.WRITE_ABORT_ON_FAILURE, Boolean.toString(abortOnFailure),
            Options.WRITE_BATCH_SIZE, batchSize != null ? batchSize.toString() : null,
            Options.WRITE_COLLECTIONS, collections,
            Options.WRITE_PERMISSIONS, permissions,
            Options.WRITE_TEMPORAL_COLLECTION, temporalCollection,
            Options.WRITE_THREAD_COUNT, threadCount != null ? threadCount.toString() : null,
            Options.WRITE_TRANSFORM_NAME, transform,
            Options.WRITE_TRANSFORM_PARAMS, transformParams,
            Options.WRITE_TRANSFORM_PARAMS_DELIMITER, transformParamsDelimiter,
            Options.WRITE_URI_PREFIX, uriPrefix,
            Options.WRITE_URI_REPLACE, uriReplace,
            Options.WRITE_URI_SUFFIX, uriSuffix,
            Options.WRITE_URI_TEMPLATE, uriTemplate
        );
    }
}
