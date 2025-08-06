/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.reprocess;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.Reprocessor;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "reprocess",
    description = "Read any data from MarkLogic via custom code and reprocess it via custom code."
)
public class ReprocessCommand extends AbstractCommand<Reprocessor> implements Reprocessor {

    @CommandLine.Mixin
    protected ReadParams readParams = new ReadParams();

    @CommandLine.Mixin
    protected WriteParams writeParams = new WriteParams();

    @Override
    protected void validateDuringApiUsage() {
        readParams.validateReader();
        readParams.validatePartitionReader();
        writeParams.validateWriter();
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.get())
            .load();
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        // Must implement this so that the repartition will occur when using the API.
        return writeParams.threadCount > 0 ? dataset.repartition(writeParams.threadCount) : dataset;
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeParams.get())
            .mode(SaveMode.Append)
            .save();
    }

    @Override
    protected String getCustomSparkMasterUrl() {
        return writeParams.threadCount > 0 ? SparkUtil.makeSparkMasterUrl(writeParams.threadCount) : null;
    }

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        super.validateCommandLineOptions(parseResult);
        validateReadParams(parseResult);
        validatePartitionParams(parseResult);
        validateWriteParams(parseResult);
    }

    private void validateReadParams(CommandLine.ParseResult parseResult) {
        String[] options = new String[]{
            "--read-invoke", "--read-javascript", "--read-xquery", "--read-javascript-file", "--read-xquery-file"
        };
        if (getCountOfNonNullParams(parseResult, options) != 1) {
            throw new FluxException(makeErrorMessage("Must specify one of ", options));
        }
    }

    private void validatePartitionParams(CommandLine.ParseResult parseResult) {
        String[] options = new String[]{
            "--read-partitions-invoke", "--read-partitions-javascript", "--read-partitions-xquery",
            "--read-partitions-javascript-file", "--read-partitions-xquery-file"
        };
        if (getCountOfNonNullParams(parseResult, options) > 1) {
            throw new FluxException(makeErrorMessage("Can only specify one of ", options));
        }
    }

    private void validateWriteParams(CommandLine.ParseResult parseResult) {
        String[] options = new String[]{
            "--write-invoke", "--write-javascript", "--write-xquery", "--write-javascript-file", "--write-xquery-file"
        };
        if (getCountOfNonNullParams(parseResult, options) != 1) {
            throw new FluxException(makeErrorMessage("Must specify one of ", options));
        }
    }

    private String makeErrorMessage(String preamble, String... args) {
        List<String> list = Arrays.asList(args);
        String str = list.subList(0, list.size() - 1).stream().collect(Collectors.joining(", "));
        return preamble + str + ", or " + list.get(list.size() - 1) + ".";
    }

    private int getCountOfNonNullParams(CommandLine.ParseResult parseResult, String... options) {
        int count = 0;
        Objects.requireNonNull(parseResult);
        for (String option : options) {
            if (parseResult.subcommand() != null && parseResult.subcommand().hasMatchedOption(option)) {
                count++;
            }
        }
        return count;
    }

    public static class ReadParams implements Supplier<Map<String, String>>, ReadOptions {

        @CommandLine.Option(
            names = {"--read-invoke"},
            description = "The path to a module to invoke for reading data; the module must be in your application’s modules database."
        )
        private String readInvoke;

        @CommandLine.Option(
            names = {"--read-javascript"},
            description = "JavaScript code to execute for reading data."
        )
        private String readJavascript;

        @CommandLine.Option(
            names = {"--read-javascript-file"},
            description = "Local file containing JavaScript code to execute for reading data."
        )
        private String readJavascriptFile;

        @CommandLine.Option(
            names = {"--read-xquery"},
            description = "XQuery code to execute for reading data."
        )
        private String readXquery;

        @CommandLine.Option(
            names = {"--read-xquery-file"},
            description = "Local file containing XQuery code to execute for reading data."
        )
        private String readXqueryFile;

        @CommandLine.Option(
            names = {"--read-partitions-invoke"},
            description = "The path to a module to invoke to define partitions that are sent to your custom code for reading; the module must be in your application’s modules database."
        )
        private String readPartitionsInvoke;

        @CommandLine.Option(
            names = {"--read-partitions-javascript"},
            description = "JavaScript code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsJavascript;

        @CommandLine.Option(
            names = {"--read-partitions-javascript-file"},
            description = "Local file containing JavaScript code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsJavascriptFile;

        @CommandLine.Option(
            names = {"--read-partitions-xquery"},
            description = "XQuery code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsXquery;

        @CommandLine.Option(
            names = {"--read-partitions-xquery-file"},
            description = "Local file containing XQuery code to execute to define partitions that are sent to your custom code for reading."
        )
        private String readPartitionsXqueryFile;

        @CommandLine.Option(
            names = "--read-var", arity = "*",
            description = "Define variables to be sent to the code for reading data; e.g. '--read-var var1=value1'."
        )
        private List<String> readVars = new ArrayList<>();

        @CommandLine.Option(
            names = "--log-read-progress",
            description = "Log a count of total items read every time this many items are read."
        )
        private int progressInterval = 10000;

        public void validateReader() {
            Map<String, String> options = get();
            if (Stream.of(Options.READ_INVOKE, Options.READ_JAVASCRIPT, Options.READ_JAVASCRIPT_FILE,
                Options.READ_XQUERY, Options.READ_XQUERY_FILE).noneMatch(options::containsKey)) {
                throw new FluxException("Must specify either JavaScript code, XQuery code, or an invokable module for reading from MarkLogic");
            }
        }

        public void validatePartitionReader() {
            Map<String, String> options = get();
            long count = Stream.of(Options.READ_PARTITIONS_JAVASCRIPT, Options.READ_PARTITIONS_JAVASCRIPT_FILE,
                    Options.READ_PARTITIONS_XQUERY, Options.READ_PARTITIONS_XQUERY_FILE, Options.READ_PARTITIONS_INVOKE)
                .filter(options::containsKey).count();
            if (count > 1) {
                throw new FluxException("Can only specify one approach for defining partitions that are sent to the code for reading from MarkLogic");
            }
        }

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
                Options.READ_PARTITIONS_XQUERY_FILE, readPartitionsXqueryFile,
                Options.READ_LOG_PROGRESS, OptionsUtil.intOption(progressInterval)
            );

            if (readVars != null) {
                readVars.forEach(readVar -> {
                    int pos = readVar.indexOf("=");
                    if (pos < 0) {
                        throw new IllegalArgumentException("Value of --read-var argument must be 'varName=varValue'; invalid value: " + readVar);
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

        @Override
        public ReadOptions logProgress(int interval) {
            this.progressInterval = interval;
            return this;
        }
    }

    public static class WriteParams implements Supplier<Map<String, String>>, WriteOptions {

        @CommandLine.Option(
            names = {"--write-invoke"},
            description = "The path to a module to invoke for writing data; the module must be in your application’s modules database."
        )
        private String writeInvoke;

        @CommandLine.Option(
            names = {"--write-javascript"},
            description = "JavaScript code to execute for writing data."
        )
        private String writeJavascript;

        @CommandLine.Option(
            names = {"--write-javascript-file"},
            description = "Local file containing JavaScript code to execute for writing data."
        )
        private String writeJavascriptFile;

        @CommandLine.Option(
            names = {"--write-xquery"},
            description = "XQuery code to execute for writing data."
        )
        private String writeXquery;

        @CommandLine.Option(
            names = {"--write-xquery-file"},
            description = "Local file containing XQuery code to execute for writing data."
        )
        private String writeXqueryFile;

        @CommandLine.Option(
            names = {"--external-variable-name"},
            description = "Name of the external variable in the custom code for writing that will be populated with each value read from MarkLogic."
        )
        private String externalVariableName = "URI";

        @CommandLine.Option(
            names = {"--external-variable-delimiter"},
            description = "Delimiter used when multiple values are included in the external variable in the code for writing."
        )
        private String externalVariableDelimiter = ",";

        @CommandLine.Option(
            names = "--write-var", arity = "*",
            description = "Define variables to be sent to the code for writing data; e.g. '--write-var var1=value1'."
        )
        private List<String> writeVars = new ArrayList<>();

        @CommandLine.Option(
            names = "--abort-on-write-failure",
            description = "Causes the command to fail when a batch of documents cannot be written to MarkLogic."
        )
        private boolean abortOnWriteFailure;

        @CommandLine.Option(
            names = "--batch-size",
            description = "The number of values sent to the code for writing data in a single call."
        )
        private int batchSize = 1;

        @CommandLine.Option(
            names = "--log-progress",
            description = "Log a count of total items processed every time this many items are processed."
        )
        private int progressInterval = 10000;

        @CommandLine.Option(
            names = "--thread-count",
            description = "The number of threads for processing items. This is an alias for '--reprocess' as it " +
                "defines the number of Spark partitions used for processing items."
        )
        private int threadCount = 16;

        public void validateWriter() {
            Map<String, String> options = get();
            if (Stream.of(Options.WRITE_INVOKE, Options.WRITE_JAVASCRIPT, Options.WRITE_JAVASCRIPT_FILE,
                Options.WRITE_XQUERY, Options.WRITE_XQUERY_FILE).noneMatch(options::containsKey)) {
                throw new FluxException("Must specify either JavaScript code, XQuery code, or an invokable module for writing to MarkLogic");
            }
        }

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
                Options.WRITE_ABORT_ON_FAILURE, abortOnWriteFailure ? "true" : "false",
                Options.WRITE_BATCH_SIZE, OptionsUtil.intOption(batchSize),
                Options.WRITE_LOG_PROGRESS, OptionsUtil.intOption(progressInterval)
            );

            if (writeVars != null) {
                writeVars.forEach(writeVar -> {
                    int pos = writeVar.indexOf("=");
                    if (pos < 0) {
                        throw new IllegalArgumentException("Value of --write-var argument must be 'varName=varValue'; invalid value: " + writeVar);
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
        public WriteOptions abortOnWriteFailure(boolean value) {
            this.abortOnWriteFailure = value;
            return this;
        }

        @Override
        public WriteOptions batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        @Override
        public WriteOptions logProgress(int interval) {
            this.progressInterval = interval;
            return this;
        }

        @Override
        public WriteOptions threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }
    }

    @Override
    public Reprocessor from(Consumer<ReadOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public Reprocessor to(Consumer<WriteOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}
