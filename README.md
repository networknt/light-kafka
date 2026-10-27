# light-kafka
common shared modules for Kafka producer, consumer and streams

```
./gradlew clean build install
```

### Schema auto-registration

Set `properties.auto.register.schemas` in `kafka-producer.yml`, `kafka-consumer.yml`,
or `kafka-streams.yml` (for example, `properties: {auto.register.schemas: false}`).
This controls serialization, not consumer deserialization; the default is `true`.
The old Kafka property `schema.registry.auto.register.schemas` remains an input
alias with a warning. If both are supplied, `auto.register.schemas` wins and the
warning says the alias is ignored. Invalid values are rejected by the serializer.

The bundled templates retain the supported `values.yml` override names
`kafka-producer.schema.registry.auto.register.schemas`,
`kafka-consumer.schema.registry.auto.register.schemas`, and
`kafka-streams.schema.registry.auto.register.schemas`. Those names are not deprecated.

**Upgrade:** Confluent previously ignored the old Kafka property, so deployments
setting it to `false` may have auto-registered schemas. They now switch to lookup-only:
pre-register required schemas (including Streams internal subjects), or explicitly
set `auto.register.schemas: true` if registration is intended. Light's own Avro
serializer already honored the old spelling.
