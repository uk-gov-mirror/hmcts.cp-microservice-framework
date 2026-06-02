package uk.gov.justice.services.messaging.logging;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;

import java.io.StringReader;

import jakarta.json.JsonObject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.gov.justice.services.messaging.JsonObjects;

class DefaultHttpTraceLoggerHelperTest {

    private final DefaultHttpTraceLoggerHelper helper = new DefaultHttpTraceLoggerHelper();

    @Test
    void toHttpHeaderTrace_fromHttpHeaders() {
        // given
        final MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.add(CONTENT_TYPE, "application/json");
        headerMap.add(ACCEPT, "application/json");
        headerMap.add(ACCEPT, "text/plain");
        headerMap.add(ID, "12345");
        headerMap.add(CLIENT_CORRELATION_ID, "corr-abc");
        headerMap.add(SESSION_ID, "sess-999");
        headerMap.add(NAME, "my-name");
        // Intentionally omit USER_ID to assert it is not present

        final HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.when(httpHeaders.getRequestHeaders()).thenReturn(headerMap);

        // when
        final String json = helper.toHttpHeaderTrace(httpHeaders);

        // then
        final JsonObject root = JsonObjects.getJsonReaderFactory()
                .createReader(new StringReader(json)).readObject();

        assertEquals("application/json", root.getString(CONTENT_TYPE));
        assertEquals("application/json,text/plain", root.getString(ACCEPT));

        assertTrue(root.containsKey(METADATA));
        final JsonObject metadata = root.getJsonObject(METADATA);
        assertEquals("12345", metadata.getString(ID));
        assertEquals("corr-abc", metadata.getString(CLIENT_CORRELATION_ID));
        assertEquals("sess-999", metadata.getString(SESSION_ID));
        assertEquals("my-name", metadata.getString(NAME));
        assertFalse(metadata.containsKey(USER_ID));
    }

    @Test
    void toHttpHeaderTrace_fromMultivaluedMap() {
        // given
        final MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.add(CONTENT_TYPE, "text/xml");
        headerMap.add(ACCEPT, "text/xml");
        headerMap.add(USER_ID, "user-1");

        // when
        final String json = helper.toHttpHeaderTrace(headerMap);

        // then
        final JsonObject root = JsonObjects.getJsonReaderFactory()
                .createReader(new StringReader(json)).readObject();

        assertEquals("text/xml", root.getString(CONTENT_TYPE));
        assertEquals("text/xml", root.getString(ACCEPT));

        assertTrue(root.containsKey(METADATA));
        final JsonObject metadata = root.getJsonObject(METADATA);
        assertEquals("user-1", metadata.getString(USER_ID));
        // Optional metadata keys should be absent when not provided
        assertFalse(metadata.containsKey(ID));
        assertFalse(metadata.containsKey(CLIENT_CORRELATION_ID));
        assertFalse(metadata.containsKey(SESSION_ID));
        assertFalse(metadata.containsKey(NAME));
    }
}