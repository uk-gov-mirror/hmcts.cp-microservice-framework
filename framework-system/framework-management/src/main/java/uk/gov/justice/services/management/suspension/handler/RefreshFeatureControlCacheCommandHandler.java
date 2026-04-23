package uk.gov.justice.services.management.suspension.handler;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;
import static uk.gov.justice.services.management.suspension.commands.RefreshFeatureControlCacheCommand.REFRESH_FEATURE_CACHE;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.featurecontrol.remote.CachingFeatureProviderTimerBean;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.suspension.commands.RefreshFeatureControlCacheCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.slf4j.Logger;

public class RefreshFeatureControlCacheCommandHandler {

    @Inject
    private CachingFeatureProviderTimerBean cachingFeatureProviderTimerBean;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(REFRESH_FEATURE_CACHE)
    public void onRefreshFeatureControlCache(
            final RefreshFeatureControlCacheCommand refreshFeatureControlCacheCommand,
            final UUID commandId,
            @SuppressWarnings("unused") final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) {

        final String systemCommandName = refreshFeatureControlCacheCommand.getName();

        final ZonedDateTime refreshStartedAt = clock.now();
        final String startMessage = format("%s command started at %tc", systemCommandName, refreshStartedAt);

        logger.info(startMessage);
        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                refreshFeatureControlCacheCommand,
                COMMAND_IN_PROGRESS,
                refreshStartedAt,
                startMessage
        ));

        try {
            cachingFeatureProviderTimerBean.reloadFeatures();
            final ZonedDateTime completedAt = clock.now();
            final String endMessage = format("%s command completed at %tc", systemCommandName, completedAt);
            logger.info(endMessage);
            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    refreshFeatureControlCacheCommand,
                    COMMAND_COMPLETE,
                    completedAt,
                    endMessage
            ));
        } catch (final Exception e) {

            final ZonedDateTime failedAt = clock.now();
            final String failureMessage = format("%s command failed at %tc: %s: %s", systemCommandName, failedAt, e.getClass().getSimpleName(), e.getMessage());
            logger.error(failureMessage, e);

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    refreshFeatureControlCacheCommand,
                    COMMAND_FAILED,
                    failedAt,
                    failureMessage
            ));
        }
    }
}


