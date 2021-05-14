package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

class OffsetResetter {
    private final Consumer<byte[], byte[]> consumer;
    private final Producer<byte[], byte[]> producer;
    private final ConnectOffsetKeyMapper connectOffsetKeyMapper;

    OffsetResetter(Consumer<byte[], byte[]> consumer, Producer<byte[], byte[]> producer, ConnectOffsetKeyMapper connectOffsetKeyMapper) {
        this.consumer = consumer;
        this.producer = producer;
        this.connectOffsetKeyMapper = connectOffsetKeyMapper;
    }

    void reset(String topic, String connector) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        consumer.subscribe(List.of(topic));

        while (true) {
            var records = consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                return;
            }

            for (var record : records) {
                if (connectOffsetKeyMapper.map(record.key()).connector().equals(connector)) {
                    sendTombstone(topic, record.partition(), record.key());

                    return;
                }
            }
        }
    }

    private void sendTombstone(String topic, Integer partition, byte[] key) throws InterruptedException, ExecutionException, TimeoutException {
        producer.send(new ProducerRecord<>(topic, partition, key, null)).get(1, SECONDS);
    }
}
