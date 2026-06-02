package uk.gov.justice.services.messaging.jms;

import static java.lang.Integer.parseInt;

import uk.gov.justice.services.common.configuration.Value;

import jakarta.inject.Inject;

public class JmsMessagingConfiguration {

    @Inject
    @Value(key = "messaging.jms.oversize.message.threshold.bytes", defaultValue = "262144")
    private String oversizeMessageThresholdBytes;

    public int getOversizeMessageThresholdBytes() {
        return parseInt(oversizeMessageThresholdBytes);
    }
}
