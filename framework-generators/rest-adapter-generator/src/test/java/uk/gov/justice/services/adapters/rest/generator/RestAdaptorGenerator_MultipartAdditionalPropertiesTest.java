package uk.gov.justice.services.adapters.rest.generator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.api.resource.DefaultCommandApiPhotographsUseridResource;
import uk.gov.justice.services.adapter.rest.mapping.ActionMapper;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetailsFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.adapter.rest.parameter.ParameterCollectionBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.ValidParameterCollectionBuilder;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import jakarta.inject.Named;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class RestAdaptorGenerator_MultipartAdditionalPropertiesTest {

    private static final String DOCUMENT_TYPE = "documentType";
    private static final String BODY = "test";

    @Mock
    private RestProcessor restProcessor;

    @Mock
    @Named("DefaultCommandApiPhotographsUseridResourceActionMapper")
    private ActionMapper actionMapper;

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private HttpHeaders headers;

    @Mock
    private FileInputDetailsFactory fileInputDetailsFactory;

    @Mock
    private ParameterCollectionBuilderFactory validParameterCollectionBuilderFactory;

    @Mock
    private TraceLogger traceLogger;

    @Mock
    private MediaType mediaType;

    @Mock
    private HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Mock
    private MultipartFormDataInput multipartFormDataInput;

    @Mock
    private InputPart additionalProperty;

    @Captor
    private ArgumentCaptor<Collection<Parameter>> parametersCaptor;

    @InjectMocks
    private DefaultCommandApiPhotographsUseridResource defaultCommandApiPhotographsUseridResource;

    @Test
    public void shouldPropagateAdditionalPropertiesAsParameters() throws IOException {

        final String action = "theAction";
        final Map<String, List<InputPart>> inputParts = new HashMap<>();
        final List<InputPart> inputPartList = new ArrayList<>();
        inputPartList.add(additionalProperty);
        inputParts.put(DOCUMENT_TYPE, inputPartList);

        when(validParameterCollectionBuilderFactory.create()).thenReturn(new ValidParameterCollectionBuilder(mock(Logger.class)));
        when(multipartFormDataInput.getFormDataMap()).thenReturn(inputParts);
        when(additionalProperty.getMediaType()).thenReturn(mediaType);
        when(mediaType.getType()).thenReturn("text");
        when(additionalProperty.getBodyAsString()).thenReturn(BODY);
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(headers))).thenReturn(action);

        defaultCommandApiPhotographsUseridResource.postPeopleUploadPhotographPhotographsByUserid(UUID.randomUUID().toString(), multipartFormDataInput);
        verify(restProcessor).process(anyString(), any(Function.class), anyString(), any(HttpHeaders.class),
                parametersCaptor.capture(), any(List.class));

        final Iterator<Parameter> parameterCollection = parametersCaptor.getValue().iterator();

        boolean failTest = true;

        while (parameterCollection.hasNext()) {

            final Parameter parameter = parameterCollection.next();
            if (parameter.getName().equals(DOCUMENT_TYPE) && parameter.getStringValue().equals(BODY)) {
                failTest = false;
            }
        }
        assertFalse(failTest);
    }
}
