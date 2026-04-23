package uk.gov.justice.services.management.suspension.executors;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsSender;
import uk.gov.justice.services.shuttering.domain.StoredCommand;
import uk.gov.justice.services.shuttering.persistence.StoredCommandRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

public class StoredCommandSender {

    @Inject
    private StoredCommandRepository storedCommandRepository;

    @Inject
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    private JmsSender jmsSender;

    @Transactional(REQUIRES_NEW)
    public void sendAndDelete(final StoredCommand storedCommand) {

        final String commandJsonEnvelope = storedCommand.getCommandJsonEnvelope();
        final JsonEnvelope jsonEnvelope = jsonObjectEnvelopeConverter.asEnvelope(commandJsonEnvelope);
        final String destination = storedCommand.getDestination();

        jmsSender.send(jsonEnvelope, destination);

        storedCommandRepository.delete(storedCommand.getEnvelopeId());
    }
}
