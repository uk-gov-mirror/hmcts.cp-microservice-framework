package uk.gov.justice.services.messaging.logging;

import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;

public final class ResponseLoggerHelper {

    private ResponseLoggerHelper() {}

    public static String toResponseTrace(final Response response) {

        final Optional<Integer> responseCode = Optional.ofNullable(Integer.valueOf(response.getStatus()));
        final Optional<MediaType> mediaType = Optional.ofNullable(response.getMediaType());
        final Optional<String> cppid = Optional.ofNullable(response.getHeaderString(ID));

        final JsonObjectBuilder builder = getJsonBuilderFactory().createObjectBuilder();

        responseCode.ifPresent(integer -> builder.add("ResponseCode", integer));
        mediaType.ifPresent(s -> builder.add("MediaType", s.getType()));
        cppid.ifPresent(s -> builder.add(ID, s));

        return builder.build().toString();
    }
}
