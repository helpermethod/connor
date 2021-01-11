package com.github.helpermethod.kafka.connect.reset;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import picocli.CommandLine;

@DisplayNameGeneration(ReplaceUnderscores.class)
@Testcontainers
class KafkaConnectOffsetResetTest {
	@Container
	KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.0.1"));

	@Test
	void test() {
		new CommandLine(new KafkaConnectOffsetReset()).execute("--bootstrap-servers", kafka.getBootstrapServers());
	}
}
