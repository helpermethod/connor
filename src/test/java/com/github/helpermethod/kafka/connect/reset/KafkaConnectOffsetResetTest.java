package com.github.helpermethod.kafka.connect.reset;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testcontainers.utility.DockerImageName.parse;

@DisplayNameGeneration(ReplaceUnderscores.class)
@Testcontainers
class KafkaConnectOffsetResetTest {
    static final String CONNECT_OFFSETS = "connect-offsets";

    @Container
    KafkaContainer kafka = new KafkaContainer(parse("confluentinc/cp-kafka:6.0.1"));

    @Test
    void test() throws InterruptedException, ExecutionException, TimeoutException {
        createConnectOffsetsTopic();

        new CommandLine(new KafkaConnectOffsetReset()).execute(
            "--bootstrap-servers", kafka.getBootstrapServers(),
            "--topic", CONNECT_OFFSETS,
            "--connector", "jdbc-source"
        );
    }

    private void createConnectOffsetsTopic() throws InterruptedException, ExecutionException, TimeoutException {
        AdminClient
            .create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))
            .createTopics(List.of(new NewTopic(CONNECT_OFFSETS, 25, (short) 1)))
            .all()
            .get(1, TimeUnit.SECONDS);
    }
}
