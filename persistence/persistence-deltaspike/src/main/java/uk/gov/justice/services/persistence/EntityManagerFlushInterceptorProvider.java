package uk.gov.justice.services.persistence;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.configuration.errors.event.EventErrorHandlingConfiguration;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;

import java.util.List;

import jakarta.inject.Inject;

public class EntityManagerFlushInterceptorProvider implements InterceptorChainEntryProvider {

    private static final int PRIORITY = 220;

    @Inject
    private EventErrorHandlingConfiguration eventErrorHandlingConfiguration;


    @Override
    public String component() {
        return EVENT_LISTENER;
    }

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {

        if (eventErrorHandlingConfiguration.isEventStreamSelfHealingEnabled()) {
            return of(new InterceptorChainEntry(PRIORITY, EntityManagerFlushInterceptor.class));
        }

        return emptyList();
    }
}
