package uk.gov.justice.services.adapter.rest.interceptor;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.multipart.FileInputDetails;

import java.util.Map;
import java.util.UUID;

import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;


@ExtendWith(MockitoExtension.class)
public class MultipleFileInputDetailsServiceTest {

    @Captor
    private ArgumentCaptor<JsonObject> metadataCaptor;

    @Mock
    Logger logger;

    @Mock
    private FileInputDetailsHandler fileInputDetailsHandler;

    @InjectMocks
    private MultipleFileInputDetailsService multipleFileInputDetailsService;

    @Test
    public void shouldStoreTheInputStreamToTheFileStoreAndReturnTheCorrectJsonEnvelope() throws Exception {

        final String fieldName_1 = "fieldName_1";
        final UUID fileId_1 = randomUUID();
        final String fieldName_2 = "fieldName_2";
        final UUID fileId_2 = randomUUID();

        final FileInputDetails fileInputDetails_1 = mock(FileInputDetails.class);
        final FileInputDetails fileInputDetails_2 = mock(FileInputDetails.class);

        when(fileInputDetails_1.getFieldName()).thenReturn(fieldName_1);
        when(fileInputDetails_2.getFieldName()).thenReturn(fieldName_2);

        when(fileInputDetailsHandler.store(fileInputDetails_1)).thenReturn(fileId_1);
        when(fileInputDetailsHandler.store(fileInputDetails_2)).thenReturn(fileId_2);

        final Map<String, UUID> results = multipleFileInputDetailsService.storeFileDetails(asList(fileInputDetails_1, fileInputDetails_2));

        assertThat(results.size(), is(2));
        assertThat(results, hasKey(fieldName_1));
        assertThat(results.get(fieldName_1), is(fileId_1));
        assertThat(results, hasKey(fieldName_2));
        assertThat(results.get(fieldName_2), is(fileId_2));
    }
}
