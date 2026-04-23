package uk.gov.justice.services.test.utils.messaging.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyJmsEnvelopeSender implements JmsEnvelopeSender {

    @Override
    public void send(final JsonEnvelope envelope, final String destinationName) {
        //Do nothing
    }
}
