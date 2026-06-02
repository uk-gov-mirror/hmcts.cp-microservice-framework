package uk.gov.justice.services.adapter.rest.processor;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategy;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;


@RunWithApplicationComposer
public class ResponseStrategyCacheIT {

    @Inject
    ResponseStrategyCache responseStrategyCache;


    @Module
    @Classes(cdi = true, value = {
            ResponseStrategyCache.class,
            ABCResponseStrategy.class,
            BCDResponseStrategy.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("ResponseStrategyCacheIT")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldReturnStrategyByName() {

        assertThat(responseStrategyCache.responseStrategyOf("ABC")
                .responseFor("", empty()).getEntity(), is("Response from Strategy ABC"));

        assertThat(responseStrategyCache.responseStrategyOf("BCD")
                .responseFor("", empty()).getEntity(), is("Response from Strategy BCD"));

        assertThat(responseStrategyCache.responseStrategyOf("ABC")
                .responseFor("", empty()).getEntity(), is("Response from Strategy ABC"));
    }

    @Test
    public void shouldThrowExceptionIfBeanNotFound() {
        assertThrows(IllegalArgumentException.class, () -> responseStrategyCache.responseStrategyOf("unknown"));
    }

    @ApplicationScoped
    @Named("ABC")
    public static class ABCResponseStrategy implements ResponseStrategy {

        @Override
        public Response responseFor(final String action, final Optional<JsonEnvelope> result) {
            return Response.ok("Response from Strategy ABC").build();
        }
    }

    @ApplicationScoped
    @Named("BCD")
    public static class BCDResponseStrategy implements ResponseStrategy {

        @Override
        public Response responseFor(final String action, final Optional<JsonEnvelope> result) {
            return Response.ok("Response from Strategy BCD").build();
        }
    }
}