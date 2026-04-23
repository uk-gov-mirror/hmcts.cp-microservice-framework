package uk.gov.justice.services.clients.core.httpclient;

import uk.gov.justice.services.clients.core.HttpCaller;
import uk.gov.justice.services.clients.core.HttpCallerResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

/**
 * Default implementation of HttpCaller backed by the JAX-RS {@link Client}.
 * The underlying {@link Client} instance is created once as a static final field and shared
 * across all instances, reusing the connection pool  of each RestEasyClient instance
 */
public class DefaultHttpCaller implements HttpCaller {

    private static final Client CLIENT = ClientBuilder.newClient();

    @Override
    public HttpCallerResponse get(final String url, final Map<String, Object> headers) {
        final Invocation.Builder builder = CLIENT.target(url).request();

        if (headers != null) {
            headers.forEach((name, value) -> builder.header(name, String.valueOf(value)));
        }

        try (final Response response = builder.get()) {
            final int status = response.getStatus();
            final String body = response.readEntity(String.class);
            return new HttpCallerResponse(status, body != null ? body : "");
        } catch (final ProcessingException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new HttpCallerException("GET request timed out for URL: " + url, e);
            }
            if (cause instanceof ConnectException) {
                throw new HttpCallerException("Could not connect to URL: " + url, e);
            }
            throw new HttpCallerException("GET request failed for URL: " + url, e);
        }
    }
}
