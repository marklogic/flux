/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.flux.api.FluxException;
import com.marklogic.spark.Util;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;

public class TdeLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SCRIPT = """
        declareUpdate(); const tde = require('/MarkLogic/tde.xqy');
        var URI, TEMPLATE, PERMISSIONS;
        const permissions = [];
        Object.keys(PERMISSIONS).forEach(roleName => {
           PERMISSIONS[roleName].forEach(capability => permissions.push(xdmp.permission(roleName, capability)));
        });
        tde.templateBatchInsert(tde.templateInfo(URI, TEMPLATE, permissions))""";

    private final DatabaseClient databaseClient;

    public TdeLoader(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public void loadTde(final TdeTemplate tdeTemplate) {
        final ObjectNode permissionsMap = buildPermissionsMap(tdeTemplate.getPermissions());
        final String uri = tdeTemplate.getUri();

        if (Util.MAIN_LOGGER.isDebugEnabled()) {
            Util.MAIN_LOGGER.debug("Loading TDE template with URI: {}; template: {}",
                uri, tdeTemplate.toPrettyString());
        } else if (Util.MAIN_LOGGER.isInfoEnabled()) {
            Util.MAIN_LOGGER.info("Loading TDE template with URI: {}", uri);
        }

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
