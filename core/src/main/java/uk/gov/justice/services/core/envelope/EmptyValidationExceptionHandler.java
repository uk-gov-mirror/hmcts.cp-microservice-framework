package uk.gov.justice.services.core.envelope;

import jakarta.enterprise.inject.Alternative;

@Alternative
public class EmptyValidationExceptionHandler implements EnvelopeValidationExceptionHandler {
    @Override
    public void handle(final EnvelopeValidationException ex) {

    }
}
