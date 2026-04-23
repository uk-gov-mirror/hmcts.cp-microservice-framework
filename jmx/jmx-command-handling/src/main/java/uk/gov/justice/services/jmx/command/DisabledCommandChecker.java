package uk.gov.justice.services.jmx.command;

import uk.gov.justice.services.common.configuration.subscription.pull.EventPullConfiguration;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import jakarta.inject.Inject;

public class DisabledCommandChecker {

    @Inject
    private EventPullConfiguration eventPullConfiguration;

    public boolean isDisabledByPullMechanism(final SystemCommand systemCommand) {

        if (eventPullConfiguration.shouldProcessEventsByPullMechanism())  {
            return systemCommand.isDisabledByPullMechanism();
        }

        return false;
    }
}
