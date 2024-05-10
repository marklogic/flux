package com.marklogic.newtool.command.importdata;

import com.marklogic.client.io.DocumentMetadataHandle;

import java.util.Map;
import java.util.Set;

public abstract class WriteUtil {

    /**
     * @param permissions
     * @return a string with a format of role,capability,role,capability,etc.
     */
    public static String permissionsToString(Map<String, Set<DocumentMetadataHandle.Capability>> permissions) {
        StringBuilder sb = new StringBuilder();
        permissions.entrySet().stream().forEach(entry -> {
            String role = entry.getKey();
            entry.getValue().forEach(capability -> {
                if (!sb.toString().equals("")) {
                    sb.append(",");
                }
                sb.append(role).append(",").append(capability.name());
            });
        });
        return sb.toString();
    }

    private WriteUtil() {
    }
}
