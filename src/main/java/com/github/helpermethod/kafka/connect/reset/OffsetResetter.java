package com.github.helpermethod.kafka.connect.reset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;

public class OffsetResetter {
    private final KafkaConsumer<byte[], byte[]> consumer;
    private final ObjectMapper objectMapper;

    public OffsetResetter(KafkaConsumer<byte[], byte[]> consumer, ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
    }

    public void reset(String topic, String connector) {
        try (consumer) {
            consumer.subscribe(List.of(topic));

            while (true) {
                var records = consumer.poll(Duration.ofMillis(100));

                for (var record : records) {
                    
                }
            }
        }
    }
}
