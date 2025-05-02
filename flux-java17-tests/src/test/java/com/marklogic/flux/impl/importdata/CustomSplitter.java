/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;
import java.util.Map;

public class CustomSplitter implements DocumentSplitter {

    private String textToReturn;

    public CustomSplitter(Map<String, String> options) {
        this.textToReturn = options.get("textToReturn");
        if (this.textToReturn == null) {
            this.textToReturn = "You passed in null!";
        }
    }

    @Override
    public List<TextSegment> split(Document document) {
        return List.of(new TextSegment(textToReturn, new Metadata()));
    }
}

