package com.github.helpermethod.kafka.connect.reset;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Command(name = "kafka-connect-reset", mixinStandardHelpOptions = true)
class KafkaConnectOffsetReset implements Runnable {
    @Option(names = {"-b", "--bootstrap-servers"}, required = true, description = "The servers to connect to")
    private String bootstrapServers;
    @Option(names = {"-t", "--topic"}, required = true, description = "The topic where Kafka Connect stores Source Connector offsets")
    private String topic;
    @Option(names = {"-c", "--connector"}, required = true, description = "The source connector to reset")
    private String connector;

    @Override
    public void run() {
        Map<String, Object> consumerConfig = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "kafka-connect-reset-" + new Random().nextInt(100_000),
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        var consumer = new KafkaConsumer<>(consumerConfig, new ByteArrayDeserializer(), new ByteArrayDeserializer());
        Map<String, Object> producerConfig = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
        );
        var producer = new KafkaProducer<>(producerConfig, new ByteArraySerializer(), new ByteArraySerializer());
        var objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            new OffsetResetter(consumer, producer, objectMapper).reset(topic, connector);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new KafkaConnectOffsetReset()).execute(args));
    }
}
