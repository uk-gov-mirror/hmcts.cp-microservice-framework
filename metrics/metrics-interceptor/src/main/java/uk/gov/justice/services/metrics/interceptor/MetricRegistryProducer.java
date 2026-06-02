package uk.gov.justice.services.metrics.interceptor;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

@ApplicationScoped
public class MetricRegistryProducer {

    private static final MetricRegistry METRICS_REGISTRY = createMetricsRegistry();

    @Produces
    public MetricRegistry metricRegistry() {
        return METRICS_REGISTRY;
    }

    private static MetricRegistry createMetricsRegistry() {

        final MetricRegistry aMetricsRegistry = new MetricRegistry();
        JmxReporter.forRegistry(aMetricsRegistry).inDomain("uk.gov.justice.metrics").build().start();

        return aMetricsRegistry;
    }
}