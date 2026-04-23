package uk.gov.justice.services.messaging.spi;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import jakarta.json.JsonObject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.jayway.jsonassert.JsonAssert.with;
import static jakarta.json.JsonValue.NULL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonMetadata.CLIENT_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.CONTEXT;
import static uk.gov.justice.services.messaging.JsonMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonMetadata.EVENT;
import static uk.gov.justice.services.messaging.JsonMetadata.EVENT_NUMBER;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.PREVIOUS_EVENT_NUMBER;
import static uk.gov.justice.services.messaging.JsonMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.SOURCE;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM;
import static uk.gov.justice.services.messaging.JsonMetadata.STREAM_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.VERSION;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilderFrom;

/**
 * Unit tests for the {@link DefaultJsonMetadata} class.
 */
public class DefaultJsonMetadataTest {

    private static final String UUID_ID = "d04885b4-9652-4c2a-87c6-299bda0a87d4";
    private static final String UUID_CLIENT_CORRELATION = "8d67ed44-ecfb-43ce-867c-53077abf97a6";
    private static final String UUID_CAUSATION = "49ef76bc-df4f-4b91-8ca7-21972c30ee4c";
    private static final String UUID_USER_ID = "182a8f83-faa0-46d6-96d0-96999f05e3a2";
    private static final String UUID_SESSION_ID = "f0132298-7b79-4397-bab6-f2f5e27915f0";
    private static final String UUID_STREAM_ID = "f29e0415-3a3b-48d8-b301-d34faa58662a";
    private static final String MESSAGE_NAME = "logical.message.name";
    private static final long STREAM_VERSION = 99L;
    private static final String SOURCE_NAME = "source.name";
    private static final long EVENT_NUMBER_VALUE = 10L;
    private static final long PREVIOUS_EVENT_NUMBER_VALUE = 9L;


    private Metadata createSomeMetadata() {
        return metadataBuilder()
                .withId(UUID.fromString(UUID_ID))
                .withName(MESSAGE_NAME)
                .withSource(SOURCE_NAME)
                .withClientCorrelationId(UUID_CLIENT_CORRELATION)
                .withCausation(UUID.fromString(UUID_CAUSATION))
                .withUserId(UUID_USER_ID)
                .withSessionId(UUID_SESSION_ID)
                .withStreamId(UUID.fromString(UUID_STREAM_ID))
                .withVersion(STREAM_VERSION)
                .withEventNumber(EVENT_NUMBER_VALUE)
                .withPreviousEventNumber(PREVIOUS_EVENT_NUMBER_VALUE)
                .build();
    }

