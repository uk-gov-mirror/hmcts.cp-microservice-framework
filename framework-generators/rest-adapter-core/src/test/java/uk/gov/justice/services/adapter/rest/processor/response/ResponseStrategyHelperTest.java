package uk.gov.justice.services.adapter.rest.processor.response;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

import jakarta.json.JsonValue;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class ResponseStrategyHelperTest {

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Function<JsonEnvelope, Response> function;

    @Mock
    private Response response;

    @Mock
    private Logger logger;

    @InjectMocks
    private ResponseStrategyHelper responseStrategyHelper;

    @Test
    public void shouldCallApplyOnFunctionOnOkResponse() throws Exception {
        when(function.apply(jsonEnvelope)).thenReturn(response);
        when(jsonEnvelope.payload()).thenReturn(JsonValue.TRUE);

        final Response result = responseStrategyHelper.responseFor("action.name", Optional.of(jsonEnvelope), function);

        assertThat(result, sameInstance(response));
    }

    @Test
    public void shouldReturnNotFoundResponseForJsonValueNull() throws Exception {
        when(jsonEnvelope.payload()).thenReturn(JsonValue.NULL);

        final Response response = responseStrategyHelper.responseFor("action.name", Optional.of(jsonEnvelope), function);

        assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnNotFoundResponseForNullPojo() throws Exception {
        when(jsonEnvelope.payload()).thenReturn(null);

        final Response response = responseStrategyHelper.responseFor("action.name", Optional.of(jsonEnvelope), function);

        assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturnInternalServerErrorResponse() throws Exception {
        final Response response = responseStrategyHelper.responseFor("action.name", Optional.empty(), function);

        assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    }
}