package uk.gov.justice.services.integrationtest.utils.jms;

import uk.gov.justice.services.test.utils.core.messaging.TopicFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;

/**
 * This caches jms consumers that are created with various message selectors to either public/private topics.
 * Provides method to clear messages from all cached consumers (so that same consumers can be reused across tests)
 * Provides method to close cached consumers without closing underlying session and connection (provides capability to recycle consumers after each Test class)
 * Provides method to close underlying session/connection
 */
class JmsMessageConsumerPool {

    private static final String MESSAGE_SELECTOR_TEMPLATE = "CPPNAME IN ('%s')";
    private final Map<MessageConsumerIdentifier, MessageConsumer> messageConsumers = new ConcurrentHashMap<>();
    private final Map<String, ActiveMQTopic> topics = new ConcurrentHashMap<>();
    private final JmsMessageConsumerFactory jmsMessageConsumerFactory;
    private final TopicFactory topicFactory;
    private final JmsMessageReader jmsMessageReader;

    JmsMessageConsumerPool(final JmsMessageConsumerFactory jmsMessageConsumerFactory,
                           final TopicFactory topicFactory,
                           final JmsMessageReader jmsMessageReader) {
        this.jmsMessageConsumerFactory = jmsMessageConsumerFactory;
        this.topicFactory = topicFactory;
        this.jmsMessageReader = jmsMessageReader;
    }

    MessageConsumer getOrCreateMessageConsumer(
            final String topicName,
            final String queueUri,
            final List<String> eventNames) {
        final String messageSelector = MESSAGE_SELECTOR_TEMPLATE.formatted(String.join(",", eventNames));

        return messageConsumers.computeIfAbsent(new MessageConsumerIdentifier(topicName, messageSelector),
                (mcIdentifier) -> createMessageConsumer(mcIdentifier, queueUri));
    }

    private MessageConsumer createMessageConsumer(final MessageConsumerIdentifier messageConsumerIdentifier, final String queueUri) {
        final ActiveMQTopic topic = topics.computeIfAbsent(messageConsumerIdentifier.topicName(), topicFactory::createTopic);
        return jmsMessageConsumerFactory.createAndStart(topic, messageConsumerIdentifier.messageSelector(), queueUri);
    }

    void clearMessages() {
        try {
            for (final MessageConsumer messageConsumer : messageConsumers.values()) {
                jmsMessageReader.clear(messageConsumer);
            }
        } catch (JMSException e) {
            throw new JmsMessagingClientException("Failed to clear consumer messages", e);
        }
    }

    void close() {
        jmsMessageConsumerFactory.close(); //Closes underlying session and connection
    }

    void closeConsumers() {
        try {
            for (final MessageConsumer messageConsumer : messageConsumers.values()) {
                messageConsumer.close();
            }
        } catch (JMSException e) {
            throw new JmsMessagingClientException("Failed to close consumers", e);
        }

        messageConsumers.clear();
    }
}
