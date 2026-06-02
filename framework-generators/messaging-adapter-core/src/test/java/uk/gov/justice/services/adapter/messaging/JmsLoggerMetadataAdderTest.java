package uk.gov.justice.services.adapter.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import jakarta.interceptor.InvocationContext;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObject;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;

@ExtendWith(MockitoExtension.class)
public class JmsLoggerMetadataAdderTest {

    @Mock
    private Logger logger;

    @Mock
    private InvocationContext context;

    @Mock
    private JmsParameterChecker parameterChecker;

    @Mock
    private ServiceContextNameProvider serviceContextNameProvider;

    @Mock
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Mock
    private TraceLogger traceLogger;

    @Mock
    private MdcWrapper mdcWrapper;

    @InjectMocks
    private JmsLoggerMetadataAdder jmsLoggerMetadataAdder;

    @Test
    public void shouldAddMetadataFromEnvelopeToMappedDiagnosticContext() throws Exception {
        final UUID messageId = randomUUID();
        final String clientCorrelationId = randomUUID().toString();
        final String name = "someName";

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataBuilder()
                        .withId(messageId)
                        .withName(name)
                        .withClientCorrelationId(clientCorrelationId),
                getJsonBuilderFactory().createObjectBuilder()
                        .add("data", "someData"));

        final TextMessage textMessage = mock(TextMessage.class);
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
                .add("id", messageId.toString()).build();

        when(context.getParameters()).thenReturn(new Object[]{textMessage});
        when(jmsMessageLoggerHelper.metadataAsJsonObject(textMessage)).thenReturn(jsonObject);
        when(serviceContextNameProvider.getServiceContextName()).thenReturn("exampleService");

        jmsLoggerMetadataAdder.addRequestDataToMdc(context, "EVENT_LISTENER");

        final ArgumentCaptor<String> envelopeJsonArgumentCaptor = forClass(String.class);

        final InOrder inOrder = inOrder(mdcWrapper);
        inOrder.verify(mdcWrapper).put(eq(REQUEST_DATA), envelopeJsonArgumentCaptor.capture());
        inOrder.verify(mdcWrapper).clear();

        final String envelopeJson = envelopeJsonArgumentCaptor.getValue();

        assertThat(envelopeJson, isJson(allOf(
                    withJsonPath("$.metadata.id", equalTo(messageId.toString())),
                    withJsonPath("$.serviceContext", equalTo("exampleService")),
                    withJsonPath("$.serviceComponent", equalTo("EVENT_LISTENER")))
            ));
    }

    @Test
    @SuppressWarnings({"deprecation"})
    public void shouldProceedWithContextAndReturnResult() throws Exception {
        final Object expectedResult = mock(Object.class);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataBuilder()
                        .withId(UUID.randomUUID())
                        .withName("someName"),
                getJsonBuilderFactory().createObjectBuilder()
                        .add("data", "someData"));

        final TextMessage textMessage = mock(TextMessage.class);

        when(context.getParameters()).thenReturn(new Object[]{textMessage});
        when(context.proceed()).thenReturn(expectedResult);

        final Object actualResult = jmsLoggerMetadataAdder.addRequestDataToMdc(context, "EVENT_LISTENER");

        assertThat(actualResult, is(expectedResult));
    }

    @Test
    public void shouldReturnMessageInMetadataIfExceptionThrownWhenAccessingTextMessage() throws Exception {
        final TextMessage textMessage = mock(TextMessage.class);

        when(context.getParameters()).thenReturn(new Object[]{textMessage});
        when(jmsMessageLoggerHelper.metadataAsJsonObject(textMessage)).thenThrow(new RuntimeException());

        jmsLoggerMetadataAdder.addRequestDataToMdc(context, "EVENT_LISTENER");

        final ArgumentCaptor<String> envelopeJsonArgumentCaptor = forClass(String.class);

        verify(mdcWrapper).put(eq(REQUEST_DATA), envelopeJsonArgumentCaptor.capture());
        verify(mdcWrapper).clear();

        final String envelopeJson = envelopeJsonArgumentCaptor.getValue();

        assertThat(envelopeJson, isJson(withJsonPath("$.metadata", equalTo("Could not find: _metadata in message"))));
    }
}
