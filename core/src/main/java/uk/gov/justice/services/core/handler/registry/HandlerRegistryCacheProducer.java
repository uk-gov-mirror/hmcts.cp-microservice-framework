package uk.gov.justice.services.core.handler.registry;

import uk.gov.justice.services.common.util.LazyValue;
import uk.gov.justice.services.core.featurecontrol.FeatureControlAnnotationFinder;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HandlerRegistryCacheProducer {

    private LazyValue lazyValue = new LazyValue();

    @Inject
    private FeatureControlAnnotationFinder featureControlAnnotationFinder;

    @Produces
    public HandlerRegistryCache handlerRegistryCache() {

        return lazyValue.createIfAbsent(() -> new HandlerRegistryCache(featureControlAnnotationFinder));
    }
}
