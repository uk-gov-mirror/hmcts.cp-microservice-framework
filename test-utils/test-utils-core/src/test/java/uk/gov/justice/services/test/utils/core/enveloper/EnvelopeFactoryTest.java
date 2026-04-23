package uk.gov.justice.services.test.utils.core.enveloper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.json.JsonObject;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;

public class EnvelopeFactoryTest {

    private EnvelopeFactory envelopeFactory = new EnvelopeFactory();

    @Test
    public void shouldCreateAJsonEnvelope() throws Exception {

        final String commandName = "some.command-or-other";

        final JsonObject payload = getJsonBuilderFactory().createObjectBuilder()
                .add("payloadName", "payloadValue")
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFactory.create(commandName, payload);

        assertThat(jsonEnvelope, jsonEnvelope(
                metadata().withName(commandName),
                payloadIsJson(
                        withJsonPath("$.payloadName", equalTo("payloadValue"))
                )));
    }
}
