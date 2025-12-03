/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Copied from https://picocli.info/#_invalid_user_input . Typically, showing the usage - which has dozens of options -
 * makes it very hard for the user to find the actual error message, which is printed first.
 */
class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

    private static final Map<String, String> SHORT_OPTIONS_REPLACED_IN_TWO_DOT_ZERO_RELEASE = Map.of(
        "-P", "--spark-prop",
        "-R", "--doc-prop",
        "-M", "--doc-metadata",
        "-S", "--splitter-prop",
        "-X", "--xpath-namespace",
        "-L", "--classifier-prop",
        "-E", "--embedder-prop"
    );

    public int handleParseException(CommandLine.ParameterException ex, String[] args) {
        final CommandLine commandLine = ex.getCommandLine();
        Objects.requireNonNull(commandLine);
        final PrintWriter err = commandLine.getErr();
        Objects.requireNonNull(err);
        final CommandLine.Help.ColorScheme colorScheme = commandLine.getColorScheme();
        Objects.requireNonNull(colorScheme);

        // if tracing at DEBUG level, show the location of the issue
        if ("DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"))) {
            err.println(colorScheme.stackTraceText(ex));
        }

        final String exceptionMessage = getErrorMessageToPrint(ex, args);
        if (exceptionMessage != null) {
            err.println(colorScheme.errorText(exceptionMessage));
            printHelpfulMessageForReplacedSingleLetterOption(exceptionMessage, err, colorScheme);
            printHelpfulMessageForMissingOptionsFile(exceptionMessage, err, colorScheme);
        }

        CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
        if (commandLine.getHelp() != null) {
            err.print(commandLine.getHelp().fullSynopsis());
        }

        CommandLine.Model.CommandSpec spec = commandLine.getCommandSpec();
        Objects.requireNonNull(spec);
        err.printf("Run '%s' for more information.%n", spec.qualifiedName(" help "));
        return commandLine.getExitCodeExceptionMapper() != null
            ? commandLine.getExitCodeExceptionMapper().getExitCode(ex)
            : spec.exitCodeOnInvalidInput();
    }

    private String getErrorMessageToPrint(Exception ex, String[] args) {
        String message = ex.getMessage();

        // picocli throws a confusing error when an option in an ArgGroup is specified multiple times.
        // The error starts with "expected only one match but got" followed by a list of all options.
        // We convert this to a more helpful message.
        if (message != null && message.startsWith("Error: expected only one match but got")) {
            return getDuplicateArgGroupOptionMessage(args);
        }

        // picocli appears to have a bug where the message will start with "Value for option option" when the user
        // provides an invalid input for a map option.
        final String buggyPicocliMessage = "Value for option option ";
        if (message != null && message.startsWith(buggyPicocliMessage)) {
            message = "Value for option " + message.substring(buggyPicocliMessage.length());
        }
        return message;
    }

    /**
     * When a user specifies the same option twice within an ArgGroup (such as ConnectionParams), picocli produces a
     * confusing error message that starts with "Error: expected only one match but got" followed by a list of all
     * options in the group. This is very confusing for a user, who is unlikely to figure out what the problem is.
     * This method attempts to find the duplicate option and produce a more helpful error message.
     */
    private String getDuplicateArgGroupOptionMessage(String[] args) {
        // Find the option that appears more than once
        Set<String> seenOptions = new HashSet<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                // Handle --option=value format
                String optionName = arg.contains("=") ? arg.substring(0, arg.indexOf('=')) : arg;
                if (!seenOptions.add(optionName)) {
                    return String.format("option '%s' should be specified only once", optionName);
                }
            }
        }

        // Fallback if the specific option cannot be determined.
        return "An option was specified more than once. Each option should only be specified once.";
    }

    private void printHelpfulMessageForReplacedSingleLetterOption(String exceptionMessage, PrintWriter err, CommandLine.Help.ColorScheme colorScheme) {
        SHORT_OPTIONS_REPLACED_IN_TWO_DOT_ZERO_RELEASE.keySet().forEach(shortOption -> {
            final String indicatorOfReplacedShortOption = "Unknown option: '" + shortOption;
            if (exceptionMessage.startsWith(indicatorOfReplacedShortOption)) {
                String longOption = SHORT_OPTIONS_REPLACED_IN_TWO_DOT_ZERO_RELEASE.get(shortOption);
                err.println("");
                err.println(colorScheme.errorText("Did you mean to use %s instead of %s, as %s was replaced in the 2.0 release with %s?"
                    .formatted(longOption, shortOption, shortOption, longOption)));

                String optionValue = exceptionMessage.substring(indicatorOfReplacedShortOption.length());
                if (optionValue.endsWith("'")) {
                    optionValue = optionValue.substring(0, optionValue.length() - 1);
                }

                err.println(colorScheme.errorText("If so, use %s %s instead.".formatted(longOption, optionValue)));
                err.println("");
            }
        });
    }

    private void printHelpfulMessageForMissingOptionsFile(String exceptionMessage, PrintWriter err, CommandLine.Help.ColorScheme colorScheme) {
        // Per https://picocli.info/#AtFiles, picocli will treat a missing options file as a regular argument, which
        // can be very confusing for a user who doesn't realize they have a typo or a permissions error on their
        // options file. So we give a better hint as to what the problem likely is.
        if (exceptionMessage.contains("Unmatched argument at index") && exceptionMessage.contains("'@")) {
            err.println(colorScheme.errorText("Please ensure the options file you are referencing exists and is readable."));
            err.println("");
        }
    }
}
