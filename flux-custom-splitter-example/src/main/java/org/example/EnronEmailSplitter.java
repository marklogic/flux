/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import dev.langchain4j.data.document.DefaultDocument;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Splits the text in each email in the Enron email dataset - https://www.loc.gov/item/2018487913/ - by first
 * attempting to remove the headers from each email. The splitting is then delegated to the recursive splitter in
 * langchain4j.
 */
public class EnronEmailSplitter implements DocumentSplitter {

    private final DocumentSplitter documentSplitter;

    public EnronEmailSplitter(Map<String, String> options) {
        int maxChunkSize = options.containsKey("maxChunkSize") ? Integer.parseInt(options.get("maxChunkSize")) : 1000;
        int maxOverlapSize = options.containsKey("maxOverlapSize") ? Integer.parseInt(options.get("maxOverlapSize")) : 0;
        this.documentSplitter = DocumentSplitters.recursive(maxChunkSize, maxOverlapSize);
    }

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();
        String textToSplit = removeEmailHeaders(text);
        return this.documentSplitter.split(new DefaultDocument(textToSplit));
    }

    /**
     * This uses a simple approach to only include lines after all the email headers are assumed to have been read. It
     * is not intended to be 100% guaranteed to remove the email headers, but is good enough for the main purpose of
     * this project which is demonstrating a custom splitter.
     */
    private String removeEmailHeaders(String text) {
        StringBuilder builder = new StringBuilder();
        boolean startAddingLines = false;
        try (Scanner scanner = new Scanner(text)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.trim().isEmpty() && !line.contains(":")) {
                    startAddingLines = true;
                }
                if (startAddingLines) {
                    builder.append(line);
                }
            }
        }
        // Example of email that never adds lines - an email with a single sentence containing a colon.
        return startAddingLines ? builder.toString() : text;
    }
}
