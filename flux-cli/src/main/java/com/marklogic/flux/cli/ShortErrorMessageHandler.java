package com.marklogic.flux.cli;

import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * Copied from https://picocli.info/#_invalid_user_input . Typically, showing the usage - which has dozens of options -
 * makes it very hard for the user to find the actual error message, which is printed first.
 */
class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

    public int handleParseException(CommandLine.ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();
        PrintWriter err = cmd.getErr();

        // if tracing at DEBUG level, show the location of the issue
        if ("DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"))) {
            err.println(cmd.getColorScheme().stackTraceText(ex));
        }

        err.println(cmd.getColorScheme().errorText(ex.getMessage())); // bold red
        CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
        err.print(cmd.getHelp().fullSynopsis());

        CommandLine.Model.CommandSpec spec = cmd.getCommandSpec();
        err.printf("Run '%s' for more information.%n", spec.qualifiedName(" help "));
        return cmd.getExitCodeExceptionMapper() != null
            ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
            : spec.exitCodeOnInvalidInput();
    }
}
