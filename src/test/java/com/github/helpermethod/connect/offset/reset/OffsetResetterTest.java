package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class OffsetResetterTest {
    @Test
    void should_send_a_tombstone_to_the_correct_partition_when_an_offset_was_found() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        var consumer = new MockConsumer<byte[], byte[]>(OffsetResetStrategy.EARLIEST);
        consumer.updateBeginningOffsets(Map.of(
            new TopicPartition("connect-offsets", 0), 0L,
            new TopicPartition("connect-offsets", 1), 0L
        ));
        consumer.schedulePollTask(() -> {
            consumer.rebalance(List.of(new TopicPartition("connect-offsets", 0), new TopicPartition("connect-offsets", 1)));
            consumer.addRecord(new ConsumerRecord<>("connect-offsets", 0, 0L, "[\"jdbc-source\", {}]".getBytes(UTF_8), "{}".getBytes(UTF_8)));
        });

        var producer = new MockProducer<>(true, new ByteArraySerializer(), new ByteArraySerializer());

        new OffsetResetter(consumer, producer, new ConnectOffsetKeyMapper()).reset("connect-offsets", "jdbc-source");

        assertThat(producer.history()).isNotEmpty();
    }
}
