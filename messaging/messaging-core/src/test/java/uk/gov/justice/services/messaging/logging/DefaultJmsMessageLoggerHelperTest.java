package uk.gov.justice.services.messaging.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonReaderFactory;

@ExtendWith(MockitoExtension.class)
public class DefaultJmsMessageLoggerHelperTest {

    private static final String A_NAME = "context.command.do-something";
    private static final String A_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String A_SESSION_ID = UUID.randomUUID().toString();
    private static final String A_USER_ID = UUID.randomUUID().toString();

    @Mock
    private TextMessage message;

    @Mock
    private List<UUID> causation;

    @InjectMocks
    private DefaultJmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Test
    public void shouldReturnValidMetadataJson() throws JMSException {
        when(message.getText()).thenReturn(envelopeString());

        JsonObject result = toJsonObject(message);

        assertThat(result.getString("id"), is(A_MESSAGE_ID));
        assertThat(result.getString("name"), is(A_NAME));
        assertThat(result.getJsonObject("context").getString("user"), is(A_USER_ID));
        assertThat(result.getJsonObject("context").getString("session"), is(A_SESSION_ID));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnErrorMessageOnError() throws JMSException {
        when(message.getText()).thenThrow(JMSException.class);

        assertThat(jmsMessageLoggerHelper.toJmsTraceString(message), containsString("Could not find: _metadata in message"));
    }

    private JsonObject toJsonObject(final Message message) {
        StringReader sw = new StringReader(jmsMessageLoggerHelper.toJmsTraceString(message));
        JsonReader jsonReader = getJsonReaderFactory().createReader(sw);
        return jsonReader.readObject();
    }

    private String envelopeString() {

        return getJsonBuilderFactory().createObjectBuilder()
                .add("_metadata", metadataBuilder()
                        .withId(UUID.fromString(A_MESSAGE_ID))
                        .withName(A_NAME)
                        .withUserId(A_USER_ID)
                        .withSessionId(A_SESSION_ID)
                        .build().asJsonObject())
                .add("something", "anything really")
                .build().toString();
    }

}