package com.marklogic.flux.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.eval.EvalResultIterator;
import com.marklogic.client.io.StringHandle;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.resource.appservers.ServerManager;
import com.marklogic.mgmt.resource.security.CertificateTemplateManager;
import com.marklogic.rest.util.Fragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Based on the TwoWaySslTest in the java-client-api repository. Should likely be migrated to marklogic-junit5 so it
 * can be easily reused in other projects.
 */
public class TwoWaySslConfigurer {

    public static final String DEFAULT_CERTIFICATE_TEMPLATE_NAME = "marklogic-test-certificate-template";
    public static final String DEFAULT_CERTIFICATE_AUTHORITY_NAME = "marklogic-test-certificate-authority";

    private static final String DEFAULT_PASSWORD = "password";
    private static final Logger logger = LoggerFactory.getLogger(TwoWaySslConfigurer.class);

    private final DatabaseClient securityClient;
    private final ManageClient manageClient;

    public TwoWaySslConfigurer(ManageClient manageClient, DatabaseClient securityClient) {
        this.manageClient = manageClient;
        this.securityClient = securityClient;
    }

    public TwoWaySslConfig setupTwoWaySsl(Path tempDir, String serverName) {
        return setupTwoWaySsl(tempDir, serverName, DEFAULT_CERTIFICATE_TEMPLATE_NAME, DEFAULT_CERTIFICATE_AUTHORITY_NAME);
    }

