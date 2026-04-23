package uk.gov.justice.services.test.utils.core.messaging;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

/**
 * Test utility class for sending messages to queues
 */
@SuppressWarnings("unused")
public class MessageProducerClient implements AutoCloseable {

    private static final String QUEUE_URI = queueUri();

    private Session session;
    private MessageProducer messageProducer;
    private Connection connection;
    private String topicName;

    private final ActiveMQConnectionFactory activeMQConnectionFactory;

    @Deprecated(since = "Please use MessageProducerClientBuilder  to create instead of using this constructor")
    public MessageProducerClient() {
        this(new ActiveMQConnectionFactory());
    }

    MessageProducerClient(final ActiveMQConnectionFactory activeMQConnectionFactory) {
        this.activeMQConnectionFactory = activeMQConnectionFactory;
    }

    /**
     * Starts the message producer for a specific topic. Must be called before any messages can be
     * sent.
     *
     * @param topicName the name of the topic to send to
     */
    public void startProducer(final String topicName) {

        if (topicName.equals(this.topicName)) {
            return;
        }

        try {
            if (connection == null) {
                createConnection();
            }

            if (messageProducer != null) {
                close(messageProducer);
            }

            final Destination destination = session.createTopic(topicName);
            messageProducer = session.createProducer(destination);

            this.topicName = topicName;
        } catch (final JMSException e) {
            close();
            throw new MessageProducerClientException("Failed to create message producer to topic: '" + topicName + "', queue uri: '" + QUEUE_URI + "'", e);
        }
    }

    private void createConnection() throws JMSException {
        activeMQConnectionFactory.setBrokerURL(QUEUE_URI);
        connection = activeMQConnectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, AUTO_ACKNOWLEDGE);
    }

    /**
     * Sends a message to the topic specified in <code>startProducer(...)</code>
     *
     * @param commandName the name of the command
     * @param payload     the payload to be wrapped in a simple JsonEnvelope
     */
    public void sendMessage(final String commandName, final JsonObject payload) {

        final JsonEnvelope jsonEnvelope = createEnvelope(commandName, payload);

        sendMessage(commandName, jsonEnvelope);
    }

    /**
     * Sends a message to the topic specified in <code>startProducer(...)</code>
     *
     * @param commandName  the name of the command
     * @param jsonEnvelope the full JsonEnvelope to send as a message
     */
    public void sendMessage(final String commandName, final JsonEnvelope jsonEnvelope) {
        if (messageProducer == null) {
            close();
            throw new RuntimeException("Message producer not started. Please call startProducer(...) first.");
        }

        @SuppressWarnings("deprecation") final String json = jsonEnvelope.toDebugStringPrettyPrint();

        try {
            final TextMessage message = session.createTextMessage();

            message.setText(json);
            message.setStringProperty("CPPNAME", commandName);

            messageProducer.send(message);
        } catch (final JMSException e) {
            close();
            throw new MessageProducerClientException("Failed to send message. commandName: '" + commandName + "', json: " + json, e);
        }
    }

    /**
     * closes all open resources
     */
    @Override
    public void close() {
        close(messageProducer);
        close(session);
        close(connection);

        session = null;
        messageProducer = null;
        connection = null;
        topicName = null;
    }

    private void close(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
