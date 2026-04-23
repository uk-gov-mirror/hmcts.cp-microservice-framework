package uk.gov.justice.services.clients.unifiedsearch.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.clients.unifiedsearch.core.UnifiedSearchTransformerCache;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.unifiedsearch.TransformerApi;
import uk.gov.justice.services.unifiedsearch.UnifiedSearchIndexer;

import jakarta.json.JsonObject;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;

@ExtendWith(MockitoExtension.class)
public class UnifiedSearchAdapterTest {

    @Mock
    private TransformerApi transformerApi;

    @Mock
    private UnifiedSearchTransformerCache unifiedSearchTransformerCache;

    @InjectMocks
    private UnifiedSearchAdapter unifiedSearchAdapter;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeArgumentCaptor;

    @Test
    public void verifyIndexerIsCalled() {

        final String eventName = "test";
        final String transformerOperations = "";

        final Metadata metadata = metadataBuilder()
                .withId(randomUUID())
                .withName(eventName)
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadata,
                getJsonBuilderFactory().createObjectBuilder().build());

        final UnifiedSearchIndexer unifiedSearchIndexer = mock(UnifiedSearchIndexer.class);
        final JsonObject transformedEvent = getJsonBuilderFactory().createObjectBuilder().add("transformed", "event").build();

        when(unifiedSearchTransformerCache.getTransformerConfigBy(eventName)).thenReturn(transformerOperations);
        when(transformerApi.transformWithJolt(transformerOperations, jsonEnvelope.payloadAsJsonObject())).thenReturn(transformedEvent);

        unifiedSearchAdapter.index(jsonEnvelope, unifiedSearchIndexer);

        verify(unifiedSearchIndexer).indexData(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> jsonObjectEnvelope = envelopeArgumentCaptor.getValue();
        assertThat(jsonObjectEnvelope.metadata(), withMetadataEnvelopedFrom(jsonEnvelope));
        assertThat(jsonObjectEnvelope.payload(), is(transformedEvent));
    }
}