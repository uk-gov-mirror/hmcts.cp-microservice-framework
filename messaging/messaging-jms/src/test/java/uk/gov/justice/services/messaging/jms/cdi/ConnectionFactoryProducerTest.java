package uk.gov.justice.services.messaging.jms.cdi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.messaging.jms.annotation.ConnectionFactoryName;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.jms.ConnectionFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConnectionFactoryProducerTest {

    @Mock
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Mock
    private JmsConnectionFactoryJndiNameProvider jmsConnectionFactoryJndiNameProvider;

    @Mock
    private JmsConnectionFactoryCache jmsConnectionFactoryCache;

    @InjectMocks
    private ConnectionFactoryProducer connectionFactoryProducer;

    @Test
    public void shouldGetTheDefaultJmsConnectionFactory() throws Exception {
        final String defaultConnectionFactoryName = "defaultConnectionFactoryName";

        final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        when(jmsConnectionFactoryJndiNameProvider
                .defaultConnectionFactoryJndiName()).thenReturn(defaultConnectionFactoryName);
        when(jmsConnectionFactoryCache.getConnectionFactory(defaultConnectionFactoryName))
                .thenReturn(connectionFactory);

        assertThat(connectionFactoryProducer.connectionFactory(), is(connectionFactory));
    }

    @Test
    public void shouldGetJmsConnectionFactoryUsingAnAnnotation() throws Exception {

        final String connectionFactoryJndiName = "connectionFactoryJndiName";
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final ConnectionFactoryName connectionFactoryName = mock(ConnectionFactoryName.class);
        final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, ConnectionFactoryName.class))
                .thenReturn(connectionFactoryName);
        when(jmsConnectionFactoryJndiNameProvider.determineConnectionFactoryName(connectionFactoryName))
                .thenReturn(connectionFactoryJndiName);
        when(jmsConnectionFactoryCache.getConnectionFactory(connectionFactoryJndiName))
                .thenReturn(connectionFactory);

        assertThat(connectionFactoryProducer.connectionFactory(injectionPoint), is(connectionFactory));
    }
}
