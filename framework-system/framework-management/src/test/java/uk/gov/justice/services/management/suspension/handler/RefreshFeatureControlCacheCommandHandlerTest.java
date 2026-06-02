package uk.gov.justice.services.management.suspension.handler;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;
import static uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.withNoCommandParameters;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.featurecontrol.remote.CachingFeatureProviderTimerBean;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.suspension.commands.RefreshFeatureControlCacheCommand;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.event.Event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class RefreshFeatureControlCacheCommandHandlerTest {

    @Mock
    private CachingFeatureProviderTimerBean cachingFeatureProviderTimerBean;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private RefreshFeatureControlCacheCommandHandler refreshFeatureControlCacheCommandHandler;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldForceReloadOfTheFeatureCacheOnCommandReceived() throws Exception {

        final UUID commandId = randomUUID();
        final RefreshFeatureControlCacheCommand refreshFeatureControlCacheCommand = new RefreshFeatureControlCacheCommand();
        final ZonedDateTime refreshStartedAt = ZonedDateTime.of(2020, 11, 11, 13, 29, 50, 0, UTC);
        final ZonedDateTime completedAt = refreshStartedAt.plusMinutes(2);

        when(clock.now()).thenReturn(refreshStartedAt, completedAt);

        refreshFeatureControlCacheCommandHandler.onRefreshFeatureControlCache(
                refreshFeatureControlCacheCommand,
                commandId,
                withNoCommandParameters()
        );

        final InOrder inOrder = inOrder(
                logger,
                cachingFeatureProviderTimerBean,
                systemCommandStateChangedEventFirer);

        inOrder.verify(logger).info("REFRESH_FEATURE_CACHE command started at Wed Nov 11 13:29:50 Z 2020");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(cachingFeatureProviderTimerBean).reloadFeatures();
        inOrder.verify(logger).info("REFRESH_FEATURE_CACHE command completed at Wed Nov 11 13:31:50 Z 2020");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> events = systemCommandStateChangedEventCaptor.getAllValues();

        assertThat(events.size(), is(2));

        assertThat(events.get(0).getCommandId(), is(commandId));
        assertThat(events.get(0).getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(events.get(0).getSystemCommand(), is(refreshFeatureControlCacheCommand));
        assertThat(events.get(0).getStatusChangedAt(), is(refreshStartedAt));
        assertThat(events.get(0).getMessage(), is("REFRESH_FEATURE_CACHE command started at Wed Nov 11 13:29:50 Z 2020"));

        assertThat(events.get(1).getCommandId(), is(commandId));
        assertThat(events.get(1).getCommandState(), is(COMMAND_COMPLETE));
        assertThat(events.get(1).getSystemCommand(), is(refreshFeatureControlCacheCommand));
        assertThat(events.get(1).getStatusChangedAt(), is(completedAt));
        assertThat(events.get(1).getMessage(), is("REFRESH_FEATURE_CACHE command completed at Wed Nov 11 13:31:50 Z 2020"));
    }

    @Test
    public void shouldMarkCommandAsFailedIfTheRefreshFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final UUID commandId = randomUUID();
        final RefreshFeatureControlCacheCommand refreshFeatureControlCacheCommand = new RefreshFeatureControlCacheCommand();
        final ZonedDateTime refreshStartedAt = ZonedDateTime.of(2020, 11, 11, 13, 29, 50, 0, UTC);
        final ZonedDateTime completedAt = refreshStartedAt.plusMinutes(2);

        when(clock.now()).thenReturn(refreshStartedAt, completedAt);
        doThrow(nullPointerException).when(cachingFeatureProviderTimerBean).reloadFeatures();

        refreshFeatureControlCacheCommandHandler.onRefreshFeatureControlCache(
                refreshFeatureControlCacheCommand,
                commandId,
                withNoCommandParameters()
        );

        final InOrder inOrder = inOrder(
                logger,
                cachingFeatureProviderTimerBean,
                systemCommandStateChangedEventFirer);

        inOrder.verify(logger).info("REFRESH_FEATURE_CACHE command started at Wed Nov 11 13:29:50 Z 2020");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(cachingFeatureProviderTimerBean).reloadFeatures();
        inOrder.verify(logger).error("REFRESH_FEATURE_CACHE command failed at Wed Nov 11 13:31:50 Z 2020: NullPointerException: Ooops", nullPointerException);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> events = systemCommandStateChangedEventCaptor.getAllValues();

        assertThat(events.size(), is(2));

        assertThat(events.get(0).getCommandId(), is(commandId));
        assertThat(events.get(0).getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(events.get(0).getSystemCommand(), is(refreshFeatureControlCacheCommand));
        assertThat(events.get(0).getStatusChangedAt(), is(refreshStartedAt));
        assertThat(events.get(0).getMessage(), is("REFRESH_FEATURE_CACHE command started at Wed Nov 11 13:29:50 Z 2020"));

        assertThat(events.get(1).getCommandId(), is(commandId));
        assertThat(events.get(1).getCommandState(), is(COMMAND_FAILED));
        assertThat(events.get(1).getSystemCommand(), is(refreshFeatureControlCacheCommand));
        assertThat(events.get(1).getStatusChangedAt(), is(completedAt));
        assertThat(events.get(1).getMessage(), is("REFRESH_FEATURE_CACHE command failed at Wed Nov 11 13:31:50 Z 2020: NullPointerException: Ooops"));
    }
}