/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.SplitterOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SplitterParams implements SplitterOptions {

    @CommandLine.Option(
        names = "--splitter-json-pointer",
        description = "One or more JSON Pointer expressions that specify the text to be split in each document."
    )
    private List<String> jsonPointer = new ArrayList<>();

    @CommandLine.Option(
        names = "--splitter-xpath",
        description = "An XPath expression that specifies the text to be split in each XML document."
    )
    private String xpath;

    @CommandLine.Option(
        names = {"-X"},
        description = "Specify one or more XML namespace prefix and URI pairs to be used with options that " +
            "require XPath expressions; e.g. -Xex=org:example ."
    )
    private Map<String, String> xmlNamespaces = new HashMap<>();

    @CommandLine.Option(
        names = "--splitter-max-chunk-size",
        description = "The maximum size in characters of each chunk that is split from the selected text."
    )
    private Integer maxChunkSize;

    @CommandLine.Option(
        names = "--splitter-max-overlap-size",
        description = "The maximum overlap size in characters between two consecutive chunks."
    )
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

    @CommandLine.Option(
        names = "--splitter-text",
        description = "Specifies that each document is a text document and thus all of the text in the document should be split."
    )
    private boolean text;

    @CommandLine.Option(
        names = "--splitter-custom-class",
        description = "Class name of a custom langchain4j DocumentSplitter implementation to use for splitting text."
    )
    private String customClass;

    @CommandLine.Option(
        names = "-S",
        description = "Key/value pairs, delimited by an equals sign, that are passed to the constructor of the " +
            "class specified by --splitter-custom-class - e.g. -Skey=value."
    )
    private Map<String, String> customClassOptions = new HashMap<>();

    @CommandLine.Option(
        names = "--splitter-sidecar-max-chunks",
        description = "Maximum number of chunks to write to a sidecar document. If not specified or set to zero, " +
            "chunks will be written to the source document."
    )
    private int maxChunks;

    @CommandLine.Option(
        names = "--splitter-sidecar-document-type",
        description = "Type of sidecar documents to write. " + OptionsUtil.VALID_VALUES_DESCRIPTION
    )
    private ChunkDocumentType documentType;

    @CommandLine.Option(
        names = "--splitter-sidecar-collections",
        description = "Comma-delimited sequence of collection names to add to each chunk document - e.g. collection1,collection2."
    )
    private String collections;

    @CommandLine.Option(
        names = "--splitter-sidecar-permissions",
        description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to each chunk document - e.g. role1,read,role2,update,role3,execute."
    )
    private String permissions;

    @CommandLine.Option(
        names = "--splitter-sidecar-root-name",
        description = "Name of a root field to add to each JSON sidecar document, or name of the root element for each XML sidecar document."
    )
    private String rootName;

    @CommandLine.Option(
        names = "--splitter-sidecar-uri-prefix",
        description = "String to prepend to each sidecar document URI. If set, a UUID will be generated and appended " +
            "to this prefix."
    )
    private String uriPrefix;

    @CommandLine.Option(
        names = "--splitter-sidecar-uri-suffix",
        description = "String to append to each sidecar document URI. If set, a UUID will be generated and prepended " +
            "to this suffix."
    )
    private String uriSuffix;

    @CommandLine.Option(
        names = "--splitter-sidecar-xml-namespace",
        description = "Namespace for the root element of chunk XML documents."
    )
    private String xmlNamespace;

    public Map<String, String> makeOptions() {
        Map<String, String> options = OptionsUtil.makeOptions(
            Options.WRITE_SPLITTER_XPATH, xpath,
            Options.WRITE_SPLITTER_MAX_CHUNK_SIZE, OptionsUtil.integerOption(maxChunkSize),
            Options.WRITE_SPLITTER_MAX_OVERLAP_SIZE, OptionsUtil.integerOption(maxOverlapSize),
            Options.WRITE_SPLITTER_TEXT, text ? "true" : null,
            Options.WRITE_SPLITTER_REGEX, regex,
            Options.WRITE_SPLITTER_JOIN_DELIMITER, joinDelimiter,
            Options.WRITE_SPLITTER_CUSTOM_CLASS, customClass,
            Options.WRITE_SPLITTER_SIDECAR_MAX_CHUNKS, OptionsUtil.intOption(maxChunks),
            Options.WRITE_SPLITTER_SIDECAR_DOCUMENT_TYPE, documentType != null ? documentType.name() : null,
            Options.WRITE_SPLITTER_SIDECAR_COLLECTIONS, collections,
            Options.WRITE_SPLITTER_SIDECAR_PERMISSIONS, permissions,
            Options.WRITE_SPLITTER_SIDECAR_ROOT_NAME, rootName,
            Options.WRITE_SPLITTER_SIDECAR_URI_PREFIX, uriPrefix,
            Options.WRITE_SPLITTER_SIDECAR_URI_SUFFIX, uriSuffix,
            Options.WRITE_SPLITTER_SIDECAR_XML_NAMESPACE, xmlNamespace
        );

        if (!jsonPointer.isEmpty()) {
            options.put(Options.WRITE_SPLITTER_JSON_POINTERS, jsonPointer.stream().collect(Collectors.joining("\n")));
        }

        xmlNamespaces.entrySet().stream().forEach(entry ->
            options.put(Options.XPATH_NAMESPACE_PREFIX + entry.getKey(), entry.getValue()));

        customClassOptions.forEach((key, value) ->
            options.put(Options.WRITE_SPLITTER_CUSTOM_CLASS_OPTION_PREFIX + key, value)
        );

        return options;
    }

    @Override
    public SplitterOptions jsonPointers(String... jsonPointers) {
        this.jsonPointer = Arrays.asList(jsonPointers);
        return this;
    }

    @Override
    public SplitterOptions xpath(String xpath) {
        this.xpath = xpath;
        return this;
    }

    @Override
    public SplitterOptions xmlNamespaces(String... prefixesAndUris) {
        this.xmlNamespaces = new HashMap<>();
        if (prefixesAndUris.length % 2 != 0) {
            throw new FluxException("Must specify an equal number of namespace prefixes and URIs.");
        }
        for (int i = 0; i <= prefixesAndUris.length - 1; i += 2) {
            this.xmlNamespaces.put(prefixesAndUris[i], prefixesAndUris[i + 1]);
        }
        return this;
    }

    @Override
    public SplitterOptions maxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    @Override
    public SplitterOptions maxOverlapSize(int maxOverlapSize) {
        this.maxOverlapSize = maxOverlapSize;
        return this;
    }

    @Override
    public SplitterOptions regex(String regex) {
        this.regex = regex;
        return this;
    }

    @Override
    public SplitterOptions joinDelimiter(String joinDelimiter) {
        this.joinDelimiter = joinDelimiter;
        return this;
    }

    @Override
    public SplitterOptions text() {
        this.text = true;
        return this;
    }

    @Override
    public SplitterOptions documentSplitterClassName(String documentSplitterClassName) {
        this.customClass = documentSplitterClassName;
        return this;
    }

    @Override
    public SplitterOptions documentSplitterClassOptions(Map<String, String> options) {
        this.customClassOptions = options;
        return this;
    }

    @Override
    public SplitterOptions outputMaxChunks(int maxChunks) {
        this.maxChunks = maxChunks;
        return this;
    }

    @Override
    public SplitterOptions outputDocumentType(ChunkDocumentType documentType) {
        this.documentType = documentType;
        return this;
    }

    @Override
    public SplitterOptions outputCollections(String... collections) {
        this.collections = Stream.of(collections).collect(Collectors.joining(","));
        return this;
    }

    @Override
    public SplitterOptions outputPermissionsString(String rolesAndCapabilities) {
        this.permissions = rolesAndCapabilities;
        return this;
    }

    @Override
    public SplitterOptions outputRootName(String rootName) {
        this.rootName = rootName;
        return this;
    }

    @Override
    public SplitterOptions outputUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
        return this;
    }

    @Override
    public SplitterOptions outputUriSuffix(String uriSuffix) {
        this.uriSuffix = uriSuffix;
        return this;
    }

    @Override
    public SplitterOptions outputXmlNamespace(String xmlNamespace) {
        this.xmlNamespace = xmlNamespace;
        return this;
    }
}
