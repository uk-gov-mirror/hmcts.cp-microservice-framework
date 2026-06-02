package uk.gov.justice.services.generators.test.utils.builder;


import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

public class HeadersBuilder {
    public static HttpHeaders headersWith(final String headerName, final String headerValue) {
        final MultivaluedHashMap<String, String> headersMap = new MultivaluedHashMap<>();
        headersMap.add(headerName, headerValue);
        return new HttpHeaders() {
            @Override
            public List<String> getRequestHeader(final String name) {
                return headersMap.getOrDefault(name, Collections.emptyList());
            }

            @Override
            public String getHeaderString(final String name) {
                final List<String> values = headersMap.get(name);
                return values != null && !values.isEmpty() ? values.get(0) : null;
            }

            @Override
            public MultivaluedMap<String, String> getRequestHeaders() {
                return headersMap;
            }

            @Override
            public List<MediaType> getAcceptableMediaTypes() {
                final List<String> acceptValues = headersMap.getOrDefault("Accept", Collections.emptyList());
                return acceptValues.stream()
                        .map(MediaType::valueOf)
                        .collect(Collectors.toList());
            }

            @Override
            public List<Locale> getAcceptableLanguages() {
                return Collections.emptyList();
            }

            @Override
            public MediaType getMediaType() {
                final String contentType = headersMap.getFirst("Content-Type");
                return contentType != null ? MediaType.valueOf(contentType) : null;
            }

            @Override
            public Locale getLanguage() {
                return null;
            }

            @Override
            public Map<String, Cookie> getCookies() {
                return Collections.emptyMap();
            }

            @Override
            public Date getDate() {
                return null;
            }

            @Override
            public int getLength() {
                return -1;
            }
        };
    }

}
