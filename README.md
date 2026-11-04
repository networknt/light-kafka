# light-kafka
common shared modules for Kafka producer, consumer and streams

```
./gradlew clean build install
```

## Consumer partition fetch default upgrade

The fallback for `max.partition.fetch.bytes` changes from 100 bytes to 102400
bytes (100 KiB). This affects consumers loading the generated configuration
without an explicit override. It matches existing light-codegen and kafka-sidecar
consumer templates and the existing `fetch.max.bytes=102400` fetch budget.

To preserve the previous setting, explicitly set
`kafka-consumer.max.partition.fetch.bytes: 100` in your external values, or set
`properties.max.partition.fetch.bytes: 100` in your consumer configuration.
Existing explicit values remain effective.

The larger per-partition allowance can increase the data returned in a fetch and
consumer buffering. `fetch.max.bytes` remains unchanged, but neither fetch setting
is a hard memory cap: Kafka can return the first oversized record batch to ensure
progress, and consumers can issue multiple fetches concurrently. This correction
has configuration tests; throughput, latency and peak memory have not been
benchmarked. Validate those metrics with representative record batches, partition
counts and concurrency before rolling it out to a memory-constrained deployment.

## Schema auto-registration

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
