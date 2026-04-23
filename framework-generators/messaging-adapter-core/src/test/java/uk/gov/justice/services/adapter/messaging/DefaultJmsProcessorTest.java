package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultJmsProcessorTest {

    @Mock
    private TextMessage textMessage;

    @Mock
    private JsonEnvelope expectedEnvelope;

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private ObjectMessage objectMessage;

    @Mock
    private TraceLogger traceLogger;

    @InjectMocks
    private DefaultJmsProcessor jmsProcessor;

    @Test
    public void shouldPassValidMessageToConsumerFunction() throws Exception {
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(expectedEnvelope);

        jmsProcessor.process(interceptorContext -> assertThat(interceptorContext.inputEnvelope(), is(expectedEnvelope)), textMessage);
    }

    @Test
    public void shouldThrowExceptionWithWrongMessageType() throws Exception {
        assertThrows(InvalidJmsMessageTypeException.class, () -> jmsProcessor.process(envelope -> {}, objectMessage));
    }

    @Test
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws Exception {
        doThrow(JMSException.class).when(objectMessage).getJMSMessageID();

        assertThrows(InvalidJmsMessageTypeException.class, () -> jmsProcessor.process(envelope -> {}, objectMessage));
    }

}
