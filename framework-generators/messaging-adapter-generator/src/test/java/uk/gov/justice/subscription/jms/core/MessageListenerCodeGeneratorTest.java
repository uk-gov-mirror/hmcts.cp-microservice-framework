package uk.gov.justice.subscription.jms.core;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.subscription.domain.builders.EventBuilder.event;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;
import static uk.gov.justice.subscription.domain.builders.LocationBuilder.location;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionsDescriptorBuilder.subscriptionsDescriptor;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;
import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.subscription.parser.SubscriptionWrapper;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageListenerCodeGeneratorTest {

    private static final String DEFAULT_TOPIC_EVENT_PROCESSOR_TYPE_SPEC = """
            @uk.gov.justice.services.core.annotation.Adapter("EVENT_PROCESSOR")
            @jakarta.ejb.MessageDriven(
                activationConfig = {
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "my-context.event"),
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CPPNAME in('my-context.events.something-happened')"),
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "my-context.event.processor.my-context.event"),
                    @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${property.mdb.EVENT_PROCESSOR.maxSession:15}")
                }
            )
            @jakarta.interceptor.Interceptors({
                uk.gov.moj.base.package.name.MyContextEventProcessorMyContextEventJmsLoggerMetadataInterceptor.class,
                uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class
            })
            public class MyContextEventProcessorMyContextEventJmsListener implements jakarta.jms.MessageListener {
              private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextEventProcessorMyContextEventJmsListener.class);
                            
              @jakarta.inject.Inject
              @uk.gov.justice.services.subscription.annotation.SubscriptionName("subscription")
              uk.gov.justice.services.subscription.SubscriptionManager subscriptionManager;
                            
              @jakarta.inject.Inject
              uk.gov.justice.services.adapter.messaging.SubscriptionJmsProcessor subscriptionJmsProcessor;
                            
              @java.lang.Override
              public void onMessage(jakarta.jms.Message message) {
                uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                subscriptionJmsProcessor.process(message, subscriptionManager);
              }
            }
            """;

    @InjectMocks
    private MessageListenerCodeGenerator messageListenerCodeGenerator;

    @Test
    public void shouldGenerateMDBForTopic() {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "EVENT_LISTENER";
        final String jmsUri = "jms:topic:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();


        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("eventsource")
                .withLocation(location()
                        .withJmsUri(jmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final List<EventSourceDefinition> eventSourceDefinitions = singletonList(eventSourceDefinition);

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventSourceName("eventsource")
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionsDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(subscriptionsDescriptor, eventSourceDefinitions);

        final ClassNameFactory classNameFactory = new ClassNameFactory(
                basePackageName,
                serviceName,
                componentName,
                jmsUri);

        final GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withCustomMDBPool();

        final TypeSpec typeSpec = messageListenerCodeGenerator.generate(subscriptionWrapper, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);

        assertThat(typeSpec.toString(), is("""
                @uk.gov.justice.services.core.annotation.Adapter("EVENT_LISTENER")
                @jakarta.ejb.MessageDriven(
                    activationConfig = {
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "my-context.handler.command"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "my-context.event.listener.my-context.handler.command"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${property.mdb.EVENT_LISTENER.maxSession:15}")
                    }
                )
                @jakarta.interceptor.Interceptors({
                    uk.gov.moj.base.package.name.MyContextEventListenerMyContextHandlerCommandJmsLoggerMetadataInterceptor.class,
                    uk.gov.moj.base.package.name.MyContextEventListenerMyContextHandlerCommandEventValidationInterceptor.class
                })
                @org.jboss.ejb3.annotation.Pool("my-context-handler-command-event-listener-pool")
                public class MyContextEventListenerMyContextHandlerCommandJmsListener implements jakarta.jms.MessageListener {
                  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextEventListenerMyContextHandlerCommandJmsListener.class);
                
                  @jakarta.inject.Inject
                  @uk.gov.justice.services.subscription.annotation.SubscriptionName("subscription")
                  uk.gov.justice.services.subscription.SubscriptionManager subscriptionManager;
                
                  @jakarta.inject.Inject
                  uk.gov.justice.services.adapter.messaging.SubscriptionJmsProcessor subscriptionJmsProcessor;
                
                  @java.lang.Override
                  public void onMessage(jakarta.jms.Message message) {
                    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                    subscriptionJmsProcessor.process(message, subscriptionManager);
                  }
                }
                """));
    }

    @Test
    public void shouldGenerateMDBForCommandHandlerQueue() {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "COMMAND_HANDLER";
        final String jmsUri = "jms:topic:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();

        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("eventSource")
                .withLocation(location()
                        .withJmsUri(jmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final List<EventSourceDefinition> eventSourceDefinitions = singletonList(eventSourceDefinition);

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventSourceName("eventSource")
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionsDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final ClassNameFactory classNameFactory = new ClassNameFactory(
                basePackageName,
                serviceName,
                componentName,
                jmsUri);

        final GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf(componentName);

        final SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(subscriptionsDescriptor, eventSourceDefinitions);

        final TypeSpec typeSpec = messageListenerCodeGenerator.generate(subscriptionWrapper, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);

        assertThat(typeSpec.toString(), is("""
                @uk.gov.justice.services.core.annotation.Adapter("COMMAND_HANDLER")
                @jakarta.ejb.MessageDriven(
                    activationConfig = {
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "my-context.handler.command"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CPPNAME in('my-context.events.something-happened','my-context.events.something-else-happened')"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${property.mdb.COMMAND_HANDLER.maxSession:15}")
                    }
                )
                @jakarta.interceptor.Interceptors({
                    uk.gov.moj.base.package.name.MyContextCommandHandlerMyContextHandlerCommandJmsLoggerMetadataInterceptor.class,
                    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class
                })
                public class MyContextCommandHandlerMyContextHandlerCommandJmsListener implements jakarta.jms.MessageListener {
                  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextCommandHandlerMyContextHandlerCommandJmsListener.class);
                
                  @jakarta.inject.Inject
                  @uk.gov.justice.services.subscription.annotation.SubscriptionName("subscription")
                  uk.gov.justice.services.subscription.SubscriptionManager subscriptionManager;
                
                  @jakarta.inject.Inject
                  uk.gov.justice.services.adapter.messaging.SubscriptionJmsProcessor subscriptionJmsProcessor;
                
                  @java.lang.Override
                  public void onMessage(jakarta.jms.Message message) {
                    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                    subscriptionJmsProcessor.process(message, subscriptionManager);
                  }
                }
                """));
    }

    @Test
    public void shouldGenerateMDBForQueue() {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "COMMAND_API";
        final String jmsUri = "jms:queue:my-context.handler.command";

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final Event event_2 = event()
                .withName("my-context.events.something-else-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-else-happened.json")
                .build();

        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("eventsource")
                .withLocation(location()
                        .withJmsUri(jmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final List<EventSourceDefinition> eventSourceDefinitions = singletonList(eventSourceDefinition);

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventSourceName("eventsource")
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionsDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final ClassNameFactory classNameFactory = new ClassNameFactory(
                basePackageName,
                serviceName,
                componentName,
                jmsUri);

        final GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf("COMMAND_API");

        final SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(subscriptionsDescriptor, eventSourceDefinitions);

        final TypeSpec typeSpec = messageListenerCodeGenerator.generate(subscriptionWrapper, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);

        assertThat(typeSpec.toString(), is("""
                @uk.gov.justice.services.core.annotation.Adapter("COMMAND_API")
                @jakarta.ejb.MessageDriven(
                    activationConfig = {
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "my-context.handler.command"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CPPNAME in('my-context.events.something-happened','my-context.events.something-else-happened')"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${property.mdb.COMMAND_API.maxSession:15}")
                    }
                )
                @jakarta.interceptor.Interceptors({
                    uk.gov.moj.base.package.name.MyContextCommandApiMyContextHandlerCommandJmsLoggerMetadataInterceptor.class,
                    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class
                })
                public class MyContextCommandApiMyContextHandlerCommandJmsListener implements jakarta.jms.MessageListener {
                  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextCommandApiMyContextHandlerCommandJmsListener.class);
                
                  @jakarta.inject.Inject
                  @uk.gov.justice.services.subscription.annotation.SubscriptionName("subscription")
                  uk.gov.justice.services.subscription.SubscriptionManager subscriptionManager;
                
                  @jakarta.inject.Inject
                  uk.gov.justice.services.adapter.messaging.SubscriptionJmsProcessor subscriptionJmsProcessor;
                
                  @java.lang.Override
                  public void onMessage(jakarta.jms.Message message) {
                    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                    subscriptionJmsProcessor.process(message, subscriptionManager);
                  }
                }
                """));
    }

    @Test
    public void shouldGenerateMDBForEventProcessorAsQueue() {

        final TypeSpec typeSpec = getTypeSpecForEventProcessor("queue");

        assertThat(typeSpec.toString(), is("""
                @uk.gov.justice.services.core.annotation.Adapter("EVENT_PROCESSOR")
                @jakarta.ejb.MessageDriven(
                    activationConfig = {
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "my-context.event"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CPPNAME in('my-context.events.something-happened')"),
                        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${property.mdb.EVENT_PROCESSOR.maxSession:15}")
                    }
                )
                @jakarta.interceptor.Interceptors({
                    uk.gov.moj.base.package.name.MyContextEventProcessorMyContextEventJmsLoggerMetadataInterceptor.class,
                    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class
                })
                public class MyContextEventProcessorMyContextEventJmsListener implements jakarta.jms.MessageListener {
                  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.MyContextEventProcessorMyContextEventJmsListener.class);
                
                  @jakarta.inject.Inject
                  @uk.gov.justice.services.subscription.annotation.SubscriptionName("subscription")
                  uk.gov.justice.services.subscription.SubscriptionManager subscriptionManager;
                
                  @jakarta.inject.Inject
                  uk.gov.justice.services.adapter.messaging.SubscriptionJmsProcessor subscriptionJmsProcessor;
                
                  @java.lang.Override
                  public void onMessage(jakarta.jms.Message message) {
                    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                    subscriptionJmsProcessor.process(message, subscriptionManager);
                  }
                }
                """));
    }

    @Test
    public void shouldGenerateMDBForEventProcessorAsTopic() {

        final TypeSpec typeSpec = getTypeSpecForEventProcessor("topic");

        assertThat(typeSpec.toString(), is(DEFAULT_TOPIC_EVENT_PROCESSOR_TYPE_SPEC));
    }

    @Test
    public void shouldGenerateMDBForEventProcessorAsTopicForUnexpectedDestinationType() {

        final TypeSpec typeSpec = getTypeSpecForEventProcessor("unexpected destination type");

        assertThat(typeSpec.toString(), is(DEFAULT_TOPIC_EVENT_PROCESSOR_TYPE_SPEC));
    }

    private TypeSpec getTypeSpecForEventProcessor(final String destinationType) {
        final String basePackageName = "uk.gov.moj.base.package.name";
        final String serviceName = "my-context";
        final String componentName = "EVENT_PROCESSOR";
        final String jmsUri = "jms:%s:my-context.event".formatted(destinationType);

        final Event event_1 = event()
                .withName("my-context.events.something-happened")
                .withSchemaUri("http://justice.gov.uk/json/schemas/domains/example/my-context.events.something-happened.json")
                .build();

        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("eventsource")
                .withLocation(location()
                        .withJmsUri(jmsUri)
                        .withRestUri("http://localhost:8080/example/event-source-api/rest")
                        .build())
                .build();

        final List<EventSourceDefinition> eventSourceDefinitions = singletonList(eventSourceDefinition);

        final Subscription subscription = subscription()
                .withName("subscription")
                .withEvent(event_1)
                .withEventSourceName("eventsource")
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionsDescriptor()
                .withSpecVersion("1.0.0")
                .withService(serviceName)
                .withServiceComponent(componentName)
                .withSubscription(subscription)
                .build();

        final ClassNameFactory classNameFactory = new ClassNameFactory(
                basePackageName,
                serviceName,
                componentName,
                jmsUri);

        final GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf("EVENT_PROCESSOR");

        final SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(subscriptionsDescriptor, eventSourceDefinitions);

        return messageListenerCodeGenerator.generate(subscriptionWrapper, subscription, (CommonGeneratorProperties) generatorProperties, classNameFactory);
    }
}
