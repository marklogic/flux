/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface ConnectionOptions {

    ConnectionOptions authenticationType(AuthenticationType authenticationType);

    ConnectionOptions connectionString(String connectionString);

    ConnectionOptions host(String host);

    ConnectionOptions port(int port);

    ConnectionOptions basePath(String basePath);

    ConnectionOptions database(String database);

    ConnectionOptions connectionType(String connectionType);

    ConnectionOptions disableGzippedResponses(boolean value);

    ConnectionOptions username(String username);

    ConnectionOptions password(String password);

    ConnectionOptions certificateFile(String certificateFile);

    ConnectionOptions certificatePassword(String certificatePassword);

    ConnectionOptions cloudApiKey(String cloudApiKey);

    ConnectionOptions kerberosPrincipal(String kerberosPrincipal);

    ConnectionOptions samlToken(String samlToken);

    /**
     * @param oauthToken
     * @return
     * @since 1.2.0
     */
    ConnectionOptions oauthToken(String oauthToken);

    ConnectionOptions sslProtocol(String sslProtocol);

    ConnectionOptions sslHostnameVerifier(SslHostnameVerifier sslHostnameVerifier);

    ConnectionOptions keyStorePath(String keyStorePath);

    ConnectionOptions keyStorePassword(String keyStorePassword);

    ConnectionOptions keyStoreType(String keyStoreType);

    ConnectionOptions keyStoreAlgorithm(String keyStoreAlgorithm);

    ConnectionOptions trustStorePath(String trustStorePath);

    ConnectionOptions trustStorePassword(String trustStorePassword);

    ConnectionOptions trustStoreType(String trustStoreType);

    ConnectionOptions trustStoreAlgorithm(String trustStoreAlgorithm);
}
