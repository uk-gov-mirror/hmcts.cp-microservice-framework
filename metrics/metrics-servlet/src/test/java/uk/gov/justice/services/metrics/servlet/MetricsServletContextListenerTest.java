package uk.gov.justice.services.metrics.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import jakarta.servlet.annotation.WebListener;

import org.junit.jupiter.api.Test;

public class MetricsServletContextListenerTest {

    @Test
    public void shouldBeAWebListener() {
        final MetricsServletContextListener listener = new MetricsServletContextListener();
        final WebListener annotation = listener.getClass().getAnnotation(WebListener.class);
        assertThat(annotation, notNullValue());
    }
}
