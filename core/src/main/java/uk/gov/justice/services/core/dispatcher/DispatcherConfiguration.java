package uk.gov.justice.services.core.dispatcher;

import static java.lang.Boolean.parseBoolean;

import uk.gov.justice.services.common.configuration.GlobalValue;

import jakarta.inject.Inject;

public class DispatcherConfiguration {

    @Inject
    @GlobalValue(key = "rest.dispatcher.response.json.validation.enabled", defaultValue = "false")
    private String validateRestResponseJson;

    public boolean shouldValidateRestResponseJson() {
        return parseBoolean(validateRestResponseJson);
    }
}
