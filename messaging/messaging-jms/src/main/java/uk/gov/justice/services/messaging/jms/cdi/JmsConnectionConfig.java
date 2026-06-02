package uk.gov.justice.services.messaging.jms.cdi;

import static java.lang.Boolean.parseBoolean;

import uk.gov.justice.services.common.configuration.GlobalValue;

import jakarta.inject.Inject;

public class JmsConnectionConfig {

    @Inject
    @GlobalValue(key = "jms.connection.audit.message.broker.enabled", defaultValue = "false")
    private String shouldUseSeparateAuditMessageBroker;

    public boolean shouldUseSeparateAuditMessageBroker() {
        return parseBoolean(shouldUseSeparateAuditMessageBroker);
    }
}
