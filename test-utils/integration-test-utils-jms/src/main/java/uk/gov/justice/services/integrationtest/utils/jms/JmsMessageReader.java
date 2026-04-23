package uk.gov.justice.services.integrationtest.utils.jms;

import uk.gov.justice.services.integrationtest.utils.jms.converters.MessageConverter;
import uk.gov.justice.services.integrationtest.utils.jms.converters.ToStringMessageConverter;

import java.util.Optional;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;


class JmsMessageReader {

    private final ToStringMessageConverter toStringMessageConverter;

    JmsMessageReader() {
        toStringMessageConverter = new ToStringMessageConverter();
    }

    <T> Optional<T> retrieveMessageNoWait(final MessageConsumer messageConsumer, final MessageConverter<T> messageConverter) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }
        return retrieve(messageConsumer::receiveNoWait, messageConverter);
    }

    <T> Optional<T> retrieveMessage(final MessageConsumer messageConsumer, final MessageConverter<T> messageConverter, final long timeout) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }
        return retrieve(() -> messageConsumer.receive(timeout), messageConverter);
    }

    private <T> Optional<T> retrieve(final MessageSupplier messageSupplier, final MessageConverter<T> messageConverter) throws JMSException {
        return Optional.ofNullable((TextMessage) messageSupplier.getMessage())
                .map(message -> messageConverter.convert(getText(message)));
    }

    private String getText(final TextMessage message) {
        try {
            return message.getText();
        } catch (JMSException e) {
            throw new JmsMessagingClientException("Failed to retrieve message", e);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    void clear(final MessageConsumer messageConsumer) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }
        while (retrieveMessageNoWait(messageConsumer, toStringMessageConverter).isPresent()) {
        }
    }

    @FunctionalInterface
    private interface MessageSupplier {
        Message getMessage() throws JMSException;
    }

}
