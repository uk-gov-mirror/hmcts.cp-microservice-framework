package uk.gov.justice.services.metrics.micrometer.prometheus;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@Dependent
public class TimerRegistrar {

    private final PrometheusMeterRegistry prometheusMeterRegistry;

    @Inject
    public TimerRegistrar(final PrometheusMeterRegistry prometheusMeterRegistry) {
        this.prometheusMeterRegistry = prometheusMeterRegistry;
    }

    public void registerTimer(String timerName, double... percentiles) {
        Timer.builder(timerName)
                .publishPercentiles(percentiles)
                .register(prometheusMeterRegistry);
    }
}
