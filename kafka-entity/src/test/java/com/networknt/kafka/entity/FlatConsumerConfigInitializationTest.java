package com.networknt.kafka.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FlatConsumerConfigInitializationTest {

    @Test
    void sidecarConsumerRecordInitializesWithFlat230Config() {
        assertDoesNotThrow(() -> Class.forName(SidecarConsumerRecord.class.getName(), true,
                SidecarConsumerRecord.class.getClassLoader()));
    }
}
