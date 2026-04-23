package uk.gov.justice.services.integrationtest.utils.jms;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.integrationtest.utils.jms.converters.ToJsonEnvelopeMessageConverter;
import uk.gov.justice.services.integrationtest.utils.jms.converters.ToJsonPathMessageConverter;
import uk.gov.justice.services.integrationtest.utils.jms.converters.ToStringMessageConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultJmsMessageConsumerClientTest {

    private static final String TOPIC_NAME = "jms.topic.public.event";
    private static final List<String> EVENT_NAMES = List.of("event1");
    private static final String QUEUE_URI = "tcp://localhost:61616";
    private static final JMSException jmsException = new JMSException("Test");
    private static final MessageConsumer messageConsumer = mock(MessageConsumer.class);

    @Mock
    private JmsMessageConsumerPool jmsMessageConsumerPool;
    @Mock
    private JmsMessageReader jmsMessageReader;
    @Mock
    private ToStringMessageConverter toStringMessageConverter;
    @Mock
    private ToJsonEnvelopeMessageConverter toJsonEnvelopeMessageConverter;
    @Mock
    private ToJsonPathMessageConverter toJsonPathMessageConverter;

    private DefaultJmsMessageConsumerClient defaultJmsMessageConsumerClient;

    @BeforeEach
    void setUp() {
        defaultJmsMessageConsumerClient = new DefaultJmsMessageConsumerClient(jmsMessageConsumerPool,
                jmsMessageReader,
                toStringMessageConverter,
                toJsonEnvelopeMessageConverter,
                toJsonPathMessageConverter);
    }

    @Test
    void shouldStartConsumer() {
        when(jmsMessageConsumerPool.getOrCreateMessageConsumer(TOPIC_NAME, QUEUE_URI, EVENT_NAMES)).thenReturn(messageConsumer);

        defaultJmsMessageConsumerClient.startConsumer(TOPIC_NAME, EVENT_NAMES);

        assertThat(getValueOfField(defaultJmsMessageConsumerClient, "messageConsumer", MessageConsumer.class), is(messageConsumer));
    }

    @Nested
    class RetrieveMessageTest {

        @BeforeEach
        void setUp() {
            setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
        }

        @Nested
        class NoWaitTest {
            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessageNoWait(messageConsumer, toStringMessageConverter)).thenReturn(of("message"));

                final Optional<String> result = defaultJmsMessageConsumerClient.retrieveMessageNoWait();

                assertThat(result, is(of("message")));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessageNoWait(messageConsumer, toStringMessageConverter);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageNoWait());

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageNoWait());

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class WithDefaultTimeoutTest {

            @Test
            void shouldDelegate() throws Exception {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
                when(jmsMessageReader.retrieveMessage(messageConsumer, toStringMessageConverter, 20_000)).thenReturn(of("message"));

                final Optional<String> result = defaultJmsMessageConsumerClient.retrieveMessage();

                assertThat(result, is(of("message")));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
                doThrow(jmsException).when(jmsMessageReader).retrieveMessage(messageConsumer, toStringMessageConverter, 20_000);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessage());

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessage());

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class WithTimeoutTest {

            @Test
            void shouldDelegate() throws Exception {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
                when(jmsMessageReader.retrieveMessage(messageConsumer, toStringMessageConverter, 1000)).thenReturn(of("message"));

                final Optional<String> result = defaultJmsMessageConsumerClient.retrieveMessage(1000);

                assertThat(result, is(of("message")));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
                doThrow(jmsException).when(jmsMessageReader).retrieveMessage(messageConsumer, toStringMessageConverter, 1000);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessage(1000));

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessage(1000));

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }
    }

    @Nested
    class RetrieveMessageAsJsonEnvelopeTest {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        @BeforeEach
        void setUp() {
            setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
        }

        @Nested
        class NoWaitTest {

            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessageNoWait(messageConsumer, toJsonEnvelopeMessageConverter)).thenReturn(of(jsonEnvelope));

                final Optional<JsonEnvelope> result = defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelopeNoWait();

                assertThat(result, is(of(jsonEnvelope)));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessageNoWait(messageConsumer, toJsonEnvelopeMessageConverter);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelopeNoWait());

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelopeNoWait());

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class WithDefaultTimeoutTest {
            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessage(messageConsumer, toJsonEnvelopeMessageConverter, 20_000)).thenReturn(of(jsonEnvelope));

                final Optional<JsonEnvelope> result = defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelope();

                assertThat(result, is(of(jsonEnvelope)));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessage(messageConsumer, toJsonEnvelopeMessageConverter, 20_000);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelope());

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelope());

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class WithTimeoutTest {

            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessage(messageConsumer, toJsonEnvelopeMessageConverter, 1000)).thenReturn(of(jsonEnvelope));

                final Optional<JsonEnvelope> result = defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(1000);

                assertThat(result, is(of(jsonEnvelope)));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessage(messageConsumer, toJsonEnvelopeMessageConverter, 1000);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(1000));

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(1000));

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }
    }

    @Nested
    class RetrieveMessageAsJsonPathTest {

        final JsonPath jsonPath = mock(JsonPath.class);

        @BeforeEach
        void setUp() {
            setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
        }

        @Nested
        class NoWaitTest {
            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessageNoWait(messageConsumer, toJsonPathMessageConverter)).thenReturn(of(jsonPath));

                final Optional<JsonPath> result = defaultJmsMessageConsumerClient.retrieveMessageAsJsonPathNoWait();

                assertThat(result, is(of(jsonPath)));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessageNoWait(messageConsumer, toJsonPathMessageConverter);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonPathNoWait());

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonPathNoWait());

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class WithDefaultTimeoutTest {

            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessage(messageConsumer, toJsonPathMessageConverter, 20_000)).thenReturn(of(jsonPath));

                final Optional<JsonPath> result = defaultJmsMessageConsumerClient.retrieveMessageAsJsonPath();

                assertThat(result, is(of(jsonPath)));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessage(messageConsumer, toJsonPathMessageConverter, 20_000);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonPath());

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonPath());

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class WithTimeoutTest {

            @Test
            void shouldDelegate() throws Exception {
                when(jmsMessageReader.retrieveMessage(messageConsumer, toJsonPathMessageConverter, 1000)).thenReturn(of(jsonPath));

                final Optional<JsonPath> result = defaultJmsMessageConsumerClient.retrieveMessageAsJsonPath(1000);

                assertThat(result, is(of(jsonPath)));
            }

            @Test
            void shouldConvertJmsException() throws Exception {
                doThrow(jmsException).when(jmsMessageReader).retrieveMessage(messageConsumer, toJsonPathMessageConverter, 1000);

                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonPath(1000));

                assertThat(e.getMessage(), is("Failed to read message"));
                assertThat(e.getCause(), is(jmsException));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessageAsJsonPath(1000));

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }

        @Nested
        class ReadMultipleMessagesTest {

            @Test
            void shouldReadMultipleMessages() throws Exception {
                final JsonPath jsonPath1 = mock(JsonPath.class);
                final JsonPath jsonPath2 = mock(JsonPath.class);
                when(jmsMessageReader.retrieveMessage(messageConsumer, toJsonPathMessageConverter, 20_000))
                        .thenReturn(of(jsonPath1))
                        .thenReturn(of(jsonPath2));

                final List<JsonPath> result = defaultJmsMessageConsumerClient.retrieveMessagesAsJsonPath(2);

                assertThat(result.size(), is(2));
                assertThat(result, hasItems(jsonPath1, jsonPath2));
            }

            @Test
            void shouldReturnNullWhenJmsReaderReturnsEmpty() throws Exception {
                when(jmsMessageReader.retrieveMessage(messageConsumer, toJsonPathMessageConverter, 20_000)).thenReturn(empty());

                final List<JsonPath> result = defaultJmsMessageConsumerClient.retrieveMessagesAsJsonPath(1);

                assertNull(result.get(0));
            }

            @Test
            void shouldThrowExceptionWhenMessageConsumerNotStarted() {
                setField(defaultJmsMessageConsumerClient, "messageConsumer", null);
                final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.retrieveMessagesAsJsonPath(1));

                assertThat(e.getMessage(), is("MessageConsumer is not started. Please call startConsumer(...) first"));
            }
        }
    }

    @Nested
    class ClearMessagesTest {

        @BeforeEach
        void setUp() {
            setField(defaultJmsMessageConsumerClient, "messageConsumer", messageConsumer);
        }

        @Test
        void shouldDelegate() throws Exception {
            defaultJmsMessageConsumerClient.clearMessages();

            verify(jmsMessageReader).clear(messageConsumer);
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            doThrow(jmsException).when(jmsMessageReader).clear(messageConsumer);

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class, () -> defaultJmsMessageConsumerClient.clearMessages());

            assertThat(e.getMessage(), is("Failed to clear messages"));
        }
    }
}