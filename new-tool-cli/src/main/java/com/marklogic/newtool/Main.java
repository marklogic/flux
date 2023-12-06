package com.marklogic.newtool;

import com.beust.jcommander.JCommander;
import com.marklogic.newtool.command.*;
import org.apache.spark.sql.SparkSession;

import java.util.List;

public class Main {

    public static void main(List<String> args) {
        main(args.toArray(new String[]{}));
    }

    public static void main(String[] args) {
        JCommander.Builder builder = JCommander
            .newBuilder()
            // TODO Only want to do this when it's running as application zip
            .programName("./bin/mc")
            .addCommand("import_files", new ImportFilesCommand())
            .addCommand("import_jdbc", new ImportJdbcCommand())
            .addCommand("export_files", new ExportFilesCommand())
            .addCommand("export_jdbc", new ExportJdbcCommand())
            .addCommand("custom", new ExecuteCustomCommand())
            .addCommand("reprocess", new ReprocessCommand());

        JCommander commander = builder.build();
        commander.setUsageFormatter(new UsageFormatter(commander));
        commander.parse(args);

        String parsedCommand = commander.getParsedCommand();
        if (parsedCommand == null) {
            commander.usage();
        } else {
            JCommander parsedCommander = commander.getCommands().get(parsedCommand);
            Command command = (Command) parsedCommander.getObjects().get(0);

            // Perhaps SparkSubmit could be used here instead?
            SparkSession session = SparkSession.builder()
                .master("local[*]")
                .config("spark.sql.session.timeZone", "UTC")
                .getOrCreate();

            command.execute(session);
        }
    }
}
