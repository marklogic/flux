package com.marklogic.flux.api;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Service
public class ReprocessorService {

    private final Reprocessor reprocessor;

    @Autowired
    public ReprocessorService(Reprocessor reprocessor) {
        this.reprocessor = reprocessor;
    }

    public void reprocess(ReprocessController.ReprocessRequest request) {
        // Map ReadOptions
        reprocessor.from(readOpts -> {
            ReprocessController.ReadOptions r = request.read;
            if (r.invoke != null) readOpts.invoke(r.invoke);
            if (r.javascript != null) readOpts.javascript(r.javascript);
            if (r.javascriptFile != null) readOpts.javascriptFile(r.javascriptFile);
            if (r.xquery != null) readOpts.xquery(r.xquery);
            if (r.xqueryFile != null) readOpts.xqueryFile(r.xqueryFile);
            if (r.partitionsInvoke != null) readOpts.partitionsInvoke(r.partitionsInvoke);
            if (r.partitionsJavascript != null) readOpts.partitionsJavascript(r.partitionsJavascript);
            if (r.partitionsJavascriptFile != null) readOpts.partitionsJavascriptFile(r.partitionsJavascriptFile);
            if (r.partitionsXquery != null) readOpts.partitionsXquery(r.partitionsXquery);
            if (r.partitionsXqueryFile != null) readOpts.partitionsXqueryFile(r.partitionsXqueryFile);
            if (r.vars != null) readOpts.vars(r.vars);
            if (r.logProgress != null) readOpts.logProgress(r.logProgress);
        });

        // Map WriteOptions
        reprocessor.to(writeOpts -> {
            ReprocessController.WriteOptions w = request.write;
            if (w.invoke != null) writeOpts.invoke(w.invoke);
            if (w.javascript != null) writeOpts.javascript(w.javascript);
            if (w.javascriptFile != null) writeOpts.javascriptFile(w.javascriptFile);
            if (w.xquery != null) writeOpts.xquery(w.xquery);
            if (w.xqueryFile != null) writeOpts.xqueryFile(w.xqueryFile);
            if (w.externalVariableName != null) writeOpts.externalVariableName(w.externalVariableName);
            if (w.externalVariableDelimiter != null) writeOpts.externalVariableDelimiter(w.externalVariableDelimiter);
            if (w.vars != null) writeOpts.vars(w.vars);
            if (w.abortOnWriteFailure != null) writeOpts.abortOnWriteFailure(w.abortOnWriteFailure);
            if (w.batchSize != null) writeOpts.batchSize(w.batchSize);
            if (w.logProgress != null) writeOpts.logProgress(w.logProgress);
            if (w.threadCount != null) writeOpts.threadCount(w.threadCount);
        });

        // Map ConnectionOptions and CommonOptions as needed (not shown: pass to Reprocessor or set up context)
        // Example: You may need to set these on the Reprocessor or in a context object
        // request.connection.host, request.connection.user, etc.
        // request.common.sparkMasterUrl, etc.

        // Execute the reprocessing
        reprocessor.execute();
    }
}
