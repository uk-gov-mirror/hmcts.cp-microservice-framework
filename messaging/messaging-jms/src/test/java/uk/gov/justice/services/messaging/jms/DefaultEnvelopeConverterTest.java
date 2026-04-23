package uk.gov.justice.services.messaging.jms;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.exception.JmsConverterException;

import java.util.UUID;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultEnvelopeConverterTest {

    private static final String MESSAGE_TEXT = "Test Message";
    private static final String NAME = "name";

    @Mock
    private OversizeMessageGuard oversizeMessageGuard;

    @Mock
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @InjectMocks
    private DefaultEnvelopeConverter envelopeConverter;

    @Mock
    private TextMessage textMessage;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    @Mock
    private JsonObject messageAsJsonObject;

    @Mock
    private Session session;

    @Test
    public void shouldReturnEnvelope() throws Exception {

        final String envelopeName = "some.envelope.name";
        final UUID envelopeId = randomUUID();
        final UUID streamId = randomUUID();

        when(textMessage.getText()).thenReturn(MESSAGE_TEXT);
        when(stringToJsonObjectConverter.convert(MESSAGE_TEXT)).thenReturn(messageAsJsonObject);
        when(jsonObjectEnvelopeConverter.asEnvelope(messageAsJsonObject)).thenReturn(envelope);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(envelopeName);
        when(metadata.id()).thenReturn(envelopeId);
        when(metadata.streamId()).thenReturn(of(streamId));

        final JsonEnvelope actualEnvelope = envelopeConverter.fromMessage(textMessage);

        assertThat(actualEnvelope, equalTo(envelope));

        verify(oversizeMessageGuard).checkSizeOf(MESSAGE_TEXT, envelopeName, envelopeId, of(streamId));
    }

    @Test
    public void shouldReturnMessage() throws Exception {
        when(jsonObjectEnvelopeConverter.asJsonString(envelope)).thenReturn(MESSAGE_TEXT);
        when(session.createTextMessage(MESSAGE_TEXT)).thenReturn(textMessage);
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);

        TextMessage actualTextMessage = envelopeConverter.toMessage(envelope, session);

        assertThat(actualTextMessage, equalTo(textMessage));
        verify(textMessage).setStringProperty(JMS_HEADER_CPPNAME, NAME);
    }

    @Test
    public void shouldThrowExceptionWhenFailToRetrieveMessageContent() throws JMSException {
        doThrow(JMSException.class).when(textMessage).getText();

        assertThrows(JmsConverterException.class, () -> envelopeConverter.fromMessage(textMessage));
    }

    @Test
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws JMSException {
        doThrow(JMSException.class).when(textMessage).getText();
        doThrow(JMSException.class).when(textMessage).getJMSMessageID();

        assertThrows(JmsConverterException.class, () -> envelopeConverter.fromMessage(textMessage));
    }

    @Test
    public void shouldThrowExceptionWhenFailToCreateTextMessage() throws JMSException {
        when(jsonObjectEnvelopeConverter.asJsonString(envelope)).thenReturn(MESSAGE_TEXT);
        doThrow(JMSException.class).when(session).createTextMessage(MESSAGE_TEXT);

        assertThrows(JmsConverterException.class, () -> envelopeConverter.toMessage(envelope, session));
    }
}
