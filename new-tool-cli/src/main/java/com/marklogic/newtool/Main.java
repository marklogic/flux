package com.marklogic.newtool;

import com.beust.jcommander.JCommander;
import com.marklogic.newtool.command.*;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String PROGRAM_NAME = "./bin/new-tool";
    private static final int COLUMN_SIZE = 200;

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

    public void run() {
        if (selectedCommand == null) {
            commander.usage();
        } else if (selectedCommand instanceof HelpCommand) {
            String commandName = args[args.length - 1];
            ((HelpCommand) selectedCommand).printUsageForCommand(commander, commandName);
        } else {
            Command command = (Command) selectedCommand;
            SparkSession session = buildSparkSession();
            if (logger.isInfoEnabled()) {
                logger.info("Spark master URL: {}", session.sparkContext().master());
            }
            Optional<Preview> preview = command.execute(session);
            if (preview.isPresent()) {
                preview.get().showPreview();
            }
        }
    }

    protected SparkSession buildSparkSession() {
        // Will make these hardcoded strings configurable soon.
        return SparkSession.builder()
            .master("local[*]")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();
    }

    private JCommander buildCommander() {
        JCommander jc = JCommander.newBuilder()
            .programName(PROGRAM_NAME)
            .addCommand("help", new HelpCommand(PROGRAM_NAME, COLUMN_SIZE))
            .addCommand("import_delimited_files", new ImportDelimitedFilesCommand())
            .addCommand("import_files", new ImportFilesCommand())
            .addCommand("import_json_lines_files", new ImportDelimitedJsonFilesCommand())
            .columnSize(COLUMN_SIZE)
            .build();
        jc.setUsageFormatter(new SummaryUsageFormatter(jc));
        return jc;
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
