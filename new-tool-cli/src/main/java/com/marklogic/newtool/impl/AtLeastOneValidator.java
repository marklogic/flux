package com.marklogic.newtool.impl;

import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.ParameterException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Reusable validator for when at least one parameter in a list must be defined.
 */
public class AtLeastOneValidator implements IParametersValidator {

    private final List<String> atLeastOneOfTheseParams;

    public AtLeastOneValidator(String... params) {
        this.atLeastOneOfTheseParams = Arrays.asList(params);
    }

    @Override
    public void validate(Map<String, Object> parameters) throws ParameterException {
        boolean hasOne = false;
        for (String param : atLeastOneOfTheseParams) {
            if (parameters.get(param) != null) {
                hasOne = true;
                break;
            }
        }
        if (!hasOne) {
            throw new ParameterException(String.format("Must specify at least one of the following options: %s.", atLeastOneOfTheseParams));
        }
    }
}
