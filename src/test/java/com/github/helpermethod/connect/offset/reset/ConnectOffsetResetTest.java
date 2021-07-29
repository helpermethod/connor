package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import picocli.CommandLine;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.testcontainers.utility.DockerImageName.parse;

@DisplayNameGeneration(ReplaceUnderscores.class)
@Testcontainers
class ConnectOffsetResetTest {
    static final String CONNECT_OFFSETS = "connect-offsets";

    @Container
    KafkaContainer kafka =
        new KafkaContainer(parse("confluentinc/cp-kafka:6.2.0"))
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false");

    @Test
    void should_send_a_tombstone_to_the_correct_partition() throws InterruptedException, ExecutionException, TimeoutException {
        createConnectOffsetsTopic();

        try (
            var consumer = createConnectOffsetsConsumer();
            var producer = createProducer()
        ) {
            var metadata =
                producer
                    .send(new ProducerRecord<>(CONNECT_OFFSETS, "[\"jdbc-source\", {}]", "{}"))
                    .get(5, SECONDS);

            new CommandLine(new ConnectOffsetReset())
                .execute(
                    "--bootstrap-servers", kafka.getBootstrapServers(),
                    "--offset-topic", CONNECT_OFFSETS,
                    "--connector-name", "jdbc-source"
                );

            var records = consumer.poll(Duration.ofSeconds(5));

            assertThat(records.records(new TopicPartition(CONNECT_OFFSETS, metadata.partition())))
                .extracting("key", "value")
                .containsExactly(
                    tuple("[\"jdbc-source\", {}]", "{}"),
                    tuple("[\"jdbc-source\", {}]", "")
                );
        }
    }

    private KafkaProducer<String, String> createProducer() {
        var producerConfig = Map.<String, Object>of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        return new KafkaProducer<>(producerConfig, new StringSerializer(), new StringSerializer());
    }

    private KafkaConsumer<String, String> createConnectOffsetsConsumer() {
        var consumerConfig = Map.<String, Object>of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "test",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        var consumer = new KafkaConsumer<>(consumerConfig, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(List.of(CONNECT_OFFSETS));

        return consumer;
    }

    private void createConnectOffsetsTopic() throws InterruptedException, ExecutionException, TimeoutException {
        try (var adminClient = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            adminClient
                .createTopics(List.of(new NewTopic(CONNECT_OFFSETS, 25, (short) 1)))
                .all()
                .get(5, SECONDS);
        }
    }
}
