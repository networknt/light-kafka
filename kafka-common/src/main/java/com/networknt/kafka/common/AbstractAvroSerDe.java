package com.networknt.kafka.common;

import com.networknt.config.Config;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;

public class AbstractAvroSerDe {
    protected static final byte MAGIC_BYTE = 0x0;
    protected static final int idSize = 4;
    protected static final int MAX_SCHEMAS_PER_SUBJECT_DEFAULT = 1000;

    static final SchemaRegistryConfig config = (SchemaRegistryConfig) Config.getInstance().getJsonObjectConfig(SchemaRegistryConfig.CONFIG_NAME, SchemaRegistryConfig.class);

    protected SchemaRegistryClient schemaRegistry;

    public AbstractAvroSerDe() {
        schemaRegistry = new CachedSchemaRegistryClient(config.getServerUrls(), MAX_SCHEMAS_PER_SUBJECT_DEFAULT);

    }
}
