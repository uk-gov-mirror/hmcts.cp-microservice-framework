package uk.gov.justice.services.messaging.jms.cdi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.ConnectionFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JmsConnectionFactoryCacheTest {

    @Mock
    private JmsConnectionFactoryLookup jmsConnectionFactoryLookup;

    @InjectMocks
    private JmsConnectionFactoryCache jmsConnectionFactoryCache;

    @Test
    public void shouldLookupConnectionFactoryAndCache() throws Exception {

        final String jndiName_1 = "connection factory JNDI name 1";
        final String jndiName_2 = "connection factory JNDI name 2";

        final ConnectionFactory connectionFactory_1 = mock(ConnectionFactory.class);
        final ConnectionFactory connectionFactory_2 = mock(ConnectionFactory.class);

        when(jmsConnectionFactoryLookup.connectionFactory(jndiName_1)).thenReturn(connectionFactory_1);
        when(jmsConnectionFactoryLookup.connectionFactory(jndiName_2)).thenReturn(connectionFactory_2);

        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_1), is(connectionFactory_1));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_2), is(connectionFactory_2));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_1), is(connectionFactory_1));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_2), is(connectionFactory_2));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_1), is(connectionFactory_1));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_2), is(connectionFactory_2));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_1), is(connectionFactory_1));
        assertThat(jmsConnectionFactoryCache.getConnectionFactory(jndiName_2), is(connectionFactory_2));

        verify(jmsConnectionFactoryLookup, times(1)).connectionFactory(jndiName_1);
        verify(jmsConnectionFactoryLookup, times(1)).connectionFactory(jndiName_2);
    }
}
