package com.marklogic.newtool;

import java.util.function.Function;

public class MyDefaultProvider implements Function<String, String> {

    @Override
    public String apply(String optionName) {
        System.out.println("OPTION: " + optionName);
        return "--collections".equals(optionName) ? "delimited-test" : null;
    }
}
