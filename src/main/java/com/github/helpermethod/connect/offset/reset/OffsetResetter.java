package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

class OffsetResetter {
    private final Consumer<byte[], byte[]> consumer;
    private final Producer<byte[], byte[]> producer;
    private final ConnectorNameExtractor connectorNameExtractor;

    OffsetResetter(Consumer<byte[], byte[]> consumer, Producer<byte[], byte[]> producer, ConnectorNameExtractor connectorNameExtractor) {
        this.consumer = consumer;
        this.producer = producer;
        this.connectorNameExtractor = connectorNameExtractor;
    }

    void reset(String topic, String connector) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        consumer.subscribe(List.of(topic));

        System.out.printf(Ansi.ON.string("Searching for committed offsets for source connector @|bold,cyan %s|@.%n"), connector);

        while (true) {
            var records= consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                System.out.println(Ansi.ON.string("@|bold,yellow No offsets were found.|@"));

                return;
            }

            for (var record : records) {
                if (connectorNameExtractor.extract(record.key()).equals(connector)) {
                    System.out.printf(Ansi.ON.string("""
I                    Found offsets in topic @|bold,cyan %s|@, partition @|bold,cyan %s|@.
                    Sending tombstone.
                    """), record.topic(), record.partition());

                    sendTombstone(topic, record.partition(), record.key());

                    System.out.println(Ansi.ON.string("@|bold,green Reset successful.|@"));

                    return;
                }
            }
        }
    }

    private void sendTombstone(String topic, Integer partition, byte[] key) throws InterruptedException, ExecutionException, TimeoutException {
        producer.send(new ProducerRecord<>(topic, partition, key, null)).get(5, SECONDS);
    }
}
