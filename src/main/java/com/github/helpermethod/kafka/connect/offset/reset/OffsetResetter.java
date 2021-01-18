package com.github.helpermethod.kafka.connect.offset.reset;

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
        try (consumer; producer) {
            consumer.subscribe(List.of(topic));

            while (true) {
                var records = consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    return;
                }

                for (var record : records) {
                    if (objectMapper.readValue(record.key(), Key.class).connector.equals(connector)) {
                        sendTombstone(topic, record.partition(), record.key());

                        return;
                    }
                }
            }
        }
    }

    private void sendTombstone(String topic, Integer partition, byte[] key) {
        producer.send(new ProducerRecord<>(topic, partition, key, null));
    }
}
