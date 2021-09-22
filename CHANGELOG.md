# Change Log

## [2.0.31](https://github.com/networknt/light-kafka/tree/2.0.31) (2021-09-22)


**Merged pull requests:**


- fixes \#104 Sync the max.poll.interval.ms to instanceTimeoutMs if latt… [\#105](https://github.com/networknt/light-kafka/pull/105) ([stevehu](https://github.com/stevehu))
- fixes \#102, implement new endpoint [\#103](https://github.com/networknt/light-kafka/pull/103) ([GavinChenYan](https://github.com/GavinChenYan))
- fixes \#100 remove the kafka server dependency and add a workaround [\#101](https://github.com/networknt/light-kafka/pull/101) ([stevehu](https://github.com/stevehu))
- dlq implementation for kafka sidecar refactoring [\#99](https://github.com/networknt/light-kafka/pull/99) ([GavinChenYan](https://github.com/GavinChenYan))
- model and config change for kafka-sidecar issue 19 implementation [\#98](https://github.com/networknt/light-kafka/pull/98) ([GavinChenYan](https://github.com/GavinChenYan))
- fixes \#96 add correlationId, traceabilityId and key to the RecordProc… [\#97](https://github.com/networknt/light-kafka/pull/97) ([stevehu](https://github.com/stevehu))
- Issue94 [\#95](https://github.com/networknt/light-kafka/pull/95) ([GavinChenYan](https://github.com/GavinChenYan))
- fixes \#92 update the kafka-streams.yml format to separate Kafka prope… [\#93](https://github.com/networknt/light-kafka/pull/93) ([stevehu](https://github.com/stevehu))
- fixes \#90 update sidecar producer to use config serializer [\#91](https://github.com/networknt/light-kafka/pull/91) ([stevehu](https://github.com/stevehu))
- fixes \#88 Treat binary key as byte array instead of string in sync wi… [\#89](https://github.com/networknt/light-kafka/pull/89) ([stevehu](https://github.com/stevehu))
- fixes \#86 Missing break in the value format switch in the KafkaConsum… [\#87](https://github.com/networknt/light-kafka/pull/87) ([stevehu](https://github.com/stevehu))
- fixes \#84 Initialize schema coverters and use them based on the confi… [\#85](https://github.com/networknt/light-kafka/pull/85) ([stevehu](https://github.com/stevehu))
## [2.0.30](https://github.com/networknt/light-kafka/tree/2.0.30) (2021-08-23)


**Merged pull requests:**


- fixes \#82 update the kafka-producer.yml and kafka-consumer.yml based … [\#83](https://github.com/networknt/light-kafka/pull/83) ([stevehu](https://github.com/stevehu))
- fixes \#78 update consumer record to alllow key and value have differe… [\#81](https://github.com/networknt/light-kafka/pull/81) ([stevehu](https://github.com/stevehu))
- fixes \#79 add default key and value formats configuration for produce… [\#80](https://github.com/networknt/light-kafka/pull/80) ([stevehu](https://github.com/stevehu))
- fixes \#76 add kafka-streams to the dependency for parent and kafka-st… [\#77](https://github.com/networknt/light-kafka/pull/77) ([stevehu](https://github.com/stevehu))
- fixes \#74 Make KafkaConsumerReadTask public and expose exception public [\#75](https://github.com/networknt/light-kafka/pull/75) ([stevehu](https://github.com/stevehu))
- fixes \#72 Add KafkaAdminConfig for Kafka client admin configuration [\#73](https://github.com/networknt/light-kafka/pull/73) ([stevehu](https://github.com/stevehu))
## [2.0.29](https://github.com/networknt/light-kafka/tree/2.0.29) (2021-07-25)


**Merged pull requests:**


- fixes \#69 update the logging statement to allow the stacktrace output [\#70](https://github.com/networknt/light-kafka/pull/70) ([stevehu](https://github.com/stevehu))


## [2.0.28](https://github.com/networknt/light-kafka/tree/2.0.28) (2021-06-27)


**Merged pull requests:**


- Issue67 [\#68](https://github.com/networknt/light-kafka/pull/68) ([stevehu](https://github.com/stevehu))
- fixes \#65 fix a warning in the producer pom.xml [\#66](https://github.com/networknt/light-kafka/pull/66) ([stevehu](https://github.com/stevehu))
-  fix the build issue [\#64](https://github.com/networknt/light-kafka/pull/64) ([chenyan71](https://github.com/chenyan71))
- fixes \#62 sync the test config file kafka-consumer.yml and kafka-prod… [\#63](https://github.com/networknt/light-kafka/pull/63) ([stevehu](https://github.com/stevehu))
## [2.0.27](https://github.com/networknt/light-kafka/tree/2.0.27) (2021-05-25)


**Merged pull requests:**


- fixes \#60 move producer entities to the kafka-entity module [\#61](https://github.com/networknt/light-kafka/pull/61) ([stevehu](https://github.com/stevehu))
- fixes \#58 support tls connection to the schema-registry [\#59](https://github.com/networknt/light-kafka/pull/59) ([stevehu](https://github.com/stevehu))
- fixes \#56 add a test case to serialize the ProduceRequest into a JSON [\#57](https://github.com/networknt/light-kafka/pull/57) ([stevehu](https://github.com/stevehu))
- fixes \#54 Allow the consumer with Avro schema to use NoWrapping conve… [\#55](https://github.com/networknt/light-kafka/pull/55) ([stevehu](https://github.com/stevehu))
- fixes \#52 add deadLetterEnabled flag to the kafka-consumer.yml [\#53](https://github.com/networknt/light-kafka/pull/53) ([stevehu](https://github.com/stevehu))
- fixes \#50 exclude conflict dependencies from kafka-avro-serializer [\#51](https://github.com/networknt/light-kafka/pull/51) ([stevehu](https://github.com/stevehu))
- fixes \#48 update consumer and producer config to support dead letter … [\#49](https://github.com/networknt/light-kafka/pull/49) ([stevehu](https://github.com/stevehu))
## [2.0.26](https://github.com/networknt/light-kafka/tree/2.0.26) (2021-04-27)


**Merged pull requests:**


- fixes \#46 add backendApiHost and backendApiPath to kafka-consumer.yml [\#47](https://github.com/networknt/light-kafka/pull/47) ([stevehu](https://github.com/stevehu))
- fixes \#44 add kafka ConsumerRecord header to the response [\#45](https://github.com/networknt/light-kafka/pull/45) ([stevehu](https://github.com/stevehu))
- fixes \#42 add sync method to commit all current offsets [\#43](https://github.com/networknt/light-kafka/pull/43) ([stevehu](https://github.com/stevehu))
- fixes \#40 make the producer schema registry url configurable in the k… [\#41](https://github.com/networknt/light-kafka/pull/41) ([stevehu](https://github.com/stevehu))
- fixes \#38 update json property for ProduceRequest and allow NoSchemaR… [\#39](https://github.com/networknt/light-kafka/pull/39) ([stevehu](https://github.com/stevehu))
- fixes \#36 resolve the javadoc issues [\#37](https://github.com/networknt/light-kafka/pull/37) ([stevehu](https://github.com/stevehu))
- fixes \#34 only overwrite the group.id if it does not exsit in the kaf… [\#35](https://github.com/networknt/light-kafka/pull/35) ([stevehu](https://github.com/stevehu))
- fixes \#32 support keyFormat and valueFormat for reactive consumer opt… [\#33](https://github.com/networknt/light-kafka/pull/33) ([stevehu](https://github.com/stevehu))
- fixes \#30 update the producer and consumer config to support all Kafk… [\#31](https://github.com/networknt/light-kafka/pull/31) ([stevehu](https://github.com/stevehu))
- Kafka mesh [\#29](https://github.com/networknt/light-kafka/pull/29) ([stevehu](https://github.com/stevehu))
## [2.0.25](https://github.com/networknt/light-kafka/tree/2.0.25) (2021-03-28)


**Merged pull requests:**




## [2.0.24](https://github.com/networknt/light-kafka/tree/2.0.24) (2021-02-24)


**Merged pull requests:**


- Bump version.jackson from 2.10.4 to 2.12.1 [\#27](https://github.com/networknt/light-kafka/pull/27) ([dependabot](https://github.com/apps/dependabot))
- fixes \#25 throw RuntimeException when Kafka is down [\#26](https://github.com/networknt/light-kafka/pull/26) ([stevehu](https://github.com/stevehu))
- Enable idempotence for producer https://github.com/networknt/light-ka… [\#23](https://github.com/networknt/light-kafka/pull/23) ([stevehu](https://github.com/stevehu))
## [2.0.23](https://github.com/networknt/light-kafka/tree/2.0.23) (2021-01-29)


**Merged pull requests:**


- fixes \#20 make the consumer public so that it can be accessed from an… [\#21](https://github.com/networknt/light-kafka/pull/21) ([stevehu](https://github.com/stevehu))
- fixes \#18 add more producer and consumer implementations [\#19](https://github.com/networknt/light-kafka/pull/19) ([stevehu](https://github.com/stevehu))
## [2.0.22](https://github.com/networknt/light-kafka/tree/2.0.22) (2020-12-22)


**Merged pull requests:**


- fixes \#16 add config dependency to the kafka-common [\#17](https://github.com/networknt/light-kafka/pull/17) ([stevehu](https://github.com/stevehu))
- fixes \#14 add kafka headers to the producer record for security, trac… [\#15](https://github.com/networknt/light-kafka/pull/15) ([stevehu](https://github.com/stevehu))


## [2.0.21](https://github.com/networknt/light-kafka/tree/2.0.21) (2020-11-25)


**Merged pull requests:**




## [2.0.20](https://github.com/networknt/light-kafka/tree/2.0.20) (2020-11-05)


**Merged pull requests:**


## [2.0.19](https://github.com/networknt/light-kafka/tree/2.0.19) (2020-11-01)


**Merged pull requests:**


## [2.0.18](https://github.com/networknt/light-kafka/tree/2.0.18) (2020-10-01)


**Merged pull requests:**


## [2.0.17](https://github.com/networknt/light-kafka/tree/2.0.17) (2020-08-28)


**Merged pull requests:**
