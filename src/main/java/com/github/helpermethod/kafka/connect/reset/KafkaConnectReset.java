package com.github.helpermethod.kafka.connect.reset;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

@Command(name = "kafka-connect-reset", mixinStandardHelpOptions = true)
class KafkaConnectReset implements Callable<Integer> {
    @Option(names = {"-b", "--bootstrap-servers"}, required = true, description = "The servers to connect to")
    String bootstrapServers;

    @Override
    public Integer call() {
        var consumer = new KafkaConsumer<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "kafka-connect-reset-" + new Random().nextInt(100_000),
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ));

        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new KafkaConnectReset()).execute(args));
    }
}
