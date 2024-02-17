package com.networknt.kafka.producer;

import com.fasterxml.jackson.databind.node.NullNode;
import com.google.protobuf.ByteString;
import com.networknt.config.Config;
import com.networknt.exception.FrameworkException;
import com.networknt.kafka.common.KafkaProducerConfig;
import com.networknt.kafka.entity.*;
import com.networknt.status.Status;
import com.networknt.utility.Constants;
import com.networknt.utility.Util;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * This is the guaranteed producer to ensure that the message is acknowledged from the Kafka brokers
 * before the service is respond to the consumer call. Although this producer is not the best one
 * for high throughput batch producing, it is the safest one. Once the caller receives the successful
 * response from the service, it can make sure that the message is on the Kafka cluster.
 *
 * @author Steve Hu
 */
public class SidecarProducer implements NativeLightProducer {
    static private final Logger logger = LoggerFactory.getLogger(SidecarProducer.class);
    public static final KafkaProducerConfig config = (KafkaProducerConfig) Config.getInstance().getJsonObjectConfig(KafkaProducerConfig.CONFIG_NAME, KafkaProducerConfig.class);
    public static Map<String, Optional<RegisteredSchema>> schemaCache = new ConcurrentHashMap<>();
    private static String FAILED_TO_GET_SCHEMA = "ERR12208";
    private SchemaManager schemaManager;
    private SchemaRecordSerializer schemaRecordSerializer;
    private NoSchemaRecordSerializer noSchemaRecordSerializer;

    public Producer<byte[], byte[]> producer;

    @Override
    public void open() {
        producer = new KafkaProducer<>(config.getProperties());
        Map<String, Object> configs = new HashMap<>();
        configs.putAll(config.getProperties());
        String url = (String) config.getProperties().get("schema.registry.url");
        Object cacheObj = config.getProperties().get("schema.registry.cache");
        int cache = 100;
        if (cacheObj != null && cacheObj instanceof String) {
            cache = Integer.valueOf((String) cacheObj);
        }
        SchemaRegistryClient schemaRegistryClient = new CachedSchemaRegistryClient(
                new RestService(singletonList(url)),
                cache,
                Arrays.asList(new AvroSchemaProvider(), new JsonSchemaProvider(), new ProtobufSchemaProvider()),
                configs,
                null
        );
        noSchemaRecordSerializer = new NoSchemaRecordSerializer(new HashMap<>());
        schemaRecordSerializer = new SchemaRecordSerializer(schemaRegistryClient, configs, configs, configs);
        schemaManager = new SchemaManagerImpl(schemaRegistryClient, new TopicNameStrategy());
        // register the config to the module registry to output in server info.
        registerModule();
    }

    @Override
    public Producer getProducer() {
        return producer;
    }

    @Override
    public void close() {
        if(producer != null) {
            producer.close();
        }
    }

