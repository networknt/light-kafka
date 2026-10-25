# Frozen Jackson 2 wire baseline

Generated before review changes from light-kafka commit
7b25cfaf641ed46003bde59e9f8b6ec39f7bb24a (the protected 2.x branch).
The original SchemaRecordSerializer, NoSchemaRecordSerializer, ProduceRecord
and ProduceRequest were compiled in a separate JVM with Jackson 2.22.3.
Inputs are reproduced in Jackson3GoldenWireTest. Registry schemas are registered
in fixed Avro/JSON Schema/Protobuf order, giving deterministic IDs 1, 2 and 3.

Twenty records preserve exact Base64 wire bytes, null behavior and handled error
outcomes, including compact/indented JSON, explicit decimal scale, binary/string,
audit JSON and produce-request bodies. Tests read this file; the production bridge
and current converters are not used to generate expected data.

Do not regenerate from Jackson 3. Baseline generation source and outputs are kept
under artifacts/implementation/light-4j/jackson-3-migration/harness/phase6-wire/2
as required by the repository's evidence rules.
