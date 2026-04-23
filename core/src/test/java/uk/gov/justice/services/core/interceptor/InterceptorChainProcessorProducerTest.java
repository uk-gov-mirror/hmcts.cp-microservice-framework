package uk.gov.justice.services.core.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.featurecontrol.FeatureControlAnnotationFinder;
import uk.gov.justice.services.core.handler.registry.HandlerRegistryCache;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.MemberInjectionPoint;
import uk.gov.justice.services.test.utils.common.envelope.EnvelopeRecordingInterceptor;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import jakarta.inject.Inject;
import java.util.LinkedList;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

@ExtendWith(MockitoExtension.class)
public class InterceptorChainProcessorProducerTest {

    private static final String ACTION_NAME = "abc";

    @Mock
    private InterceptorCache interceptorCache;

    @Spy
    private ComponentNameExtractor componentNameExtractor = new ComponentNameExtractor();

    @Mock
    private Logger logger;

    @InjectMocks
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    private EnvelopeRecordingInterceptor envelopeRecordingInterceptor = new EnvelopeRecordingInterceptor();
    private EnvelopeRecordingHandler envelopeRecordingHandler = new EnvelopeRecordingHandler();

    private DispatcherCache dispatcherCache;

    @BeforeEach
    public void setUp() throws Exception {

        dispatcherCache = new DispatcherCache(
                new DispatcherFactory(
                        new EnvelopePayloadTypeConverter(new ObjectMapperProducer().objectMapper()),
                        new JsonEnvelopeRepacker(),
                        new HandlerRegistryCache(new FeatureControlAnnotationFinder())),
                componentNameExtractor);

        setField(interceptorChainProcessorProducer, "dispatcherCache", dispatcherCache);

        envelopeRecordingInterceptor.reset();
    }

    @Test
    public void shouldProduceProcessorThatDispatchesEnvelope_FromAdapter() throws Exception {
        when(interceptorCache.getInterceptors("EVENT_LISTENER")).thenReturn(envelopeRecordingInterceptor());

        final MemberInjectionPoint injectionPoint = injectionPointWith(EventListenerAdapter.class.getDeclaredField("processor"));
        dispatcherCache.dispatcherFor(injectionPoint).register(envelopeRecordingHandler);
        final InterceptorChainProcessor processor = interceptorChainProcessorProducer.produceProcessor(injectionPoint);

        final JsonEnvelope dispatchedEnvelope = envelopeFrom(metadataBuilder().withId(randomUUID()).withName(ACTION_NAME), getJsonBuilderFactory().createObjectBuilder());

        processor.process(interceptorContextWithInput(dispatchedEnvelope));

        assertThat(envelopeRecordingInterceptor.firstRecordedEnvelope(), is(dispatchedEnvelope));
        assertThat(envelopeRecordingHandler.firstRecordedEnvelope(), is(dispatchedEnvelope));
    }

    @Test
    public void shouldProduceProcessorThatDispatchesEnvelope_FromDirectAdapter() throws Exception {
        when(interceptorCache.getInterceptors("QUERY_API")).thenReturn(envelopeRecordingInterceptor());

        final MemberInjectionPoint injectionPoint = injectionPointWith(QueryApiDirectAdapter.class.getDeclaredField("processor"));
        dispatcherCache.dispatcherFor(injectionPoint).register(envelopeRecordingHandler);
        final InterceptorChainProcessor processor = interceptorChainProcessorProducer.produceProcessor(injectionPoint);

        final JsonEnvelope dispatchedEnvelope = envelopeFrom(metadataBuilder().withId(randomUUID()).withName(ACTION_NAME), getJsonBuilderFactory().createObjectBuilder());

        processor.process(interceptorContextWithInput(dispatchedEnvelope));

        assertThat(envelopeRecordingInterceptor.firstRecordedEnvelope(), is(dispatchedEnvelope));
        assertThat(envelopeRecordingHandler.firstRecordedEnvelope(), is(dispatchedEnvelope));
    }

    @Test
    public void shouldProduceProcessorThatDispatchesEnvelope_UsingComponentNameAndLocation() throws Exception {
        final String component = "EVENT_LISTENER";

        when(interceptorCache.getInterceptors(component)).thenReturn(envelopeRecordingInterceptor());

        final MemberInjectionPoint injectionPoint = injectionPointWith(EventListenerAdapter.class.getDeclaredField("processor"));
        dispatcherCache.dispatcherFor(injectionPoint).register(envelopeRecordingHandler);


        final InterceptorChainProcessor processor = interceptorChainProcessorProducer.produceLocalProcessor(component);

        final JsonEnvelope dispatchedEnvelope = envelopeFrom(metadataBuilder().withId(randomUUID()).withName(ACTION_NAME), getJsonBuilderFactory().createObjectBuilder());

        processor.process(interceptorContextWithInput(dispatchedEnvelope));

        assertThat(envelopeRecordingInterceptor.firstRecordedEnvelope(), is(dispatchedEnvelope));
        assertThat(envelopeRecordingHandler.firstRecordedEnvelope(), is(dispatchedEnvelope));
    }

    @Adapter(EVENT_LISTENER)
    public static class EventListenerAdapter {
        @Inject
        InterceptorChainProcessor processor;
    }

    @DirectAdapter(value = QUERY_API)
    public static class QueryApiDirectAdapter {
        @Inject
        InterceptorChainProcessor processor;
    }

    private LinkedList<Interceptor> envelopeRecordingInterceptor() {
        final LinkedList<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(envelopeRecordingInterceptor);
        return interceptors;
    }

    public static class EnvelopeRecordingHandler extends TestEnvelopeRecorder {
        @Handles(ACTION_NAME)
        public void handles(final JsonEnvelope envelope) {
            record(envelope);
        }
    }
}
