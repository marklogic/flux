/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Read data from MarkLogic via custom code and reprocess it via custom code.
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

        ReadOptions logProgress(int interval);
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

        WriteOptions abortOnWriteFailure(boolean value);

        WriteOptions batchSize(int batchSize);

        WriteOptions logProgress(int interval);

        /**
         * @param threadCount the number of threads, which equates to the number of Spark partitions, to use for
         *                    reprocessing items.
         * @return instance of this executor
         * @since 1.2.0
         */
        WriteOptions threadCount(int threadCount);
    }

    Reprocessor from(Consumer<ReadOptions> consumer);

    Reprocessor to(Consumer<WriteOptions> consumer);
}
