package uk.gov.justice.services.metrics.micrometer.azure.config;

import uk.gov.justice.services.metrics.micrometer.config.MetricsConfiguration;

import jakarta.inject.Inject;

import io.micrometer.azuremonitor.AzureMonitorConfig;
import io.micrometer.common.lang.Nullable;

public class FrameworkAzureMonitorConfig implements AzureMonitorConfig {

    @Inject
    private MetricsConfiguration metricsConfiguration;

    @Override
    public String connectionString() {
        return metricsConfiguration.azureMonitorConnectionString();
    }

    @Override
    public String get(@Nullable final String key) {
        return null;
    }

    @Override
    public boolean enabled() {
        return metricsConfiguration.micrometerMetricsEnabled();
    }
}
