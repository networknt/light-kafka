# light-kafka
common shared modules for Kafka producer, consumer and streams

```
./gradlew clean build install
```

### Schema auto-registration

Use `properties.auto.register.schemas` in producer, consumer, or Streams configuration;
this controls serialization, not consumer deserialization, and defaults to `true`.
The bundled templates use `kafka-producer.auto.register.schemas`,
`kafka-consumer.auto.register.schemas`, and `kafka-streams.auto.register.schemas`
in `values.yml`. Rename any old `kafka-*.schema.registry.auto.register.schemas`
overrides to these names; old overrides are unused and the default remains `true`.
To request lookup-only behavior explicitly, set the canonical property to `false`.

Confluent ignores the old Kafka property `schema.registry.auto.register.schemas`;
Light logs a warning but does not translate it. Light's own Avro serializer is the
exception: it historically supported that spelling and retains it as a fallback
when the canonical key is absent. The canonical key wins when both are supplied.
Light strictly validates Boolean or true/false string values during serialization.
Users of Light's serializer should migrate old overrides explicitly, especially
`false` values: renaming the bundled template placeholder otherwise restores the
`true` default. Pre-register required schemas before switching to lookup-only mode.

## TLS hostname verification migration (#265)

Generated producer, consumer and Streams defaults now use
`ssl.endpoint.identification.algorithm=https`, matching Kafka 4.3.1 and
Confluent 8.3.2. The previous `also-name` default (and downstream `algo-name`
samples) is accepted as a string by configuration parsing but fails a real
Java TLS handshake with an unknown identification algorithm error.

Existing external keys `kafka-*.ssl.endpoint.identification.algorithm` are
unchanged. Remove an explicit `also-name`/`algo-name` override or replace it
with `https`. Certificates must match the broker hostname; correct the
certificate SAN or connection hostname if the new setting exposes a mismatch.
An empty string disables hostname verification and must be configured
explicitly, for example `kafka-producer.ssl.endpoint.identification.algorithm: ""`
in `values.yml`. This weakens TLS peer verification; it is never the default.
Schema Registry's separate `schema.registry.ssl.*` settings remain unchanged.
No deployed configuration is changed by this source update.

See `doc/kafka-config-defaults.md` for the pinned-default audit and retained
framework overrides. `KafkaSslEndpointIdentificationTest` exercises generated
resources, annotations, injected overrides and local JSSE handshakes.
