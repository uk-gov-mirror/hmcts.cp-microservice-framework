package uk.gov.justice.services.integrationtest.utils.jms;

import uk.gov.justice.services.test.utils.core.messaging.TopicFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;

class JmsMessageProducerFactory {

    private Session session;
    private final Map<String, MessageProducer> messageProducers = new ConcurrentHashMap<>();
    private final JmsSessionFactory jmsSessionFactory;
    private final TopicFactory topicFactory;

    JmsMessageProducerFactory(final JmsSessionFactory jmsSessionFactory, final TopicFactory topicFactory) {
        this.jmsSessionFactory = jmsSessionFactory;
        this.topicFactory = topicFactory;
    }

    MessageProducer getOrCreateMessageProducer(final String topicName, final String queueUri) {
        createSessionIfNull(queueUri);
        return messageProducers.computeIfAbsent(topicName, this::createMessageProducer);
    }

    Session getSession(String queueUri) {
        createSessionIfNull(queueUri);
        return session;
    }

    void closeProducers() {
        try{
            for(final MessageProducer messageProducer : messageProducers.values()) {
                messageProducer.close();
            }
            messageProducers.clear();
        } catch (JMSException e) {
            throw new JmsMessagingClientException("Failed to close producers", e);
        }
    }

    void close() {
        jmsSessionFactory.close(); //closes session and underlying connection, connectionFactory
    }

    private void createSessionIfNull(String queueUri) {
        if (session == null) {
            session = jmsSessionFactory.create(queueUri);
        }
    }

    private MessageProducer createMessageProducer(final String topicName) {
        try {
            final Destination destination = topicFactory.createTopic(topicName);
            return session.createProducer(destination);
        } catch (JMSException e) {
            throw new JmsMessagingClientException("Failed to create producer for topic:%s".formatted(topicName), e);
        }
    }
}
