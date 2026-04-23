package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.subscription.SubscriptionManager;

import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class SubscriptionJmsProcessorDelegateTest {

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Mock
    private Logger logger;

    @InjectMocks
    private SubscriptionJmsProcessorDelegate subscriptionJmsProcessorDelegate;

    @Test
    public void shouldConvertTextMessageToJsonEnvelopeSendItToTheSubscriptionManager() throws Exception {

        final TextMessage textMessage = mock(TextMessage.class);
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(jsonEnvelope);

        subscriptionJmsProcessorDelegate.process(textMessage, subscriptionManager);

        verify(subscriptionManager).process(jsonEnvelope);
    }

    @Test
    public void shouldLogAtTraceIfTraceEnabledIsTrue() throws Exception {

        final TextMessage textMessage = mock(TextMessage.class);
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        when(logger.isTraceEnabled()).thenReturn(true);
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(jsonEnvelope);
        when(jmsMessageLoggerHelper.toJmsTraceString(textMessage)).thenReturn("jms-message-as-string");
        when(jsonEnvelope.toString()).thenReturn("json-envelope-as-string");

        subscriptionJmsProcessorDelegate.process(textMessage, subscriptionManager);

        final InOrder inOrder = inOrder(logger, subscriptionManager);

        inOrder.verify(logger).trace("Processing JMS message: 'jms-message-as-string'");
        inOrder.verify(subscriptionManager).process(jsonEnvelope);
        inOrder.verify(logger).trace("JMS message processed: 'json-envelope-as-string'");
    }

    @Test
    public void shouldThrowInvalidJmsMessageTypeExceptionIfMessageNotInstanceOfTextMessage() throws Exception {

        final ObjectMessage objectMessage = mock(ObjectMessage.class);
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        when(objectMessage.getJMSMessageID()).thenReturn("the-message-id");

        final InvalidJmsMessageTypeException invalidJmsMessageTypeException = assertThrows(
                InvalidJmsMessageTypeException.class,
                () -> subscriptionJmsProcessorDelegate.process(objectMessage, subscriptionManager));

        assertThat(invalidJmsMessageTypeException.getMessage(), is("Message is not an instance of TextMessage. MessageId: 'the-message-id'"));

        verifyNoInteractions(subscriptionManager);
    }

    @Test
    public void shouldThrowInvalidJmsMessageIfGettingMessageIdFails() throws Exception {

        final JMSException jmsException = new JMSException("Ooops");
        final ObjectMessage objectMessage = mock(ObjectMessage.class);
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        when(objectMessage.getJMSMessageID()).thenThrow(jmsException);

        final InvalidJmsMessageTypeException invalidJmsMessageTypeException = assertThrows(
                InvalidJmsMessageTypeException.class,
                () -> subscriptionJmsProcessorDelegate.process(objectMessage, subscriptionManager));

        assertThat(invalidJmsMessageTypeException.getCause(), is(jmsException));
        assertThat(invalidJmsMessageTypeException.getMessage(), is("Failed to get message id from JMS message"));

        verifyNoInteractions(subscriptionManager);
    }
}