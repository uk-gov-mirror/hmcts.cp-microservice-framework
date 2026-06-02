package uk.gov.justice.services.messaging.jms.cdi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.jms.JndiException;

import jakarta.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JmsConnectionFactoryLookupTest {

    @Mock
    private InitialContext initialContext;

    @InjectMocks
    private JmsConnectionFactoryLookup jmsConnectionFactoryLookup;

    @Test
    public void shouldGetTheNamedConnectionFactoryFromJndi() throws Exception {

        final String jndiName = "the jndi name";

        final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        when(initialContext.lookup(jndiName)).thenReturn(connectionFactory);

        assertThat(jmsConnectionFactoryLookup.connectionFactory(jndiName), is(connectionFactory));
    }

    @Test
    public void shouldFailIfTheConnectionFactoryCannotBeFound() throws Exception {

        final String jndiName = "the jndi name";
        final NamingException namingException = new NamingException("Ooops");

        final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        when(initialContext.lookup(jndiName)).thenThrow(namingException);

        try {
            jmsConnectionFactoryLookup.connectionFactory(jndiName);
            fail();
        } catch (final JndiException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to lookup ConnectionFactory using jndi name 'the jndi name'"));
        }
    }
}
