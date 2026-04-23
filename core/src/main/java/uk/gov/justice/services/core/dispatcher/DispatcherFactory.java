package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.handler.registry.HandlerRegistryCache;

import jakarta.inject.Inject;

public class DispatcherFactory {

    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;
    private HandlerRegistryCache handlerRegistryCache;

    @Inject
    public DispatcherFactory(
            final EnvelopePayloadTypeConverter envelopePayloadTypeConverter,
            final JsonEnvelopeRepacker jsonEnvelopeRepacker,
            final HandlerRegistryCache handlerRegistryCache) {
        this.envelopePayloadTypeConverter = envelopePayloadTypeConverter;
        this.jsonEnvelopeRepacker = jsonEnvelopeRepacker;
        this.handlerRegistryCache = handlerRegistryCache;
    }

    public Dispatcher createNew(final String serviceComponent, final ServiceComponentLocation location) {
        return new Dispatcher(
                handlerRegistryCache.handlerRegistryFor(serviceComponent, location),
                envelopePayloadTypeConverter,
                jsonEnvelopeRepacker);
    }
}
