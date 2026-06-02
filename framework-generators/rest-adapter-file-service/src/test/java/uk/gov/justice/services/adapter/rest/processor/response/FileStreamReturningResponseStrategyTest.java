package uk.gov.justice.services.adapter.rest.processor.response;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.fileservice.api.FileRetriever;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.domain.FileReference;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;


@ExtendWith(MockitoExtension.class)
public class FileStreamReturningResponseStrategyTest {

    @Mock
    private FileRetriever fileRetriever;

    @Mock
    private Logger logger;

    private ResponseStrategyHelper responseStrategyHelper;

    @InjectMocks
    private FileStreamReturningResponseStrategy strategy;

    @BeforeEach
    public void setUp() throws Exception {
        responseStrategyHelper = new ResponseStrategyHelper();
        responseStrategyHelper.logger = logger;
        strategy.responseStrategyHelper = responseStrategyHelper;
    }

    @Test
    public void shouldReturnFileStreamInResponse() throws Exception {
        final UUID fileId = randomUUID();
        final InputStream fileStream = toInputStream("someFileContentABC");
        when(fileRetriever.retrieve(fileId)).thenReturn(Optional.of(new FileReference(fileId, null, fileStream, false)));

        final Response response = strategy.responseFor("someAction", Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf(fileId, "fileId").build()));

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getEntity(), instanceOf(InputStream.class));
        assertThat(response.getEntity(), is(sameInstance((fileStream))));
    }


    @Test
    public void shouldThrowExceptionIfFileServiceThrowsException() throws FileServiceException {

        final UUID fileId = randomUUID();
        when(fileRetriever.retrieve(fileId)).thenThrow(new FileServiceException(""));

        final Optional<JsonEnvelope> jsonEnvelope = Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf(fileId, "fileId").build());

        assertThrows(InternalServerErrorException.class, () -> strategy.responseFor("someAction", jsonEnvelope));
    }

    @Test
    public void shouldThrowExceptionIfFileNotFound() throws Exception {

        final UUID fileId = randomUUID();
        when(fileRetriever.retrieve(fileId)).thenReturn(empty());

        final Optional<JsonEnvelope> jsonEnvelope = Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf(fileId, "fileId").build());

        assertThrows(NotFoundException.class, () -> strategy.responseFor("someAction", jsonEnvelope));
    }

    @Test
    public void shouldThrowExceptionIfPayloadNull() throws Exception {
        final Optional<JsonEnvelope> jsonEnvelope = Optional.of(envelope().with(metadataWithDefaults()).withNullPayload().build());

        assertThrows(NotFoundException.class, () -> strategy.responseFor("someAction", jsonEnvelope));
    }

    @Test
    public void shouldThrowExceptionIfFileIdNotPresentInPayload() throws Exception {
        final Optional<JsonEnvelope> jsonEnvelope = Optional.of(envelope().with(metadataWithDefaults()).withPayloadOf("", "someOtherField").build());

        assertThrows(InternalServerErrorException.class, () -> strategy.responseFor("someAction", jsonEnvelope));
    }

    @Test
    public void shouldThrowExceptionIfResultEmpty() throws Exception {
        assertThrows(InternalServerErrorException.class, () -> strategy.responseFor("someAction", Optional.empty()));
    }
}