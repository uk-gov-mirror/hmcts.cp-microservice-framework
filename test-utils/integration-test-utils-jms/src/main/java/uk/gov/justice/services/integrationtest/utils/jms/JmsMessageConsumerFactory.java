package uk.gov.justice.services.integrationtest.utils.jms;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;

class JmsMessageConsumerFactory {

    private final JmsSessionFactory jmsSessionFactory;
    private Session session;

    JmsMessageConsumerFactory(final JmsSessionFactory jmsSessionFactory) {
        this.jmsSessionFactory = jmsSessionFactory;
    }

    MessageConsumer createAndStart(final ActiveMQTopic topic, final String messageSelector, final String queueUri) {

        if (session == null) {
            session = jmsSessionFactory.create(queueUri);
        }

        try{
            return session.createConsumer(topic, messageSelector);
        } catch (JMSException e) {
            throw  new JmsMessagingClientException("Error creating consumer topic:%s, messageSelector:%s".formatted(topic.getTopicName(), messageSelector), e);
        }
    }

    void close() {
        jmsSessionFactory.close(); //closes session and underlying connection, connectionFactory
        this.session = null;
    }
}
