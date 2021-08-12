package com.github.helpermethod.connor.substitutions;


import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.utils.AppInfoParser;

@TargetClass(AppInfoParser.class)
final class NoopAppInfoParser {
    @Substitute
    public static void registerAppInfo(String prefix, String id, Metrics metrics, long nowMs) {
    }

    @Substitute
    public static void unregisterAppInfo(String prefix, String id, Metrics metrics) {
    }
}
