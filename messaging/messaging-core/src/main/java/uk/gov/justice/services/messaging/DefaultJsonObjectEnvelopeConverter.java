package uk.gov.justice.services.messaging;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import java.io.StringReader;

import static jakarta.json.JsonValue.ValueType.OBJECT;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonReaderFactory;

/**
 * A converter class to convert between {@link JsonEnvelope} and {@link JsonObject}.
 */
@ApplicationScoped
public class DefaultJsonObjectEnvelopeConverter implements JsonObjectEnvelopeConverter {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public JsonEnvelope asEnvelope(final JsonObject envelopeJsonObject) {
        return envelopeFrom(
                metadataFrom(envelopeJsonObject.getJsonObject(METADATA)),
                extractPayloadFromEnvelope(envelopeJsonObject));
    }

    @Override
    public JsonEnvelope asEnvelope(final String jsonString) {
        try (JsonReader reader = getJsonReaderFactory().createReader(new StringReader(jsonString))) {
            return asEnvelope(reader.readObject());
        }
    }


    @Override
    public JsonObject fromEnvelope(final JsonEnvelope envelope) {
        final Metadata metadata = envelope.metadata();

        if (metadata == null) {
            throw new IllegalArgumentException("Failed to convert envelope, no metadata present.");
        }

        final JsonObjectBuilder builder = getJsonBuilderFactory().createObjectBuilder();
        builder.add(METADATA, metadata.asJsonObject());

        final ValueType payloadType = envelope.payload().getValueType();
        if (payloadType == OBJECT) {
            final JsonObject payloadAsJsonObject = envelope.payloadAsJsonObject();
            payloadAsJsonObject.keySet().forEach(key -> builder.add(key, payloadAsJsonObject.get(key)));
        } else {
            throw new IllegalArgumentException(String.format("Payload type %s not supported.", payloadType));
        }

        return builder.build();
    }

    @Override
    public JsonValue extractPayloadFromEnvelope(final JsonObject envelope) {
        final JsonObjectBuilder builder = getJsonBuilderFactory().createObjectBuilder();
        envelope.keySet().stream().filter(key -> !METADATA.equals(key)).forEach(key -> builder.add(key, envelope.get(key)));
        return builder.build();
    }

    @Override
    public String asJsonString(final JsonEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(fromEnvelope(envelope));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize JSON envelope", e);
        }
    }

}