    public final CompletableFuture<ProduceResponse> produceWithSchema(
            String topicName,
            String serviceId,
            Optional<Integer> partition,
            ProduceRequest request,
            Headers headers, List<AuditRecord> auditRecords,
            boolean isReplay) {
        // get key schema based on different scenarios.
        long startSchema = System.currentTimeMillis();
        Optional<RegisteredSchema> keySchema = Optional.empty();
        if(null != request.getKeySchemaId() && request.getKeySchemaId().isPresent()) {
            // get from the cache first if keySchemaId is not empty.
            keySchema = schemaCache.get(topicName + "k" + request.getKeySchemaId().get());
        } else if (null !=request.getKeySchemaVersion() &&  request.getKeySchemaVersion().isPresent()) {
            // get form the cache first if KeySchemaVersion is not empty
            if(null != request.getKeySchemaSubject() && request.getKeySchemaSubject().isPresent()) {
                // use the supplied subject
                keySchema = schemaCache.get(request.getKeySchemaSubject().get() + request.getKeySchemaVersion().get());
            } else {
                // default to topic + isKey
                keySchema = schemaCache.get(topicName + "k" + request.getKeySchemaVersion().get());
            }
        }else if (isReplay) {
            //get the schema with topic name and v- value type
            keySchema = schemaCache.get(topicName + "k");
        }
        // reset the KeySchema as the cache will return null if the entry doesn't exist.
        if(keySchema == null) keySchema = Optional.empty();

        if(keySchema.isEmpty() && request.getKeyFormat().isPresent() && request.getKeyFormat().get().requiresSchema()) {
            keySchema =
                    getSchema(
                            topicName,
                            request.getKeyFormat(),
                            request.getKeySchemaSubject(),
                            request.getKeySchemaId(),
                            request.getKeySchemaVersion(),
                            request.getKeySchema(),
                            /* isKey= */ true);
            if(keySchema.isPresent()) {
                if(request.getKeySchemaId().isPresent()) {
                    schemaCache.put(topicName + "k" + request.getKeySchemaId().get(), keySchema);
                } else if(request.getKeySchemaVersion().isPresent()) {
                    if(request.getKeySchemaSubject().isPresent()) {
                        schemaCache.put(request.getKeySchemaSubject().get() + request.getKeySchemaVersion().get(), keySchema);
                    } else {
                        schemaCache.put(topicName + "k" + request.getKeySchemaVersion().get(), keySchema);
                    }
                } else if(isReplay){
                    schemaCache.put(topicName + "k" , keySchema);
                } else {
                    logger.error("Could not put key schema into the cache. It means that neither keySchemaId nor keySchemaVersion is supplied and Kafka Schema Registry will be overloaded.");
                }
            }
        }
        Optional<EmbeddedFormat> keyFormat =
                keySchema.map(schema -> Optional.of(schema.getFormat()))
                        .orElse(request.getKeyFormat());

        // get value schema based on different scenarios.
        Optional<RegisteredSchema> valueSchema = Optional.empty();
        if(null !=request.getValueSchemaId() &&  request.getValueSchemaId().isPresent()) {
            // get from the cache first if ValueSchemaId is not empty
            valueSchema = schemaCache.get(topicName + "v" + request.getValueSchemaId().get());
        } else if (null !=request.getValueSchemaVersion() && request.getValueSchemaVersion().isPresent()) {
            // get from the cache first if ValueSchemaVersion is not empty
            if(null != request.getValueSchemaSubject() && request.getValueSchemaSubject().isPresent()) {
                // use the supplied subject
                valueSchema = schemaCache.get(request.getValueSchemaSubject().get() + request.getValueSchemaVersion().get());
            } else {
                // default to topic + isKey
                valueSchema = schemaCache.get(topicName + "v" + request.getValueSchemaVersion().get());
            }
        }else if (isReplay) {
            //get the schema with topic name and v- value type
            valueSchema = schemaCache.get(topicName + "v");
        }
        // reset the valueSchema as the cache will return null if the entry doesn't exist.
        if(valueSchema == null) valueSchema = Optional.empty();

        if(valueSchema.isEmpty() && request.getValueFormat().isPresent() && request.getValueFormat().get().requiresSchema()) {
            valueSchema =
                    getSchema(
                            topicName,
                            request.getValueFormat(),
                            request.getValueSchemaSubject(),
                            request.getValueSchemaId(),
                            request.getValueSchemaVersion(),
                            request.getValueSchema(),
                            /* isKey= */ false);
            if(valueSchema.isPresent()) {
                if(request.getValueSchemaId().isPresent()) {
                    schemaCache.put(topicName + "v" + request.getValueSchemaId().get(), valueSchema);
                } else if(request.getValueSchemaVersion().isPresent()) {
                    if(request.getValueSchemaSubject().isPresent()) {
                        schemaCache.put(request.getValueSchemaSubject().get() + request.getValueSchemaVersion().get(), valueSchema);
                    } else {
                        schemaCache.put(topicName + "v" + request.getValueSchemaVersion().get(), valueSchema);
                    }
                } else if(isReplay){
                        schemaCache.put(topicName + "v" , valueSchema);
                }else {
                    logger.error("Could not put value schema into the cache. It means that neither valueSchemaId nor valueSchemaVersion is supplied and Kafka Schema Registry will be overloaded.");
                }
            }
        }
        Optional<EmbeddedFormat> valueFormat =
                valueSchema.map(schema -> Optional.of(schema.getFormat()))
                        .orElse(request.getValueFormat());

        List<SerializedKeyAndValue> serialized =
                serialize(
                        keyFormat,
                        valueFormat,
                        topicName,
                        partition,
                        keySchema,
                        valueSchema,
                        request.getRecords());
        if(logger.isDebugEnabled()) {
            logger.debug("Serializing key and value with schema registry takes " + (System.currentTimeMillis() - startSchema));
        }
        long startProduce = System.currentTimeMillis();
        List<CompletableFuture<ProduceResult>> resultFutures = doProduce(topicName, serviceId, serialized, headers, auditRecords);
        if(logger.isDebugEnabled()) {
            logger.debug("Producing the entire batch to Kafka takes " + (System.currentTimeMillis() - startProduce));
        }
        return produceResultsToResponse(keySchema, valueSchema, resultFutures);
    }

