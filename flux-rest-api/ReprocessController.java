package com.marklogic.flux.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/reprocess")
public class ReprocessController {

    private final ReprocessorService reprocessorService;

    @Autowired
    public ReprocessController(ReprocessorService reprocessorService) {
        this.reprocessorService = reprocessorService;
    }

    @PostMapping
    public ResponseEntity<String> reprocess(@RequestBody @Validated ReprocessRequest request) {
        // Delegate to a service that wraps the actual reprocessing logic
        reprocessorService.reprocess(request);
        return ResponseEntity.ok("Reprocessing completed successfully");
    }

    // DTOs matching the OpenAPI spec

    public static class ReprocessRequest {
        public ReadOptions read;
        public WriteOptions write;
        public ConnectionOptions connection;
        public CommonOptions common;
    }

    public static class ReadOptions {
        public String invoke;
        public String javascript;
        public String javascriptFile;
        public String xquery;
        public String xqueryFile;
        public String partitionsInvoke;
        public String partitionsJavascript;
        public String partitionsJavascriptFile;
        public String partitionsXquery;
        public String partitionsXqueryFile;
        public Map<String, String> vars;
        public Integer logProgress;
    }

    public static class WriteOptions {
        public String invoke;
        public String javascript;
        public String javascriptFile;
        public String xquery;
        public String xqueryFile;
        public String externalVariableName;
        public String externalVariableDelimiter;
        public Map<String, String> vars;
        public Boolean abortOnWriteFailure;
        public Integer batchSize;
        public Integer logProgress;
        public Integer threadCount;
    }

    public static class ConnectionOptions {
        public String host;
        public Integer port;
        public String database;
        public String user;
        public String password;
        public Boolean ssl;
        public String auth;
    }

    public static class CommonOptions {
        public String sparkMasterUrl;
        public Integer repartition;
        public Integer limit;
        public Boolean count;
        public Integer preview;
        public Map<String, String> sparkSessionConfig;
    }
}
