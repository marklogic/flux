package com.marklogic.newtool.command;

import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Parameters(
    commandDescription = "Read data from MarkLogic via custom code and reprocess it (often, but not necessarily, by writing data) via custom code.",
    parametersValidators = ReprocessCommand.ReprocessValidator.class
)
public class ReprocessCommand extends AbstractCommand {

    @Parameter(
        names = {"--readInvoke"},
        description = "The path to a module to invoke for reading data; the module must be in your application’s modules database."
    )
    private String readInvoke;

    @Parameter(
        names = {"--readJavascript"},
        description = "JavaScript code to execute for reading data."
    )
    private String readJavascript;

    @Parameter(
        names = {"--readXquery"},
        description = "XQuery code to execute for reading data."
    )
    private String readXquery;

    @Parameter(
        names = {"--readPartitionsInvoke"},
        description = "The path to a module to invoke to define partitions that are sent to your custom code for reading; the module must be in your application’s modules database."
    )
    private String readPartitionsInvoke;

    @Parameter(
        names = {"--readPartitionsJavascript"},
        description = "JavaScript code to execute to define partitions that are sent to your custom code for reading."
    )
    private String readPartitionsJavascript;

    @Parameter(
        names = {"--readPartitionsXquery"},
        description = "XQuery code to execute to define partitions that are sent to your custom code for reading."
    )
    private String readPartitionsXquery;

    @Parameter(
        names = "--readVar", variableArity = true,
        description = "Define variables to be sent to the code for reading data; e.g. '--readVar var1=value1'."
    )
    private List<String> readVars = new ArrayList<>();

    @Parameter(
        names = {"--writeInvoke"},
        description = "The path to a module to invoke for writing data; the module must be in your application’s modules database."
    )
    private String writeInvoke;

    @Parameter(
        names = {"--writeJavascript"},
        description = "JavaScript code to execute for writing data."
    )
    private String writeJavascript;

    @Parameter(
        names = {"--writeXquery"},
        description = "XQuery code to execute for writing data."
    )
    private String writeXquery;

    @Parameter(
        names = {"--externalVariableName"},
        description = "Name of the external variable in the custom code for writing that will be populated with each value read from MarkLogic."
    )
    private String externalVariableName = "URI";

    @Parameter(
        names = {"--externalVariableDelimiter"},
        description = "Delimiter used when multiple values are included in the external variable in the code for writing."
    )
    private String externalVariableDelimiter = ",";

    @Parameter(
        names = "--writeVar", variableArity = true,
        description = "Define variables to be sent to the code for writing data; e.g. '--writeVar var1=value1'."
    )
    private List<String> writeVars = new ArrayList<>();

    @Parameter(
        names = "--abortOnFailure", arity = 1,
        description = "Set to true to cause the command to abort when writing data causes an error."
    )
    private boolean abortOnFailure = true;

    @Parameter(
        names = "--batchSize",
        description = "The number of values sent to the code for writing data in a single call."
    )
    private Integer batchSize = 1;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(makeReadOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(makeWriteOptions())
            .mode(SaveMode.Append)
            .save();
    }

    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = OptionsUtil.makeOptions(
            Options.READ_INVOKE, readInvoke,
            Options.READ_JAVASCRIPT, readJavascript,
            Options.READ_XQUERY, readXquery,
            Options.READ_PARTITIONS_INVOKE, readPartitionsInvoke,
            Options.READ_PARTITIONS_JAVASCRIPT, readPartitionsJavascript,
            Options.READ_PARTITIONS_XQUERY, readPartitionsXquery
        );
        if (readVars != null) {
            readVars.forEach(readVar -> {
                int pos = readVar.indexOf("=");
                if (pos < 0) {
                    throw new IllegalArgumentException("Value of --readVar argument must be 'varName=varValue'; invalid value: " + readVar);
                }
                options.put(Options.READ_VARS_PREFIX + readVar.substring(0, pos), readVar.substring(pos + 1));
            });
        }
        return options;
    }

    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = OptionsUtil.makeOptions(
            Options.WRITE_INVOKE, writeInvoke,
            Options.WRITE_JAVASCRIPT, writeJavascript,
            Options.WRITE_XQUERY, writeXquery,
            Options.WRITE_EXTERNAL_VARIABLE_NAME, externalVariableName,
            Options.WRITE_EXTERNAL_VARIABLE_DELIMITER, externalVariableDelimiter,
            Options.WRITE_ABORT_ON_FAILURE, Boolean.toString(abortOnFailure),
            Options.WRITE_BATCH_SIZE, batchSize != null ? batchSize.toString() : null
        );
        if (writeVars != null) {
            writeVars.forEach(writeVar -> {
                int pos = writeVar.indexOf("=");
                if (pos < 0) {
                    throw new IllegalArgumentException("Value of --writeVar argument must be 'varName=varValue'; invalid value: " + writeVar);
                }
                options.put(Options.WRITE_VARS_PREFIX + writeVar.substring(0, pos), writeVar.substring(pos + 1));
            });
        }
        return options;
    }

    public static class ReprocessValidator implements IParametersValidator {

        @Override
        public void validate(Map<String, Object> params) throws ParameterException {
            int readCount = getCountOfNonNullParams(params, "--readInvoke", "--readJavascript", "--readXquery");
            if (readCount != 1) {
                throw new ParameterException("Must specify one of --readInvoke, --readJavascript, or --readXquery.");
            }
            int readPartitionsCount = getCountOfNonNullParams(params, "--readPartitionsInvoke", "--readPartitionsJavascript", "--readPartitionsXquery");
            if (readPartitionsCount > 1) {
                throw new ParameterException("Can only specify one of --readPartitionsInvoke, --readPartitionsJavascript, and --readPartitionsXquery.");
            }
            int writeCount = getCountOfNonNullParams(params, "--writeInvoke", "--writeJavascript", "--writeXquery");
            if (writeCount != 1) {
                throw new ParameterException("Must specify one of --writeInvoke, --writeJavascript, or --writeXquery.");
            }
        }

        private int getCountOfNonNullParams(Map<String, Object> params, String... paramNames) {
            int count = 0;
            for (String paramName : paramNames) {
                if (params.get(paramName) != null) {
                    count++;
                }
            }
            return count;
        }
    }
}
