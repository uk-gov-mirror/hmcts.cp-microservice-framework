package uk.gov.justice.subscription.jms.core;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import java.util.Optional;

import jakarta.jms.Destination;
import jakarta.jms.Queue;
import jakarta.jms.Topic;


public class JmsUriToDestinationTypeConverter {

    private static final String QUEUE = "queue";
    private static final String TOPIC = "topic";

    /**
     * Retrieves the destination from jmsUri in the format jms:queue:some-more-string
     *
     * @return Optional of Queue.class, Topic.class or null
     */
    Optional<Class<? extends Destination>> convert(final String jmsUri) {

        final String[] parts = trimToEmpty(jmsUri).split(":");
        final String destinationType = (parts.length > 1) ? parts[1] : null;

        if (QUEUE.equalsIgnoreCase(destinationType)) {
            return Optional.of(Queue.class);
        }

        if (TOPIC.equalsIgnoreCase(destinationType)) {
            return Optional.of(Topic.class);
        }

        return Optional.empty();
    }

    /**
     * EVENT_PROCESSOR.
     * For any other component, return null
     *
     * @param serviceComponent EVENT_PROCESSOR, EVENT_LISTENER, etc
     * @param jmsUri           a fully blown jms URI
     * @return a destination type by calling convert() only if the component type is an EVENT_PROCESSOR
     */
    public Optional<Class<? extends Destination>> convertForEventProcessor(final String serviceComponent, final String jmsUri) {
        if (isEventProcessor(serviceComponent)) {
            return convert(jmsUri);
        }

        return Optional.empty();
    }

    public boolean isEventProcessor(final String serviceComponent) {
        return (serviceComponent != null && serviceComponent.contains(EVENT_PROCESSOR));
    }
}
