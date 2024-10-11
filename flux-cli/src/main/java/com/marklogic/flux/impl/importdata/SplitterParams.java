/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import org.jdom2.Namespace;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SplitterParams implements CommandLine.ITypeConverter<Namespace> {

    @CommandLine.Option(names = "--splitter-json-pointer")
    private List<String> jsonPointer = new ArrayList<>();

    @CommandLine.Option(names = "--splitter-xml-path")
    private String xmlPath;

    @CommandLine.Option(
        names = "--splitter-xml-namespace",
        converter = SplitterParams.class
    )
    private List<Namespace> xmlNamespaces = new ArrayList<>();

    @CommandLine.Option(names = "--splitter-max-chunk-size")
    private Integer maxChunkSize;

    @CommandLine.Option(names = "--splitter-max-overlap-size")
    private Integer maxOverlapSize;

    @CommandLine.Option(
        names = "--splitter-regex",
        description = "Regular expression for splitting the selected text into chunks."
    )
    private String regex;

    @CommandLine.Option(
        names = "--splitter-join-delimiter",
        description = "Delimiter for joining chunks split via --splitter-regex when two or more consecutive " +
            "chunks can be combined and still be smaller than the max chunk size."
    )
    private String joinDelimiter;

    @CommandLine.Option(names = "--splitter-text")
    private boolean text;

    public Map<String, String> makeOptions() {
        Map<String, String> options = OptionsUtil.makeOptions(
            Options.WRITE_SPLITTER_XML_PATH, xmlPath,
            Options.WRITE_SPLITTER_MAX_CHUNK_SIZE, OptionsUtil.integerOption(maxChunkSize),
            Options.WRITE_SPLITTER_MAX_OVERLAP_SIZE, OptionsUtil.integerOption(maxOverlapSize),
            Options.WRITE_SPLITTER_TEXT, text ? "true" : null,
            Options.WRITE_SPLITTER_REGEX, regex,
            Options.WRITE_SPLITTER_JOIN_DELIMITER, joinDelimiter
        );

        if (!jsonPointer.isEmpty()) {
            options.put(Options.WRITE_SPLITTER_JSON_POINTERS, jsonPointer.stream().collect(Collectors.joining("\n")));
        }

        xmlNamespaces.forEach(namespace ->
            options.put(Options.WRITE_SPLITTER_XML_NAMESPACE_PREFIX + namespace.getPrefix(), namespace.getURI()));

        return options;
    }

    @Override
    public Namespace convert(String value) {
        String[] tokens = value.split("=");
        if (tokens.length % 2 != 0) {
            throw new FluxException("The value must match the pattern prefix=namespaceURI");
        }
        return Namespace.getNamespace(tokens[0], tokens[1]);
    }
}
