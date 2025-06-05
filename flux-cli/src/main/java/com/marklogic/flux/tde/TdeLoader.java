/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marklogicspark.marklogic.client.DatabaseClient;
import marklogicspark.marklogic.client.io.DocumentMetadataHandle;
import marklogicspark.marklogic.client.io.JacksonHandle;

public class TdeLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SCRIPT = "declareUpdate(); const tde = require('/MarkLogic/tde.xqy');\n" +
        "var URI, TEMPLATE, PERMISSIONS;\n" +
        "const permissions = [];\n" +
        "Object.keys(PERMISSIONS).forEach(roleName => {\n" +
        "  PERMISSIONS[roleName].forEach(capability => permissions.push(xdmp.permission(roleName, capability)));\n" +
        "});\n" +
        "tde.templateBatchInsert(tde.templateInfo(URI, TEMPLATE, permissions))";

    private final DatabaseClient databaseClient;

    public TdeLoader(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public void loadTde(TdeInputs inputs) {
        TdeTemplate tdeTemplate = TdeBuilder.newTdeBuilder(inputs).buildTde(inputs);
        ObjectNode permissionsMap = buildPermissionsMap(inputs);

        final String inputUri = inputs.getUri();
        final String uri = inputUri != null && !inputUri.isEmpty() ? inputUri :
            String.format("/tde/%s/%s.json", inputs.getSchemaName(), inputs.getViewName());
        
        databaseClient.newServerEval().javascript(SCRIPT)
            .addVariable("URI", uri)
            .addVariable("TEMPLATE", tdeTemplate.toWriteHandle())
            .addVariable("PERMISSIONS", new JacksonHandle(permissionsMap))
            .evalAs(String.class);
    }

    private ObjectNode buildPermissionsMap(TdeInputs inputs) {
        ObjectNode permissionsMap = MAPPER.createObjectNode();
        if (inputs.getPermissions() != null && !inputs.getPermissions().isEmpty()) {
            DocumentMetadataHandle metadata = new DocumentMetadataHandle();
            metadata.getPermissions().addFromDelimitedString(inputs.getPermissions());

            metadata.getPermissions().entrySet().forEach(entry -> {
                ArrayNode capabilities = permissionsMap.putArray(entry.getKey());
                entry.getValue().forEach(capability -> capabilities.add(capability.name().toLowerCase()));
            });
        }
        return permissionsMap;
    }
}
