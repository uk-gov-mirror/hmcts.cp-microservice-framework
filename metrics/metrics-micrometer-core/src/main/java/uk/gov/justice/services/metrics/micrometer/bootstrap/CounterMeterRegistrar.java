package uk.gov.justice.services.metrics.micrometer.bootstrap;

import static java.lang.String.format;

import jakarta.inject.Inject;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import uk.gov.justice.services.metrics.micrometer.config.TagFactory;
import uk.gov.justice.services.metrics.micrometer.meters.MetricsMeter;

import java.util.List;

public class CounterMeterRegistrar {

    @Inject
    private Logger logger;

    @Inject
    private TagFactory tagFactory;

    public void registerCounterMeter(final MetricsMeter metricsMeter, final MeterRegistry meterRegistry) {
        logger.info(format("Registering Micrometer Counter '%s'", metricsMeter.metricName()));
        final List<Tag> tags = tagFactory.getSourceComponentTags(metricsMeter.sourceComponentPair());
        Counter.builder(metricsMeter.metricName())
                .tags(tags)
                .description(metricsMeter.metricDescription())
                .register(meterRegistry);
    }
}
