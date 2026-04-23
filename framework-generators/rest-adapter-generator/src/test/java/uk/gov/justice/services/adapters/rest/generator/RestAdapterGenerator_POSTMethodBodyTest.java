package uk.gov.justice.services.adapters.rest.generator;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.services.adapter.rest.mapping.ActionMapperHelper;
import uk.gov.justice.services.adapter.rest.mapping.BasicActionMapperHelper;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.json.JsonObject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.valueOf;
import static jakarta.ws.rs.core.Response.Status.ACCEPTED;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.MappingBuilder.mapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.builder.ResponseBuilder.response;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

public class RestAdapterGenerator_POSTMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final JsonObject NOT_USED_JSONOBJECT = getJsonBuilderFactory().createObjectBuilder()
            .add("name", "Frederick Bloggs")
            .build();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(
                                resource("/path")
                                        .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiPathResource");

        final Object resourceObject = getInstanceOf(resourceClass);

        final Response processorResponse = Response.ok().build();

        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);
        when(restProcessor.process(
                any(String.class),
                any(Function.class),
                eq(action),
                any(Optional.class),
                any(HttpHeaders.class),
                any(Collection.class))).thenReturn(processorResponse);

        final Method method = firstMethodOf(resourceClass).get();



        final Object result = method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        assertThat(result, is(processorResponse));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldCallInterceptorChainProcessor() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(
                                resource("/path")
                                        .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiPathResource");

        final Object resourceObject = getInstanceOf(resourceClass);

        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final Method method = firstMethodOf(resourceClass).get();

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Function> functionCaptor = ArgumentCaptor.forClass(Function.class);
        verify(restProcessor).process(anyString(), functionCaptor.capture(), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope);
        functionCaptor.getValue().apply(interceptorContext);

        verify(interceptorChainProcessor).process(interceptorContext);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldProcessAsynchronouslyIfAcceptedResponseTypePresent() throws Exception {

        final Map<String, org.raml.model.Response> responses = new HashMap<>();
        responses.put(valueOf(INTERNAL_SERVER_ERROR.getStatusCode()), response().build());
        responses.put(valueOf(BAD_REQUEST.getStatusCode()), response().build());
        responses.put(valueOf(ACCEPTED.getStatusCode()), response().build());

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/path")
                                .with(httpActionWithDefaultMapping(POST)
                                        .withHttpActionOfDefaultRequestType()
                                        .withResponsesFrom(responses))
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiPathResource");

        final Object resourceObject = getInstanceOf(resourceClass);
        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final Method method = firstMethodOf(resourceClass).get();

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);


        final ArgumentCaptor<Function> functionCaptor = ArgumentCaptor.forClass(Function.class);
        verify(restProcessor).process(anyString(), functionCaptor.capture(), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class));

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope);
        functionCaptor.getValue().apply(interceptorContext);

        verify(interceptorChainProcessor).process(interceptorContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassJsonObjectToRestProcessor() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/path")
                                .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiPathResource");

        final Object resourceObject = getInstanceOf(resourceClass);
        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final Optional<JsonObject> jsonObject = Optional.of(getJsonBuilderFactory().createObjectBuilder().add("dummy", "abc").build());

        final Method method = firstMethodOf(resourceClass).get();
        method.invoke(resourceObject, jsonObject.get());

        verify(restProcessor).process(anyString(), any(Function.class), anyString(), eq(jsonObject), any(HttpHeaders.class), any(Collection.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/path")
                                .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiPathResource");

        final Object resourceObject = getInstanceOf(resourceClass);
        final HttpHeaders headers = mock(HttpHeaders.class);
        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(headers))).thenReturn(action);

        setField(resourceObject, "headers", headers);

        final Method method = firstMethodOf(resourceClass).get();
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).process(anyString(), any(Function.class), anyString(), any(Optional.class), eq(headers), any(Collection.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/some/path/{paramA}", "paramA")
                                .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiSomePathParamAResource");

        final Object resourceObject = getInstanceOf(resourceClass);
        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final Method method = firstMethodOf(resourceClass).get();
        method.invoke(resourceObject, "paramValue1234", NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).process(anyString(), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        final Collection<Parameter> pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams, hasSize(1));
        final Parameter pathParam = pathParams.iterator().next();
        assertThat(pathParam.getName(), is("paramA"));
        assertThat(pathParam.getStringValue(), is("paramValue1234"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldInvoke2ndMethodAndPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/some/path/{p1}", "p1")
                                .with(httpActionWithDefaultMapping(POST,
                                        "application/vnd.type-aa+json",
                                        "application/vnd.type-bb+json")
                                        .with(mapping()
                                                .withName("cmd-aa")
                                                .withRequestType("application/vnd.type-aa+json"))
                                        .with(mapping()
                                                .withName("cmd-bb")
                                                .withRequestType("application/vnd.type-bb+json"))
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiSomePathP1Resource");

        final Object resourceObject = getInstanceOf(resourceClass);
        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final List<Method> methods = methodsOf(resourceClass);

        final Method secondMethod = methods.get(1);
        secondMethod.invoke(resourceObject, "paramValueXYZ", NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).process(anyString(), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        final Collection<Parameter> pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams, hasSize(1));
        final Parameter pathParam = pathParams.iterator().next();
        assertThat(pathParam.getName(), is("p1"));
        assertThat(pathParam.getStringValue(), is("paramValueXYZ"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithTwoPathParamsToRestProcessor() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/some/path/{param1}/{param2}", "param1", "param2")
                                .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiSomePathParam1Param2Resource");

        final Object resourceObject = getInstanceOf(resourceClass);
        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final Method method = firstMethodOf(resourceClass).get();
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF", NOT_USED_JSONOBJECT);

        final ArgumentCaptor<Collection> pathParamsCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(restProcessor).process(anyString(), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        final Collection<Parameter> pathParams = pathParamsCaptor.getValue();

        assertThat(pathParams, hasSize(2));

        assertThat(pathParams, hasItems(
                allOf(hasProperty("name", equalTo("param1")), hasProperty("stringValue", equalTo("paramValueABC"))),
                allOf(hasProperty("name", equalTo("param2")), hasProperty("stringValue", equalTo("paramValueDEF")))
        ));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassActionToRestProcessor() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults().with(
                        resource("/user")
                                .with(httpActionWithDefaultMapping(POST)
                                        .with(mapping()
                                                .withName("contextA.someAction")
                                                .withRequestType("application/vnd.somemediatype1+json"))

                                        .withMediaTypeWithDefaultSchema("application/vnd.somemediatype1+json")
                                        .withHttpActionResponseAndNoBody()
                                )
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiUserResource");

        final Object resourceObject = getInstanceOf(resourceClass);

        final Class<?> actionMapperClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "mapper",
                "DefaultCommandApiUserResourceActionMapper");

        final Object actionMapperObject = actionMapperClass.getConstructor(ActionMapperHelper.class).newInstance(new BasicActionMapperHelper());
        setField(resourceObject, "actionMapper", actionMapperObject);

        setField(resourceObject, "headers", headersWith("Content-Type", "application/vnd.somemediatype1+json"));
        final Method method = firstMethodOf(resourceClass).get();
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).process(anyString(), any(Function.class), eq("contextA.someAction"), any(Optional.class), any(HttpHeaders.class), any(Collection.class));
    }

}
