package uk.gov.justice.services.adapter.rest.processor.response;

import static jakarta.ws.rs.core.Response.Status.ACCEPTED;
import static jakarta.ws.rs.core.Response.status;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.ACCEPTED_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Named(ACCEPTED_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY)
public class AcceptedStatusEnvelopeEntityResponseStrategy implements ResponseStrategy {

    @Inject
    ResponseStrategyHelper responseStrategyHelper;

    /**
     * Returns a Response of ACCEPTED with the payload of the result if present.
     *
     * @param action the action being processed
     * @param result the resulting JsonEnvelope
     * @return the {@link Response} to return from the REST call
     */
    @Override
    public Response responseFor(final String action, final Optional<JsonEnvelope> result) {
        return responseStrategyHelper.responseFor(action, result,
                jsonEnvelope -> status(ACCEPTED)
                        .header(ID, jsonEnvelope.metadata().id())
                        .entity(jsonEnvelope.payloadAsJsonObject())
                        .build());
    }
}