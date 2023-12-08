package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReprocessCommand extends AbstractCommand {

    @Parameter(names = "--read-invoke")
    private String readInvoke;

    @Parameter(names = "--read-javascript")
    private String readJavascript;

    @Parameter(names = "--read-xquery")
    private String readXQuery;

    @Parameter(names = "--read-partitions-invoke")
    private String readPartitionsInvoke;

    @Parameter(names = "--read-partitions-javascript")
    private String readPartitionsJavascript;

    @Parameter(names = "--read-partitions-xquery")
    private String readPartitionsXQuery;

    @DynamicParameter(names = "-RV:", description = "Variables to pass to custom code for reading rows")
    private Map<String, String> readVars = new HashMap<>();

    @Parameter(names = "--write-invoke")
    private String writeInvoke;

    @Parameter(names = "--write-javascript")
    private String writeJavascript;

    @Parameter(names = "--write-xquery")
    private String writeXQuery;

    @Parameter(names = "--write-external-variable-name")
    private String writeExternalVariableName;

    @Parameter(names = "--write-external-variable-delimiter")
    private String writeExternalVariableDelimiter;

    @DynamicParameter(names = "-WV:", description = "Variables to pass to custom code for processing rows")
    private Map<String, String> writeVars = new HashMap<>();


    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        Map<String, String> readOptions = makeReadOptions(
            Options.READ_INVOKE, readInvoke,
            Options.READ_JAVASCRIPT, readJavascript,
            Options.READ_XQUERY, readXQuery,
            Options.READ_PARTITIONS_INVOKE, readPartitionsInvoke,
            Options.READ_PARTITIONS_JAVASCRIPT, readPartitionsJavascript,
            Options.READ_PARTITIONS_XQUERY, readPartitionsXQuery
        );
        readVars.forEach((key, value) -> readOptions.put(Options.READ_VARS_PREFIX + key, value));

        Map<String, String> writeOptions = makeWriteOptions(
            Options.WRITE_INVOKE, writeInvoke,
            Options.WRITE_JAVASCRIPT, writeJavascript,
            Options.WRITE_XQUERY, writeXQuery,
            Options.WRITE_EXTERNAL_VARIABLE_NAME, writeExternalVariableName,
            Options.WRITE_EXTERNAL_VARIABLE_DELIMITER, writeExternalVariableDelimiter
        );
        writeVars.forEach((key, value) -> writeOptions.put(Options.WRITE_VARS_PREFIX + key, value));

        session.read()
            .format(MARKLOGIC_CONNECTOR)
            .options(readOptions)
            .load()
            .write()
            .format(MARKLOGIC_CONNECTOR)
            .options(writeOptions)
            .mode(SaveMode.Append)
            .save();

        // TODO Can support preview here too.
        return Optional.empty();
    }

    public void setReadInvoke(String readInvoke) {
        this.readInvoke = readInvoke;
    }

    public void setReadJavascript(String readJavascript) {
        this.readJavascript = readJavascript;
    }

    public void setReadXQuery(String readXQuery) {
        this.readXQuery = readXQuery;
    }

    public void setReadPartitionsInvoke(String readPartitionsInvoke) {
        this.readPartitionsInvoke = readPartitionsInvoke;
    }

    public void setReadPartitionsJavascript(String readPartitionsJavascript) {
        this.readPartitionsJavascript = readPartitionsJavascript;
    }

    public void setReadPartitionsXQuery(String readPartitionsXQuery) {
        this.readPartitionsXQuery = readPartitionsXQuery;
    }

    public void setReadVars(Map<String, String> readVars) {
        this.readVars = readVars;
    }

    public void setWriteInvoke(String writeInvoke) {
        this.writeInvoke = writeInvoke;
    }

    public void setWriteJavascript(String writeJavascript) {
        this.writeJavascript = writeJavascript;
    }

    public void setWriteXQuery(String writeXQuery) {
        this.writeXQuery = writeXQuery;
    }

    public void setWriteExternalVariableName(String writeExternalVariableName) {
        this.writeExternalVariableName = writeExternalVariableName;
    }

    public void setWriteExternalVariableDelimiter(String writeExternalVariableDelimiter) {
        this.writeExternalVariableDelimiter = writeExternalVariableDelimiter;
    }

    public void setWriteVars(Map<String, String> writeVars) {
        this.writeVars = writeVars;
    }
}
