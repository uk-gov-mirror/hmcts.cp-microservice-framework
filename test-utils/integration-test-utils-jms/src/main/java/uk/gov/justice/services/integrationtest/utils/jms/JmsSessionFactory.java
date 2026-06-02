package uk.gov.justice.services.integrationtest.utils.jms;

import static java.lang.String.format;
import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

class JmsSessionFactory implements AutoCloseable {

    private final ActiveMQConnectionFactory activeMQConnectionFactory;

    private Session session;
    private Connection connection;
    
    JmsSessionFactory(final ActiveMQConnectionFactory activeMQConnectionFactory) {
        this.activeMQConnectionFactory = activeMQConnectionFactory;
    }

    Session create(final String queueUri) {

        try {
            connection = activeMQConnectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            return session;
        } catch (final JMSException e) {
            throw new JmsMessagingClientException(format("Failed to create JMS session for queue uri '%s'", queueUri), e);
        }
    }

    @Override
    public void close() {
        doClose(session);
        doClose(connection);
        doClose(activeMQConnectionFactory);
    }

    private void doClose(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception ignored) {
                // do nothing
            }
        }
    }
}
