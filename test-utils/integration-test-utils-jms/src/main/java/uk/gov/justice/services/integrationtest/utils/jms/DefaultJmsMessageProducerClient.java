package uk.gov.justice.services.integrationtest.utils.jms;

import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;

/**
 * Use {@link JmsMessageProducerClientProvider} to create instance
 * It's safe to create multiple instances of this class with same parameters, as underlying jms producer is cached and it retrieves existing producer
 *  It's not recommended to cache these classes across Test classes, reusability is recommended only within same Test class. After each Test class all cached producers are closed.
 *  Hence, don't create instances of this class in Helper/Utility classes and assign it to static class variables. Doing this within a Test class is totally fine though
 * Life cycle of underlying jms producer is not managed by this class (Managed by {@link JmsMessageConsumerPool} through Junit hooks {@link JmsResourceManagementExtension}) and hence these instances can be created without worrying about cleaning underlying jms resources
 * This class provides all various helper methods to send message to underlying topic
 * If there is no convenient method that you are looking for, please enhance this class rather than creating them in context ITs. This approach avoids duplication and promotes reusability across different context Integration tests
 */@SuppressWarnings("unused")
public class DefaultJmsMessageProducerClient implements JmsMessageProducerClient {

    private static final String QUEUE_URI = queueUri();

    private final JmsMessageProducerFactory jmsMessageProducerFactory;
    private MessageProducer messageProducer;

    DefaultJmsMessageProducerClient(JmsMessageProducerFactory jmsMessageProducerFactory) {
        this.jmsMessageProducerFactory = jmsMessageProducerFactory;
    }

    @Override
    public void createProducer(String topicName) {
        this.messageProducer = jmsMessageProducerFactory.getOrCreateMessageProducer(topicName, QUEUE_URI);
    }

    //Add convenient methods to send message
    @Override
    public void sendMessage(final String commandName, final JsonObject payload) {
        final JsonEnvelope jsonEnvelope = createEnvelope(commandName, payload);

        sendMessage(commandName, jsonEnvelope);
    }

    @Override
    public void sendMessage(final String commandName, final JsonEnvelope jsonEnvelope) {
        if (messageProducer == null) {
            throw new JmsMessagingClientException("Message producer should not be null. Invoke createProducer(...) first");
        }

        @SuppressWarnings("deprecation")
        final String json = jsonEnvelope.toDebugStringPrettyPrint();

        try {
            final TextMessage message = createTextMessage(commandName, json);
            messageProducer.send(message);
        } catch (final JMSException e) {
            throw new JmsMessagingClientException("Failed to send message. commandName: '" + commandName + "', json: " + json, e);
        }
    }

    private TextMessage createTextMessage(String commandName, String json) throws JMSException {
        final TextMessage message = jmsMessageProducerFactory.getSession(QUEUE_URI).createTextMessage();
        message.setText(json);
        message.setStringProperty("CPPNAME", commandName);

        return message;
    }
}
