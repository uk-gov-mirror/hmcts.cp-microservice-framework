package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;

public final class DefaultJmsMessageLoggerHelper implements JmsMessageLoggerHelper {

    @Override
    public String toJmsTraceString(final Message message) {
        try {
            return metadataAsJsonObject((TextMessage) message).toString();
        } catch (Exception e) {
            return "Could not find: _metadata in message";

        }
    }

    @Override
    public JsonObject metadataAsJsonObject(final TextMessage message) throws JMSException {
        return new DefaultJsonObjectEnvelopeConverter()
                .asEnvelope(new StringToJsonObjectConverter().convert(message.getText()))
                .metadata()
                .asJsonObject();
    }
}
