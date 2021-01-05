package com.github.helpermethod.kafka.connect.reset;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "kafka-connect-reset", mixinStandardHelpOptions = true)
class KafkaConnectReset implements Callable<Integer> {
    @Option(names = {"-b", "--bootstrap-server"})
    String bootstrapServers = "localhost:9092";

    @Override
    public Integer call() {
        System.out.println(bootstrapServers);

        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new KafkaConnectReset()).execute(args));
    }
}
