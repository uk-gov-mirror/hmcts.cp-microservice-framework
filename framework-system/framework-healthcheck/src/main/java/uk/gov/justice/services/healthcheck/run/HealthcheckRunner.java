package uk.gov.justice.services.healthcheck.run;

import static java.lang.String.format;

import uk.gov.justice.services.healthcheck.api.Healthcheck;
import uk.gov.justice.services.healthcheck.api.HealthcheckResult;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class HealthcheckRunner {

    @Inject
    private Logger logger;

    public HealthcheckRunDetails runSingleHealthcheck(final Healthcheck healthcheck) {

        final String healthcheckName = healthcheck.getHealthcheckName();
        final String healthcheckDescription = healthcheck.healthcheckDescription();

        try {
            final HealthcheckResult healthcheckResult = healthcheck.runHealthcheck();

            return new HealthcheckRunDetails(
                    healthcheckName,
                    healthcheckDescription,
                    healthcheckResult.isPassed(),
                    healthcheckResult.getErrorMessage().orElse(null)
            );
        } catch (final Throwable e) {

            logger.error(format("Failed to run healthcheck '%s'", healthcheck.getHealthcheckName()), e);
            return new HealthcheckRunDetails(
                    healthcheckName,
                    healthcheckDescription,
                    false,
                    format("%s thrown when running healthcheck. Exception message: '%s'", e.getClass().getSimpleName(), e.getMessage()));
        }
    }
}