    @Test
    public void shouldThrowExceptionOnMissingId() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
                    final JsonObject joEnvelope = new StringToJsonObjectConverter()
                            .convert(jsonFromFile("json/envelope-missing-id.json"));
                    metadataBuilderFrom(joEnvelope.getJsonObject(METADATA));
                }
        );
    }

    @Test
    public void shouldThrowExceptionOnMissingName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> metadataBuilderFrom(new StringToJsonObjectConverter()
                .convert(jsonFromFile("json/envelope-missing-name")).getJsonObject(METADATA)));
    }

    @Test
    public void shouldReturnId() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.id(), equalTo(UUID.fromString(UUID_ID)));
    }

    @Test
    public void shouldReturnName() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.name(), equalTo(MESSAGE_NAME));
    }

    @Test
    public void shouldReturnClientCorrelationId() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.clientCorrelationId().isPresent(), is(true));
        assertThat(metadata.clientCorrelationId().get(), equalTo(UUID_CLIENT_CORRELATION));
    }

    @Test
    public void shouldReturnCausation() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.causation(), equalTo(ImmutableList.of(UUID.fromString(UUID_CAUSATION))));
    }

    @Test
    public void shouldReturnUserId() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.userId().isPresent(), is(true));
        assertThat(metadata.userId().get(), equalTo(UUID_USER_ID));
    }

    @Test
    public void shouldReturnSource() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.source().isPresent(), is(true));
        assertThat(metadata.source().get(), equalTo(SOURCE_NAME));
    }

    @Test
    public void shouldReturnSessionId() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.sessionId().isPresent(), is(true));
        assertThat(metadata.sessionId().get(), equalTo(UUID_SESSION_ID));
    }

    @Test
    public void shouldReturnStreamId() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.streamId().isPresent(), is(true));
        assertThat(metadata.streamId().get(), equalTo(UUID.fromString(UUID_STREAM_ID)));
    }

    @Deprecated // renamed to position.
    @Test
    public void shouldReturnStreamVersion() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.version().isPresent(), is(true));
        assertThat(metadata.version().get(), equalTo(STREAM_VERSION));
    }

    @Test
    public void shouldReturnStreamPosition() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.position().isPresent(), is(true));
        assertThat(metadata.position().get(), equalTo(STREAM_VERSION));
    }

    @Test
    public void shouldReturnJsonObject() throws Exception {
        final Metadata metadata = createSomeMetadata();
        final JsonObject jsonObject = metadata.asJsonObject();

        with(jsonObject.toString())
                .assertEquals(ID, UUID_ID)
                .assertEquals(NAME, MESSAGE_NAME)
                .assertEquals(SOURCE, SOURCE_NAME)
                .assertEquals(CORRELATION + "." + CLIENT_ID, UUID_CLIENT_CORRELATION)
                .assertEquals(CAUSATION + "[0]", UUID_CAUSATION)
                .assertEquals(CONTEXT + "." + USER_ID, UUID_USER_ID)
                .assertEquals(CONTEXT + "." + SESSION_ID, UUID_SESSION_ID)
                .assertEquals(STREAM + "." + STREAM_ID, UUID_STREAM_ID)
                .assertEquals(STREAM + "." + VERSION, 99)
                .assertEquals(EVENT + "." + EVENT_NUMBER, 10)
                .assertEquals(EVENT + "." + PREVIOUS_EVENT_NUMBER, 9);
    }

    @Test
    public void shouldReturnEventNumber() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.eventNumber().isPresent(), is(true));
        assertThat(metadata.eventNumber().get(), equalTo(EVENT_NUMBER_VALUE));
    }

    @Test
    public void shouldReturnPreviousEventNumber() throws Exception {
        final Metadata metadata = createSomeMetadata();
        assertThat(metadata.previousEventNumber().isPresent(), is(true));
        assertThat(metadata.previousEventNumber().get(), equalTo(PREVIOUS_EVENT_NUMBER_VALUE));
    }

    @Test
    public void shouldThrowExceptionIfIdIsMissing() throws Exception {

        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder().build();
        final MetadataBuilder metadataBuilder = metadataBuilderFrom(jsonObject);
        assertThrows(IllegalArgumentException.class, metadataBuilder::build);
    }

    @Test
    public void shouldThrowExceptionIfIdIsNotUUID() throws Exception {
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder().add(ID, "blah").build();
        final MetadataBuilder metadataBuilder = metadataBuilderFrom(jsonObject);
        assertThrows(IllegalArgumentException.class, metadataBuilder::build);
    }

    @Test
    public void shouldThrowExceptionIfIdIsNull() throws Exception {
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder().add(ID, NULL).build();
        final MetadataBuilder metadataBuilder = metadataBuilderFrom(jsonObject);
        assertThrows(IllegalArgumentException.class, metadataBuilder::build);
    }

    @Test
    public void shouldThrowExceptionIfNameIsMissing() throws Exception {
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder().add(ID, UUID_ID).build();
        final MetadataBuilder metadataBuilder = metadataBuilderFrom(jsonObject);
        assertThrows(IllegalArgumentException.class, metadataBuilder::build);
    }

    @Test
    public void shouldThrowExceptionIfNameIsEmpty() throws Exception {
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
                .add(ID, UUID_ID)
                .add(NAME, "")
                .build();
        final MetadataBuilder metadataBuilder = metadataBuilderFrom(jsonObject);
        assertThrows(IllegalArgumentException.class, metadataBuilder::build);
    }

    @Test
    public void shouldThrowExceptionIfNameIsNull() throws Exception {
        final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
                .add(ID, UUID_ID)
                .add(NAME, NULL)
                .build();
        final MetadataBuilder metadataBuilder = metadataBuilderFrom(jsonObject);
        assertThrows(IllegalArgumentException.class, metadataBuilder::build);
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() {

        final Metadata item1 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item2 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item3 = metadata(UUID.randomUUID().toString(), UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item4 = metadata(UUID_ID, UUID.randomUUID().toString(), UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item5 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID.randomUUID().toString(), UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item6 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID.randomUUID().toString(), UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item7 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID.randomUUID().toString(), UUID_STREAM_ID, MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item8 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID.randomUUID().toString(), MESSAGE_NAME, STREAM_VERSION, SOURCE_NAME);
        final Metadata item9 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, "dummy name", STREAM_VERSION, SOURCE_NAME);
        final Metadata item10 = metadata(UUID_ID, UUID_CLIENT_CORRELATION, UUID_CAUSATION, UUID_USER_ID, UUID_SESSION_ID, UUID_STREAM_ID, MESSAGE_NAME, 0L, SOURCE_NAME);

        new EqualsTester()
                .addEqualityGroup(item1, item2)
                .addEqualityGroup(item3)
                .addEqualityGroup(item4)
                .addEqualityGroup(item5)
                .addEqualityGroup(item6)
                .addEqualityGroup(item7)
                .addEqualityGroup(item8)
                .addEqualityGroup(item9)
                .addEqualityGroup(item10)
                .testEquals();
    }

    private Metadata metadata(final String id, final String uuidClientCorrelation, final String uuidCausation, final String uuidUserId,
                              final String uuidSessionId, final String uuidStreamId, final String messageName, final Long streamVersion, final String source) {
        return metadataBuilderFrom(
                getJsonBuilderFactory().createObjectBuilder()
                        .add(ID, id)
                        .add(NAME, messageName)
                        .add(SOURCE, source)
                        .add(CORRELATION, getJsonBuilderFactory().createObjectBuilder()
                                .add(CLIENT_ID, uuidClientCorrelation)
                        )
                        .add(CAUSATION, getJsonBuilderFactory().createArrayBuilder()
                                .add(uuidCausation)
                        )
                        .add(CONTEXT, getJsonBuilderFactory().createObjectBuilder()
                                .add(USER_ID, uuidUserId)
                                .add(SESSION_ID, uuidSessionId)
                        )
                        .add(STREAM, getJsonBuilderFactory().createObjectBuilder()
                                .add(STREAM_ID, uuidStreamId)
                                .add(VERSION, streamVersion)
                        )
                        .build())
                .build();

    }

    private String jsonFromFile(final String name) throws IOException {
        return Resources.toString(Resources.getResource(String.format("json/%s.json", name)), Charset.defaultCharset());
    }
}
