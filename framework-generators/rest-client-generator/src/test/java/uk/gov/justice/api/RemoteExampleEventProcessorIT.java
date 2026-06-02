package uk.gov.justice.api;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.clients.core.DefaultRestClientHelper;
import uk.gov.justice.services.clients.core.DefaultRestClientProcessor;
import uk.gov.justice.services.clients.core.webclient.BaseUriFactory;
import uk.gov.justice.services.clients.core.webclient.ContextMatcher;
import uk.gov.justice.services.clients.core.webclient.MockServerPortProvider;
import uk.gov.justice.services.clients.core.webclient.WebTargetFactoryFactory;
import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.converter.JsonObjectConvertersProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.http.DefaultServerPortProvider;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherConfiguration;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.featurecontrol.FeatureControlAnnotationFinder;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.FileBasedJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.PayloadExtractor;
import uk.gov.justice.services.core.json.SchemaCatalogAwareJsonSchemaValidator;
import uk.gov.justice.services.core.json.SchemaValidationErrorMessageGenerator;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserver;
import uk.gov.justice.services.core.mapping.DefaultMediaTypesMappingCache;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.DefaultSchemaIdMappingCache;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.core.producers.EnvelopeValidatorFactory;
import uk.gov.justice.services.core.producers.RequestResponseEnvelopeValidatorFactory;
import uk.gov.justice.services.core.producers.RequesterProducer;
import uk.gov.justice.services.core.producers.SenderProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.jdbc.persistence.JndiAppNameProvider;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.test.utils.common.validator.DummyJsonSchemaValidator;
import uk.gov.justice.services.test.utils.core.handler.registry.TestHandlerRegistryCacheProducer;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import jakarta.inject.Inject;
import java.util.Properties;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

@RunWithApplicationComposer
@ServiceComponent(EVENT_PROCESSOR)
@WireMockTest(httpPort = 19091)
public class RemoteExampleEventProcessorIT {

    private static final String PORT = "19091";
    private static int port = -1;
    private static final String BASE_PATH = "/rest-client-generator/command/api/rest/example";
    private static final String MOCK_SERVER_PORT = "mock.server.port";

    private static final UUID QUERY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final String USER_NAME = "John Smith";
    private static final String GROUP_NAME = "admin";

    private static final JsonEnvelope RESPONSE = envelope()
            .with(metadataWithRandomUUID("people.group"))
            .withPayloadOf(GROUP_ID, "groupId")
            .withPayloadOf(GROUP_NAME, "groupName")
            .build();

    @Inject
    Sender sender;

    @Inject
    Requester requester;

    @BeforeAll
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        System.setProperty(MOCK_SERVER_PORT, PORT);
    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            AccessControlFailureMessageGenerator.class,
            DefaultAccessControlService.class,
            AllowAllPolicyEvaluator.class,
            BaseUriFactory.class,
            BeanInstantiater.class,
            ContextMatcher.class,
            DefaultServerPortProvider.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            EnvelopePayloadTypeConverter.class,
            JsonEnvelopeRepacker.class,
            EmptySystemUserProvider.class,
            DefaultEnveloper.class,
            InterceptorCache.class,
            InterceptorChainProcessor.class,
            InterceptorChainProcessorProducer.class,
            JndiBasedServiceContextNameProvider.class,
            DefaultJsonObjectEnvelopeConverter.class,
            LoggerProducer.class,
            MockServerPortProvider.class,
            ObjectMapperProducer.class,
            ObjectToJsonValueConverter.class,
            PolicyEvaluator.class,
            RemoteEventProcessor2ExampleCommandApi.class,
            RequesterProducer.class,
            DefaultRestClientHelper.class,
            DefaultRestClientProcessor.class,
            ServiceComponentObserver.class,
            StringToJsonObjectConverter.class,
            SystemUserUtil.class,
            WebTargetFactoryFactory.class,
            UtcClock.class,
            SenderProducer.class,
            FileBasedJsonSchemaValidator.class,
            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,
            DefaultTraceLogger.class,

            JsonSchemaLoader.class,

            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,
            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,

            CatalogProducer.class,
            SchemaCatalogService.class,
            SchemaCatalogResolverProducer.class,

            DefaultMediaTypesMappingCache.class,
            ActionNameToMediaTypesMappingObserver.class,
            MediaTypeProvider.class,
            DummyJsonSchemaValidator.class,
            EnvelopeInspector.class,

            MediaTypesMappingCacheInitialiser.class,
            SchemaIdMappingCacheInitialiser.class,

            DefaultEventSourceDefinitionFactory.class,
            ComponentNameExtractor.class,
            JndiAppNameProvider.class,

