/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.Command;
import com.marklogic.flux.impl.Preview;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.flux.impl.copy.CopyCommand;
import com.marklogic.flux.impl.custom.CustomExportDocumentsCommand;
import com.marklogic.flux.impl.custom.CustomExportRowsCommand;
import com.marklogic.flux.impl.custom.CustomImportCommand;
import com.marklogic.flux.impl.export.*;
import com.marklogic.flux.impl.importdata.*;
import com.marklogic.flux.impl.reprocess.ReprocessCommand;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "./bin/flux",

    // The scope allows for the following attributes to be inherited by the subcommands.
    scope = CommandLine.ScopeType.INHERIT,
    abbreviateSynopsis = true,
    showAtFileInUsageHelp = true,
    separator = " ",
    requiredOptionMarker = '*',

    subcommands = {
        CommandLine.HelpCommand.class,
        CopyCommand.class,
        CustomExportDocumentsCommand.class,
        CustomExportRowsCommand.class,
        CustomImportCommand.class,
        ExportArchiveFilesCommand.class,
        ExportAvroFilesCommand.class,
        ExportDelimitedFilesCommand.class,
        ExportFilesCommand.class,
        ExportJdbcCommand.class,
        ExportJsonLinesFilesCommand.class,
        ExportOrcFilesCommand.class,
        ExportParquetFilesCommand.class,
        ExportRdfFilesCommand.class,
        ImportAggregateXmlCommand.class,
        ImportArchiveFilesCommand.class,
        ImportAvroFilesCommand.class,
        ImportDelimitedFilesCommand.class,
        ImportFilesCommand.class,
        ImportJdbcCommand.class,
        ImportJsonFilesCommand.class,
        ImportMlcpArchiveFilesCommand.class,
        ImportOrcFilesCommand.class,
        ImportParquetFilesCommand.class,
        ImportRdfFilesCommand.class,
        ReprocessCommand.class
    }
)
public class PicoMain {

    private static final Logger logger = LoggerFactory.getLogger("com.marklogic.flux");

    public static void main(String[] args) {
        if (args.length == 0 || args[0].trim().equals("")) {
            args = new String[]{"help"};
        } else if (args[0].equals("help") && args.length == 1) {
            args = new String[]{"help", "-h"};
        }

        if (args[0].equals("help")) {
            new CommandLine(new PicoMain())
                .setUsageHelpWidth(120)
                .setAbbreviatedSubcommandsAllowed(true)
                .execute(args);
        } else {
            new PicoMain().newCommandLine().execute(args);
        }
    }

    // Sonar's not happy about stderr/stdout usage; will revisit this, ignoring warnings for now.
    @SuppressWarnings({"java:S106", "java:S4507"})
    public CommandLine newCommandLine() {
        return new CommandLine(new PicoMain())
            .setAbbreviatedOptionsAllowed(true)
            .setAbbreviatedSubcommandsAllowed(true)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setParameterExceptionHandler(new ShortErrorMessageHandler())
            .setExecutionStrategy(parseResult -> {
                final Command command = (Command) parseResult.subcommand().commandSpec().userObject();
                try {
                    command.validateCommandLineOptions(parseResult);
                    SparkSession session = buildSparkSession(command);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Spark master URL: {}", session.sparkContext().master());
                    }
                    Optional<Preview> preview = command.execute(session);
                    if (preview.isPresent()) {
                        preview.get().showPreview();
                    }
                } catch (Exception ex) {
                    if (parseResult.subcommand().hasMatchedOption("--stacktrace")) {
                        ex.printStackTrace();
                    }
                    System.err.println(String.format("%nCommand failed, cause: %s", ex.getMessage()));
                    return CommandLine.ExitCode.SOFTWARE;
                }
                return CommandLine.ExitCode.OK;
            })
            .setUseSimplifiedAtFiles(true);
    }

    protected SparkSession buildSparkSession(Command selectedCommand) {
        String masterUrl = null;
        if (selectedCommand instanceof AbstractCommand) {
            masterUrl = ((AbstractCommand) selectedCommand).getCommonParams().getSparkMasterUrl();
        }
        return masterUrl != null && masterUrl.trim().length() > 0 ?
            SparkUtil.buildSparkSession(masterUrl) :
            SparkUtil.buildSparkSession();
    }
}
