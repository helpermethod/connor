package com.github.helpermethod.kafka.connect.reset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.time.Duration;
import java.util.List;


class OffsetResetter {
    private final KafkaConsumer<byte[], byte[]> consumer;
    private final KafkaProducer<byte[], byte[]> producer;
    private final ObjectMapper objectMapper;

    OffsetResetter(KafkaConsumer<byte[], byte[]> consumer, KafkaProducer<byte[], byte[]> producer, ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    void reset(String topic, String connector) throws IOException {
        try (consumer) {
            consumer.subscribe(List.of(topic));

            outer:
            while (true) {
                var records = consumer.poll(Duration.ofMillis(100));

                for (var record : records) {
                    if (objectMapper.readValue(record.key(), Key.class).connector.equals(connector)) {
                        producer.send(new ProducerRecord<>(topic, record.partition(), record.key(), null));

                        break outer;
                    }
                }
            }
        }
    }
}
