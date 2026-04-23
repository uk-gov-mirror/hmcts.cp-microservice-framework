package uk.gov.justice.services.healthcheck.registration;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

import uk.gov.justice.services.common.configuration.ContextNameProvider;
import uk.gov.justice.services.healthcheck.api.DefaultIgnoredHealthcheckNamesProvider;
import uk.gov.justice.services.healthcheck.api.Healthcheck;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;

@Singleton
public class HealthcheckRegistry {

    @Inject
    private Logger logger;

    @Inject
    private DefaultIgnoredHealthcheckNamesProvider ignoredHealthcheckNamesProvider;

    @Inject
    private ContextNameProvider contextNameProvider;

    private final List<Healthcheck> healthchecks = new ArrayList<>();

    public void addHealthcheck(final Healthcheck healthcheck) {

        if(ignoredHealthcheckNamesProvider.getNamesOfIgnoredHealthChecks().contains(healthcheck.getHealthcheckName())) {
            logger.info(format("Ignoring healthcheck '%s' as it's marked as ignored for '%s' context", healthcheck.getHealthcheckName(), contextNameProvider.getContextName()));
        } else {
            logger.info(format("Registering healthcheck class '%s' as '%s' for '%s' context", healthcheck.getClass().getSimpleName(), healthcheck.getHealthcheckName(), contextNameProvider.getContextName()));
            healthchecks.add(healthcheck);
        }
    }

    public List<Healthcheck> getHealthChecks() {
        return unmodifiableList(healthchecks);
    }
}
