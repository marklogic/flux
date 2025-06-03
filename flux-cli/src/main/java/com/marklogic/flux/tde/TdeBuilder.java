/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

public interface TdeBuilder {

    static TdeBuilder newTdeBuilder(TdeInputs inputs) {
        return inputs.isJson() ? new JsonTdeBuilder() : new XmlTdeBuilder();
    }
    
    String buildTde(TdeInputs inputs);
}
