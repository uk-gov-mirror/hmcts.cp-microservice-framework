package uk.gov.justice.services.metrics.micrometer.bootstrap;

import static java.lang.String.format;

import jakarta.inject.Inject;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import uk.gov.justice.services.metrics.micrometer.config.TagFactory;
import uk.gov.justice.services.metrics.micrometer.meters.GaugeMetricsMeter;

import java.util.List;

public class GaugeMeterRegistrar {

    @Inject
    private Logger logger;

    @Inject
    private TagFactory tagFactory;

    public void registerGaugeMeter(final GaugeMetricsMeter gaugeMetricsMeter, final MeterRegistry meterRegistry) {
        logger.info(format("Registering Micrometer Gauge '%s'", gaugeMetricsMeter.metricName()));
        final List<Tag> tags = tagFactory.getSourceComponentTags(gaugeMetricsMeter.sourceComponentPair());
        Gauge.builder(gaugeMetricsMeter.metricName(), gaugeMetricsMeter)
                .tags(tags)
                .description(gaugeMetricsMeter.metricDescription())
                .register(meterRegistry);
    }
}
