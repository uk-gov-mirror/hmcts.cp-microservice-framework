package uk.gov.justice.services.integrationtest.utils.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultJmsMessageProducerClientTest {

    private static final String TOPIC_NAME = "jms.topic.public.event";
    private static final String QUEUE_URI = "tcp://localhost:61616";

    @Mock
    private JmsMessageProducerFactory jmsMessageProducerFactory;

    private DefaultJmsMessageProducerClient defaultJmsMessageProducerClient;

    @BeforeEach
    void setUp() {
        defaultJmsMessageProducerClient = new DefaultJmsMessageProducerClient(jmsMessageProducerFactory);
    }

    @Test
    void createProducerShouldDelegate() {
        defaultJmsMessageProducerClient.createProducer(TOPIC_NAME);

        verify(jmsMessageProducerFactory).getOrCreateMessageProducer(TOPIC_NAME, QUEUE_URI);
    }

    @Nested
    class SendJsonEnvelopeMessageTest {

        @Test
        void shouldThrowExceptionWhenMessageProducerNotCreated() {
            final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> defaultJmsMessageProducerClient.sendMessage("commandName", jsonEnvelope));
        }

        @Test
        void shouldCreateTextMessageAndSend() throws Exception {
            final MessageProducer messageProducer = mock(MessageProducer.class);
            final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
            final Session session = mock(Session.class);
            final TextMessage textMessage = mock(TextMessage.class);
            setField(defaultJmsMessageProducerClient, "messageProducer", messageProducer);
            //noinspection deprecation
            when(jsonEnvelope.toDebugStringPrettyPrint()).thenReturn("{}");
            when(jmsMessageProducerFactory.getSession(QUEUE_URI)).thenReturn(session);
            when(session.createTextMessage()).thenReturn(textMessage);

            defaultJmsMessageProducerClient.sendMessage("commandName", jsonEnvelope);

            verify(textMessage).setText("{}");
            verify(textMessage).setStringProperty("CPPNAME", "commandName");
            verify(messageProducer).send(textMessage);
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            final MessageProducer messageProducer = mock(MessageProducer.class);
            final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
            final Session session = mock(Session.class);
            setField(defaultJmsMessageProducerClient, "messageProducer", messageProducer);
            //noinspection deprecation
            when(jsonEnvelope.toDebugStringPrettyPrint()).thenReturn("{}");
            when(jmsMessageProducerFactory.getSession(QUEUE_URI)).thenReturn(session);
            doThrow(new JMSException("Test")).when(session).createTextMessage();

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> defaultJmsMessageProducerClient.sendMessage("commandName", jsonEnvelope));
            assertThat(e.getMessage(), is("Failed to send message. commandName: 'commandName', json: {}"));
        }
    }

    @Test
    void shouldSendJsonObject() {
        final MessageProducer messageProducer = mock(MessageProducer.class);
        setField(defaultJmsMessageProducerClient, "messageProducer", messageProducer);

        try (MockedStatic<EnvelopeFactory> envelopeFactory = mockStatic(EnvelopeFactory.class)) {            final JsonObject jsonObject = mock(JsonObject.class);
            final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
            envelopeFactory.when(() -> EnvelopeFactory.createEnvelope("commandName",jsonObject)).thenReturn(jsonEnvelope);
            final DefaultJmsMessageProducerClient jmpc = spy(defaultJmsMessageProducerClient); //May not be a nice way to use spy for the class under test, but this avoids duplication of all test scenarios when this method is invoking another method in the same class that is well tested
            doNothing().when(jmpc).sendMessage("commandName", jsonEnvelope);

            jmpc.sendMessage("commandName", jsonObject);

            verify(jmpc).sendMessage("commandName", jsonEnvelope);
        }
    }
}