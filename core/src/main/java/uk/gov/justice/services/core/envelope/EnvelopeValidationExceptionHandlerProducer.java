package uk.gov.justice.services.core.envelope;

import uk.gov.justice.services.common.configuration.GlobalValue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class EnvelopeValidationExceptionHandlerProducer {

    @Inject
    @GlobalValue(key = "envelope.validation.exception.handler", defaultValue = "uk.gov.justice.services.core.envelope.EmptyValidationExceptionHandler")
    String handlerClass;

    @Produces
    public EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler() {
        try {
            final Class<?> clazz = Class.forName(handlerClass);
            return (EnvelopeValidationExceptionHandler) clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate validation exception handler.", e);
        }
    }
}
