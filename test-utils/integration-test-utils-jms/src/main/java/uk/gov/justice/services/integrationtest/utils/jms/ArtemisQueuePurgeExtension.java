package uk.gov.justice.services.integrationtest.utils.jms;

import static java.lang.String.format;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that drains all pending messages from a named Artemis queue before any tests
 * in a test class run. Use with {@code @RegisterExtension} and pass the queue name:
 *
 * <pre>
 * {@literal @}RegisterExtension
 * static ArtemisQueuePurgeExtension queuePurge =
 *     new ArtemisQueuePurgeExtension("mycontext\\.event\\.listener\\.mycontext\\.event");
 * </pre>
 *
 * The queue name uses WildFly's backslash-dot separator convention for durable subscription queues.
 */
public class ArtemisQueuePurgeExtension implements BeforeAllCallback {

    private final String queueName;

    public ArtemisQueuePurgeExtension(final String queueName) {
        this.queueName = queueName;
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        try (final ServerLocator locator = ActiveMQClient.createServerLocator(queueUri());
             final ClientSessionFactory sessionFactory = locator.createSessionFactory();
             final ClientSession session = sessionFactory.createSession()) {

            session.start();
            try (final ClientConsumer consumer = session.createConsumer(queueName)) {
                while (consumer.receiveImmediate() != null) {
                    // drain all pending messages
                }
            }
        } catch (final ActiveMQException e) {
            if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
                throw new JmsMessagingClientException(format("Failed to find queue with name '%s'", queueName), e);
            } else {
                throw new JmsMessagingClientException("Failed to purge Artemis queue: " + queueName, e);
            }
        } catch (final Exception e) {
            throw new JmsMessagingClientException("Failed to purge Artemis queue: " + queueName, e);
        }
    }
}
