package uk.gov.justice.services.core.interceptor;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.exception.InterceptorCacheException;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import jakarta.enterprise.inject.spi.Bean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InterceptorCacheTest {

    private static final InterceptorOne INTERCEPTOR_1 = new InterceptorOne();
    private static final InterceptorTwo INTERCEPTOR_2 = new InterceptorTwo();
    private static final InterceptorThree INTERCEPTOR_3 = new InterceptorThree();

    @Mock
    private InterceptorChainObserver observer;

    @Mock
    private BeanInstantiater beanInstantiater;

    @InjectMocks
    private InterceptorCache interceptorCache;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnEmptyDequeIfEmptyInterceptorChainProvided() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainEntryProvider interceptorChainEntryProvider = new EmptyInterceptorChainProvider();
        final Bean<InterceptorChainEntryProvider> interceptorChainProviderBean = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(singletonList(interceptorChainProviderBean));
        when(beanInstantiater.instantiate(interceptorChainProviderBean)).thenReturn(interceptorChainEntryProvider);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors("Empty Component");

        assertThat(interceptors, empty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnInstancesOfInterceptorChainTypesInOrderAddedInProvider() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainEntryProvider interceptorChainEntryProvider = new ComponentOneInterceptorChainProvider();
        final Bean<InterceptorChainEntryProvider> interceptorChainProviderBean = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(singletonList(interceptorChainProviderBean));
        when(beanInstantiater.instantiate(interceptorChainProviderBean)).thenReturn(interceptorChainEntryProvider);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors("Component_1");

        assertThat(interceptors, contains(INTERCEPTOR_1, INTERCEPTOR_2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnCorrectInterceptorsForEachComponent() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainEntryProvider interceptorChainEntryProvider_1 = new ComponentOneInterceptorChainProvider();
        final InterceptorChainEntryProvider interceptorChainEntryProvider_2 = new ComponentTwoInterceptorChainProvider();

        final Bean<InterceptorChainEntryProvider> bean_1 = mock(Bean.class);
        final Bean<InterceptorChainEntryProvider> bean_2 = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(asList(bean_1, bean_2));

        when(beanInstantiater.instantiate(bean_1)).thenReturn(interceptorChainEntryProvider_1);
        when(beanInstantiater.instantiate(bean_2)).thenReturn(interceptorChainEntryProvider_2);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors_1 = interceptorCache.getInterceptors("Component_1");
        final Deque<Interceptor> interceptors_2 = interceptorCache.getInterceptors("Component_2");

        assertThat(interceptors_1, contains(INTERCEPTOR_1, INTERCEPTOR_2));
        assertThat(interceptors_2, contains(INTERCEPTOR_2, INTERCEPTOR_3));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCombineInterceptorsFromMultipleProvidersForComponentAndOrderInPriority() throws Exception {
        givenThreeInterceptorBeans();

        final InterceptorChainEntryProvider interceptorChainEntryProvider = new ComponentOneInterceptorChainProvider();
        final InterceptorChainEntryProvider interceptorChainEntryProviderExtra = new ComponentOneExtraInterceptorChainProvider();

        final Bean<InterceptorChainEntryProvider> interceptorChainProviderBean_1 = mock(Bean.class);
        final Bean<InterceptorChainEntryProvider> interceptorChainProviderBean_2 = mock(Bean.class);

        when(observer.getInterceptorChainProviderBeans()).thenReturn(asList(interceptorChainProviderBean_1, interceptorChainProviderBean_2));

        when(beanInstantiater.instantiate(interceptorChainProviderBean_1)).thenReturn(interceptorChainEntryProvider);
        when(beanInstantiater.instantiate(interceptorChainProviderBean_2)).thenReturn(interceptorChainEntryProviderExtra);

        interceptorCache.initialise();

        final Deque<Interceptor> interceptors = interceptorCache.getInterceptors("Component_1");

        assertThat(interceptors, contains(INTERCEPTOR_1, INTERCEPTOR_3, INTERCEPTOR_2));
    }

    @Test
    public void shouldThrowExceptionIfComponentHasNoInterceptorChainProviderRegistered() throws Exception {

        final InterceptorCacheException interceptorCacheException = assertThrows(InterceptorCacheException.class, () ->
                interceptorCache.getInterceptors("Unknown Component")
        );

        assertThat(interceptorCacheException.getMessage(), is("Component [Unknown Component] does not have any cached Interceptors, check if there is an InterceptorChainProvider for this component."));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldThrowExceptionIfInterceptorBeanNotInstantiated() throws Exception {

        final Class interceptorClass_1 = InterceptorOne.class;

        final Bean<Interceptor> interceptorBean_1 = mock(Bean.class);
        when(observer.getInterceptorBeans()).thenReturn(singletonList(interceptorBean_1));

        when(interceptorBean_1.getBeanClass()).thenReturn(interceptorClass_1);
        when(beanInstantiater.instantiate(interceptorBean_1)).thenReturn(null);
        final Bean<InterceptorChainEntryProvider> interceptorChainProviderBean = mock(Bean.class);
        when(observer.getInterceptorChainProviderBeans()).thenReturn(singletonList(interceptorChainProviderBean));

        when(beanInstantiater.instantiate(interceptorChainProviderBean)).thenReturn(new InterceptorChainEntryProvider() {
            @Override
            public String component() {
                return "some component";
            }

            @Override
            public List<InterceptorChainEntry> interceptorChainTypes() {
                final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
                interceptorChainTypes.add(new InterceptorChainEntry(1, interceptorClass_1));
                return interceptorChainTypes;
            }
        });

        final InterceptorCacheException interceptorCacheException = assertThrows(InterceptorCacheException.class, () ->
                interceptorCache.initialise()
        );

        assertThat(interceptorCacheException.getMessage(), is("Could not instantiate interceptor bean of type: uk.gov.justice.services.core.interceptor.InterceptorCacheTest$InterceptorOne"));
    }

    @SuppressWarnings("unchecked")
    private void givenThreeInterceptorBeans() {
        final Bean<Interceptor> interceptorBean_1 = mock(Bean.class);
        final Bean<Interceptor> interceptorBean_2 = mock(Bean.class);
        final Bean<Interceptor> interceptorBean_3 = mock(Bean.class);

        final Class interceptorClass_1 = InterceptorOne.class;
        final Class interceptorClass_2 = InterceptorTwo.class;
        final Class interceptorClass_3 = InterceptorThree.class;

        when(observer.getInterceptorBeans()).thenReturn(asList(interceptorBean_1, interceptorBean_2, interceptorBean_3));

        when(interceptorBean_1.getBeanClass()).thenReturn(interceptorClass_1);
        when(interceptorBean_2.getBeanClass()).thenReturn(interceptorClass_2);
        when(interceptorBean_3.getBeanClass()).thenReturn(interceptorClass_3);

        when(beanInstantiater.instantiate(interceptorBean_1)).thenReturn(INTERCEPTOR_1);
        when(beanInstantiater.instantiate(interceptorBean_2)).thenReturn(INTERCEPTOR_2);
        when(beanInstantiater.instantiate(interceptorBean_3)).thenReturn(INTERCEPTOR_3);
    }

    public class EmptyInterceptorChainProvider implements InterceptorChainEntryProvider {

        @Override
        public String component() {
            return "Empty Component";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            return emptyList();
        }
    }

    public class ComponentOneInterceptorChainProvider implements InterceptorChainEntryProvider {

        @Override
        public String component() {
            return "Component_1";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(1, InterceptorOne.class));
            interceptorChainTypes.add(new InterceptorChainEntry(3, InterceptorTwo.class));
            return interceptorChainTypes;
        }
    }

    public class ComponentTwoInterceptorChainProvider implements InterceptorChainEntryProvider {

        @Override
        public String component() {
            return "Component_2";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(1, InterceptorTwo.class));
            interceptorChainTypes.add(new InterceptorChainEntry(2, InterceptorThree.class));
            return interceptorChainTypes;
        }
    }

    public class ComponentOneExtraInterceptorChainProvider implements InterceptorChainEntryProvider {

        @Override
        public String component() {
            return "Component_1";
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(2, InterceptorThree.class));
            return interceptorChainTypes;
        }
    }

    public static class InterceptorOne implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }

    public static class InterceptorTwo implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }

    public static class InterceptorThree implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }
}
