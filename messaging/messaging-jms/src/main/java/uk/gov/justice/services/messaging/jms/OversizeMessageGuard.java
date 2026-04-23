package uk.gov.justice.services.messaging.jms;

import static java.lang.String.format;

import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class OversizeMessageGuard {


    @Inject
    private JmsMessagingConfiguration jmsMessagingConfiguration;

    @Inject
    private Logger logger;

    public void checkSizeOf(final String messageString, final String envelopeName, final UUID envelopeId, final Optional<UUID> streamId) {
        final int oversizeMessageThresholdBytes = jmsMessagingConfiguration.getOversizeMessageThresholdBytes();
        final int size = messageString.getBytes().length;
        if (size > oversizeMessageThresholdBytes) {
            final String errorMessage = "OVERSIZED_MESSAGE: %d bytes. " +
                    "The size of message '%s', with id '%s' and streamId '%s' is %d bytes. " +
                    "This exceeds the message size threshold of %d bytes.";
            logger.warn(format(errorMessage,
                    size,
                    envelopeName,
                    envelopeId,
                    streamId.orElse(null),
                    size,
                    oversizeMessageThresholdBytes));
        }
    }
}
