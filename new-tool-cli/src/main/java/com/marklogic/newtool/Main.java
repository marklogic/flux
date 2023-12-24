package com.marklogic.newtool;

import com.beust.jcommander.JCommander;
import com.marklogic.newtool.command.*;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Optional;

public class Main {

    private static final String PROGRAM_NAME = "./bin/new-tool";

    private final String[] args;
    private final JCommander commander;
    private final Object selectedCommand;

    public static void main(String[] args) {
        new Main(args).run();
    }

    public Main(String... args) {
        this.args = args;
        this.commander = buildCommander();
        this.selectedCommand = getSelectedCommand(commander, args);
    }

    public Optional<List<Row>> run() {
        if (selectedCommand == null) {
            commander.usage();
        } else if (selectedCommand instanceof HelpCommand) {
            // Command will be the last arg
            String commandName = args[args.length - 1];
            ((HelpCommand) selectedCommand).viewUsage(commander, commandName);
        } else {
            Command command = (Command) selectedCommand;
            // TODO Allow for user to customize these inputs.
            SparkSession session = SparkSession.builder()
                .master("local[*]")
                .config("spark.sql.session.timeZone", "UTC")
                .getOrCreate();
            return command.execute(session);
        }
        return Optional.empty();
    }

    private JCommander buildCommander() {
        JCommander commander = JCommander.newBuilder()
            .programName(PROGRAM_NAME)
            .addCommand("help", new HelpCommand(PROGRAM_NAME))
            .addCommand("import_files", new ImportFilesCommand())
            .addCommand("import_jdbc", new ImportJdbcCommand())
            .addCommand("export_jdbc", new ExportJdbcCommand())
            .build();

        commander.setUsageFormatter(new UsageFormatter(commander));
        return commander;
    }

    private Object getSelectedCommand(JCommander commander, String... args) {
        commander.parse(args);
        String parsedCommand = commander.getParsedCommand();
        if (parsedCommand == null) {
            return null;
        }
        return commander.getCommands().get(parsedCommand).getObjects().get(0);
    }

    public Object getSelectedCommand() {
        return selectedCommand;
    }
}
