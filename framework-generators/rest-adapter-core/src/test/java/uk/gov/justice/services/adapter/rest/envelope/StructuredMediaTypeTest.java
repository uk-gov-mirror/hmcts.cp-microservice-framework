package uk.gov.justice.services.adapter.rest.envelope;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link StructuredMediaType} class.
 */
public class StructuredMediaTypeTest {

    @Test
    public void shouldThrowExceptionIfNoNameFound() throws Exception {
        final StructuredMediaType mediaType = new StructuredMediaType(APPLICATION_JSON_TYPE);

        assertThrows(IllegalStateException.class, mediaType::getName);
    }

    @Test
    public void shouldThrowExceptionIfMoreThanOneNameFound() throws Exception {
        final StructuredMediaType mediaType = new StructuredMediaType(
                new MediaType(
                        "application",
                        "json+vnd.blah+vnd.blah")
        );

        assertThrows(IllegalStateException.class, mediaType::getName);
    }

    @Test
    public void shouldReturnName() throws Exception {
        StructuredMediaType mediaType = new StructuredMediaType(new MediaType("application", "vnd.blah+json"));
        String name = mediaType.getName();
        assertThat(name, equalTo("blah"));
    }
}
