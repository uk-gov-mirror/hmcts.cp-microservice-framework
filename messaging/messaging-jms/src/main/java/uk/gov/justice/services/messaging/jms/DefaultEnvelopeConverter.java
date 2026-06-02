package uk.gov.justice.services.messaging.jms;

import static uk.gov.justice.services.messaging.jms.HeaderConstants.JMS_HEADER_CPPNAME;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.jms.exception.JmsConverterException;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

/**
 * Implementation of {@link EnvelopeConverter} for {@link JsonEnvelope}
 */
@ApplicationScoped
public class DefaultEnvelopeConverter implements EnvelopeConverter {

    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    private OversizeMessageGuard oversizeMessageGuard;

    @Override
    public JsonEnvelope fromMessage(final TextMessage message) {

        try {
            final String messageAsString = message.getText();
            final JsonEnvelope jsonEnvelope = jsonObjectEnvelopeConverter.asEnvelope(stringToJsonObjectConverter.convert(messageAsString));

            final Metadata metadata = jsonEnvelope.metadata();
            final String envelopeName = metadata.name();
            final UUID envelopeId = metadata.id();
            final Optional<UUID> streamId = metadata.streamId();

            oversizeMessageGuard.checkSizeOf(messageAsString, envelopeName, envelopeId, streamId);

            return jsonEnvelope;
        } catch (JMSException e) {
            throw createJmsConverterException(message, e);
        }
    }

    @Override
    public TextMessage toMessage(final JsonEnvelope envelope, final Session session) {
        final String envelopeAsString = jsonObjectEnvelopeConverter.asJsonString(envelope);

        try {
            final TextMessage textMessage = session.createTextMessage(envelopeAsString);
            textMessage.setStringProperty(JMS_HEADER_CPPNAME, envelope.metadata().name());
            return textMessage;
        } catch (JMSException e) {
            throw new JmsConverterException(String.format("Exception while creating message from envelope %s", envelopeAsString), e);
        }
    }

    private JmsConverterException createJmsConverterException(final TextMessage message, final Throwable e) {
        try {
            return new JmsConverterException(String.format("Exception while creating envelope from message %s", message.getJMSMessageID()), e);
        } catch (JMSException e1) {
            return new JmsConverterException(String.format("Exception while creating envelope from message. Failed to retrieve messageId from %s", message), e1);
        }
    }

}
