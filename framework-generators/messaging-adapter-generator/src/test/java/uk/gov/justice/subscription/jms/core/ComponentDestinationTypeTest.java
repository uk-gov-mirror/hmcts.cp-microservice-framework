package uk.gov.justice.subscription.jms.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import jakarta.jms.Queue;
import jakarta.jms.Topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ComponentDestinationTypeTest {
    
    private static final String UNKNOWN = "unknown";
    private ComponentDestinationType componentDestinationType;

    @BeforeEach
    public void setup() {
        componentDestinationType = new ComponentDestinationType();
    }

    @Test
    public void shouldReturnDestinationType() {
        assertThat(componentDestinationType.inputTypeFor(COMMAND_API), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor(COMMAND_CONTROLLER), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor(COMMAND_HANDLER), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor(EVENT_PROCESSOR), equalTo(Topic.class));
        assertThat(componentDestinationType.inputTypeFor(EVENT_LISTENER), equalTo(Topic.class));
        assertThat(componentDestinationType.inputTypeFor(EVENT_INDEXER), equalTo(Topic.class));
    }

    @Test
    public void shouldReturnDestinationTypeForCustomComponents() {
        assertThat(componentDestinationType.inputTypeFor("CUSTOM_COMMAND_API"), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor("CUSTOM_COMMAND_CONTROLLER"), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor("CUSTOM_COMMAND_HANDLER"), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor("CUSTOM_EVENT_PROCESSOR"), equalTo(Topic.class));
        assertThat(componentDestinationType.inputTypeFor("CUSTOM_EVENT_LISTENER"), equalTo(Topic.class));
        assertThat(componentDestinationType.inputTypeFor("CUSTOM_EVENT_INDEXER"), equalTo(Topic.class));
    }

    @Test
    public void shouldReturnTrueForSupportedComponents() {
        assertThat(componentDestinationType.isSupported(COMMAND_API), equalTo(true));
        assertThat(componentDestinationType.isSupported(COMMAND_CONTROLLER), equalTo(true));
        assertThat(componentDestinationType.isSupported(COMMAND_HANDLER), equalTo(true));
        assertThat(componentDestinationType.isSupported(EVENT_PROCESSOR), equalTo(true));
        assertThat(componentDestinationType.isSupported(EVENT_LISTENER), equalTo(true));
        assertThat(componentDestinationType.isSupported(EVENT_INDEXER), equalTo(true));
    }

    @Test
    public void shouldReturnTrueForSupportedCustomComponents() {
        assertThat(componentDestinationType.isSupported("CUSTOM_COMMAND_API"), equalTo(true));
        assertThat(componentDestinationType.isSupported("CUSTOM_COMMAND_CONTROLLER"), equalTo(true));
        assertThat(componentDestinationType.isSupported("CUSTOM_COMMAND_HANDLER"), equalTo(true));
        assertThat(componentDestinationType.isSupported("CUSTOM_EVENT_PROCESSOR"), equalTo(true));
        assertThat(componentDestinationType.isSupported("CUSTOM_EVENT_LISTENER"), equalTo(true));
        assertThat(componentDestinationType.isSupported("CUSTOM_EVENT_INDEXER"), equalTo(true));
    }

    @Test
    public void shouldReturnFalseForUnsupportedComponents() {
        assertThat(componentDestinationType.isSupported(EVENT_API), equalTo(false));
        assertThat(componentDestinationType.isSupported(QUERY_API), equalTo(false));
        assertThat(componentDestinationType.isSupported(QUERY_CONTROLLER), equalTo(false));
        assertThat(componentDestinationType.isSupported(QUERY_VIEW), equalTo(false));
        assertThat(componentDestinationType.isSupported(UNKNOWN), equalTo(false));
    }

    @Test
    public void shouldThrowExceptionIfNoInputDestinationTypeFound() {

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
                componentDestinationType.inputTypeFor(EVENT_API)
        );

        assertThat(illegalArgumentException.getMessage(), is("No input destination type defined for service component of type EVENT_API"));
    }

}