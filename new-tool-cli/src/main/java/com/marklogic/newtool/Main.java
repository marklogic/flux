package com.marklogic.newtool;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.marklogic.newtool.command.*;
import org.apache.spark.SparkException;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger("com.marklogic.newtool");

    // TBD on the real name here. And will need to account for Windows.
    private static final String PROGRAM_NAME = "./bin/nt";

    private static final int COLUMN_SIZE = 120;

    private final String[] args;
    private final JCommander commander;
    private final Object selectedCommand;

    // Only used for logging
    private String selectedCommandName;

    // Sonar's not happy about stderr/stdout usage; will revisit this, ignoring warnings for now.
    @SuppressWarnings({"java:S106", "java:S4507"})
    public static void main(String[] args) {
        try {
            Main main = new Main(args);
            main.run();
        } catch (MissingCommandException ex) {
            System.err.println("Invalid command name: " + args[0]);
            System.err.println(String.format("To see all commands, run '%s' with no arguments.", PROGRAM_NAME));
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            System.err.println(String.format("To see usage for the command, run '%s help %s'.", PROGRAM_NAME, args[0]));
        } catch (Exception ex) {
            for (String arg : args) {
                if ("--stacktrace".equals(arg)) {
                    ex.printStackTrace();
                }
            }
            if (ex instanceof SparkException && ex.getCause() != null) {
                String message = extractExceptionMessage((SparkException) ex);
                System.err.println(String.format("%nCommand failed, cause: %s", message));
            } else {
                System.err.println(String.format("%nCommand failed, cause: %s", ex.getMessage()));
            }
        }
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
            if (logger.isInfoEnabled()) {
                logger.info("Executing command: {}", selectedCommandName);
            }
            Command command = (Command) selectedCommand;
            SparkSession session = buildSparkSession();
            if (logger.isDebugEnabled()) {
                logger.debug("Spark master URL: {}", session.sparkContext().master());
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
            .config("spark.ui.showConsoleProgress", "true")
            .config("spark.sql.session.timeZone", "UTC")
            .getOrCreate();
    }

    private JCommander buildCommander() {
        JCommander jc = JCommander.newBuilder()
            .programName(PROGRAM_NAME)
            .addCommand("copy", new CopyCommand())
            .addCommand("export_avro_files", new ExportAvroFilesCommand())
            .addCommand("export_files", new ExportFilesCommand())
            .addCommand("export_jdbc", new ExportJdbcCommand())
            .addCommand("export_orc_files", new ExportOrcFilesCommand())
            .addCommand("export_parquet_files", new ExportParquetFilesCommand())
            .addCommand("help", new HelpCommand(PROGRAM_NAME, COLUMN_SIZE))
            .addCommand("import_delimited_files", new ImportDelimitedFilesCommand())
            .addCommand("import_files", new ImportFilesCommand())
            .addCommand("import_jdbc", new ImportJdbcCommand())
            .addCommand("import_json_lines_files", new ImportDelimitedJsonFilesCommand())
            .addCommand("import_parquet_files", new ImportParquetFilesCommand())
            .addCommand("import_avro_files", new ImportAvroFilesCommand())
            .addCommand("import_aggregate_xml_files", new ImportAggregateXmlCommand())
            .addCommand("import_orc_files", new ImportOrcFilesCommand())
            .addCommand("reprocess", new ReprocessCommand())
            .columnSize(COLUMN_SIZE)
            .build();
        jc.setUsageFormatter(new SummaryUsageFormatter(jc));
        return jc;
    }

    private Object getSelectedCommand(JCommander commander, String... args) {
        commander.parse(args);
        this.selectedCommandName = commander.getParsedCommand();
        if (this.selectedCommandName == null) {
            return null;
        }
        return commander.getCommands().get(this.selectedCommandName).getObjects().get(0);
    }

    private static String extractExceptionMessage(SparkException ex) {
        // The SparkException message typically has a stacktrace in it that is not likely to be helpful.
        String message = ex.getCause().getMessage();
        if (ex.getCause() instanceof SparkException && ex.getCause().getCause() != null) {
            // For some errors, Spark throws a SparkException that wraps a SparkException, and it's the
            // wrapped SparkException that has a more useful error.
            message = ex.getCause().getCause().getMessage();
        }
        return message;
    }

    public Object getSelectedCommand() {
        return selectedCommand;
    }
}
