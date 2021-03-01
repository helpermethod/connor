package com.github.helpermethod.connect.offset.reset;

import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
public class OffsetResetterTest {
    @Test
    void should_not_send_a_tombstone_if_no_offset_was_found() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        var consumer = new MockConsumer<byte[], byte[]>(OffsetResetStrategy.EARLIEST);
        var producer = new MockProducer<>(true, new ByteArraySerializer(), new ByteArraySerializer());

        new OffsetResetter(consumer, producer, new ConnectOffsetMapper()).reset("connect-offsets", "jdbc-source");

        assertThat(producer.history()).isEmpty();
    }
}
