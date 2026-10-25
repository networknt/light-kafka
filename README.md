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

Confluent 8.3.2 remains Jackson 2. The `kafka-common` and `kafka-producer` POMs
explicitly pin Jackson 2 databind 2.22.3 for that boundary; the parent manages both
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
