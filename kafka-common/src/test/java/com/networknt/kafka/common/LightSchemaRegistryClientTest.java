package com.networknt.kafka.common;

import com.networknt.service.SingletonServiceFactory;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LightSchemaRegistryClientTest {
    @Test
    public void testDefaultConstructor() {
        SchemaRegistryClient client = SingletonServiceFactory.getBean(SchemaRegistryClient.class);
        Assertions.assertNotNull(client);
    }

}
