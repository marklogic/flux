package com.marklogic.flux.cli;

import com.marklogic.flux.impl.export.ExportDelimitedFilesCommand;
import picocli.CommandLine;

@CommandLine.Command(
    subcommands = {
        ExportDelimitedFilesCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class PicoMain {

    public static void main(String[] args) {
        new CommandLine(new PicoMain())
            .setUseSimplifiedAtFiles(true)
            .execute(args);
    }
}
