package com.marklogic.newtool.command.export;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import org.apache.spark.sql.SaveMode;

import java.util.EnumSet;

/**
 * A JCommander converter - https://jcommander.org/#_custom_types_converters_and_splitters - that handles the fact that
 * Spark's {@code SaveMode} uses camel-cased enum values instead of uppercase values.
 */
class SaveModeConverter implements IStringConverter<SaveMode> {

    @Override
    public SaveMode convert(String value) {
        if ("append".equalsIgnoreCase(value)) {
            value = "Append";
        } else if ("overwrite".equalsIgnoreCase(value)) {
            value = "Overwrite";
        } else if ("errorifexists".equalsIgnoreCase(value)) {
            value = "ErrorIfExists";
        } else if ("ignore".equalsIgnoreCase(value)) {
            value = "Ignore";
        }
        try {
            return SaveMode.valueOf(value);
        } catch (Exception ex) {
            throw new ParameterException("Invalid value for mode parameter. Allowed values:" + EnumSet.allOf(SaveMode.class));
        }
    }

}
