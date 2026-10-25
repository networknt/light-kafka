package com.networknt.kafka.producer;
import com.networknt.kafka.entity.EmbeddedFormat;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.*;
import io.confluent.kafka.schemaregistry.json.*;
import io.confluent.kafka.schemaregistry.protobuf.*;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import java.util.*;
class Jackson3GoldenWireTest {
  private MockSchemaRegistryClient registry;
  @org.junit.jupiter.api.AfterEach
  void resetRegistry() { if (registry != null) registry.reset(); }
  @org.junit.jupiter.api.Test
  void matchesTheFrozenJackson2WireBaseline() throws Exception {
    java.util.List<String> actual = new java.util.ArrayList<>();
    var mapper = com.networknt.config.Jackson2Defaults.mapper();
    String[] corpus={"{\"text\":\"雪\",\"array\":[null,true,1.25,123456789012345678901234567890]}", "[1.0,-0.0,1.0E20]", "null", "42", "\"hello\""};
    for(boolean indent:new boolean[]{false,true}) {
      var raw=new NoSchemaRecordSerializer(Map.of("json.indent.output",indent));
      for(int i=0;i<corpus.length;i++) {
        var value=raw.serialize(i,EmbeddedFormat.JSON,mapper.readTree(corpus[i]));
        actual.add("WIRE|json-"+indent+"-"+i+"|"+(value.isEmpty()?"NULL":Base64.getEncoder().encodeToString(value.get().toByteArray())));
      }
    }
    var raw=new NoSchemaRecordSerializer(Map.of());
    var precise=mapper.createObjectNode();
    precise.put("decimal",new java.math.BigDecimal("0.1234567890123456789012345678900"));
    actual.add("WIRE|precise-decimal|"+Base64.getEncoder().encodeToString(raw.serialize(0,EmbeddedFormat.JSON,precise).get().toByteArray()));
    actual.add("WIRE|binary|"+Base64.getEncoder().encodeToString(raw.serialize(0,EmbeddedFormat.BINARY,mapper.readTree("\"AAH/\"")).get().toByteArray()));
    actual.add("WIRE|string|"+Base64.getEncoder().encodeToString(raw.serialize(0,EmbeddedFormat.STRING,mapper.readTree("\"雪\"")).get().toByteArray()));
    var audit=new com.networknt.kafka.entity.AuditRecord();
    audit.setId("fixed");audit.setTopic("events");audit.setTimestamp(123L);
    audit.setAuditType(com.networknt.kafka.entity.AuditRecord.AuditType.ACTIVE_CONSUMER);
    audit.setAuditStatus(com.networknt.kafka.entity.AuditRecord.AuditStatus.SUCCESS);
    actual.add("WIRE|audit|"+Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(audit)));
    var record=com.networknt.kafka.entity.ProduceRecord.create(mapper.readTree("1"),mapper.readTree(corpus[0]));
    record.setHeaders(Optional.of(Map.of("x","y")));record.setTimestamp(Optional.of(123L));
    var request=new com.networknt.kafka.entity.ProduceRequest();request.setRecords(List.of(record));
    actual.add("WIRE|produce-request|"+Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(request)));
    registry=(MockSchemaRegistryClient)MockSchemaRegistry.getClientForScope(UUID.randomUUID().toString(),List.of(new AvroSchemaProvider(),new JsonSchemaProvider(),new ProtobufSchemaProvider()));
    var config=Map.<String,Object>of("schema.registry.url","mock://parity");
    var serializer=new SchemaRecordSerializer(registry,config,config,config);
    ParsedSchema[] schemas={new AvroSchema("{\"type\":\"record\",\"name\":\"Item\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"qty\",\"type\":\"int\"}]}"),new JsonSchema("{\"type\":\"object\"}"),new ProtobufSchema("syntax=\"proto3\"; message Item {string name=1; int32 qty=2;}")};
    EmbeddedFormat[] formats={EmbeddedFormat.AVRO,EmbeddedFormat.JSONSCHEMA,EmbeddedFormat.PROTOBUF};
    for(int i=0;i<3;i++) {
      String topic="events"+i,subject=topic+"-value";
      int id=registry.register(subject,schemas[i]);
      var schema=new RegisteredSchema(subject,id,1,schemas[i]);
      String input=i==1?corpus[0]:"{\"name\":\"雪\",\"qty\":7}";
      byte[] bytes=serializer.serialize(0,formats[i],topic,Optional.of(schema),mapper.readTree(input),false).get().toByteArray();
      actual.add("WIRE|"+formats[i]+"|"+Base64.getEncoder().encodeToString(bytes));
    }
    try {raw.serialize(3,EmbeddedFormat.BINARY,mapper.readTree("\"!\""));}
    catch(RuntimeException ex){actual.add("WIRE|bad-binary|"+ex.getClass().getName()+"|"+ex.getMessage());}
    var badSchema=new JsonSchema("{\"type\":\"integer\"}");
    int id=registry.register("bad-value",badSchema);
    try {serializer.serialize(0,EmbeddedFormat.JSONSCHEMA,"bad",Optional.of(new RegisteredSchema("bad-value",id,1,badSchema)),mapper.readTree("\"invalid\""),false);}
    catch(com.networknt.exception.FrameworkException ex){actual.add("WIRE|bad-schema|"+ex.getStatus().getCode());}
    try (var stream = getClass().getResourceAsStream("/jackson2-baseline/wire.txt")) {
      org.junit.jupiter.api.Assertions.assertNotNull(stream);
      var expected = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).lines().toList();
      org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
  }
}
