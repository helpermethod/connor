package com.github.helpermethod.connor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import picocli.CommandLine.Help.Ansi;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.StreamSupport.stream;

class OffsetResetter {
    private final Consumer<String, byte[]> consumer;
    private final Producer<String, byte[]> producer;
    private final ConnectorNameExtractor connectorNameExtractor;

    OffsetResetter(Consumer<String, byte[]> consumer, Producer<String, byte[]> producer, ConnectorNameExtractor connectorNameExtractor) {
        this.consumer = consumer;
        this.producer = producer;
        this.connectorNameExtractor = connectorNameExtractor;
    }

    void reset(String topic, String connector) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        consumer.subscribe(List.of(topic));

        System.out.printf(Ansi.AUTO.string("Searching for source connector offsets for @|bold,cyan %s|@.%n"), connector);

        record Offset(Integer partition, String key, boolean tombstone) {}

        var offsetsByKey =
            Stream
                .generate(() -> consumer.poll(Duration.ofSeconds(5)))
                .takeWhile(not(ConsumerRecords::isEmpty))
                .flatMap(records ->
                    stream(records.spliterator(), false)
                        .filter(record -> connectorNameExtractor.extract(record.key()).equals(connector))
                )
                .map(record -> new Offset(record.partition(), record.key(), record.value() == null))
                .collect(groupingBy(Offset::key));

        var offsets =
            offsetsByKey
                .entrySet()
                .stream()
                .filter(not(entry -> entry.getValue().stream().anyMatch(Offset::tombstone)))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());

        if (offsets.isEmpty()) {
            System.out.println(Ansi.AUTO.string("@|bold,yellow No offsets found.|@"));

            return;
        }

        System.out.printf(Ansi.AUTO.string("@|bold,green %s|@ offset(s) found.%n"), offsets.size());

        for (var offset : offsets) {
            System.out.printf(Ansi.AUTO.string("Sending tombstone to topic @|bold,cyan %s|@, partition @|bold,cyan %s.|@%n"), topic, offset.partition());

            sendTombstone(topic, offset.partition(), offset.key());
        }

        System.out.println(Ansi.AUTO.string("@|bold,green Reset successful.|@"));
    }

    private void sendTombstone(String topic, Integer partition,String key) throws InterruptedException, ExecutionException, TimeoutException {
        producer.send(new ProducerRecord<>(topic, partition, key, null)).get(5, SECONDS);
    }
}
