/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.flux.api.FluxException;
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

    public void loadTde(final TdeTemplate tdeTemplate) {
        final ObjectNode permissionsMap = buildPermissionsMap(tdeTemplate.getPermissions());
        final String uri = tdeTemplate.getUri();

        try {
            databaseClient.newServerEval().javascript(SCRIPT)
                .addVariable("URI", uri)
                .addVariable("TEMPLATE", tdeTemplate.getWriteHandle())
                .addVariable("PERMISSIONS", new JacksonHandle(permissionsMap))
                .evalAs(String.class);
        } catch (Exception e) {
            String message = String.format("Failed to load TDE template to URI '%s'; template: %s; cause: %s",
                uri, tdeTemplate.toPrettyString(), e.getMessage());
            throw new FluxException(message, e);
        }
    }

    private ObjectNode buildPermissionsMap(String permissions) {
        ObjectNode permissionsMap = MAPPER.createObjectNode();
        if (permissions != null && !permissions.isEmpty()) {
            DocumentMetadataHandle metadata = new DocumentMetadataHandle();
            metadata.getPermissions().addFromDelimitedString(permissions);

            metadata.getPermissions().entrySet().forEach(entry -> {
                ArrayNode capabilities = permissionsMap.putArray(entry.getKey());
                entry.getValue().forEach(capability -> capabilities.add(capability.name().toLowerCase()));
            });
        }
        return permissionsMap;
    }
}
