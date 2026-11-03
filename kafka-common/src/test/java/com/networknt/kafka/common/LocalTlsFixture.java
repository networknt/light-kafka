package com.networknt.kafka.common;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Loopback-only JSSE fixture with a trusted certificate valid only for localhost. */
final class LocalTlsFixture {
    static void handshake(String algorithm, String hostname) throws Exception {
        SSLContext context = context();
        var executor = Executors.newSingleThreadExecutor();
        try (SSLServerSocket server = (SSLServerSocket) context.getServerSocketFactory()
                .createServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(5000);
            var peer = executor.submit(() -> {
                try (SSLSocket socket = (SSLSocket) server.accept()) {
                    socket.setSoTimeout(5000);
                    socket.startHandshake();
                    socket.getOutputStream().write(42);
                } catch (java.io.IOException expectedForRejectedClient) {
                    // The client is the assertion boundary; rejection alerts are expected here.
                }
            });
            try (Socket transport = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort());
                 SSLSocket client = (SSLSocket) context.getSocketFactory().createSocket(transport, hostname, server.getLocalPort(), true)) {
                client.setSoTimeout(5000);
                SSLParameters parameters = client.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm(algorithm);
                client.setSSLParameters(parameters);
                client.startHandshake();
                if (client.getInputStream().read() != 42) throw new AssertionError("Server did not complete the handshake");
            } finally {
                peer.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) throw new AssertionError("TLS fixture thread did not stop");
        }
    }

    private static SSLContext context() throws Exception {
        java.security.cert.Certificate certificate;
        try (var input = LocalTlsFixture.class.getResourceAsStream("/tls/localhost-cert.pem")) {
            certificate = CertificateFactory.getInstance("X.509").generateCertificate(input);
        }
        String pem;
        try (var input = LocalTlsFixture.class.getResourceAsStream("/tls/localhost-key.pem")) {
            pem = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
        }
        var privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getMimeDecoder()
                .decode(pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", ""))));
        char[] password = "test-only".toCharArray();
        KeyStore keys = KeyStore.getInstance("PKCS12");
        keys.load(null, password);
        keys.setKeyEntry("localhost", privateKey, password, new java.security.cert.Certificate[] {certificate});
        var keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagers.init(keys, password);
        KeyStore trust = KeyStore.getInstance("PKCS12");
        trust.load(null, password);
        trust.setCertificateEntry("localhost", certificate);
        var trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagers.init(trust);
        var context = SSLContext.getInstance("TLS");
        context.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);
        return context;
    }
}
