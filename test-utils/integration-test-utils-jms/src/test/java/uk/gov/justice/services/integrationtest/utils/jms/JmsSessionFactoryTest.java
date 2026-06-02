package uk.gov.justice.services.integrationtest.utils.jms;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JmsSessionFactoryTest {

    private static final String QUEUE_URI = "tcp://localhost:61616";

    @Mock
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    private JmsSessionFactory jmsSessionFactory;

    @BeforeEach
    void setUp() {
        jmsSessionFactory = new JmsSessionFactory(activeMQConnectionFactory);
    }

    @Nested
    class CreateTest {

        @Test
        void shouldCreateSession() throws Exception {
            final Connection connection = mock(Connection.class);
            final Session session = mock(Session.class);
            when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
            when(connection.createSession(false, AUTO_ACKNOWLEDGE)).thenReturn(session);

            final Session result = jmsSessionFactory.create(QUEUE_URI);

            assertThat(result, is(session));
            verify(connection).start();
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            final Connection connection = mock(Connection.class);
            doThrow(new JMSException("Test")).when(activeMQConnectionFactory).createConnection();

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsSessionFactory.create(QUEUE_URI));

            assertThat(e.getMessage(), is("Failed to create JMS session for queue uri 'tcp://localhost:61616'"));
        }
    }

    @Nested
    class CloseTest {

        @Test
        void shouldCloseSessionAndConnectionAndConnectionFactory() throws Exception {
            final Session session = mock(Session.class);
            final Connection connection = mock(Connection.class);
            setField(jmsSessionFactory, "session", session);
            setField(jmsSessionFactory, "connection", connection);

            jmsSessionFactory.close();

            verify(session).close();
            verify(connection).close();
            verify(activeMQConnectionFactory).close();
        }

        @Test
        void shouldIgnoreExceptionOnClose() throws Exception {
            final Connection connection = mock(Connection.class);
            final Session session = mock(Session.class);
            setField(jmsSessionFactory, "session", session);
            setField(jmsSessionFactory, "connection", connection);
            doThrow(new JMSException("Test")).when(session).close();
            doThrow(new JMSException("Test")).when(connection).close();
            doThrow(new JMSException("Test")).when(activeMQConnectionFactory).close();

            jmsSessionFactory.close();

            verify(session).close();
            verify(connection).close();
            verify(activeMQConnectionFactory).close();
        }
    }
}