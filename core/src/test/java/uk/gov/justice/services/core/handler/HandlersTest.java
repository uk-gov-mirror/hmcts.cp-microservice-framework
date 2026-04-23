package uk.gov.justice.services.core.handler;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import jakarta.json.JsonObject;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;

public class HandlersTest {

    @Test
    public void shouldFindHandlerMethods() {
        List<Method> methods = Handlers.handlerMethodsFrom(new CommandHandler());
        assertThat(methods, notNullValue());
        assertThat(methods, IsCollectionWithSize.hasSize(3));
    }

    @Test
    public void shouldThrowExceptionWithInvalidHandlerMethods() {
        assertThrows(InvalidHandlerException.class, () -> Handlers.handlerMethodsFrom(new InvalidHandler()));
    }

    public static class CommandHandler {
        @Handles("test-context.command.create-something")
        public void handler1(String jsonString) {
        }

        @Handles("test-context.command.update-something")
        public void handler2(JsonObject envelopeAsJsonObject) {
        }

        @Handles("test-context.command.delete-something")
        public void handler2(DeleteSomething deleteSomething) {
        }

        public void nonHandlerMethod(Integer command) {
        }
    }

    public static class InvalidHandler {

        public void nonHandlerMethod(String command) {
        }
    }

    public static class DeleteSomething {
        UUID someId;
    }

}