package com.marklogic.newtool;

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

    void printUsageForCommand(JCommander commander, String commandName) {
        JCommander parsedCommander = commander.getCommands().get(commandName);
        if (parsedCommander == null) {
            throw new IllegalArgumentException("Unrecognized command name: " + commandName);
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
