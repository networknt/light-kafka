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
strings `true`/`false` are accepted. Shared configuration loading, reloading, and
`setProperties` translate the alias but preserve raw values, as they do for other
Kafka properties. Invalid values are rejected when configuring Confluent's
serializer or constructing Light's Avro serializer, with no default substituted. Light's Avro
serializer defaults to `true` when neither spelling is present, matching Confluent.
This setting controls serialization, including Streams output, rather than
consumer deserialization.

Light's Avro serializer captures the effective auto-registration flag when the
serializer is constructed. Later configuration reloads or map changes do not change
that instance's flag; construct a new serializer to apply an updated setting.

The bundled templates intentionally retain the existing external override names
`kafka-producer.schema.registry.auto.register.schemas`,
`kafka-consumer.schema.registry.auto.register.schemas`, and
`kafka-streams.schema.registry.auto.register.schemas` in `values.yml`, so existing
external overrides continue to work. Those placeholders now populate the
canonical Kafka property. Custom templates should likewise emit the canonical
property while retaining any external override names their deployments require.

**Upgrade behavior:** deployments that set the deprecated key to `false` while
using Confluent serializers may previously have auto-registered schemas because
Confluent ignored that key and defaulted to `true`. After this fix those deployments
switch to lookup-only serialization. Register the required schemas before upgrading,
or explicitly set `auto.register.schemas: true` if registration is intended. Light's
own Avro serializer already honored the deprecated key; that case does not change.
