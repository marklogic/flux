package com.marklogic.newtool.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.UsageFormatter;

@Parameters(commandDescription = "View usage for only the named command")
public class HelpCommand {

    @Parameter
    private String commandName;

    private final String programName;

    public HelpCommand(String programName) {
        this.programName = programName;
    }

    public void viewUsage(JCommander commander, String commandName) {
        JCommander parsedCommander = commander.getCommands().get(commandName);
        Command command = (Command) parsedCommander.getObjects().get(0);

        JCommander usageCommander = JCommander.newBuilder().programName(programName).addCommand(commandName, command).build();
        usageCommander.setUsageFormatter(new UsageFormatter(usageCommander, commandName));
        usageCommander.usage();
    }
}
