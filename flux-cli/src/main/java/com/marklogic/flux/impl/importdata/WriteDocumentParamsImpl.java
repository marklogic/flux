/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import picocli.CommandLine;

import java.util.Map;

public class WriteDocumentParamsImpl extends WriteDocumentParams<WriteDocumentParamsImpl> {

    @CommandLine.Mixin
    private SplitterParams splitterParams = new SplitterParams();

    @Override
    public Map<String, String> makeOptions() {
        Map<String, String> options = super.makeOptions();
        options.putAll(splitterParams.makeOptions());
        return options;
    }
}
