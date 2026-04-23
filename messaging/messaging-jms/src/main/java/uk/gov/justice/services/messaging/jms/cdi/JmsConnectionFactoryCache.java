package uk.gov.justice.services.messaging.jms.cdi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.jms.ConnectionFactory;

@Singleton
public class JmsConnectionFactoryCache {

    @Inject
    private JmsConnectionFactoryLookup jmsConnectionFactoryLookup;

    private final Map<String, ConnectionFactory> connectionFactories = new ConcurrentHashMap<>();

    public ConnectionFactory getConnectionFactory(final String connectionFactoryJndiName) {
        return connectionFactories.computeIfAbsent(connectionFactoryJndiName, jmsConnectionFactoryLookup::connectionFactory);
    }
}
