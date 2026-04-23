package uk.gov.justice.subscription.jms.core;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import java.util.Optional;

import jakarta.jms.Destination;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import org.junit.jupiter.api.Test;

class JmsUriToDestinationTypeConverterTest {

    private JmsUriToDestinationTypeConverter jmsUriToDestinationTypeConverter = new JmsUriToDestinationTypeConverter();

    @Test
    public void shouldConvertJmsTopicUriToDestinationType() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("jms:topic:public.event");

        assertThat(destinationType, is(of(Topic.class)));
    }

    @Test
    public void shouldConvertJmsTopicUriToDestinationTypeCaseInsensitive() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("jms:toPIc:public.event");

        assertThat(destinationType, is(of(Topic.class)));
    }

    @Test
    public void shouldConvertJmsQueueUriToDestinationType() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("jms:queue:command.handler");

        assertThat(destinationType, is(of(Queue.class)));
    }

    @Test
    public void shouldConvertJmsQueueUriToDestinationTypeCaseInsensitive() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("jms:quEUe:command.handler");

        assertThat(destinationType, is(of(Queue.class)));
    }

    @Test
    public void shouldConvertJmsInvalidUriToDestinationTypeEmpty() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("jms:xxqueue:command.handler");

        assertThat(destinationType, is(empty()));
    }

    @Test
    public void shouldConvertHelloJmsdUriToDestinationTypeEmpty() {

        final Optional<Class<? extends Destination>> destinationType  = jmsUriToDestinationTypeConverter.convert("hello");

        assertThat(destinationType, is(empty()));
    }

    @Test
    public void shouldConvertEmptyJmsdUriToDestinationTypeEmpty() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("");

        assertThat(destinationType, is(empty()));
    }

    @Test
    public void shouldConvertNullJmsdUriToDestinationTypeEmpty() {

        final Optional<Class<? extends Destination>> destinationType  = jmsUriToDestinationTypeConverter.convert(null);

        assertThat(destinationType, is(empty()));
    }

    @Test
    public void shouldConvertJmsTopicInPositionZeroToDestinationTypeEmpty() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("topic:jms:public.event");

        assertThat(destinationType, is(empty()));
    }

    @Test
    public void shouldConvertJmsTopicInPositionTwoToDestinationTypeEmpty() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convert("jms:alpha:topic:public.event");

        assertThat(destinationType, is(empty()));
    }

    @Test
    public void shouldConvertForEventProcessor() {

        final Optional<Class<? extends Destination>> destinationType  = jmsUriToDestinationTypeConverter.convertForEventProcessor(EVENT_PROCESSOR, "jms:topic:public.event");

        assertThat(destinationType, is(of(Topic.class)));
    }

    @Test
    public void shouldNotConvertForEventProcessorIfComponentIsEventListener() {

        final Optional<Class<? extends Destination>> destinationType = jmsUriToDestinationTypeConverter.convertForEventProcessor(EVENT_LISTENER, "jms:topic:public.event");

        assertThat(destinationType, is(empty()));
    }
}