            SchemaValidationErrorMessageGenerator.class,

            DispatcherConfiguration.class,

            JsonObjectConvertersProducer.class,
            FeatureControlAnnotationFinder.class,
            TestHandlerRegistryCacheProducer.class,

            RequestResponseEnvelopeValidatorFactory.class,
            EnvelopeValidatorFactory.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("RemoteExampleEventProcessorIT")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldSendPostCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.create-user+json";
        final String bodyPayload = getJsonBuilderFactory().createObjectBuilder().add("userName", USER_NAME).build().toString();

        stubFor(post(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.create-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(postRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }

    @Test
    public void shouldSendPutCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.update-user+json";
        final String bodyPayload = getJsonBuilderFactory().createObjectBuilder().add("userName", USER_NAME).build().toString();

        stubFor(put(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.update-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(putRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }

    @Test
    public void shouldSendPatchCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.modify-user+json";
        final String bodyPayload = getJsonBuilderFactory().createObjectBuilder().add("userName", USER_NAME).build().toString();

        stubFor(patch(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.modify-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(patchRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
                .withRequestBody(equalToJson(bodyPayload))
        );
    }

    @Test
    public void shouldSendDeleteCommandToRemoteService() {
        final String path = format("/users/%s", USER_ID.toString());
        final String mimeType = "application/vnd.people.delete-user+json";

        stubFor(delete(urlEqualTo(BASE_PATH + path))
                .willReturn(aResponse()
                        .withStatus(ACCEPTED.getStatusCode())));

        sender.send(envelope()
                .with(metadataOf(QUERY_ID, "people.delete-user"))
                .withPayloadOf(USER_ID.toString(), "userId")
                .withPayloadOf(USER_NAME, "userName")
                .build());

        verify(deleteRequestedFor(urlEqualTo(BASE_PATH + path))
                .withHeader(CONTENT_TYPE, equalTo(mimeType))
        );
    }

    @Test
    public void shouldRequestSynchronousPostToRemoteService() {

        final String name = "people.create-group";

        final String path = "/groups/" + GROUP_ID.toString();
        final String mimeType = "application/vnd.people.group+json";
        final String bodyPayload = getJsonBuilderFactory().createObjectBuilder()
                .add("groupName", GROUP_NAME)
                .build().toString();

        stubFor(post(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));

        final JsonEnvelope group = envelope()
                .with(metadataOf(randomUUID(), name))
                .withPayloadOf(GROUP_ID.toString(), "groupId")
                .withPayloadOf(GROUP_NAME, "groupName")
                .build();

        final JsonEnvelope response = requester.request(group);

        assertThat(response, jsonEnvelope(
                metadata().withName("people.group"),
                payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.groupName", is(GROUP_NAME)))
                )));
    }

    @Test
    public void shouldRequestSynchronousPutToRemoteService() {

        final String name = "people.update-group";

        final String path = "/groups/" + GROUP_ID.toString();
        final String mimeType = "application/vnd.people.group+json";
        final String bodyPayload = getJsonBuilderFactory().createObjectBuilder()
                .add("groupName", GROUP_NAME)
                .build().toString();

        stubFor(put(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));

        final JsonEnvelope group = envelope()
                .with(metadataOf(randomUUID(), name))
                .withPayloadOf(GROUP_ID.toString(), "groupId")
                .withPayloadOf(GROUP_NAME, "groupName")
                .build();

        final JsonEnvelope response = requester.request(group);

        assertThat(response, jsonEnvelope(
                metadata().withName("people.group"),
                payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.groupName", is(GROUP_NAME)))
                )));
    }

    @Test
    public void shouldRequestSynchronousPatchToRemoteService() {

        final String name = "people.modify-group";

        final String path = "/groups/" + GROUP_ID.toString();
        final String mimeType = "application/vnd.people.group+json";
        final String bodyPayload = getJsonBuilderFactory().createObjectBuilder()
                .add("groupName", GROUP_NAME)
                .build().toString();

        stubFor(patch(urlEqualTo(BASE_PATH + path))
                .withRequestBody(equalToJson(bodyPayload))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", mimeType)
                        .withBody(RESPONSE.toDebugStringPrettyPrint())));

        final JsonEnvelope group = envelope()
                .with(metadataOf(randomUUID(), name))
                .withPayloadOf(GROUP_ID.toString(), "groupId")
                .withPayloadOf(GROUP_NAME, "groupName")
                .build();

        final JsonEnvelope response = requester.request(group);

        assertThat(response, jsonEnvelope(
                metadata().withName("people.group"),
                payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.groupName", is(GROUP_NAME)))
                )));
    }
}
