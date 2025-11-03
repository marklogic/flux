/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.marklogic.flux.impl.*;
import com.marklogic.flux.impl.copy.CopyCommand;
import com.marklogic.flux.impl.copy.OutputConnectionParams;
import com.marklogic.flux.impl.custom.CustomExportDocumentsCommand;
import com.marklogic.flux.impl.custom.CustomExportRowsCommand;
import com.marklogic.flux.impl.custom.CustomImportCommand;
import com.marklogic.flux.impl.export.*;
import com.marklogic.flux.impl.importdata.*;
import com.marklogic.flux.impl.reprocess.ReprocessCommand;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
        ImportAggregateJsonFilesCommand.class,
        ImportAggregateXmlFilesCommand.class,
        ImportArchiveFilesCommand.class,
        ImportAvroFilesCommand.class,
        ImportDelimitedFilesCommand.class,
        ImportFilesCommand.class,
        ImportJdbcCommand.class,
        ImportMlcpArchiveFilesCommand.class,
        ImportOrcFilesCommand.class,
        ImportParquetFilesCommand.class,
        ImportRdfFilesCommand.class,
        ReprocessCommand.class,
        VersionCommand.class
    }
)
public class Main {

    private static final Logger logger = LoggerFactory.getLogger("com.marklogic.flux");

    private final SparkConf sparkConf;

    public Main() {
        this(null);
    }

    /**
     * Allows for providing a SparkConf that will be used when creating a SparkSession. The options for configuring
     * Spark can still be used, but this allows for programmatically providing a shared SparkConf in an environment
     * where this class is being invoked multiple times in the same JVM.
     *
     * @since 2.0.0
     */
    public Main(SparkConf sparkConf) {
        this.sparkConf = sparkConf;
    }

