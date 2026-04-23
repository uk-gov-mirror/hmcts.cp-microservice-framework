package uk.gov.justice.services.integrationtest.utils.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JmsMessageConsumerFactoryTest {

    @Mock
    private JmsSessionFactory jmsSessionFactory;

    private JmsMessageConsumerFactory jmsMessageConsumerFactory;

    @BeforeEach
    void setUp() {
        jmsMessageConsumerFactory = new JmsMessageConsumerFactory(jmsSessionFactory);
    }

    @Nested
    class CreateAndStartTest {

        private final String queueUri = "tcp://localhost:61616";
        private final String messageSelector = "eventName";
        private final ActiveMQTopic topic = new ActiveMQTopic("jms.topic.public.event");

        @Test
        void shouldCreateSessionAndConsumer() throws Exception {
            final Session session = mock(Session.class);
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            when(jmsSessionFactory.create(queueUri)).thenReturn(session);
            when(session.createConsumer(topic, messageSelector)).thenReturn(messageConsumer);

            final MessageConsumer result = jmsMessageConsumerFactory.createAndStart(topic, messageSelector, queueUri);

            assertThat(result, is(messageConsumer));
        }

        @Test
        void shouldReuseExistingSession() throws Exception {
            final Session session = mock(Session.class);
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            setField(jmsMessageConsumerFactory, "session", session);
            when(session.createConsumer(topic, messageSelector)).thenReturn(messageConsumer);

            final MessageConsumer result = jmsMessageConsumerFactory.createAndStart(topic, messageSelector, queueUri);

            assertThat(result, is(messageConsumer));
            verifyNoInteractions(jmsSessionFactory);
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            final Session session = mock(Session.class);
            setField(jmsMessageConsumerFactory, "session", session);
            doThrow(new JMSException("Test")).when(session).createConsumer(topic, messageSelector);

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> jmsMessageConsumerFactory.createAndStart(topic, messageSelector, queueUri));

            assertThat(e.getMessage(), is("Error creating consumer topic:jms.topic.public.event, messageSelector:eventName"));
        }
    }

    @Test
    void closeShouldDelete() {
        jmsMessageConsumerFactory.close();

        verify(jmsSessionFactory).close();
    }
}