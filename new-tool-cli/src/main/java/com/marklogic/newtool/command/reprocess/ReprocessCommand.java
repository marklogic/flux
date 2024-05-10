package com.marklogic.newtool.command.reprocess;

import com.beust.jcommander.*;
import com.marklogic.newtool.api.Reprocessor;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Parameters(
    commandDescription = "Read data from MarkLogic via custom code and reprocess it (often, but not necessarily, by writing data) via custom code.",
    parametersValidators = ReprocessCommand.ReprocessValidator.class
)
public class ReprocessCommand extends AbstractCommand<Reprocessor> implements Reprocessor {

    @ParametersDelegate
    protected ReadParams readParams = new ReadParams();

    @ParametersDelegate
    protected WriteParams writeParams = new WriteParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.get())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeParams.get())
            .mode(SaveMode.Append)
            .save();
    }

    public static class ReprocessValidator implements IParametersValidator {

        @Override
        public void validate(Map<String, Object> params) throws ParameterException {
            validateReadParams(params);
            validatePartitionParams(params);
            validateWriteParams(params);
        }

        private void validateReadParams(Map<String, Object> params) {
            String[] readParams = new String[]{
                "--readInvoke", "--readJavascript", "--readXquery", "--readJavascriptFile", "--readXqueryFile"
            };
            if (getCountOfNonNullParams(params, readParams) != 1) {
                throw new ParameterException(makeErrorMessage("Must specify one of ", readParams));
            }
        }

        private void validatePartitionParams(Map<String, Object> params) {
            String[] partitionParams = new String[]{
                "--readPartitionsInvoke", "--readPartitionsJavascript", "--readPartitionsXquery",
                "--readPartitionsJavascriptFile", "--readPartitionsXqueryFile"
            };
            if (getCountOfNonNullParams(params, partitionParams) > 1) {
                throw new ParameterException(makeErrorMessage("Can only specify one of ", partitionParams));
            }
        }

        private void validateWriteParams(Map<String, Object> params) {
            if (params.get("--preview") != null) {
                return;
            }
            String[] writeParams = new String[]{
                "--writeInvoke", "--writeJavascript", "--writeXquery", "--writeJavascriptFile", "--writeXqueryFile"
            };
            if (getCountOfNonNullParams(params, writeParams) != 1) {
                throw new ParameterException(makeErrorMessage("Must specify one of ", writeParams));
            }
        }

        private String makeErrorMessage(String preamble, String... args) {
            List<String> list = Arrays.asList(args);
            String str = list.subList(0, list.size() - 1).stream().collect(Collectors.joining(", "));
            return preamble + str + ", or " + list.get(list.size() - 1) + ".";
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

    public static class ReadParams implements Supplier<Map<String, String>>, ReadOptions {

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
            names = {"--readJavascriptFile"},
            description = "Local file containing JavaScript code to execute for reading data."
        )
        private String readJavascriptFile;

        @Parameter(
            names = {"--readXquery"},
            description = "XQuery code to execute for reading data."
        )
        private String readXquery;

        @Parameter(
            names = {"--readXqueryFile"},
            description = "Local file containing XQuery code to execute for reading data."
        )
        private String readXqueryFile;

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
            names = {"--readPartitionsJavascriptFile"},
            description = "Local file containing JavaScript code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsJavascriptFile;

        @Parameter(
            names = {"--readPartitionsXquery"},
            description = "XQuery code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsXquery;

        @Parameter(
            names = {"--readPartitionsXqueryFile"},
            description = "Local file containing XQuery code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsXqueryFile;

        @Parameter(
            names = "--readVar", variableArity = true,
            description = "Define variables to be sent to the code for reading data; e.g. '--readVar var1=value1'."
        )
        private List<String> readVars = new ArrayList<>();

        @Override
        public Map<String, String> get() {
            Map<String, String> options = OptionsUtil.makeOptions(
                Options.READ_INVOKE, readInvoke,
                Options.READ_JAVASCRIPT, readJavascript,
                Options.READ_JAVASCRIPT_FILE, readJavascriptFile,
                Options.READ_XQUERY, readXquery,
                Options.READ_XQUERY_FILE, readXqueryFile,
                Options.READ_PARTITIONS_INVOKE, readPartitionsInvoke,
                Options.READ_PARTITIONS_JAVASCRIPT, readPartitionsJavascript,
                Options.READ_PARTITIONS_JAVASCRIPT_FILE, readPartitionsJavascriptFile,
                Options.READ_PARTITIONS_XQUERY, readPartitionsXquery,
                Options.READ_PARTITIONS_XQUERY_FILE, readPartitionsXqueryFile
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

        @Override
        public ReadOptions invoke(String modulePath) {
            this.readInvoke = modulePath;
            return this;
        }

        @Override
        public ReadOptions javascript(String query) {
            this.readJavascript = query;
            return this;
        }

        @Override
        public ReadOptions javascriptFile(String path) {
            this.readJavascriptFile = path;
            return this;
        }

        @Override
        public ReadOptions xquery(String query) {
            this.readXquery = query;
            return this;
        }

        @Override
        public ReadOptions xqueryFile(String path) {
            this.readXqueryFile = path;
            return this;
        }

        @Override
        public ReadOptions partitionsInvoke(String modulePath) {
            this.readPartitionsInvoke = modulePath;
            return this;
        }

        @Override
        public ReadOptions partitionsJavascript(String query) {
            this.readPartitionsJavascript = query;
            return this;
        }

        @Override
        public ReadOptions partitionsJavascriptFile(String path) {
            this.readPartitionsJavascriptFile = path;
            return this;
        }

        @Override
        public ReadOptions partitionsXquery(String query) {
            this.readPartitionsXquery = query;
            return this;
        }

        @Override
        public ReadOptions partitionsXqueryFile(String path) {
            this.readPartitionsXqueryFile = path;
            return this;
        }

        @Override
        public ReadOptions vars(Map<String, String> namesAndValues) {
            this.readVars = namesAndValues.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());
            return this;
        }
    }

    public static class WriteParams implements Supplier<Map<String, String>>, WriteOptions {

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
            names = {"--writeJavascriptFile"},
            description = "Local file containing JavaScript code to execute for writing data."
        )
        private String writeJavascriptFile;

        @Parameter(
            names = {"--writeXquery"},
            description = "XQuery code to execute for writing data."
        )
        private String writeXquery;

        @Parameter(
            names = {"--writeXqueryFile"},
            description = "Local file containing XQuery code to execute for writing data."
        )
        private String writeXqueryFile;

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
            names = "--abortOnWriteFailure",
            description = "Include this option to cause the command to fail when a batch of documents cannot be written to MarkLogic."
        )
        private Boolean abortOnWriteFailure;

        @Parameter(
            names = "--batchSize",
            description = "The number of values sent to the code for writing data in a single call."
        )
        private Integer batchSize = 1;

        @Override
        public Map<String, String> get() {
            Map<String, String> options = OptionsUtil.makeOptions(
                Options.WRITE_INVOKE, writeInvoke,
                Options.WRITE_JAVASCRIPT, writeJavascript,
                Options.WRITE_JAVASCRIPT_FILE, writeJavascriptFile,
                Options.WRITE_XQUERY, writeXquery,
                Options.WRITE_XQUERY_FILE, writeXqueryFile,
                Options.WRITE_EXTERNAL_VARIABLE_NAME, externalVariableName,
                Options.WRITE_EXTERNAL_VARIABLE_DELIMITER, externalVariableDelimiter,
                Options.WRITE_ABORT_ON_FAILURE, abortOnWriteFailure != null ? Boolean.toString(abortOnWriteFailure) : "false",
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

        @Override
        public WriteOptions invoke(String modulePath) {
            this.writeInvoke = modulePath;
            return this;
        }

        @Override
        public WriteOptions javascript(String query) {
            this.writeJavascript = query;
            return this;
        }

        @Override
        public WriteOptions javascriptFile(String path) {
            this.writeJavascriptFile = path;
            return this;
        }

        @Override
        public WriteOptions xquery(String query) {
            this.writeXquery = query;
            return this;
        }

        @Override
        public WriteOptions xqueryFile(String path) {
            this.writeXqueryFile = path;
            return this;
        }

        @Override
        public WriteOptions externalVariableName(String name) {
            this.externalVariableName = name;
            return this;
        }

        @Override
        public WriteOptions externalVariableDelimiter(String delimiter) {
            this.externalVariableDelimiter = delimiter;
            return this;
        }

        @Override
        public WriteOptions vars(Map<String, String> namesAndValues) {
            this.writeVars = namesAndValues.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());
            return this;
        }

        @Override
        public WriteOptions abortOnWriteFailure(Boolean value) {
            this.abortOnWriteFailure = value;
            return this;
        }

        @Override
        public WriteOptions batchSize(Integer batchSize) {
            this.batchSize = batchSize;
            return this;
        }
    }

    @Override
    public Reprocessor readItems(Consumer<ReadOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public Reprocessor writeItems(Consumer<WriteOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