    // Intended to be invoked solely by the application script. Tests should invoke "run" instead to avoid the
    // System.exit call.
    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String... args) {
        return run(null, null, args);
    }

    /**
     * @param args
     * @param outWriter solely intended for usage in tests, allowing for stdout output to be captured and verified.
     * @param errWriter solely intended for usage in tests, allowing for stderr output to be captured and verified.
     * @return the system exit code.
     * @since 1.4.0
     */
    public static int run(PrintWriter outWriter, PrintWriter errWriter, String... args) {
        if (args.length == 0 || args[0].trim().equals("")) {
            args = new String[]{"help"};
        } else if (args[0].equals("help") && args.length == 1) {
            args = new String[]{"help", "-h"};
        }

        final boolean isHelpCommand = args[0].equals("help");
        final CommandLine commandLine = isHelpCommand ?
            new CommandLine(new Main())
                .setUsageHelpWidth(120)
                .setAbbreviatedSubcommandsAllowed(true) :
            new Main().newCommandLine();

        if (outWriter != null) {
            commandLine.setOut(outWriter);
        }
        if (errWriter != null) {
            commandLine.setErr(errWriter);
        }

        if (isHelpCommand) {
            commandLine.execute(args);
            return CommandLine.ExitCode.USAGE;
        }
        return commandLine.execute(args);
    }

    /**
     * @since 2.0.0
     */
    public static class CommandContext {
        public final Command command;
        public final SparkSession sparkSession;

        public CommandContext(Command command, SparkSession sparkSession) {
            this.command = command;
            this.sparkSession = sparkSession;
        }
    }

    /**
     * Parse the args and return a command that can be executed with a user-provided Spark session.
     *
     * @param outWriter optional, can be null
     * @param errWriter optional, can be null
     * @param args
     * @return
     * @since 2.0.0
     * <p>
     */
    public CommandContext buildCommandContext(PrintWriter outWriter, PrintWriter errWriter, String... args) {
        CommandLine commandLine = new Main().newCommandLine();
        if (outWriter != null) {
            commandLine.setOut(outWriter);
        }
        if (errWriter != null) {
            commandLine.setErr(errWriter);
        }
        AtomicReference<CommandContext> commandRef = new AtomicReference<>();
        commandLine.setExecutionStrategy(parseResult -> {
            CommandContext context = parseAndReturnCommand(parseResult);
            commandRef.set(context);
            return 0;
        });
        commandLine.execute(args);
        return commandRef.get();
    }

    protected CommandLine newCommandLine() {
        return new CommandLine(this)
            .setAbbreviatedOptionsAllowed(true)
            .setAbbreviatedSubcommandsAllowed(true)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setParameterExceptionHandler(new ShortErrorMessageHandler())
            .setExecutionStrategy(this::executeCommand)
            // Allows for values like Optic and serialized CTS queries to have newline symbols in them.
            .setUseSimplifiedAtFiles(false);
    }

    private int executeCommand(CommandLine.ParseResult parseResult) {
        Objects.requireNonNull(parseResult);
        try {
            final CommandContext commandContext = parseAndReturnCommand(parseResult);
            if (logger.isDebugEnabled()) {
                logger.debug("Spark master URL: {}", commandContext.sparkSession.sparkContext().master());
            }
            commandContext.command.execute(commandContext.sparkSession);
        } catch (Exception ex) {
            printException(parseResult, ex);
            return CommandLine.ExitCode.SOFTWARE;
        }
        return CommandLine.ExitCode.OK;
    }

    private CommandContext parseAndReturnCommand(CommandLine.ParseResult parseResult) {
        Objects.requireNonNull(parseResult);
        final Command command = PicoliUtil.getCommandInstance(parseResult);
        Objects.requireNonNull(command);
        command.validateCommandLineOptions(parseResult);
        return new CommandContext(command, buildSparkSession(command));
    }

    protected SparkSession buildSparkSession(Command selectedCommand) {
        String masterUrl = null;
        Map<String, String> sparkConfigurationProperties = null;
        if (selectedCommand instanceof AbstractCommand) {
            AbstractCommand<?> abstractCommand = (AbstractCommand<?>) selectedCommand;
            masterUrl = abstractCommand.determineSparkMasterUrl();
            if (abstractCommand.getCommonParams() != null) {
                sparkConfigurationProperties = abstractCommand.getCommonParams().getSparkConfigurationProperties();
            }
        }
        return SparkUtil.buildSparkSession(masterUrl, sparkConf, sparkConfigurationProperties);
    }

    private void printException(CommandLine.ParseResult parseResult, Exception ex) {
        CommandLine.ParseResult subcommand = parseResult.subcommand();
        Objects.requireNonNull(subcommand);
        final boolean includeStacktrace = subcommand.hasMatchedOption("--stacktrace");
        if (includeStacktrace) {
            logger.error("Displaying stacktrace due to use of --stacktrace option", ex);
        }

        String message = removeStacktraceFromExceptionMessage(ex);
        PrintWriter stderr = parseResult.commandSpec().commandLine().getErr();

        if (includeStacktrace) {
            stderr.println(String.format("%nCommand failed, error: %s", message));
        } else {
            stderr.println(String.format("%nCommand failed; consider running the command with the --stacktrace option to see more error information."));
            if (ex.getCause() instanceof SQLException) {
                stderr.println("The error is from the database you are connecting to via the configured JDBC driver.");
                stderr.println("You may find it helpful to consult your database documentation for the error message.");
            }
            stderr.println(String.format("Error: %s", message));
        }

        if (message != null && message.contains("XDMP-OLDSTAMP")) {
            printMessageForTimestampError(stderr);
        }
    }

    /**
     * In some errors from our connector, such as when the custom code reader invokes invalid code,
     * Spark will oddly put the entire stacktrace into the exception message. Showing that stacktrace isn't a
     * good UX unless the user has asked to see the stacktrace, which we support via --stacktrace. So this
     * does some basic checking to see if the exception message contains a stacktrace, and if so, only the
     * first line in the exception message is returned.
     *
     * @param ex
     * @return
     */
    private String removeStacktraceFromExceptionMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null) {
            return message;
        }
        String[] lines = message.split("\\n");
        // If at least the 3 lines after the exception message start with "at ", we assume that only the first
        // line is useful and the rest is all a stacktrace.
        if (lines.length >= 4 && isStacktraceLine(lines[1]) && isStacktraceLine(lines[2]) && isStacktraceLine(lines[3])) {
            return lines[0];
        }
        return message;
    }

    private boolean isStacktraceLine(String line) {
        return line != null && line.trim().startsWith("at ");
    }

    /**
     * A user can encounter an OLDSTAMP error when exporting data with a consistent snapshot, but it can be difficult
     * to know how to resolve the error. Thus, additional information is printed to help the user with resolving this
     * error.
     */
    private void printMessageForTimestampError(PrintWriter stderr) {
        stderr.println(String.format("To resolve an XDMP-OLDSTAMP error, consider using the --no-snapshot option " +
            "or consult the Flux documentation at https://marklogic.github.io/flux/ for " +
            "information on configuring your database to support point-in-time queries."));
    }

    /**
     * @return list of connection option names like "--host", "--port", etc.
     * @since 2.0.0
     */
    public static List<String> getConnectionOptionNames() {
        return ConnectionParams.getOptionNames();
    }

    /**
     * @return list of output connection option names like "--output-host", "--output-port", etc.
     * @since 2.0.0
     */
    public static List<String> getOutputConnectionOptionNames() {
        return OutputConnectionParams.getOptionNames();
    }
}
