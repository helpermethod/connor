package com.github.helpermethod.connor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(ReplaceUnderscores.class)
class OffsetResetterTest {

    static final String CONNECT_OFFSETS = "connect-offsets";

    @Test
    void shouldn_send_no_tombstone_when_execute_is_set_to_false()
        throws IOException, ExecutionException, InterruptedException, TimeoutException {
        var consumer = new MockConsumer<String, byte[]>(OffsetResetStrategy.EARLIEST);
        var beginningOffsets = Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L);
        consumer.updateBeginningOffsets(beginningOffsets);
        consumer.schedulePollTask(() -> {
            consumer.rebalance(beginningOffsets.keySet());
            consumer.addRecord(
                new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8))
            );
        });

        var producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());

        new OffsetResetter(consumer, producer, new ConnectorNameExtractor(), false)
            .reset(CONNECT_OFFSETS, "jdbc-source");

        assertThat(producer.history()).isEmpty();
    }

    @Test
    void should_send_no_tombstone_when_no_offset_was_found()
        throws InterruptedException, ExecutionException, TimeoutException, IOException {
        var consumer = new MockConsumer<String, byte[]>(OffsetResetStrategy.EARLIEST);
        var beginningOffsets = Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L);
        consumer.updateBeginningOffsets(beginningOffsets);
        consumer.schedulePollTask(() -> consumer.rebalance(beginningOffsets.keySet()));
        var producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());

        new OffsetResetter(consumer, producer, new ConnectorNameExtractor(), true)
            .reset(CONNECT_OFFSETS, "jdbc-source");

        assertThat(producer.history()).isEmpty();
    }

    @MethodSource
    @ParameterizedTest
    void should_send_tombstones_to_all_partitions_where_an_offset_was_found(
        Map<TopicPartition, Long> beginningOffsets,
        List<List<ConsumerRecord<String, byte[]>>> consumerRecordsPerPoll,
        List<ProducerRecord<String, byte[]>> tombstones
    ) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        var consumer = new MockConsumer<String, byte[]>(OffsetResetStrategy.EARLIEST);
        consumer.updateBeginningOffsets(beginningOffsets);
        consumer.schedulePollTask(() -> {
            consumer.rebalance(beginningOffsets.keySet());
            head(consumerRecordsPerPoll).forEach(consumer::addRecord);
        });
        tail(consumerRecordsPerPoll)
            .forEach(records -> consumer.schedulePollTask(() -> records.forEach(consumer::addRecord)));

        var producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());

        new OffsetResetter(consumer, producer, new ConnectorNameExtractor(), true)
            .reset(CONNECT_OFFSETS, "jdbc-source");

        assertThat(producer.history())
            .usingRecursiveFieldByFieldElementComparatorOnFields("topic", "partition", "key", "value")
            .containsExactlyInAnyOrderElementsOf(tombstones);
    }

    static Stream<Arguments> should_send_tombstones_to_all_partitions_where_an_offset_was_found() {
        return Stream.of(
            arguments(
                Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L),
                List.of(
                    List.of(new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8)))
                ),
                List.of(new ProducerRecord<>(CONNECT_OFFSETS, 0, "[\"jdbc-source\", {}]", null))
            ),
            arguments(
                Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L),
                List.of(
                    List.of(
                        new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"mongo-source\", {}]", "{}".getBytes(UTF_8)),
                        new ConsumerRecord<>(CONNECT_OFFSETS, 0, 1, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8))
                    )
                ),
                List.of(new ProducerRecord<>(CONNECT_OFFSETS, 0, "[\"jdbc-source\", {}]", null))
            ),
            arguments(
                Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L, new TopicPartition(CONNECT_OFFSETS, 1), 0L),
                List.of(
                    List.of(
                        new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"mongo-source\", {}]", "{}".getBytes(UTF_8)),
                        new ConsumerRecord<>(CONNECT_OFFSETS, 1, 0, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8))
                    )
                ),
                List.of(new ProducerRecord<>(CONNECT_OFFSETS, 1, "[\"jdbc-source\", {}]", null))
            ),
            arguments(
                Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L),
                List.of(
                    List.of(
                        new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"mongo-source\", {}]", "{}".getBytes(UTF_8))
                    ),
                    List.of(new ConsumerRecord<>(CONNECT_OFFSETS, 0, 1, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8)))
                ),
                List.of(new ProducerRecord<>(CONNECT_OFFSETS, 0, "[\"jdbc-source\", {}]", null))
            ),
            arguments(
                Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L),
                List.of(
                    List.of(new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8))),
                    List.of(new ConsumerRecord<>(CONNECT_OFFSETS, 0, 1, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8)))
                ),
                List.of(new ProducerRecord<>(CONNECT_OFFSETS, 0, "[\"jdbc-source\", {}]", null))
            ),
            arguments(
                Map.of(new TopicPartition(CONNECT_OFFSETS, 0), 0L, new TopicPartition(CONNECT_OFFSETS, 1), 0L),
                List.of(
                    List.of(new ConsumerRecord<>(CONNECT_OFFSETS, 0, 0, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8))),
                    List.of(new ConsumerRecord<>(CONNECT_OFFSETS, 1, 0, "[\"jdbc-source\", {}]", "{}".getBytes(UTF_8)))
                ),
                List.of(
                    new ProducerRecord<>(CONNECT_OFFSETS, 0, "[\"jdbc-source\", {}]", null),
                    new ProducerRecord<>(CONNECT_OFFSETS, 1, "[\"jdbc-source\", {}]", null)
                )
            )
        );
    }

    static <T> T head(List<T> list) {
        return list.get(0);
    }

    static <T> List<T> tail(List<T> list) {
        return list.subList(1, list.size());
    }
}
