package com.github.helpermethod.connect.offset.reset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

class OffsetResetter {
    private final KafkaConsumer<byte[], byte[]> consumer;
    private final KafkaProducer<byte[], byte[]> producer;
    private final ObjectMapper objectMapper;

    OffsetResetter(KafkaConsumer<byte[], byte[]> consumer, KafkaProducer<byte[], byte[]> producer, ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    void reset(String topic, String connector) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        try (consumer; producer) {
            consumer.subscribe(List.of(topic));

            while (true) {
                var records = consumer.poll(Duration.ofSeconds(5));

                if (records.isEmpty()) {
                    return;
                }

                for (var record : records) {
                    if (objectMapper.readValue(record.key(), Key.class).connector.equals(connector)) {
                        var metadata = sendTombstone(topic, record.partition(), record.key());

                        return;
                    }
                }
            }
        }
    }

    private RecordMetadata sendTombstone(String topic, Integer partition, byte[] key) throws InterruptedException, ExecutionException, TimeoutException {
        return producer.send(new ProducerRecord<>(topic, partition, key, null)).get(1, SECONDS);
    }
}
