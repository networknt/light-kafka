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

Reproduce without changing the frozen file (Java 25, Python 3.9+ and Maven):

```sh
# Use a fresh external evidence directory and the isolated source-build cache.
git fetch origin 7b25cfaf641ed46003bde59e9f8b6ec39f7bb24a
mvn -Dmaven.repo.local=/absolute/path/to/private-m2 clean install
mvn -Dmaven.repo.local=/absolute/path/to/private-m2 -pl kafka-producer \
  org.apache.maven.plugins:maven-dependency-plugin:3.7.0:build-classpath \
  -Dmdep.outputFile=/absolute/path/to/bootstrap.classpath
python3 scripts/verify-jackson2-wire-baseline.py \
  --maven-repo /absolute/path/to/private-m2 \
  --output-dir /absolute/path/to/new-evidence-directory
```

Run from the repository root. The verifier uses Maven offline after the source
build and explicit plugin preparation above. A clean install alone does not resolve
Maven’s dependency plugin; the verifier pins 3.7.0 and does not use plugin-prefix
lookup in offline mode. It extracts seven original serializer/entity classes from that exact
commit, compiles them with the committed
`kafka-producer/src/test/baseline/Jackson2WireBaseline.java`, and starts a separate
Jackson 2 JVM. Extracted classes precede dependency jars on the classpath; no
current producer classes, bridge or converters generate the expected bytes.
The dependency classpath uses this PR's pinned library versions, including
Jackson 2.22.3 and Confluent 8.3.2. This qualifies those original source classes
against these dependencies, rather than claiming a historical published-jar run.

The verifier writes extracted sources, classpath, compile/run logs, baseline SHA
and regenerated `wire.txt` outside the checkout, compares all 20 records, and
fails on any mismatch. It never overwrites the committed fixture. The generator
is outside Maven's test source root because it requires Jackson 2 APIs. No broker
or external registry is used. Do not regenerate expected bytes from Jackson 3.

`WireCorpus` and `WireScenario` share the input corpus, exact key manifest, schema
setup and wire-operation driver across both JVMs. Only the small typed Jackson 2
and Jackson 3 adapters differ. The verifier compiles this common driver separately
with the extracted original serializers; it does not load current producer code.
The golden factory checks the exact 20-key set before returning any test cases,
rejecting empty/truncated, malformed, duplicate and unknown records.
