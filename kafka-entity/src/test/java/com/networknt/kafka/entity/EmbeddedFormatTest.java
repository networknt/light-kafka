package com.networknt.kafka.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmbeddedFormatTest {
    @Test
    public void testValueOf() {
        String binaryValue = "BINARY";
        EmbeddedFormat format = EmbeddedFormat.valueOf(binaryValue);
        Assertions.assertTrue(format == EmbeddedFormat.BINARY);
    }

    @Test
    public void testValueOfWrongCase() {
        String binaryValue = "binary";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            EmbeddedFormat format = EmbeddedFormat.valueOf(binaryValue);
        });
    }
}
