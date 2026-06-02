package uk.gov.justice.services.healthcheck.run;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.common.configuration.ContextNameProvider;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.healthcheck.api.IgnoredHealthcheckNamesProvider;
import uk.gov.justice.services.healthcheck.registration.HealthcheckRegistry;

import java.util.List;

import jakarta.inject.Inject;

public class HealthcheckProcessRunner {

    @Inject
    private HealthcheckRegistry healthcheckRegistry;

    @Inject
    private HealthcheckRunner healthcheckRunner;

    @Inject
    private ResultsCounter resultsCounter;

    @Inject
    private ContextNameProvider contextNameProvider;

    @Inject
    private IgnoredHealthcheckNamesProvider ignoredHealthcheckNamesProvider;

    @Inject
    private UtcClock clock;

    public HealthcheckRunResults runAllHealthchecks() {

        final List<HealthcheckRunDetails> healthcheckRunDetails = healthcheckRegistry.getHealthChecks()
                .stream()
                .map(healthcheckRunner::runSingleHealthcheck)
                .collect(toList());

        final int numberPassed = resultsCounter.countNumberPassed(healthcheckRunDetails);
        final int numberFailed = resultsCounter.countNumberFailed(healthcheckRunDetails);
        final int totalNumberOfHealthchecks = healthcheckRunDetails.size();
        final boolean allHealthchecksPassed = numberFailed == 0;
        final String ignoredHealthcheckMessage = generateIgnoredHealthcheckMessage();
        final String resultsMessage = generateResultsMessage(
                allHealthchecksPassed,
                totalNumberOfHealthchecks,
                numberFailed
        );

        return new HealthcheckRunResults(
                contextNameProvider.getContextName(),
                clock.now(),
                allHealthchecksPassed,
                resultsMessage,
                totalNumberOfHealthchecks,
                numberPassed,
                numberFailed,
                ignoredHealthcheckMessage,
                healthcheckRunDetails
        );
    }

    private String generateResultsMessage(
            final boolean allHealthchecksPassed,
            final int totalNumberOfHealthchecks,
            final int numberFailed) {

        if (allHealthchecksPassed) {
            return format("SUCCESS: all %d out of %d healthchecks passed", totalNumberOfHealthchecks, totalNumberOfHealthchecks);
        }

        return format("ERROR: %d out of %d healthchecks failed", numberFailed, totalNumberOfHealthchecks);
    }

    private String generateIgnoredHealthcheckMessage() {
        final List<String> namesOfIgnoredHealthChecks = ignoredHealthcheckNamesProvider.getNamesOfIgnoredHealthChecks();

        if (namesOfIgnoredHealthChecks.isEmpty())  {
            return "None";
        }

        return namesOfIgnoredHealthChecks.toString();
    }
}
