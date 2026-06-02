package uk.gov.justice.services.metrics.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import jakarta.servlet.annotation.WebListener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link HealthCheckServletContextListener} class.
 */
public class HealthCheckServletContextListenerTest {

    private HealthCheckServletContextListener listener;

    @BeforeEach
    public void setup() {
        listener = new HealthCheckServletContextListener();
    }

    @Test
    public void shouldBeAWebListener() {
        final WebListener annotation = listener.getClass().getAnnotation(WebListener.class);
        assertThat(annotation, notNullValue());
    }
}
