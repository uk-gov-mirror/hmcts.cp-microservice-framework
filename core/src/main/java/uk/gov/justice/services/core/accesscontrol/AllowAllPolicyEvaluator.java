package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.empty;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(1)
public class AllowAllPolicyEvaluator implements PolicyEvaluator {

    @Override
    public Optional<AccessControlViolation> checkAccessPolicyFor(final String component,
                                                                 @SuppressWarnings("unused") final JsonEnvelope jsonEnvelope) {
        return empty();
    }
}
