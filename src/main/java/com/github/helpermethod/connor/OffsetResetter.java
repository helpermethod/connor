package com.github.helpermethod.connor;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.generate;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
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
            out.println(Ansi.AUTO.string("@|bold,yellow Performing dry run.|@"));
        }

        out.printf(Ansi.AUTO.string("Searching for source connector offsets for @|bold,cyan %s|@.%n"), connector);

        record Offset(int partition, String key) {}

        var offsets = generate(() -> consumer.poll(Duration.ofSeconds(5)))
            .takeWhile(not(ConsumerRecords::isEmpty))
            .flatMap(records ->
                stream(records.spliterator(), false)
                    .filter(record -> connectorNameExtractor.extract(record.key()).equals(connector))
            )
            .map(record -> new Offset(record.partition(), record.key()))
            .collect(toUnmodifiableSet());

        if (offsets.isEmpty()) {
            out.println(Ansi.AUTO.string("@|bold,yellow No offsets found.|@"));

            return;
        }

        out.printf(Ansi.AUTO.string("@|bold,green %s|@ offset(s) found.%n"), offsets.size());

        if (!execute) {
            return;
        }

        for (var offset : offsets) {
            out.printf(
                Ansi.AUTO.string("Sending tombstone to topic @|bold,cyan %s|@, partition @|bold,cyan %s.|@%n"),
                topic,
                offset.partition()
            );

            sendTombstone(topic, offset.partition(), offset.key());
        }

        out.println(Ansi.AUTO.string("@|bold,green Reset successful.|@"));
    }

    private void sendTombstone(String topic, Integer partition, String key)
        throws InterruptedException, ExecutionException, TimeoutException {
        producer.send(new ProducerRecord<>(topic, partition, key, null)).get(5, SECONDS);
    }
}
