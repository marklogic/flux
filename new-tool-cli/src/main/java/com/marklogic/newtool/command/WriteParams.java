package com.marklogic.newtool.command;

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

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        options.put(Options.WRITE_PERMISSIONS, permissions);
        options.put(Options.WRITE_URI_PREFIX, uriPrefix);
        options.put(Options.WRITE_URI_SUFFIX, uriSuffix);
        options.put(Options.WRITE_URI_TEMPLATE, uriTemplate);
        options.put(Options.WRITE_COLLECTIONS, collections);
        return options;
    }
}
