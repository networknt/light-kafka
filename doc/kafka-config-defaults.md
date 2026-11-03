# Kafka configuration defaults audit (#265)

Audited against the resolved Kafka **4.3.1** producer, consumer and Streams
`ConfigDef` definitions and Confluent **8.3.2**
`AbstractKafkaSchemaSerDeConfig.baseConfigDef()`. The accompanying TSV inventories
all 75 annotated fields in the three PropertiesConfig models, including
framework-only fields. `KafkaConfigurationDefaultsAuditTest` regenerates the
inventory in `kafka-common/target/` and compares it with the reviewed TSV, so an
upstream/default change requires a new review.

`<required>` means `ConfigDef.NO_DEFAULT_VALUE`; `<null>` is a nullable default;
`<empty>` denotes an empty string.
Streams does not declare the SSL/client-only keys in its own ConfigDef: rows
labelled "Streams forwarding" use the pinned consumer definition for those
underlying-client properties. This comparison does not claim that every
consumer property is effective when passed to Streams; Streams controls its own
internal byte-array deserializers.

## TLS decision

The former `also-name` default is not a JSSE endpoint-identification algorithm.
`algo-name` in downstream examples is invalid too. Kafka's ConfigDef accepts
arbitrary algorithm strings, but the actual TLS handshake rejects both with
`Unknown identification algorithm`. The three generated defaults now use `https`,
matching the Kafka client SSL default. Confluent's independently prefixed
`schema.registry.ssl.endpoint.identification.algorithm` is also `https` in its
ConfigDef; those separate settings have not been changed.

The local JSSE fixture trusts a localhost-only certificate, accepts `localhost`,
rejects `wrong.invalid`, and demonstrates that only an explicitly empty algorithm
disables hostname checks. Generated YAML defaults, JSON schema defaults,
annotations and injected overrides are covered. Neither fixture nor fix contacts
a broker, registry or shared service.

## Retained framework choices

These values are deliberate framework/example settings rather than claims about
upstream defaults. This TLS change preserves them:

- Consumer manual commit (`false`), earliest offsets, a 100-record poll cap and
  102400-byte aggregate fetch cap support sidecar processing/cache control.
- Producer retries (`3`) and linger (`1` ms) are existing bounded retry/latency
  choices, distinct from Kafka's 2147483647 retries and 5 ms linger.
- Example bootstrap servers, group/application IDs, `/tmp` state directory,
  byte-array serializers/deserializers, rack names, SASL_SSL/PLAIN settings,
  truststore locations/password placeholders and USER_INFO registry credentials
  form the existing deployment scaffold. They must be supplied for the deployment;
  they are not Kafka/Confluent defaults. Consumer/Streams earliest-offset policy
  differs from the consumer ConfigDef's latest policy.
- `additionalKafkaProperties` and split SASL module/username/password fields are
  Light inputs; normalization synthesizes the actual `sasl.jaas.config` property.
  `schema.registry.cache=100` is consumed by LightSchemaRegistryClient, not the
  Confluent serializer ConfigDef. Producer `client.rack` is retained as a scaffold
  field but is not a recognized Kafka ProducerConfig property; no producer-side
  rack behavior is promised by this inventory.
- Matching defaults (acks, batch/buffer/request sizes, in-flight limit,
  auto-registration and endpoint identification) remain aligned as shown in TSV.

## Separate follow-ups

- [#266](https://github.com/networknt/light-kafka/issues/266): producer
  `enable.idempotence=false` contradicts its "Default to true" description and
  the pinned Kafka default. Delivery/transaction policy must be decided separately.
- [#267](https://github.com/networknt/light-kafka/issues/267): consumer
  `max.partition.fetch.bytes=100` versus Kafka's 1048576 has no documented
  framework rationale. Validate this independently before changing throughput policy.

## Regeneration and downstream scope

Use `mvn -o -q -P schema-generation clean test` (space after `-P`). The existing
processor detects the profile from separate JVM command arguments; joined
`-Pschema-generation` enables Maven's profile but does not trigger that detector.
This invocation regenerates the committed YAML/JSON schemas and their target
copies from annotations; inspect `git diff` for unrelated regeneration drift.

Both maintained branches of light-codegen and kafka-sidecar are included in
companion PRs. light-example-4j is excluded per maintainer direction because it
is planned for archival. All changes are source/configuration preparation only.
