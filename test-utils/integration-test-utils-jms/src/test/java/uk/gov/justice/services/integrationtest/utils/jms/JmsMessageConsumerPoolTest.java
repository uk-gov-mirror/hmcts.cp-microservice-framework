package uk.gov.justice.services.integrationtest.utils.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.test.utils.core.messaging.TopicFactory;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JmsMessageConsumerPoolTest {

    private static final String TOPIC_NAME = "jms.topic.public.event";
    private static final String QUEUE_URI = "tcp://localhost:61616";

    @Mock
    private TopicFactory topicFactory;

    @Mock
    private JmsMessageConsumerFactory jmsMessageConsumerFactory;

    @Mock
    private JmsMessageReader jmsMessageReader;

    private JmsMessageConsumerPool jmsMessageConsumerPool;

    @BeforeEach
    void setUp() {
        jmsMessageConsumerPool = new JmsMessageConsumerPool(jmsMessageConsumerFactory, topicFactory, jmsMessageReader);
    }

    @Nested
    class CreateConsumerTest {
        @Test
        void shouldCreateIfNotExist() {
            final ActiveMQTopic topic = mock(ActiveMQTopic.class);
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            when(topicFactory.createTopic(TOPIC_NAME)).thenReturn(topic);
            when(jmsMessageConsumerFactory.createAndStart(topic, "CPPNAME IN ('event1')", QUEUE_URI)).thenReturn(messageConsumer);

            final MessageConsumer result = jmsMessageConsumerPool.getOrCreateMessageConsumer(TOPIC_NAME, QUEUE_URI, List.of("event1"));

            assertThat(result, is(messageConsumer));
        }

        @Test
        void shouldHandleMultipleEventNames() {
            final ActiveMQTopic topic = mock(ActiveMQTopic.class);
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            when(topicFactory.createTopic(TOPIC_NAME)).thenReturn(topic);
            when(jmsMessageConsumerFactory.createAndStart(topic, "CPPNAME IN ('event1,event2')", QUEUE_URI)).thenReturn(messageConsumer);

            final MessageConsumer result = jmsMessageConsumerPool.getOrCreateMessageConsumer(TOPIC_NAME, QUEUE_URI, List.of("event1", "event2"));

            assertThat(result, is(messageConsumer));
        }

        @Test
        void shouldNotCreateIfAlreadyExist() {
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            final Map<MessageConsumerIdentifier, MessageConsumer> existingMessageConsumers = new ConcurrentHashMap<>();
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event1')"), messageConsumer);
            ReflectionUtil.setField(jmsMessageConsumerPool, "messageConsumers", existingMessageConsumers);

            final MessageConsumer result = jmsMessageConsumerPool.getOrCreateMessageConsumer(TOPIC_NAME, QUEUE_URI, List.of("event1"));

            assertThat(result, is(messageConsumer));
            verifyNoInteractions(topicFactory);
            verifyNoInteractions(jmsMessageConsumerFactory);
        }
    }

    @Nested
    class ClearMessagesTest {

        @Test
        void shouldClearFromAllCachedConsumers() throws Exception {
            final MessageConsumer messageConsumer1 = mock(MessageConsumer.class);
            final MessageConsumer messageConsumer2 = mock(MessageConsumer.class);
            final Map<MessageConsumerIdentifier, MessageConsumer> existingMessageConsumers = new ConcurrentHashMap<>();
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event1')"), messageConsumer1);
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event2')"), messageConsumer2);
            ReflectionUtil.setField(jmsMessageConsumerPool, "messageConsumers", existingMessageConsumers);

            jmsMessageConsumerPool.clearMessages();

            verify(jmsMessageReader).clear(messageConsumer1);
            verify(jmsMessageReader).clear(messageConsumer2);
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            final MessageConsumer messageConsumer1 = mock(MessageConsumer.class);
            final Map<MessageConsumerIdentifier, MessageConsumer> existingMessageConsumers = new ConcurrentHashMap<>();
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event1')"), messageConsumer1);
            ReflectionUtil.setField(jmsMessageConsumerPool, "messageConsumers", existingMessageConsumers);
            doThrow(new JMSException("Test")).when(jmsMessageReader).clear(messageConsumer1);

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> jmsMessageConsumerPool.clearMessages());

            assertThat(e.getMessage(), is("Failed to clear consumer messages"));
        }
    }

    @Nested
    class CloseConsumersTest {

        @Test
        void shouldCloseAllCachedConsumers() throws Exception {
            final MessageConsumer messageConsumer1 = mock(MessageConsumer.class);
            final MessageConsumer messageConsumer2 = mock(MessageConsumer.class);
            final Map<MessageConsumerIdentifier, MessageConsumer> existingMessageConsumers = new ConcurrentHashMap<>();
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event1')"), messageConsumer1);
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event2')"), messageConsumer2);
            ReflectionUtil.setField(jmsMessageConsumerPool, "messageConsumers", existingMessageConsumers);

            jmsMessageConsumerPool.closeConsumers();

            verify(messageConsumer1).close();
            verify(messageConsumer2).close();
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            final MessageConsumer messageConsumer1 = mock(MessageConsumer.class);
            final Map<MessageConsumerIdentifier, MessageConsumer> existingMessageConsumers = new ConcurrentHashMap<>();
            existingMessageConsumers.put(new MessageConsumerIdentifier(TOPIC_NAME, "CPPNAME IN ('event1')"), messageConsumer1);
            ReflectionUtil.setField(jmsMessageConsumerPool, "messageConsumers", existingMessageConsumers);
            doThrow(new JMSException("Test")).when(messageConsumer1).close();

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> jmsMessageConsumerPool.closeConsumers());

            assertThat(e.getMessage(), is("Failed to close consumers"));
        }
    }

    @Test
    void closeShouldDelegate() {
        jmsMessageConsumerPool.close();

        verify(jmsMessageConsumerFactory).close();
    }
}