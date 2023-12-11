package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * So the purpose of this command would really be - we want to read via something to MarkLogic and then process it
 * too. Which may require two different connections. I think we'd want dynamic params for overriding the "write"
 * connection, which otherwise defaults to the regular connection params.
 */
public class ReprocessCommand extends AbstractCommand {

    @ParametersDelegate
    private ReadParams readParams = new ReadParams();

    @ParametersDelegate
    private WriteParams writeParams = new WriteParams();

    @DynamicParameter(names = "-WC:", description = "Overrides the connection parameters used for writing to MarkLogic; " +
        "can omit 'spark.marklogic.client.' for brevity; e.g. -WC:host=otherhost.")
    private Map<String, String> writeConnectionParams = new HashMap<>();

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        Map<String, String> writeConnectionOptions = getConnectionParams().makeOptions();
        writeConnectionParams.forEach((key, value) -> {
            String optionName = key;
            if (!key.startsWith("spark.marklogic.client")) {
                optionName = "spark.marklogic.client." + key;
            }
            writeConnectionOptions.put(optionName, value);
        });

        session.read()
            .format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load()
            .write()
            .format(MARKLOGIC_CONNECTOR)
            .options(writeConnectionParams)
            .options(writeParams.makeOptions())
            .mode(writeParams.getSaveMode())
            .save();

        // TODO Can support preview here too.
        return Optional.empty();
    }

    public ReadParams getReadParams() {
        return readParams;
    }

    public void setReadParams(ReadParams readParams) {
        this.readParams = readParams;
    }

    public WriteParams getWriteParams() {
        return writeParams;
    }

    public void setWriteParams(WriteParams writeParams) {
        this.writeParams = writeParams;
    }

    public Map<String, String> getWriteConnectionParams() {
        return writeConnectionParams;
    }

    public void setWriteConnectionParams(Map<String, String> writeConnectionParams) {
        this.writeConnectionParams = writeConnectionParams;
    }
}
