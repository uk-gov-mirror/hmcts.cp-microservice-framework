package uk.gov.justice.raml.jms.core;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.generators.subscription.parser.EventSourcesFileParserFactory;
import uk.gov.justice.services.generators.subscription.parser.JmsUriToDestinationConverter;
import uk.gov.justice.subscription.jms.core.JmsUriToDestinationTypeConverter;

import java.util.Optional;

import jakarta.jms.Destination;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class EventSourcesDestinationTypeFinderTest {

    @Mock
    private Logger logger;
    @Spy
    private JmsUriToDestinationTypeConverter uriToDestinationTypeConverter;
    @Spy
    private JmsUriToDestinationConverter jmsUriToDestinationConverter;
    @Spy
    private EventSourcesFileParserFactory eventSourceYamlParserFactory;


    @InjectMocks
    private EventSourcesDestinationTypeFinder destinationTypeFinder;

    @Test
    void shouldFindDestinationTypeForEventProcessorDefinedAsTopic() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(EVENT_PROCESSOR, "structure.event");

        assertThat(res, is(of(Topic.class)));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorDefinedAsQueue() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(EVENT_PROCESSOR, "queuestructure.event");

        assertThat(res, is(of(Queue.class)));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorDefinedAsEmptyWhenException() {

        when(uriToDestinationTypeConverter.convertForEventProcessor(anyString(), anyString())).
                thenThrow(new NullPointerException("Could not find any event-sources.yaml file"));

        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(EVENT_PROCESSOR, "queuestructure.event");

        assertThat(res, is(empty()));
        verify(logger).
                warn("Failed in findForEventProcessor: {}", "Could not find any event-sources.yaml file");

        verifyNoMoreInteractions(logger);
    }


    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenComponentIsEventListener() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(EVENT_LISTENER, "structure.event");

        assertThat(res, is(empty()));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenComponentIsUnknown() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor("unknown-component", "structure.event");

        assertThat(res, is(empty()));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenComponentAndDestinationAreNull() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(null, null);

        assertThat(res, is(empty()));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenComponentIsNull() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(null, "some-destination");

        assertThat(res, is(empty()));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenComponentIsEmpty() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(" ", "some-destination");

        assertThat(res, is(empty()));
    }


    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenDestinationIsNull() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(EVENT_PROCESSOR, null);

        assertThat(res, is(empty()));
    }

    @Test
    void shouldFindDestinationTypeForEventProcessorAsEmptyWhenDestinationIsEmptyl() {
        final Optional<Class<? extends Destination>> res = destinationTypeFinder.findForEventProcessor(EVENT_PROCESSOR, "");

        assertThat(res, is(empty()));
    }
}