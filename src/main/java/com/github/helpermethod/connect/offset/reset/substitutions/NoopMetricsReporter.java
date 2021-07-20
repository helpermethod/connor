package com.github.helpermethod.connect.offset.reset.substitutions;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.kafka.common.metrics.JmxReporter;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;

import java.util.List;
import java.util.Map;

@Substitute
@TargetClass(JmxReporter.class)
final class NoopMetricsReporter implements MetricsReporter {
    @Substitute
    public NoopMetricsReporter() {
    }

    @Substitute
    public NoopMetricsReporter(String prefix) {
    }

    @Substitute
    @Override
    public void init(List<KafkaMetric> metrics) {
    }

    @Substitute
    @Override
    public void metricChange(KafkaMetric metric) {
    }

    @Substitute
    @Override
    public void metricRemoval(KafkaMetric metric) {
    }

    @Substitute
    @Override
    public void close() {
    }

    @Substitute
    @Override
    public void configure(Map<String, ?> configs) {
    }
}
