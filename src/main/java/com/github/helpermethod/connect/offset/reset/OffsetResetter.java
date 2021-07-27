package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.Records;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.of;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.*;

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

        System.out.printf(Ansi.AUTO.string("Searching for committed offsets for @|bold,cyan %s|@.%n"), connector);

        var offsets =
            Stream
                .generate(() -> consumer.poll(Duration.ofSeconds(5)))
                .takeWhile(not(ConsumerRecords::isEmpty))
                .flatMap(records ->
                    stream(records.spliterator(), false)
                        .filter(record -> connectorNameExtractor.extract(record.key()).equals(connector))
                )
                .collect(toSet());

        if (offsets.isEmpty()) {
            System.out.println(Ansi.AUTO.string("@|bold,yellow No offsets were found.|@"));

            return;
        }

        System.out.printf(Ansi.AUTO.string("@|bold,green %s|@ offset(s) found.%n"), offsets.size());

        for (var offset : offsets) {
            System.out.printf(Ansi.AUTO.string("Sending tombstone to topic @|bold,cyan %s|@, partition @|bold,cyan %s.|@%n"), offset.topic(), offset.partition());

            sendTombstone(offset.topic(), offset.partition(), offset.key());
        }

        System.out.println(Ansi.AUTO.string("@|bold,green Reset successful.|@"));
    }

    private void sendTombstone(String topic, Integer partition, byte[] key) throws InterruptedException, ExecutionException, TimeoutException {
        producer.send(new ProducerRecord<>(topic, partition, key, null)).get(5, SECONDS);
    }
}
