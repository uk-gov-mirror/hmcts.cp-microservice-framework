package uk.gov.justice.services.core.envelope;

import jakarta.enterprise.inject.Alternative;

@Alternative
public class RethrowingValidationExceptionHandler implements EnvelopeValidationExceptionHandler {
    @Override
    public void handle(final EnvelopeValidationException ex) {
        throw ex;
    }
}
