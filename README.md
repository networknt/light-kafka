# light-kafka
common shared modules for Kafka producer, consumer and streams

```
./gradlew clean build install
```

### Schema auto-registration

Use the Confluent serializer property `auto.register.schemas` in the `properties`
map of `kafka-producer.yml`, `kafka-consumer.yml`, or `kafka-streams.yml`:

```yaml
properties:
  auto.register.schemas: false
```

The deprecated `schema.registry.auto.register.schemas` spelling remains accepted
as an input alias. Client property maps emit only `auto.register.schemas`; when
both spellings are supplied, the canonical spelling wins. Boolean values and
strings `true`/`false` are accepted; other values are rejected. Light's Avro
serializer defaults to `true` when neither spelling is present, matching Confluent.
This setting controls serialization, including Streams output, rather than
consumer deserialization.

The bundled templates intentionally retain the existing external override names
`kafka-producer.schema.registry.auto.register.schemas`,
`kafka-consumer.schema.registry.auto.register.schemas`, and
`kafka-streams.schema.registry.auto.register.schemas` in `values.yml`, so existing
external overrides continue to work. Those placeholders now populate the
canonical Kafka property. Custom templates should likewise emit the canonical
property while retaining any external override names their deployments require.
