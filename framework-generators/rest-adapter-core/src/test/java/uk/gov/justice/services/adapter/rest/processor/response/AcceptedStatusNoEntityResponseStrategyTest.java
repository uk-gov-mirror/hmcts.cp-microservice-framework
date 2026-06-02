package uk.gov.justice.services.adapter.rest.processor.response;

import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AcceptedStatusNoEntityResponseStrategyTest {

    @InjectMocks
    private AcceptedStatusNoEntityResponseStrategy acceptedStatusNoEntityResponseStrategy;

    @Test
    public void shouldReturnAcceptedStatusWithNoEntity() throws Exception {
        final UUID id = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "Name"))
                .withPayloadOf("payload", "payload")
                .build();

        final Response response = acceptedStatusNoEntityResponseStrategy
                .responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(ACCEPTED.getStatusCode()));

        final Object entity = response.getEntity();
        assertThat(entity, is(nullValue()));
    }
}