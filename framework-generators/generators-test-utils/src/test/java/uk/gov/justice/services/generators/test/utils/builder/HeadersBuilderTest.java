package uk.gov.justice.services.generators.test.utils.builder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;

import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

public class HeadersBuilderTest {

    @Test
    public void shouldReturnTrueWhenHeaderValueMatchesPredicate() {
        final HttpHeaders headers = headersWith("Accept-Encoding", "gzip");

        assertThat(headers.containsHeaderString("Accept-Encoding", ",", "gzip"::equals), is(true));
    }

    @Test
    public void shouldReturnFalseWhenHeaderValueDoesNotMatchPredicate() {
        final HttpHeaders headers = headersWith("Accept-Encoding", "gzip");

        assertThat(headers.containsHeaderString("Accept-Encoding", ",", "deflate"::equals), is(false));
    }

    @Test
    public void shouldReturnFalseWhenHeaderNotPresent() {
        final HttpHeaders headers = headersWith("Content-Type", "application/json");

        assertThat(headers.containsHeaderString("Accept-Encoding", ",", "gzip"::equals), is(false));
    }

    @Test
    public void shouldSplitMultipleValuesBySeparatorAndMatchAfterTrim() {
        final HttpHeaders headers = headersWith("Accept-Encoding", "gzip, deflate");

        assertThat(headers.containsHeaderString("Accept-Encoding", ",", "deflate"::equals), is(true));
    }
}
