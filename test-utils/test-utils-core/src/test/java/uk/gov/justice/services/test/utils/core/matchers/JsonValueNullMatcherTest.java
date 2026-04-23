package uk.gov.justice.services.test.utils.core.matchers;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;

public class JsonValueNullMatcherTest {

    @Test
    public void shouldMatchJsonValueNull() throws Exception {
        assertThat(JsonValue.NULL, JsonValueNullMatcher.isJsonValueNull());
    }

    @Test
    public void shouldNotMatchJsonObject() throws Exception {
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
                .add("someId", "idValue")
                .build();

        assertThrows(AssertionError.class, () -> assertThat(jsonObject, JsonValueNullMatcher.isJsonValueNull()));
    }
}