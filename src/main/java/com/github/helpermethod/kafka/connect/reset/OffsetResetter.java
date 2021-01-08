package com.github.helpermethod.kafka.connect.reset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.ARRAY;


class OffsetResetter {
    private final KafkaConsumer<byte[], byte[]> consumer;
    private final ObjectMapper objectMapper;

    OffsetResetter(KafkaConsumer<byte[], byte[]> consumer, ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
    }

    void reset(String topic, String connector) {
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
