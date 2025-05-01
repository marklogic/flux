/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import picocli.CommandLine;

import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Copied from https://picocli.info/#_invalid_user_input . Typically, showing the usage - which has dozens of options -
 * makes it very hard for the user to find the actual error message, which is printed first.
 */
class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

    public int handleParseException(@NotNull CommandLine.ParameterException ex, String[] args) {
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

        final String exceptionMessage = getErrorMessageToPrint(ex);
        err.println(colorScheme.errorText(exceptionMessage)); // bold red

        CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
        if (commandLine.getHelp() != null) {
            err.print(commandLine.getHelp().fullSynopsis());
        }

        CommandLine.Model.CommandSpec spec = commandLine.getCommandSpec();
        err.printf("Run '%s' for more information.%n", spec.qualifiedName(" help "));
        return commandLine.getExitCodeExceptionMapper() != null
            ? commandLine.getExitCodeExceptionMapper().getExitCode(ex)
            : spec.exitCodeOnInvalidInput();
    }

    private String getErrorMessageToPrint(Exception ex) {
        String message = ex.getMessage();
        // picocli appears to have a bug where the message will start with "Value for option option" when the user
        // provides an invalid input for a map option.
        final String buggyPicocliMessage = "Value for option option ";
        if (message != null && message.startsWith(buggyPicocliMessage)) {
            message = "Value for option " + message.substring(buggyPicocliMessage.length());
        }
        return message;
    }
}
