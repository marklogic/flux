/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read data from MarkLogic via custom code and reprocess it (often, but not necessarily, by writing data) via custom code.
 */
public interface Reprocessor extends Executor<Reprocessor> {

    interface ReadOptions {
        ReadOptions invoke(String modulePath);

        ReadOptions javascript(String query);

        ReadOptions javascriptFile(String path);

        ReadOptions xquery(String query);

        ReadOptions xqueryFile(String path);

        ReadOptions partitionsInvoke(String modulePath);

        ReadOptions partitionsJavascript(String query);

        ReadOptions partitionsJavascriptFile(String path);

        ReadOptions partitionsXquery(String query);

        ReadOptions partitionsXqueryFile(String path);

        ReadOptions vars(Map<String, String> namesAndValues);
    }

    interface WriteOptions {
        WriteOptions invoke(String modulePath);

        WriteOptions javascript(String query);

        WriteOptions javascriptFile(String path);

        WriteOptions xquery(String query);

        WriteOptions xqueryFile(String path);

        WriteOptions externalVariableName(String name);

        WriteOptions externalVariableDelimiter(String delimiter);

        WriteOptions vars(Map<String, String> namesAndValues);

        WriteOptions abortOnWriteFailure(Boolean value);

        WriteOptions batchSize(Integer batchSize);
    }

    Reprocessor from(Consumer<ReadOptions> consumer);

    Reprocessor to(Consumer<WriteOptions> consumer);
}
