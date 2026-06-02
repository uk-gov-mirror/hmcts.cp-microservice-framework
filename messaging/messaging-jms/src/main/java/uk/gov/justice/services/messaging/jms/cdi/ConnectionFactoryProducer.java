package uk.gov.justice.services.messaging.jms.cdi;

import uk.gov.justice.services.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.messaging.jms.annotation.ConnectionFactoryName;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;

@ApplicationScoped
@Default
@Priority(200)
public class ConnectionFactoryProducer {

    @Inject
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Inject
    private JmsConnectionFactoryJndiNameProvider jmsConnectionFactoryJndiNameProvider;

    @Inject
    private JmsConnectionFactoryCache jmsConnectionFactoryCache;

    @Produces
    public ConnectionFactory connectionFactory() {
        final String defaultConnectionFactoryName = jmsConnectionFactoryJndiNameProvider
                .defaultConnectionFactoryJndiName();

        return jmsConnectionFactoryCache.getConnectionFactory(defaultConnectionFactoryName);
    }

    @Produces
    @ConnectionFactoryName
    public ConnectionFactory connectionFactory(final InjectionPoint injectionPoint) {
        final ConnectionFactoryName connectionFactoryName = qualifierAnnotationExtractor
                .getFrom(injectionPoint, ConnectionFactoryName.class);

        final String connectionFactoryJndiName = jmsConnectionFactoryJndiNameProvider
                .determineConnectionFactoryName(connectionFactoryName);

        return jmsConnectionFactoryCache.getConnectionFactory(connectionFactoryJndiName);
    }
}
