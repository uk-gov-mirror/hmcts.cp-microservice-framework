package uk.gov.justice.services.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.configuration.errors.event.EventErrorHandlingConfiguration;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntityManagerFlushInterceptorProviderTest {

    @Mock
    private EventErrorHandlingConfiguration eventErrorHandlingConfiguration;

    @InjectMocks
    private EntityManagerFlushInterceptorProvider entityManagerFlushInterceptorProvider;

    @Test
    public void shouldHaveEventListenerAsComponent() throws Exception {
        assertThat(entityManagerFlushInterceptorProvider.component(), is(EVENT_LISTENER));
    }

    @Test
    public void shouldCreateInterceptorChainEntryForEntityManagerFlushInterceptorWithTheCorrectPriority() throws Exception {

        when(eventErrorHandlingConfiguration.isEventStreamSelfHealingEnabled()).thenReturn(true);

        final List<InterceptorChainEntry> interceptorChainEntries = entityManagerFlushInterceptorProvider.interceptorChainTypes();

        assertThat(interceptorChainEntries.size(), is(1));
        assertThat(interceptorChainEntries.get(0).getPriority(), is(220));
        assertThat(interceptorChainEntries.get(0).getInterceptorType(), is(EntityManagerFlushInterceptor.class));
    }

    @Test
    public void shouldReturnEmptyListIfEventErrorHandlingIsDisabled() throws Exception {

        when(eventErrorHandlingConfiguration.isEventStreamSelfHealingEnabled()).thenReturn(false);

        assertThat(entityManagerFlushInterceptorProvider.interceptorChainTypes().isEmpty(), is(true));
    }
}
