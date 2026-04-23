package uk.gov.justice.services.adapter.direct;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.inject.Inject;

import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;

@RunWithApplicationComposer
public class SynchronousDirectAdapterCacheIT {

    @Inject
    private SynchronousDirectAdapterCache adapterCache;

    @Module
    @Classes(cdi = true, value = {
            SynchronousDirectAdapterCache.class,
            DirectAdapterAbc.class,
            DirectAdapterBcd.class

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("SynchronousDirectAdapterCacheIT")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldProduceAdapterBasingOnServiceComponentAnnotation() {
        assertThat(adapterCache.directAdapterForComponent("COMPONENT_ABC"), instanceOf(DirectAdapterAbc.class));
        assertThat(adapterCache.directAdapterForComponent("COMPONENT_BCD"), instanceOf(DirectAdapterBcd.class));
    }

    @Test
    public void shouldThrowExceptionIfBeanNotFound() {
        assertThrows(IllegalArgumentException.class, () -> adapterCache.directAdapterForComponent("UNKNOWN"));
    }

    @DirectAdapter("COMPONENT_ABC")
    public static class DirectAdapterAbc implements SynchronousDirectAdapter {

        @Override
        public JsonEnvelope process(JsonEnvelope envelope) {
            return null;
        }
    }

    @DirectAdapter("COMPONENT_BCD")
    public static class DirectAdapterBcd implements SynchronousDirectAdapter {

        @Override
        public JsonEnvelope process(JsonEnvelope envelope) {
            return null;
        }
    }
}
