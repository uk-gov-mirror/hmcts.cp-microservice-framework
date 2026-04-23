package uk.gov.justice.services.metrics.micrometer.config;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.Value;

import jakarta.inject.Inject;

public class MetricsConfiguration {

    private static final String TEN_SECONDS = "10000";
    private static final String ONE_MINUTE = "60000";

    @Inject
    @Value(key = "micrometer.metrics.enabled", defaultValue = "false")
    private String micrometerMetricsEnabled;

    @Inject
    @Value(key = "azure.metrics.monitor.connection.string", defaultValue = "azure-metrics-connection-string-not-set")
    private String azureMonitorConnectionString;

    @Inject
    @Value(key = "micrometer.metrics.statistic.timer.interval.milliseconds", defaultValue = ONE_MINUTE)
    private String statisticTimerIntervalMilliseconds;

    @Inject
    @Value(key = "micrometer.metrics.statistic.timer.delay.milliseconds", defaultValue = TEN_SECONDS)
    private String statisticTimerDelayMilliseconds;

    @Inject
    @Value(key = "micrometer.metrics.env", defaultValue = "local")
    private String micrometerEnv;

    public boolean micrometerMetricsEnabled() {
        return parseBoolean(micrometerMetricsEnabled);
    }

    public String azureMonitorConnectionString() {
        return azureMonitorConnectionString;
    }

    public long statisticTimerIntervalMilliseconds() {
        return parseLong(statisticTimerIntervalMilliseconds);
    }

    public long statisticTimerDelayMilliseconds() {
        return parseLong(statisticTimerDelayMilliseconds);
    }

    public String micrometerEnv() {
        return micrometerEnv;
    }
}
