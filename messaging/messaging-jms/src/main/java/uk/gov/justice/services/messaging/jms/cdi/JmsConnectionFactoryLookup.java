package uk.gov.justice.services.messaging.jms.cdi;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.jms.JndiException;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JmsConnectionFactoryLookup {

    @Inject
    private InitialContext initialContext;

    public ConnectionFactory connectionFactory(final String jndiName) {
        try {
            return (ConnectionFactory) initialContext.lookup(jndiName);
        } catch (final NamingException e) {
            throw new JndiException(format("Failed to lookup ConnectionFactory using jndi name '%s'", jndiName), e);
        }
    }
}
