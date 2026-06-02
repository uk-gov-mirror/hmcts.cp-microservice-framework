package uk.gov.justice.services.metrics.micrometer.bootstrap;

import static io.micrometer.core.instrument.Clock.SYSTEM;

import uk.gov.justice.services.metrics.micrometer.azure.config.FrameworkAzureMonitorConfig;

import jakarta.inject.Inject;

import io.micrometer.azuremonitor.AzureMonitorMeterRegistry;

public class AzureMonitorMeterRegistryFactory {

    @Inject
    private FrameworkAzureMonitorConfig frameworkAzureMonitorConfig;

    public AzureMonitorMeterRegistry create() {
        return new AzureMonitorMeterRegistry(
                frameworkAzureMonitorConfig,
                SYSTEM);
    }
}
