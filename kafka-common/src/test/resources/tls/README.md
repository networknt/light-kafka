# Local TLS test fixture

The checked-in RSA private key is public test data, used only by a loopback
server. Never use it outside tests. The self-signed certificate has a single
DNS SAN, `localhost`, and is trusted only by the fixture's in-memory truststore.

Generated with:

```sh
openssl req -x509 -newkey rsa:2048 -nodes -days 36500 \
  -keyout localhost-key.pem -out localhost-cert.pem \
  -subj /CN=localhost -addext subjectAltName=DNS:localhost
```

The handshake tests use `wrong.invalid` as the logical TLS peer name over an
already connected loopback socket, so no DNS lookup or external service is used.
Socket, accept and future timeouts bound failures; the fixture closes sockets
and joins its server thread on every path.
