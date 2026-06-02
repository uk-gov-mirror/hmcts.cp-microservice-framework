package uk.gov.justice.services.messaging.logging;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.logging.ResponseLoggerHelper.toResponseTrace;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResponseLoggerHelperTest {

    private static final String CPP_ID = "145e0eca-f0a6-40b7-8f91-2a2709ab2a8a";
    private static final int STATUS_CODE = 202;
    private static final String MEDIA_TYPE = "context.command.dosomething";

    @Mock
    private MediaType mediaType;

    @Mock
    private Response response;


    @Test
    public void shouldPrintResponseParameters() {
        when(response.getHeaderString(ID)).thenReturn(CPP_ID);
        when(response.getMediaType()).thenReturn(mediaType);
        when(response.getStatus()).thenReturn(STATUS_CODE);
        when(mediaType.getType()).thenReturn(MEDIA_TYPE);

        with(toResponseTrace(response))
                .assertEquals("MediaType", MEDIA_TYPE)
                .assertEquals(ID, CPP_ID)
                .assertEquals("ResponseCode", STATUS_CODE);
    }

    @Test
    public void shouldNotPrintMissingResponseParameters() {
        when(response.getHeaderString(ID)).thenReturn(CPP_ID);
        when(response.getMediaType()).thenReturn(null);
        when(response.getStatus()).thenReturn(404);

        with(toResponseTrace(response))
                .assertNotDefined("MediaType")
                .assertEquals(ID, CPP_ID)
                .assertEquals("ResponseCode", 404);
    }

}
