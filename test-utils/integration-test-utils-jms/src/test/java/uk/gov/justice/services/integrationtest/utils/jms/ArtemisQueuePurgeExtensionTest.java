package uk.gov.justice.services.integrationtest.utils.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;

class ArtemisQueuePurgeExtensionTest {

    private static final String QUEUE_NAME = "mycontext\\.event\\.listener\\.mycontext\\.event";
    private static final String QUEUE_URI = "tcp://localhost:61616";

    private final ServerLocator serverLocator = mock(ServerLocator.class);
    private final ClientSessionFactory sessionFactory = mock(ClientSessionFactory.class);
    private final ClientSession session = mock(ClientSession.class);
    private final ClientConsumer consumer = mock(ClientConsumer.class);
    private final ExtensionContext extensionContext = mock(ExtensionContext.class);

    private ArtemisQueuePurgeExtension extension;

    @BeforeEach
    void createExtension() {
        extension = new ArtemisQueuePurgeExtension(QUEUE_NAME);
    }

    @Test
    void shouldDrainAllPendingMessagesFromQueue() throws Exception {
        final ClientMessage message1 = mock(ClientMessage.class);
        final ClientMessage message2 = mock(ClientMessage.class);

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);
        when(session.createConsumer(QUEUE_NAME)).thenReturn(consumer);
        when(consumer.receiveImmediate()).thenReturn(message1, message2, null);

        try (final MockedStatic<ActiveMQClient> activeMQClient = mockStatic(ActiveMQClient.class);
             final MockedStatic<QueueUriProvider> queueUriProvider = mockStatic(QueueUriProvider.class)) {

            queueUriProvider.when(QueueUriProvider::queueUri).thenReturn(QUEUE_URI);
            activeMQClient.when(() -> ActiveMQClient.createServerLocator(QUEUE_URI)).thenReturn(serverLocator);

            extension.beforeAll(extensionContext);
        }

        verify(session).start();
        verify(consumer, times(3)).receiveImmediate();
    }

    @Test
    void shouldThrowJmsMessagingClientException_whenQueueDoesNotExist() throws Exception {
        final ActiveMQException queueNotFound = new ActiveMQException("Queue does not exist");

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);
        when(session.createConsumer(QUEUE_NAME)).thenThrow(queueNotFound);

        try (final MockedStatic<ActiveMQClient> activeMQClient = mockStatic(ActiveMQClient.class);
             final MockedStatic<QueueUriProvider> queueUriProvider = mockStatic(QueueUriProvider.class)) {

            queueUriProvider.when(QueueUriProvider::queueUri).thenReturn(QUEUE_URI);
            activeMQClient.when(() -> ActiveMQClient.createServerLocator(QUEUE_URI)).thenReturn(serverLocator);

            final JmsMessagingClientException thrown = assertThrows(JmsMessagingClientException.class,
                    () -> extension.beforeAll(extensionContext));

            assertThat(thrown.getMessage(), is("Failed to find queue with name '" + QUEUE_NAME + "'"));
            assertThat(thrown.getCause(), is(queueNotFound));
        }
    }

    @Test
    void shouldThrowJmsMessagingClientException_whenActiveMqExceptionHasNullMessage() throws Exception {
        final ActiveMQException nullMessageException = new ActiveMQException((String) null);

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);
        when(session.createConsumer(QUEUE_NAME)).thenThrow(nullMessageException);

        try (final MockedStatic<ActiveMQClient> activeMQClient = mockStatic(ActiveMQClient.class);
             final MockedStatic<QueueUriProvider> queueUriProvider = mockStatic(QueueUriProvider.class)) {

            queueUriProvider.when(QueueUriProvider::queueUri).thenReturn(QUEUE_URI);
            activeMQClient.when(() -> ActiveMQClient.createServerLocator(QUEUE_URI)).thenReturn(serverLocator);

            final JmsMessagingClientException thrown = assertThrows(JmsMessagingClientException.class,
                    () -> extension.beforeAll(extensionContext));

            assertThat(thrown.getMessage(), is("Failed to purge Artemis queue: " + QUEUE_NAME));
            assertThat(thrown.getCause(), is(nullMessageException));
        }
    }

    @Test
    void shouldThrowJmsMessagingClientException_whenActiveMqExceptionIsUnrelated() throws Exception {
        final ActiveMQException connectionFailed = new ActiveMQException("Connection refused");

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);
        when(session.createConsumer(QUEUE_NAME)).thenThrow(connectionFailed);

        try (final MockedStatic<ActiveMQClient> activeMQClient = mockStatic(ActiveMQClient.class);
             final MockedStatic<QueueUriProvider> queueUriProvider = mockStatic(QueueUriProvider.class)) {

            queueUriProvider.when(QueueUriProvider::queueUri).thenReturn(QUEUE_URI);
            activeMQClient.when(() -> ActiveMQClient.createServerLocator(QUEUE_URI)).thenReturn(serverLocator);

            final JmsMessagingClientException thrown = assertThrows(JmsMessagingClientException.class,
                    () -> extension.beforeAll(extensionContext));

            assertThat(thrown.getMessage(), is("Failed to purge Artemis queue: " + QUEUE_NAME));
            assertThat(thrown.getCause(), is(connectionFailed));
        }
    }

    @Test
    void shouldThrowJmsMessagingClientException_whenUnexpectedExceptionOccurs() throws Exception {
        final RuntimeException unexpected = new RuntimeException("unexpected failure");

        when(serverLocator.createSessionFactory()).thenReturn(sessionFactory);
        when(sessionFactory.createSession()).thenReturn(session);
        when(session.createConsumer(QUEUE_NAME)).thenThrow(unexpected);

        try (final MockedStatic<ActiveMQClient> activeMQClient = mockStatic(ActiveMQClient.class);
             final MockedStatic<QueueUriProvider> queueUriProvider = mockStatic(QueueUriProvider.class)) {

            queueUriProvider.when(QueueUriProvider::queueUri).thenReturn(QUEUE_URI);
            activeMQClient.when(() -> ActiveMQClient.createServerLocator(QUEUE_URI)).thenReturn(serverLocator);

            final JmsMessagingClientException thrown = assertThrows(JmsMessagingClientException.class,
                    () -> extension.beforeAll(extensionContext));

            assertThat(thrown.getMessage(), is("Failed to purge Artemis queue: " + QUEUE_NAME));
            assertThat(thrown.getCause(), is(unexpected));
        }
    }
}
