package uk.gov.justice.services.core.envelope;

import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import org.slf4j.Logger;

@Alternative
public class LoggingValidationExceptionHandler implements EnvelopeValidationExceptionHandler {

    @Inject
    private Logger logger;

    @Override
    public void handle(final EnvelopeValidationException ex) {
        logger.warn("Message validation failed", ex);
    }
}
