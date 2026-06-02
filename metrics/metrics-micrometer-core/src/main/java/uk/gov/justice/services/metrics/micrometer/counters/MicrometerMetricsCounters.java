package uk.gov.justice.services.metrics.micrometer.counters;

import static java.lang.String.format;
import static uk.gov.justice.services.metrics.micrometer.meters.MetricsMeterNames.EVENTS_FAILED_COUNTER_NAME;
import static uk.gov.justice.services.metrics.micrometer.meters.MetricsMeterNames.EVENTS_IGNORED_COUNTER_NAME;
import static uk.gov.justice.services.metrics.micrometer.meters.MetricsMeterNames.EVENTS_PROCESSED_COUNTER_NAME;
import static uk.gov.justice.services.metrics.micrometer.meters.MetricsMeterNames.EVENTS_RECEIVED_COUNTER_NAME;
import static uk.gov.justice.services.metrics.micrometer.meters.MetricsMeterNames.EVENTS_SUCCEEDED_COUNTER_NAME;

import uk.gov.justice.services.metrics.micrometer.config.MetricsConfiguration;
import uk.gov.justice.services.metrics.micrometer.config.TagFactory;

import java.util.List;

import jakarta.inject.Inject;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import org.slf4j.Logger;

public class MicrometerMetricsCounters {

    @Inject
    private CompositeMeterRegistry compositeMeterRegistry;

    @Inject
    private MetricsConfiguration metricsConfiguration;

    @Inject
    private TagFactory tagFactory;

    @Inject
    private Logger logger;

    public void incrementEventsReceivedCount(final String source, final String component) {
        incrementCounterByTag(source, component, EVENTS_RECEIVED_COUNTER_NAME);
    }

    public void incrementEventsProcessedCount(final String source, final String component) {
        incrementCounterByTag(source, component, EVENTS_PROCESSED_COUNTER_NAME);
    }

    public void incrementEventsSucceededCount(final String source, final String component) {
        incrementCounterByTag(source, component, EVENTS_SUCCEEDED_COUNTER_NAME);

    }

    public void incrementEventsIgnoredCount(final String source, final String component) {
        incrementCounterByTag(source, component, EVENTS_IGNORED_COUNTER_NAME);

    }

    public void incrementEventsFailedCount(final String source, final String component) {
        incrementCounterByTag(source, component, EVENTS_FAILED_COUNTER_NAME);
    }

    private void incrementCounterByTag(final String source, final String component, final String counterName) {
        if (metricsConfiguration.micrometerMetricsEnabled()) {
            final List<Tag> tags = tagFactory.getSourceComponentTags(source, component);
            try {
                compositeMeterRegistry.get(counterName)
                        .tags(tags)
                        .counter()
                        .increment();
            } catch (final MeterNotFoundException e) {
                logger.warn(format("Failed to find metrics counter meter '%s' with tags '%s'", counterName, tags));
            }
        }
    }
}
