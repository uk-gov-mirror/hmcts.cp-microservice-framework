package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

/**
 * Creates and caches {@link Dispatcher} for {@link InjectionPoint} or {@link
 * ServiceComponentFoundEvent}.
 */
@ApplicationScoped
public class DispatcherCache {

    private final Map<DispatcherKey, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();

    private DispatcherFactory dispatcherFactory;
    private ComponentNameExtractor componentNameExtractor;

    @Inject
    public DispatcherCache(final DispatcherFactory dispatcherFactory,
                           final ComponentNameExtractor componentNameExtractor) {
        this.dispatcherFactory = dispatcherFactory;
        this.componentNameExtractor = componentNameExtractor;
    }

    public DispatcherCache() {

    }

    /**
     * Return a {@link Dispatcher} for the given {@link InjectionPoint}.
     *
     * @param injectionPoint the given {@link InjectionPoint}
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final InjectionPoint injectionPoint) {
        final String componentName = componentNameExtractor.componentFrom(injectionPoint);
        final ServiceComponentLocation location = componentLocationFrom(injectionPoint);

        final DispatcherKey component = new DispatcherKey(
                componentName,
                location);
        return dispatcherMap.computeIfAbsent(component, c -> dispatcherFactory.createNew(
                componentName,
                location
        ));
    }

    /**
     * Return the {@link Dispatcher} for the given {@link ServiceComponentFoundEvent}.
     *
     * @param event the given {@link ServiceComponentFoundEvent}
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final ServiceComponentFoundEvent event) {
        final String componentName = event.getComponentName();
        final ServiceComponentLocation location = event.getLocation();
        final DispatcherKey component = new DispatcherKey(
                componentName,
                location);
        return dispatcherMap.computeIfAbsent(component, c -> dispatcherFactory.createNew(
                componentName,
                location));
    }

    /**
     * Return the {@link Dispatcher} for the given {@link Component} and {@link
     * ServiceComponentLocation}.
     *
     * @param component the component type for which the dispatcher is for
     * @param location  whether the dispatcher is local or remote
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final String component, final ServiceComponentLocation location) {
        final DispatcherKey dispatcherKey = new DispatcherKey(component, location);
        return dispatcherMap.computeIfAbsent(
                dispatcherKey,
                theDispatcherKey -> dispatcherFactory.createNew(component, location));
    }

    private static class DispatcherKey {

        private final String componentName;
        private final ServiceComponentLocation location;

        private DispatcherKey(final String componentName, final ServiceComponentLocation location) {
            this.componentName = componentName;
            this.location = location;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DispatcherKey that = (DispatcherKey) o;
            return Objects.equals(componentName, that.componentName) &&
                    location == that.location;
        }

        @Override
        public int hashCode() {
            return Objects.hash(componentName, location);
        }
    }
}
