package uk.gov.justice.services.messaging.jms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.jms.exception.JmsEnvelopeSenderException;

import jakarta.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DestinationProviderTest {

    @Mock
    private InitialContext initialContext;

    @InjectMocks
    private DestinationProvider destinationProvider;

    @Test
    public void shouldGetDestinationFromInitialContext() throws Exception {

        final String destinationName = "destination name";

        final Destination destination = mock(Destination.class);

        when(initialContext.lookup(destinationName)).thenReturn(destination);

        assertThat(destinationProvider.getDestination(destinationName), is(destination));
    }

    @Test
    public void shouldThrowExceptionIfInitialContextThrowsNamingException() throws Exception {

        final NamingException namingException = new NamingException("Ooops");

        final String destinationName = "destination name";


        when(initialContext.lookup(destinationName)).thenThrow(namingException);

        try {
            destinationProvider.getDestination(destinationName);
            fail();
        } catch (final JmsEnvelopeSenderException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Exception while looking up JMS destination name 'destination name'"));
        }
    }
}
