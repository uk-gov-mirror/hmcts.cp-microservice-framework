package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.subscription.SubscriptionManager;

import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import org.slf4j.Logger;

public class SubscriptionJmsProcessorDelegate {

    @Inject
    private EnvelopeConverter envelopeConverter;

    @Inject
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Inject
    private Logger logger;

    public void process(final Message message, final SubscriptionManager subscriptionManager) {

        if (logger.isTraceEnabled()) {
            logger.trace(format("Processing JMS message: '%s'", jmsMessageLoggerHelper.toJmsTraceString(message)));
        }

        if (message instanceof TextMessage) {
            final JsonEnvelope jsonEnvelope = envelopeConverter.fromMessage((TextMessage) message);

            subscriptionManager.process(jsonEnvelope);

            if (logger.isTraceEnabled()) {
                logger.trace(format("JMS message processed: '%s'", jsonEnvelope));
            }
        } else {
            throw new InvalidJmsMessageTypeException(format("Message is not an instance of TextMessage. MessageId: '%s'", getIdFrom(message)));
        }
    }

    private String getIdFrom(final Message message) {
        try {
            return message.getJMSMessageID();
        } catch (final JMSException e) {
            throw new InvalidJmsMessageTypeException("Failed to get message id from JMS message", e);
        }
    }
}
