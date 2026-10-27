# light-kafka
common shared modules for Kafka producer, consumer and streams

```
./gradlew clean build install
```

## Jackson 3 migration

The main development line uses Jackson 3.2.3 (`tools.jackson.*`), with Java 17+
required by Jackson (this project's build targets Java 25). The `2.x` maintenance
branch preserves the pre-migration source. This is a source/binary API migration:
`ProduceRecord`, `SchemaConverter.JsonNodeAndSize`, serializer inputs and mapper
parameters now use Jackson 3 types. Versions are not changed by this work.

Application mappers use `Jackson2Defaults` from the migrated light-4j config
module, including its measured Jackson 2 feature and stream-constraint profile.
Jackson 3 mappers are immutable: configure a builder rather than calling mapper
`configure` or `registerModule`. Jackson exceptions are unchecked; keep existing
handled-error boundaries and catch `JacksonException` alongside checked I/O where
both operations occur. Annotations stay in `com.fasterxml.jackson.annotation`.

Confluent 8.3.2 remains Jackson 2. Only `kafka-producer` declares Jackson 2 databind directly for that boundary;
`kafka-common` receives it transitively from Confluent. The parent pins 2.22.3
and manages both
distinct Jackson BOMs. Their runtime dependencies are inherited by consumers,
so dependency trees contain both Jackson versions. Do not remove Jackson 2 from
the runtime classpath while using Confluent. All application/REST tree types
are Jackson 3; only the package-private producer `Jackson2Bridge` and the private
`NoSchemaRecordSerializer.JsonSerializer` use Jackson 2 types. No Jackson 2 type
is added to a public producer signature. Boundary tests intentionally exercise
Jackson 2 decoders/reference serializers.

The bridge crosses through JSON bytes, selectively retaining explicit decimal
nodes and their scale. Ordinary doubles retain their wire representation,
including signed zero and exponent notation. Internal bridge parsers accept the
serialized contents of already-constructed trees without introducing new string,
field-name or number-length limits; request/config parsing and writer nesting
limits are unchanged. Keeping Confluent's raw JSON
serializer preserves options such as `json.indent.output`. Converter byte outputs
are parsed back into Jackson 3 trees. Avro/JSON Schema/Protobuf framing and handled
schema-error codes remain unchanged.

Build against the migrated light-4j/http-client snapshots from source before
`mvn clean test` and local installation. Unchanged snapshot coordinates can resolve
an older Jackson 2 artifact in a clean published-artifact build; no snapshots
are deployed here. `ConfluentBoundaryTest` uses Confluent's in-memory mock registry,
not a broker or running schema registry. Tests cover raw JSON options, schema
serialization, conversion errors and numeric/Unicode/null round trips.

The source-build workflow resolves prerequisites to full commit SHAs before
checkout, builds with a fresh Maven repository, and uploads `source-refs.json`,
`source-commits.txt`, logs and test reports. Manual `source_refs` replay becomes
available after the workflow reaches the default branch; before merge, reproduce
locally using those recorded SHAs. No remote snapshot deployment is required.

`Jackson3GoldenWireTest` compares 20 records with frozen output from the `2.x`
source, including exact Avro, JSON Schema and Protobuf frames. Expected bytes are
not generated through the bridge. The bridge is forward-only; decimal repair
updates only decimal leaves rather than rewriting every field.

Downstream migration remains required: a targeted compile of Portal’s
`SidecarFailureEnvelopePublisher` fails at its Jackson 2 mapper assignment and
Jackson 2 nodes passed to `ProduceRecord.create`. `user-query` also uses the Kafka
consumer API; `maproot-query` uses streams and `schedule-query` declares Kafka
dependencies. These consumers must be qualified in Phase 7, with Portal last.
The unchanged snapshot coordinates must not be treated as a compatible 2.x
release; publication/version coordination is required before downstream rollout.

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
