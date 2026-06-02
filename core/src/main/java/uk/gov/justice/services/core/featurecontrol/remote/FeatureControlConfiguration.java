package uk.gov.justice.services.core.featurecontrol.remote;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.GlobalValue;

import jakarta.inject.Inject;

public class FeatureControlConfiguration {

    private final String TEN_MINUTES_IN_MILLISECONDS = 10 * 60 * 1000 + "";

    @Inject
    @GlobalValue(key = "feature-control.enabled", defaultValue = "false")
    private String featureControlEnabled;

    @Inject
    @GlobalValue(key = "feature-control-cache.enabled", defaultValue = "false")
    private String featureControlCacheEnabled;

    @Inject
    @GlobalValue(key = "feature-control-cache-refresh-rate.timer.interval.milliseconds", defaultValue = TEN_MINUTES_IN_MILLISECONDS)
    private String timerIntervalMilliseconds;

    @Inject
    @GlobalValue(key = "feature-control-cache-refresh-rate.timer.start.wait.milliseconds", defaultValue = "0")
    private String timerStartWaitMilliseconds;


    public boolean isFeatureControlEnabled() {
        return parseBoolean(featureControlEnabled);
    }

    public long getTimerStartWaitMilliseconds() {
        return parseLong(timerStartWaitMilliseconds);
    }

    public long getTimerIntervalMilliseconds() {
        return parseLong(timerIntervalMilliseconds);
    }

    public boolean isFeatureControlCacheEnabled() {
        return parseBoolean(featureControlCacheEnabled);
    }
}
