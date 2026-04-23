package uk.gov.justice.services.core.producers;

import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.json.JsonSchemaValidator;

import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnvelopeValidatorFactory {

    @Inject
    private JsonSchemaValidator jsonSchemaValidator;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    public EnvelopeValidator createNew() {
        return new EnvelopeValidator(
                jsonSchemaValidator,
                objectMapper,
                envelopeValidationExceptionHandler
        );
    }
}
