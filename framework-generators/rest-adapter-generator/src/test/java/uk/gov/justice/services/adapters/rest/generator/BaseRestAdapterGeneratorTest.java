package uk.gov.justice.services.adapters.rest.generator;


import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.adapter.rest.mapping.ActionMapper;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetailsFactory;
import uk.gov.justice.services.adapter.rest.parameter.ParameterCollectionBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.ValidParameterCollectionBuilder;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.adapter.rest.processor.response.AcceptedStatusNoEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopeEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopePayloadEntityResponseStrategy;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public abstract class BaseRestAdapterGeneratorTest {

    public static final JavaCompilerUtility COMPILER = javaCompilerUtil();

    private static final String INTERCEPTOR_CHAIN_PROCESSOR = "interceptorChainProcessor";
    private static final String REST_PROCESSOR = "restProcessor";
    private static final String ACTION_MAPPER = "actionMapper";
    private static final String FILE_INPUT_DETAILS_FACTORY = "fileInputDetailsFactory";
    private static final String VALID_PARAMETER_COLLECTION_BUILDER_FACTORY_FIELD = "validParameterCollectionBuilderFactory";
    private static final String TRACE_LOGGER_FIELD = "traceLogger";
    private static final String HTTP_TRACE_LOGGER_HELPER_FIELD = "httpTraceLoggerHelper";
    private static final String LOGGER_FIELD = "logger";
    private static final String HTTP_HEADERS_FIELD = "headers";

    @Mock
    protected InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    protected ActionMapper actionMapper;

    @Mock
    protected RestProcessor restProcessor;

    @Mock
    protected OkStatusEnvelopeEntityResponseStrategy okStatusEnvelopeEntityResponseStrategy;

    @Mock
    protected OkStatusEnvelopePayloadEntityResponseStrategy okStatusEnvelopePayloadEntityResponseStrategy;

    @Mock
    protected AcceptedStatusNoEntityResponseStrategy acceptedStatusNoEntityResponseStrategy;

    @Mock
    protected FileInputDetailsFactory fileInputDetailsFactory;

    @Mock
    protected ParameterCollectionBuilderFactory validParameterCollectionBuilderFactory;

    @Mock
    protected TraceLogger traceLogger;

    @Mock
    protected Logger logger;

    @Mock
    protected HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Mock
    protected HttpHeaders httpHeaders;

    @TempDir
    public File outputFolder;

    protected final RestAdapterGenerator generator = new RestAdapterGenerator();

    protected static final String BASE_PACKAGE = "org.raml.test";


    @BeforeEach
    public void before() {
        setField(generator, LOGGER_FIELD, logger);
    }

    protected Object getInstanceOf(final Class<?> resourceClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Object resourceObject = resourceClass.getDeclaredConstructor().newInstance();
        setField(resourceObject, REST_PROCESSOR, restProcessor);
        setField(resourceObject, INTERCEPTOR_CHAIN_PROCESSOR, interceptorChainProcessor);
        setField(resourceObject, ACTION_MAPPER, actionMapper);
        setField(resourceObject, FILE_INPUT_DETAILS_FACTORY, fileInputDetailsFactory);
        setField(resourceObject, VALID_PARAMETER_COLLECTION_BUILDER_FACTORY_FIELD, validParameterCollectionBuilderFactory);
        setField(resourceObject, TRACE_LOGGER_FIELD, traceLogger);
        setField(resourceObject, HTTP_TRACE_LOGGER_HELPER_FIELD, httpTraceLoggerHelper);
        setField(resourceObject, HTTP_HEADERS_FIELD, httpHeaders);


        when(validParameterCollectionBuilderFactory.create()).thenReturn(new ValidParameterCollectionBuilder(logger));

        return resourceObject;
    }
}