    public final CompletableFuture<ProduceResponse> produceWithSchema(
            String topicName,
            String serviceId,
            Optional<Integer> partition,
            ProduceRequest request,
            Headers headers, List<AuditRecord> auditRecords) {
        return produceWithSchema(topicName, serviceId, partition, request, headers, auditRecords, false);
    }

    private List<SerializedKeyAndValue> serialize(
            Optional<EmbeddedFormat> keyFormat,
            Optional<EmbeddedFormat> valueFormat,
            String topicName,
            Optional<Integer> partition,
            Optional<RegisteredSchema> keySchema,
            Optional<RegisteredSchema> valueSchema,
            List<ProduceRecord> records) {

        AtomicInteger atomicInteger= new AtomicInteger(0);
        return records.stream()
                .map(
                        record ->
                        {
                            int atomicIntegerNew=atomicInteger.getAndIncrement();
                            return new SerializedKeyAndValue(
                                    record.getPartition().map(Optional::of).orElse(partition),
                                    record.getTraceabilityId(),
                                    record.getCorrelationId(),
                                    keyFormat.isPresent() && keyFormat.get().requiresSchema() ?
                                            schemaRecordSerializer
                                                    .serialize(
                                                            atomicIntegerNew,
                                                            keyFormat.get(),
                                                            topicName,
                                                            keySchema,
                                                            record.getKey().orElse(NullNode.getInstance()),
                                                            /* isKey= */ true) :
                                            noSchemaRecordSerializer
                                                    .serialize(atomicIntegerNew, keyFormat.orElse(EmbeddedFormat.valueOf(config.getKeyFormat().toUpperCase())), record.getKey().orElse(NullNode.getInstance())),
                                    valueFormat.isPresent() && valueFormat.get().requiresSchema() ?
                                            schemaRecordSerializer
                                                    .serialize(
                                                            atomicIntegerNew,
                                                            valueFormat.get(),
                                                            topicName,
                                                            valueSchema,
                                                            record.getValue().orElse(NullNode.getInstance()),
                                                            /* isKey= */ false) :
                                            noSchemaRecordSerializer
                                                    .serialize(atomicIntegerNew, valueFormat.orElse(EmbeddedFormat.valueOf(config.getValueFormat().toUpperCase())), record.getValue().orElse(NullNode.getInstance())),
                                    record.getHeaders(),
                                    record.getTimestamp());
                        })
                .collect(Collectors.toList());
    }