    public TwoWaySslConfig setupTwoWaySsl(Path tempDir, String serverName, String templateName, String certificateAuthorityName) {
        createCertificateTemplate(templateName);
        String certificateAuthorityId = createCertificateAuthority(certificateAuthorityName);
        makeAppServerRequireTwoWaySsl(serverName, templateName, certificateAuthorityId);

        ClientCertificate clientCertificate = createClientCertificate(certificateAuthorityName);
        try {
            writeClientCertificateFilesToTempDir(clientCertificate, tempDir);
            createPkcs12File(tempDir);
            File keyStoreFile = createKeystoreFile(tempDir);
            File trustStoreFile = new File(tempDir.toFile(), "trustStore.jks");
            addServerCertificateToTrustStore(templateName, tempDir, trustStoreFile);
            return new TwoWaySslConfig(keyStoreFile, DEFAULT_PASSWORD, trustStoreFile, DEFAULT_PASSWORD);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void teardownTwoWaySsl(String serverName) {
        teardownTwoWaySsl(serverName, DEFAULT_CERTIFICATE_TEMPLATE_NAME, DEFAULT_CERTIFICATE_AUTHORITY_NAME);
    }

    public void teardownTwoWaySsl(String serverName, String templateName, String certificateAuthorityName) {
        removeTwoWaySslConfig(serverName);
        try {
            deleteCertificateAuthority(certificateAuthorityName);
        } catch (Exception e) {
            logger.warn("Unable to delete certificate authority {}; cause: {}", certificateAuthorityName, e.getMessage());
        }
        try {
            new CertificateTemplateManager(manageClient).deleteByIdField(templateName);
        } catch (Exception e) {
            logger.warn("Unable to delete certificate template {}; cause: {}", templateName, e.getMessage());
        }
    }

    private void createCertificateTemplate(String templateName) {
        CertificateTemplateManager mgr = new CertificateTemplateManager(manageClient);
        mgr.save(makeCertificateTemplate(templateName));
        mgr.generateTemporaryCertificate(templateName, securityClient.getHost());
    }

    /**
     * See https://docs.marklogic.com/pki:create-authority for more information. This results in both a new
     * CA in MarkLogic and a new "secure credential".
     */
    private String createCertificateAuthority(String certificateAuthorityName) {
        String xquery = String.format("xquery version \"1.0-ml\";\n" +
                "import module namespace pki = \"http://marklogic.com/xdmp/pki\" at \"/MarkLogic/pki.xqy\";\n" +
                "declare namespace x509 = \"http://marklogic.com/xdmp/x509\";\n" +
                "\n" +
                "pki:create-authority(\n" +
                "  \"%s\", \"%s\",\n" +
                "  element x509:subject {\n" +
                "    element x509:countryName            {\"US\"},\n" +
                "    element x509:stateOrProvinceName    {\"California\"},\n" +
                "    element x509:localityName           {\"San Carlos\"},\n" +
                "    element x509:organizationName       {\"%s\"},\n" +
                "    element x509:organizationalUnitName {\"Engineering\"},\n" +
                "    element x509:commonName             {\"marklogic-test-ca\"},\n" +
                "    element x509:emailAddress           {\"marklogic-test-ca@example.org\"}\n" +
                "  },\n" +
                "  fn:current-dateTime(),\n" +
                "  fn:current-dateTime() + xs:dayTimeDuration(\"P365D\"),\n" +
                "  (xdmp:permission(\"admin\",\"read\")))",
            certificateAuthorityName, certificateAuthorityName, certificateAuthorityName
        );
        return securityClient.newServerEval().xquery(xquery).evalAs(String.class);
    }

    /**
     * See https://docs.marklogic.com/pki:authority-create-client-certificate for more information.
     * The commonName matches that of a known test user so that certificate authentication can be tested too.
     */
    private ClientCertificate createClientCertificate(String certificateAuthorityName) {
        String xquery = String.format("xquery version \"1.0-ml\";\n" +
            "import module namespace sec = \"http://marklogic.com/xdmp/security\" at \"/MarkLogic/security.xqy\"; \n" +
            "import module namespace pki = \"http://marklogic.com/xdmp/pki\" at \"/MarkLogic/pki.xqy\";\n" +
            "declare namespace x509 = \"http://marklogic.com/xdmp/x509\";\n" +
            "\n" +
            "pki:authority-create-client-certificate(\n" +
            "  xdmp:credential-id(\"%s\"),\n" +
            "  element x509:subject {\n" +
            "    element x509:countryName            {\"US\"},\n" +
            "    element x509:stateOrProvinceName    {\"California\"},\n" +
            "    element x509:localityName           {\"San Carlos\"},\n" +
            "    element x509:organizationName       {\"ProgressMarkLogic\"},\n" +
            "    element x509:organizationalUnitName {\"Engineering\"},\n" +
            "    element x509:commonName             {\"JavaClientCertificateUser\"},\n" +
            "    element x509:emailAddress           {\"java.client@example.org\"}\n" +
            "  },\n" +
            "  fn:current-dateTime(),\n" +
            "  fn:current-dateTime() + xs:dayTimeDuration(\"P365D\"))\n", certificateAuthorityName);

        EvalResultIterator iter = securityClient.newServerEval().xquery(xquery).eval();
        String cert = null;
        String key = null;
        while (iter.hasNext()) {
            if (cert == null) {
                cert = iter.next().getString();
            } else {
                key = iter.next().getString();
            }
        }
        return new ClientCertificate(cert, key);
    }

    private void makeAppServerRequireTwoWaySsl(String serverName, String templateName, String certificateAuthorityId) {
        String certificateAuthorityCertificate = getCertificateAuthorityCertificate(certificateAuthorityId);
        ObjectNode payload = newServerPayload(serverName)
            .put("ssl-certificate-template", templateName)
            .put("ssl-require-client-certificate", true)
            .put("ssl-client-issuer-authority-verification", true);
        payload.putArray("ssl-client-certificate-pem").add(certificateAuthorityCertificate);
        logger.info("Requiring two-way SSL on app server: {}", serverName);
        new ServerManager(manageClient).save(payload.toString());
    }

    /**
     * Couldn't find a Manage API endpoint that returns the CA certificate, so directly accessing the Security
     * database and reading a known URI to get an XML document associated with the CA's secure credential, which has
     * the certificate in it.
     */
    private String getCertificateAuthorityCertificate(String certificateAuthorityId) {
        String certificateUri = String.format("http://marklogic.com/xdmp/credentials/%s", certificateAuthorityId);
        String xml = securityClient.newXMLDocumentManager().read(certificateUri, new StringHandle()).get();
        return new Fragment(xml).getElementValue("/sec:credential/sec:credential-certificate");
    }

    private void deleteCertificateAuthority(String certificateAuthorityName) {
        String xquery = String.format("xquery version \"1.0-ml\";\n" +
            "import module namespace pki = \"http://marklogic.com/xdmp/pki\" at \"/MarkLogic/pki.xqy\";\n" +
            "\n" +
            "pki:delete-authority(\"%s\")", certificateAuthorityName);

        securityClient.newServerEval().xquery(xquery).evalAs(String.class);
    }

    private void removeTwoWaySslConfig(String serverName) {
        ObjectNode payload = newServerPayload(serverName)
            .put("ssl-certificate-template", "")
            .put("ssl-require-client-certificate", false)
            .put("ssl-client-issuer-authority-verification", false);
        payload.putArray("ssl-client-certificate-pem");
        logger.info("Removing two-way SSL config from app server: {}", serverName);
        new ServerManager(manageClient).save(payload.toString());
    }

    private String makeCertificateTemplate(String templateName) {
        return String.format("<certificate-template-properties xmlns=\"http://marklogic.com/manage\">\n" +
            "  <template-name>%s</template-name>\n" +
            "  <template-description>Sample description</template-description>\n" +
            "  <key-type>rsa</key-type>\n" +
            "  <key-options />\n" +
            "  <req>\n" +
            "    <version>0</version>\n" +
            "    <subject>\n" +
            "      <countryName>US</countryName>\n" +
            "      <stateOrProvinceName>CA</stateOrProvinceName>\n" +
            "      <localityName>San Carlos</localityName>\n" +
            "      <organizationName>MarkLogic</organizationName>\n" +
            "      <organizationalUnitName>Engineering</organizationalUnitName>\n" +
            "      <emailAddress>ssl-test@marklogic.com</emailAddress>\n" +
            "    </subject>\n" +
            "  </req>\n" +
            "</certificate-template-properties>", templateName);
    }

    private ObjectNode newServerPayload(String serverName) {
        ObjectNode payload = new ObjectMapper().createObjectNode();
        payload.put("server-name", serverName);
        payload.put("group-name", "Default");
        return payload;
    }

    /**
     * Writes the client certificate PEM and private keys to disk so that they can accessed by the openssl program.
     *
     * @param clientCertificate
     * @param tempDir
     * @throws IOException
     */
    private void writeClientCertificateFilesToTempDir(ClientCertificate clientCertificate, Path tempDir) throws IOException {
        File certFile = new File(tempDir.toFile(), "cert.pem");
        FileCopyUtils.copy(clientCertificate.pemEncodedCertificate.getBytes(), certFile);
        File keyFile = new File(tempDir.toFile(), "client.key");
        FileCopyUtils.copy(clientCertificate.privateKey.getBytes(), keyFile);
    }

    /**
     * See https://stackoverflow.com/a/8224863/3306099 for where this approach was obtained from.
     */
    private void createPkcs12File(Path tempDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(tempDir.toFile());
        builder.command("openssl", "pkcs12", "-export",
            "-in", "cert.pem", "-inkey", "client.key",
            "-out", "client.p12",
            "-name", "my-client",
            "-passout", "pass:" + DEFAULT_PASSWORD);

        int exitCode = runProcess(builder);
        assertEquals(0, exitCode, "Unable to create pkcs12 file using openssl");
    }

    private File createKeystoreFile(Path tempDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(tempDir.toFile());
        final String filename = "keyStore.jks";
        builder.command("keytool", "-importkeystore",
            "-deststorepass", DEFAULT_PASSWORD,
            "-destkeypass", DEFAULT_PASSWORD,
            "-destkeystore", filename,
            "-srckeystore", "client.p12",
            "-srcstoretype", "PKCS12",
            "-srcstorepass", DEFAULT_PASSWORD,
            "-alias", "my-client");

        int exitCode = runProcess(builder);
        assertEquals(0, exitCode, "Unable to create keystore using keytool");
        return new File(tempDir.toFile(), filename);
    }

    /**
     * Retrieves the server certificate associated with the certificate template for this test and stores it in the
     * key store so that the key store can also act as a trust store.
     *
     * @param tempDir
     * @throws Exception
     */
    private void addServerCertificateToTrustStore(String templateName, Path tempDir, File trustStoreFile) throws Exception {
        Fragment xml = new CertificateTemplateManager(manageClient).getCertificatesForTemplate(templateName);
        String serverCertificate = xml.getElementValue("/msec:certificate-list/msec:certificate/msec:pem");

        File certificateFile = new File(tempDir.toFile(), "server.cert");
        FileCopyUtils.copy(serverCertificate.getBytes(), certificateFile);

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(tempDir.toFile());
        builder.command("keytool", "-importcert",
            "-keystore", trustStoreFile.getAbsolutePath(),
            "-storepass", DEFAULT_PASSWORD,
            "-file", certificateFile.getAbsolutePath(),
            "-noprompt",
            "-alias", templateName + "-certificate");

        int exitCode = runProcess(builder);
        assertEquals(0, exitCode, "Unable to add server public certificate to keystore.");
    }

    private int runProcess(ProcessBuilder builder) throws Exception {
        Process process = builder.start();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new StreamGobbler(process.getInputStream(), System.out::println));
        executorService.submit(new StreamGobbler(process.getErrorStream(), System.err::println));
        return process.waitFor();
    }

    /**
     * Copied from https://www.baeldung.com/run-shell-command-in-java .
     */
    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                .forEach(consumer);
        }
    }

    private static class ClientCertificate {
        final String pemEncodedCertificate;
        final String privateKey;

        public ClientCertificate(String pemEncodedCertificate, String privateKey) {
            this.pemEncodedCertificate = pemEncodedCertificate;
            this.privateKey = privateKey;
        }
    }

    public static class TwoWaySslConfig {
        private final File keyStoreFile;
        private final String keyStorePassword;
        private final File trustStoreFile;
        private final String trustStorePassword;

        private TwoWaySslConfig(File keyStoreFile, String keyStorePassword, File trustStoreFile, String trustStorePassword) {
            this.keyStoreFile = keyStoreFile;
            this.trustStoreFile = trustStoreFile;
            this.keyStorePassword = keyStorePassword;
            this.trustStorePassword = trustStorePassword;
        }

        public File getKeyStoreFile() {
            return keyStoreFile;
        }

        public File getTrustStoreFile() {
            return trustStoreFile;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }
    }


}

