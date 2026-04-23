package uk.gov.justice.services.core.handler.registry;

import uk.gov.justice.services.core.featurecontrol.FeatureControlAnnotationFinder;

import jakarta.enterprise.inject.Produces;

/**
 * Test version of HandlerRegistryCacheProducer to stop open ejb freaking out about
 * @Singletons in its web scope
 */
public class TestHandlerRegistryCacheProducer {

    @Produces
    public HandlerRegistryCache handlerRegistryCache() {
        return new HandlerRegistryCache(new FeatureControlAnnotationFinder());
    }
}
