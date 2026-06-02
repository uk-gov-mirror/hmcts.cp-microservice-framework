package uk.gov.justice.services.metrics.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import jakarta.servlet.annotation.WebServlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link MetricsAdminServlet} class.
 */
public class MetricsAdminServletTest {

    private MetricsAdminServlet listener;

    @BeforeEach
    public void setup() {
        listener = new MetricsAdminServlet();
    }

    @Test
    public void shouldBeMappedToCorrectUrl() {
        WebServlet annotation = listener.getClass().getAnnotation(WebServlet.class);
        assertThat(annotation.value().length, equalTo(1));
        assertThat(annotation.value()[0], equalTo("/internal/metrics/*"));
    }
}