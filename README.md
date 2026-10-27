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
