package uk.gov.justice.services.adapter.rest.mapper;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.UUID.randomUUID;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.justice.services.adapter.rest.exception.ConflictedResourceException;

import java.util.UUID;

import jakarta.ws.rs.core.Response;

import org.everit.json.schema.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class ConflictedResourceExceptionMapperTest {

    private static final String TEST_ERROR_MESSAGE = "Test Error Message.";

    @Mock
    private Logger logger;

    @Mock
    private Schema schema;

    @InjectMocks
    private ConflictedResourceExceptionMapper exceptionMapper;

    @Test
    public void shouldReturn409ResponseForConflictedResourceException() throws Exception {
        final UUID id = randomUUID();

        final Response response = exceptionMapper.toResponse(new ConflictedResourceException(TEST_ERROR_MESSAGE, id));
        final Object responseEntity = response.getEntity();

        assertThat(response.getStatus(), is(CONFLICT.getStatusCode()));
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.toString(),
                hasJsonPath("$.error", equalTo(TEST_ERROR_MESSAGE)));
        assertThat(responseEntity.toString(),
                hasJsonPath("$.id", equalTo(id.toString())));
    }

}