package com.github.helpermethod.kafka.connect.offset.reset;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Command(name = "kafka-connect-offset-reset", mixinStandardHelpOptions = true)
public class KafkaConnectOffsetReset implements Runnable {
    @Option(names = {"-b", "--bootstrap-servers"}, required = true, description = "The servers to connect to")
    private String bootstrapServers;
    @Option(names = {"-t", "--topic"}, required = true, description = "The topic where Kafka Connect stores Source Connector offsets")
    private String topic;
    @Option(names = {"-c", "--connector"}, required = true, description = "The source connector to reset")
    private String connector;

    @Override
    public void run() {
        try {
            new OffsetResetter(createConsumer(), createProducer(), createObjectMapper()).reset(topic, connector);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private KafkaConsumer<byte[], byte[]> createConsumer() {
        Map<String, Object> consumerConfig = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "kafka-connect-offset-reset-" + new Random().nextInt(100_000),
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        return new KafkaConsumer<>(consumerConfig, new ByteArrayDeserializer(), new ByteArrayDeserializer());
    }

    private KafkaProducer<byte[], byte[]> createProducer() {
        Map<String, Object> producerConfig = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        return new KafkaProducer<>(producerConfig, new ByteArraySerializer(), new ByteArraySerializer());
    }

    private ObjectMapper createObjectMapper() {
        return JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new KafkaConnectOffsetReset()).execute(args));
    }
}
