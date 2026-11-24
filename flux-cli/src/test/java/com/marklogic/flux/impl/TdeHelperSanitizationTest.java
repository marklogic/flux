/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.flux.impl.importdata.TdeParams;
import com.marklogic.spark.Util;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TdeHelperSanitizationTest {

    @Test
    void previewAppliesFlagOnlyToSparkColumns() throws Exception {
        TdeParams params = new TdeParams();
        new CommandLine(params).parseArgs("--tde-schema", "schema", "--tde-view", "view", "--tde-preview",
            "--sanitize-tde-column-names", "--tde-support-incremental-write");
        TdeHelper helper = new TdeHelper(params, null, null, null, "hash/name");
        StructType schema = new StructType().add("first/name", DataTypes.StringType);

        Logger logger = (Logger) LogManager.getLogger(Util.MAIN_LOGGER.getName());
        Level previousLevel = logger.getLevel();
        List<String> messages = new ArrayList<>();
        AbstractAppender appender = new AbstractAppender("tde-sanitization-test", null, null, false, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                messages.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
        try {
            assertEquals(TdeHelper.Result.TEMPLATE_LOGGED, helper.logOrLoadTemplate(schema, null));
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
        }

        String message = messages.stream().filter(value -> value.startsWith("Generated TDE:\n")).findFirst().orElseThrow();
        JsonNode columns = new ObjectMapper().readTree(message.substring("Generated TDE:\n".length()))
            .path("template").path("rows").get(0).path("columns");
        assertEquals("uri", columns.get(0).path("name").asText());
        assertEquals("hash/name", columns.get(1).path("name").asText());
        assertEquals("first_name", columns.get(2).path("name").asText());
        assertEquals("first/name", columns.get(2).path("val").asText());
    }
}
