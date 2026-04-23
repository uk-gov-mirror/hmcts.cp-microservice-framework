package uk.gov.justice.services.adapter.messaging;

import uk.gov.justice.services.subscription.SubscriptionManager;

import jakarta.inject.Inject;
import jakarta.jms.Message;

/**
 * In order to minimise the amount of generated code in the JMS Listener implementation classes,
 * this service encapsulates all the logic for validating a message and converting it to an
 * envelope, passing it to a subscription manager to manage the message.  This allows testing of
 * this logic independently from the automated generation code.
 */
public class DefaultSubscriptionJmsProcessor implements SubscriptionJmsProcessor {

    @Inject
    private SubscriptionJmsProcessorDelegate subscriptionJmsProcessorDelegate;

    @Override
    public void process(final Message message, final SubscriptionManager subscriptionManager) {
        processEventFromEventTopic(message, subscriptionManager);
    }

    private void processEventFromEventTopic(final Message message, final SubscriptionManager subscriptionManager) {
        subscriptionJmsProcessorDelegate.process(message, subscriptionManager);
    }
}