    private Optional<RegisteredSchema> getSchema(
            String topicName,
            Optional<EmbeddedFormat> format,
            Optional<String> subject,
            Optional<Integer> schemaId,
            Optional<Integer> schemaVersion,
            Optional<String> schema,
            boolean isKey) {

        try {
            return Optional.of(
                    schemaManager.getSchema(
                            /* topicName= */ topicName,
                            /* format= */ format,
                            /* subject= */ subject,
                            /* subjectNameStrategy= */ Optional.empty(),
                            /* schemaId= */ schemaId,
                            /* schemaVersion= */ schemaVersion,
                            /* rawSchema= */ schema,
                            /* isKey= */ isKey));
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException:", e);
            Status status = new Status(FAILED_TO_GET_SCHEMA);
            throw new FrameworkException(status, e);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private List<CompletableFuture<ProduceResult>> doProduce(
            String topicName, String serviceId, List<SerializedKeyAndValue> serialized, Headers headers, List<AuditRecord> auditRecords) {
        return serialized.stream()
                .map(
                        record ->
                                produce(
                                topicName,
                                record.getPartitionId(),
                                record.getTraceabilityId(),
                                record.getCorrelationId().isPresent() ? record.getCorrelationId() : Optional.of(Util.getUUID()),
                                serviceId,
                                headers,
                                auditRecords,
                                record.getKey(),
                                record.getValue(),
                                record.getHeaders(),
                                /* timestamp= */ (record.getTimestamp().isPresent() && record.getTimestamp().get()>0) ? Instant.ofEpochMilli(record.getTimestamp().get()) : Instant.now()))
                .collect(Collectors.toList());
    }

    public CompletableFuture<ProduceResult> produce(
            String topicName,
            Optional<Integer> partitionId,
            Optional<String> traceabilityId,
            Optional<String> correlationId,
            String serviceId,
            Headers headers,
            List<AuditRecord> auditRecords,
            Optional<ByteString> key,
            Optional<ByteString> value,
            Optional<Map<String,String>> recordHeaders,
            Instant timestamp
    ) {
        // populate the headers with the traceabilityId if it is not empty.
        if(recordHeaders.isPresent()){
            recordHeaders.get().entrySet().forEach(entry -> headers.add(entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8)));
        }
        if(traceabilityId.isPresent()) {
            headers.remove(Constants.TRACEABILITY_ID_STRING); // remove the entry populated by the previous record as the headers is shard.
            headers.add(Constants.TRACEABILITY_ID_STRING, traceabilityId.get().getBytes(StandardCharsets.UTF_8));
        } else {
            headers.remove(Constants.TRACEABILITY_ID_STRING); // remove the entry populated by the previous record as the headers is shard.
        }
        // populate the headers with the correlationId. The correlationId here will have a value as it is created in the caller if necessary.
        headers.remove(Constants.CORRELATION_ID_STRING); // remove the entry populated by the previous record as the headers is shard.
        headers.add(Constants.CORRELATION_ID_STRING, correlationId.get().getBytes(StandardCharsets.UTF_8));
        if(traceabilityId.isPresent()) {
            logger.info("Associate traceability Id " + traceabilityId.get() + " with correlation Id " + correlationId.get());
        }

        CompletableFuture<ProduceResult> result = new CompletableFuture<>();
        producer.send(
                new ProducerRecord<>(
                        topicName,
                        partitionId.orElse(null),
                        timestamp.toEpochMilli(),
                        key.map(ByteString::toByteArray).orElse(null),
                        value.map(ByteString::toByteArray).orElse(null),
                        headers),
                (metadata, exception) -> {
                    if (exception != null) {
                        // we cannot call the writeAuditLog in the callback function. It needs to be processed with another thread.
                        if(config.isAuditEnabled()) {
                            synchronized (auditRecords) {
                                auditRecords.add(auditFromRecordMetadata(null,topicName, exception, serviceId, key, traceabilityId, correlationId, false));
                            }
                        }
                        result.completeExceptionally(exception);
                    } else {
                        //writeAuditLog(metadata, null, headers, true);
                        if(config.isAuditEnabled()) {
                            synchronized (auditRecords) {
                                auditRecords.add(auditFromRecordMetadata(metadata,topicName, null, serviceId, key, traceabilityId, correlationId, true));
                            }
                        }
                        result.complete(ProduceResult.fromRecordMetadata(metadata));
                    }
                });
        return result;
    }

    private static CompletableFuture<ProduceResponse> produceResultsToResponse(
            Optional<RegisteredSchema> keySchema,
            Optional<RegisteredSchema> valueSchema,
            List<CompletableFuture<ProduceResult>> resultFutures
    ) {
        CompletableFuture<List<PartitionOffset>> offsetsFuture =
                CompletableFutures.allAsList(
                        resultFutures.stream()
                                .map(
                                        future ->
                                                future.thenApply(
                                                        result ->
                                                                new PartitionOffset(
                                                                        result.getPartitionId(),
                                                                        result.getOffset(),
                                                                        /* errorCode= */ null,
                                                                        /* error= */ null)))
                                .map(
                                        future ->
                                                future.exceptionally(
                                                        throwable ->
                                                                new PartitionOffset(
                                                                        /* partition= */ null,
                                                                        /* offset= */ null,
                                                                        errorCodeFromProducerException(throwable.getCause()),
                                                                        throwable.getCause().getMessage())))
                                .collect(Collectors.toList()));

        return offsetsFuture.thenApply(
                offsets ->
                        new ProduceResponse(
                                offsets,
                                keySchema.map(RegisteredSchema::getSchemaId).orElse(null),
                                valueSchema.map(RegisteredSchema::getSchemaId).orElse(null)));
    }

    private static int errorCodeFromProducerException(Throwable e) {
        if (e instanceof AuthenticationException) {
            return ProduceResponse.KAFKA_AUTHENTICATION_ERROR_CODE;
        } else if (e instanceof AuthorizationException) {
            return ProduceResponse.KAFKA_AUTHORIZATION_ERROR_CODE;
        } else if (e instanceof RetriableException) {
            return ProduceResponse.KAFKA_RETRIABLE_ERROR_ERROR_CODE;
        } else if (e instanceof KafkaException) {
            return ProduceResponse.KAFKA_ERROR_ERROR_CODE;
        } else {
            // We shouldn't see any non-Kafka exceptions, but this covers us in case we do see an
            // unexpected error. In that case we fail the entire request -- this loses information
            // since some messages may have been produced correctly, but is the right thing to do from
            // a REST perspective since there was an internal error with the service while processing
            // the request.
            logger.error("Unexpected Producer Exception", e);
            throw new RuntimeException("Unexpected Producer Exception", e);
        }
    }

    protected AuditRecord auditFromRecordMetadata(RecordMetadata rmd, String topicName,Exception e, String serviceId, Optional<ByteString> key, Optional<String> traceabilityId, Optional<String> correlationId, boolean produced) {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setTopic(topicName);
        auditRecord.setId(UUID.randomUUID().toString());
        auditRecord.setServiceId(serviceId);
        auditRecord.setAuditType(AuditRecord.AuditType.PRODUCER);
        if(rmd != null) {
            auditRecord.setPartition(rmd.partition());
            auditRecord.setOffset(rmd.offset());
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            auditRecord.setStacktrace(sw.toString());
        }
        if(correlationId.isPresent()) {
            auditRecord.setCorrelationId((correlationId.get()));
        }
        if(traceabilityId.isPresent()) {
            auditRecord.setTraceabilityId(traceabilityId.get());
        }

        auditRecord.setAuditStatus(produced ? AuditRecord.AuditStatus.SUCCESS : AuditRecord.AuditStatus.FAILURE);
        auditRecord.setTimestamp(System.currentTimeMillis());
        if(key.isPresent()) auditRecord.setKey(key.get().toString(StandardCharsets.UTF_8));
        return auditRecord;
    }


}
