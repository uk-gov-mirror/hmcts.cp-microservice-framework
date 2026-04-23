package uk.gov.justice.services.integrationtest.utils.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.integrationtest.utils.jms.converters.ToStringMessageConverter;

import java.util.Optional;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JmsMessageReaderTest {

    private final JmsMessageReader jmsMessageReader = new JmsMessageReader();

    @Nested
    class RetrieveMessageNoWaitTest {

        @Test
        void shouldReadAndReturnMessage() throws Exception {
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            final ToStringMessageConverter messageConverter = new ToStringMessageConverter();
            final TextMessage textMessage = mock(TextMessage.class);
            when(messageConsumer.receiveNoWait()).thenReturn(textMessage);
            when(textMessage.getText()).thenReturn("message");

            final Optional<String> result = jmsMessageReader.retrieveMessageNoWait(messageConsumer, messageConverter);

            assertThat(result.get(), is("message"));
        }

        @Test
        void shouldConvertJmsExceptionOnMessageReadFailure() throws Exception {
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            final ToStringMessageConverter messageConverter = new ToStringMessageConverter();
            final TextMessage textMessage = mock(TextMessage.class);
            when(messageConsumer.receiveNoWait()).thenReturn(textMessage);
            doThrow(new JMSException("Test")).when(textMessage).getText();

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageReader.retrieveMessageNoWait(messageConsumer, messageConverter));

            assertThat(e.getMessage(), is("Failed to retrieve message"));
        }

        @Test
        void shouldThrowExceptionWhenNoMessageConsumerSupplied() {
            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageReader.retrieveMessageNoWait(null, null));

            assertThat(e.getMessage(), is("Message consumer not started"));
        }
    }

    @Nested
    class RetrieveMessageWithTimeOutTest {

        @Test
        void shouldReadAndReturnMessage() throws Exception {
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            final ToStringMessageConverter messageConverter = new ToStringMessageConverter();
            final TextMessage textMessage = mock(TextMessage.class);
            when(messageConsumer.receive(1000)).thenReturn(textMessage);
            when(textMessage.getText()).thenReturn("message");

            final Optional<String> result = jmsMessageReader.retrieveMessage(messageConsumer, messageConverter, 1000);

            assertThat(result.get(), is("message"));
        }

        @Test
        void shouldConvertJmsExceptionOnMessageReadFailure() throws Exception {
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            final ToStringMessageConverter messageConverter = new ToStringMessageConverter();
            final TextMessage textMessage = mock(TextMessage.class);
            when(messageConsumer.receive(1000)).thenReturn(textMessage);
            doThrow(new JMSException("Test")).when(textMessage).getText();

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageReader.retrieveMessage(messageConsumer, messageConverter, 1000));

            assertThat(e.getMessage(), is("Failed to retrieve message"));
        }

        @Test
        void shouldThrowExceptionWhenNoMessageConsumerSupplied() {
            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageReader.retrieveMessage(null, null, 1000));

            assertThat(e.getMessage(), is("Message consumer not started"));
        }
    }

    @Nested
    class ClearTest {

        @Test
        void shouldReadAllMessagesUntilNoMessageFound() throws Exception {
            final MessageConsumer messageConsumer = mock(MessageConsumer.class);
            final TextMessage textMessage = mock(TextMessage.class);
            when(messageConsumer.receiveNoWait()).thenReturn(textMessage).thenReturn(null);
            when(textMessage.getText()).thenReturn("message");

            jmsMessageReader.clear(messageConsumer);

            verify(messageConsumer, times(2)).receiveNoWait();
        }

        @Test
        void shouldThrowExceptionWhenNoMessageConsumerSupplied() {
            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageReader.clear(null));

            assertThat(e.getMessage(), is("Message consumer not started"));
        }
    }
}