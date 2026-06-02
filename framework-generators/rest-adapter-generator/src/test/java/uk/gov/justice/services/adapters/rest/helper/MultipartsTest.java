package uk.gov.justice.services.adapters.rest.helper;

import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raml.model.MimeType;

@ExtendWith(MockitoExtension.class)
public class MultipartsTest {

    @Mock
    private MimeType mimeType;

    @Test
    public void shouldReturnTrueForApplicationFormUrlencodedType() throws Exception {
        when(mimeType.getType()).thenReturn(APPLICATION_FORM_URLENCODED);

        assertThat(Multiparts.isMultipartResource(mimeType), is(true));
    }

    @Test
    public void shouldReturnTrueForMultipartFormDataType() throws Exception {
        when(mimeType.getType()).thenReturn(MULTIPART_FORM_DATA);

        assertThat(Multiparts.isMultipartResource(mimeType), is(true));
    }

    @Test
    public void shouldReturnFalseForAnyOtherType() throws Exception {
        when(mimeType.getType()).thenReturn("application/vnd.test+json");

        assertThat(Multiparts.isMultipartResource(mimeType), is(false));
    }
}