/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "View details for the named command.")
class HelpCommand {

    @Parameter
    private String commandName;

    private final String programName;
    private final int columnSize;

    HelpCommand(String programName, int columnSize) {
        this.programName = programName;
        this.columnSize = columnSize;
    }

    // Sonar's not happy about stderr/stdout usage; will revisit this, ignoring warnings for now.
    @SuppressWarnings("java:S106")
    void printUsageForCommand(JCommander commander, String commandName) {
        JCommander parsedCommander = commander.getCommands().get(commandName);
        if (parsedCommander == null) {
            System.out.println("Unrecognized command name: " + commandName);
            System.out.println(String.format("To see all commands, run '%s' with no arguments.", programName));
            return;
        }

        // Get the selected command and construct a new JCommander with only that command so that we can print usage for
        // the single command.
        Object command = parsedCommander.getObjects().get(0);
        JCommander.newBuilder()
            .programName(programName)
            .addCommand(commandName, command)
            .columnSize(columnSize)
            .build()
            .usage();
    }
}
