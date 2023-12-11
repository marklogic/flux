package com.marklogic.newtool;

import com.beust.jcommander.JCommander;
import com.marklogic.newtool.command.*;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Optional;

public class Main {

    private static final String PROGRAM_NAME = "./bin/new-tool";

    public static void main(String[] args) {
        run(args);
    }

    public static Optional<List<Row>> run(String... args) {
        JCommander.Builder builder = JCommander
            .newBuilder()
            .programName(PROGRAM_NAME)
            .addCommand("help", new HelpCommand(PROGRAM_NAME))
            .addCommand("import_avro", new ImportAvroCommand())
            .addCommand("import_jdbc", new ImportJdbcCommand())
            .addCommand("import_json", new ImportJsonCommand())
            .addCommand("export_avro", new ExportAvroCommand())
            .addCommand("export_jdbc", new ExportJdbcCommand())
            .addCommand("export_orc", new ExportOrcCommand())
            .addCommand("custom", new ExecuteCustomCommand())
            .addCommand("reprocess", new ProcessContentCommand());

        JCommander commander = builder.build();
        commander.setUsageFormatter(new UsageFormatter(commander));
        commander.parse(args);

        String parsedCommand = commander.getParsedCommand();
        if (parsedCommand == null) {
            commander.usage();
        } else {
            JCommander parsedCommander = commander.getCommands().get(parsedCommand);
            Object objectCommand = parsedCommander.getObjects().get(0);
            if (objectCommand instanceof HelpCommand) {
                // Command will be the last arg
                String commandName = args[args.length - 1];
                ((HelpCommand) objectCommand).viewUsage(commander, commandName);
            } else {
                Command command = (Command) objectCommand;
                // TODO Allow for user to customize these inputs.
                SparkSession session = SparkSession.builder()
                    .master("local[*]")
                    .config("spark.sql.session.timeZone", "UTC")
                    .getOrCreate();
                return command.execute(session);
            }
        }
        return Optional.empty();
    }
}
