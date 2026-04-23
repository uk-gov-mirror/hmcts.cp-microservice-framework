package uk.gov.justice.services.core.producers;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.requester.Requester;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

@SuppressWarnings("CdiInjectionPointsInspection")
@ApplicationScoped
public class RequesterProducer {

    @Inject
    private DispatcherCache dispatcherCache;

    @Inject
    private SystemUserUtil systemUserUtil;

    @Inject
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Inject
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Inject
    private RequestResponseEnvelopeValidatorFactory requestResponseEnvelopeValidatorFactory;

    /**
     * Produces the correct implementation of a requester depending on the {@link ServiceComponent}
     * annotation at the injection point.
     *
     * @param injectionPoint class where the {@link Requester} is being injected
     * @return the correct requester instance
     * @throws IllegalStateException if the injection point does not have a {@link ServiceComponent}
     *                               annotation
     */
    @Produces
    public Requester produceRequester(final InjectionPoint injectionPoint) {

        final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator
                = requestResponseEnvelopeValidatorFactory.createNew();

        return new DispatcherDelegate(
                dispatcherCache.dispatcherFor(injectionPoint),
                systemUserUtil,
                requestResponseEnvelopeValidator,
                envelopePayloadTypeConverter,
                jsonEnvelopeRepacker);
    }
}
