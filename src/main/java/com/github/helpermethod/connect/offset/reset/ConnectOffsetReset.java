package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Command(name = "connect-offset-reset", mixinStandardHelpOptions = true, version = "0.1.0")
public class ConnectOffsetReset implements Runnable {
    @Option(names = {"-b", "--bootstrap-servers"}, required = true, description = "A comma-separated list of broker urls.")
    private String bootstrapServers;
    @Option(names = {"-t", "--offset-topic"}, required = true, description = "The topic where Kafka Connect stores its Source Connector offsets.")
    private String topic;
    @Option(names = {"-n", "--connector-name"}, required = true, description = "The source connector name for which to reset the offset.")
    private String connector;

    @Override
    public void run() {
        try (
            var consumer = createConsumer();
            var producer = createProducer()
        ) {
            new OffsetResetter(consumer, producer, new ConnectorNameExtractor()).reset(topic, connector);
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private KafkaConsumer<String, byte[]> createConsumer() {
        var consumerConfig = Map.<String, Object>of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "connect-offset-reset-" + new Random().nextInt(100_000),
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        return new KafkaConsumer<>(consumerConfig, new StringDeserializer(), new ByteArrayDeserializer());
    }

    private KafkaProducer<String, byte[]> createProducer() {
        var producerConfig = Map.<String, Object>of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        return new KafkaProducer<>(producerConfig, new StringSerializer(), new ByteArraySerializer());
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new ConnectOffsetReset()).execute(args));
    }
}
