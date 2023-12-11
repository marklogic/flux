package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.HashMap;
import java.util.Map;

public class WriteParams {

    @Parameter(names = "--collections", description = "Comma-delimited")
    private String collections;

    @Parameter(names = "--permissions")
    private String permissions = "new-tool-role,read,new-tool-role,update";

    @Parameter(names = "--uriTemplate")
    private String uriTemplate;

    @Parameter(names = "--uriPrefix")
    private String uriPrefix;

    @Parameter(names = "--uriSuffix")
    private String uriSuffix;

    @Parameter(names = "--writeInvoke")
    private String writeInvoke;

    @Parameter(names = "--writeJavascript")
    private String writeJavascript;

    @Parameter(names = "--writeXquery")
    private String writeXQuery;

    @Parameter(names = "--writeExternalVariableName")
    private String writeExternalVariableName;

    @Parameter(names = "--writeExternalVariableDelimiter")
    private String writeExternalVariableDelimiter;

    @DynamicParameter(names = "-WV:", description = "Variables to pass to custom code for processing rows")
    private Map<String, String> writeExternalVariables = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        options.put(Options.WRITE_PERMISSIONS, permissions);
        options.put(Options.WRITE_URI_PREFIX, uriPrefix);
        options.put(Options.WRITE_URI_SUFFIX, uriSuffix);
        options.put(Options.WRITE_URI_TEMPLATE, uriTemplate);
        options.put(Options.WRITE_COLLECTIONS, collections);
        options.put(Options.WRITE_INVOKE, writeInvoke);
        options.put(Options.WRITE_JAVASCRIPT, writeJavascript);
        options.put(Options.WRITE_XQUERY, writeXQuery);
        options.put(Options.WRITE_EXTERNAL_VARIABLE_NAME, writeExternalVariableName);
        options.put(Options.WRITE_EXTERNAL_VARIABLE_DELIMITER, writeExternalVariableDelimiter);
        writeExternalVariables.forEach((key, value) -> options.put(Options.WRITE_VARS_PREFIX + key, value));
        return options;
    }

    public String getCollections() {
        return collections;
    }

    public void setCollections(String collections) {
        this.collections = collections;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getUriPrefix() {
        return uriPrefix;
    }

    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    public String getUriSuffix() {
        return uriSuffix;
    }

    public void setUriSuffix(String uriSuffix) {
        this.uriSuffix = uriSuffix;
    }

    public String getWriteInvoke() {
        return writeInvoke;
    }

    public void setWriteInvoke(String writeInvoke) {
        this.writeInvoke = writeInvoke;
    }

    public String getWriteJavascript() {
        return writeJavascript;
    }

    public void setWriteJavascript(String writeJavascript) {
        this.writeJavascript = writeJavascript;
    }

    public String getWriteXQuery() {
        return writeXQuery;
    }

    public void setWriteXQuery(String writeXQuery) {
        this.writeXQuery = writeXQuery;
    }

    public String getWriteExternalVariableName() {
        return writeExternalVariableName;
    }

    public void setWriteExternalVariableName(String writeExternalVariableName) {
        this.writeExternalVariableName = writeExternalVariableName;
    }

    public String getWriteExternalVariableDelimiter() {
        return writeExternalVariableDelimiter;
    }

    public void setWriteExternalVariableDelimiter(String writeExternalVariableDelimiter) {
        this.writeExternalVariableDelimiter = writeExternalVariableDelimiter;
    }

    public Map<String, String> getWriteExternalVariables() {
        return writeExternalVariables;
    }

    public void setWriteExternalVariables(Map<String, String> writeExternalVariables) {
        this.writeExternalVariables = writeExternalVariables;
    }
}
