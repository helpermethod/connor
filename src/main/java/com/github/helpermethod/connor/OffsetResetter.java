package com.github.helpermethod.connor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.*;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import picocli.CommandLine.Help.Ansi;

class OffsetResetter {

    private final Consumer<String, byte[]> consumer;
    private final Producer<String, byte[]> producer;
    private final ConnectorNameExtractor connectorNameExtractor;
    private final boolean execute;

    OffsetResetter(
        Consumer<String, byte[]> consumer,
        Producer<String, byte[]> producer,
        ConnectorNameExtractor connectorNameExtractor,
        boolean execute
    ) {
        this.consumer = consumer;
        this.producer = producer;
        this.connectorNameExtractor = connectorNameExtractor;
        this.execute = execute;
    }

    void reset(String topic, String connector)
        throws IOException, InterruptedException, ExecutionException, TimeoutException {
        consumer.subscribe(List.of(topic));

        if (!execute) {
            System.out.println(Ansi.AUTO.string("@|bold, yellow Performing dry run.|@%n"));
        }

        System.out.printf(
            Ansi.AUTO.string("Searching for source connector offsets for @|bold,cyan %s|@.%n"),
            connector
        );

        var offsets = uniqueOffsets(groupOffsetsByKey(connector));

        if (offsets.isEmpty()) {
            System.out.println(Ansi.AUTO.string("@|bold,yellow No offsets found.|@"));

            return;
        }

        System.out.printf(Ansi.AUTO.string("@|bold,green %s|@ offset(s) found.%n"), offsets.size());

        if (!execute) {
            return;
        }

        for (var offset : offsets) {
            System.out.printf(
                Ansi.AUTO.string("Sending tombstone to topic @|bold,cyan %s|@, partition @|bold,cyan %s.|@%n"),
                topic,
                offset.partition()
            );

            sendTombstone(topic, offset.partition(), offset.key());
        }

        System.out.println(Ansi.AUTO.string("@|bold,green Reset successful.|@"));
    }

    private Map<String, List<Offset>> groupOffsetsByKey(String connector) {
        return generate(() -> consumer.poll(Duration.ofSeconds(5)))
            .takeWhile(not(ConsumerRecords::isEmpty))
            .flatMap(records ->
                stream(records.spliterator(), false)
                    .filter(record -> connectorNameExtractor.extract(record.key()).equals(connector))
            )
            .map(record -> new Offset(record.partition(), record.key(), record.value() == null))
            .collect(groupingBy(Offset::key));
    }

    private Set<Offset> uniqueOffsets(Map<String, List<Offset>> offsetsByKey) {
        return offsetsByKey
            .entrySet()
            .stream()
            .filter(not(entry -> entry.getValue().stream().anyMatch(Offset::tombstone)))
            .flatMap(entry -> entry.getValue().stream())
            .collect(toSet());
    }

    private void sendTombstone(String topic, Integer partition, String key)
        throws InterruptedException, ExecutionException, TimeoutException {
        producer.send(new ProducerRecord<>(topic, partition, key, null)).get(5, SECONDS);
    }

    private record Offset(Integer partition, String key, boolean tombstone) {}
}
