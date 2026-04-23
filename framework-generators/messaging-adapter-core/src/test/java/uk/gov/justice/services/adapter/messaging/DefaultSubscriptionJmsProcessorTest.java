package uk.gov.justice.services.adapter.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.subscription.SubscriptionManager;

import jakarta.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultSubscriptionJmsProcessorTest {

    @Mock
    private SubscriptionJmsProcessorDelegate subscriptionJmsProcessorDelegate;

    @InjectMocks
    private DefaultSubscriptionJmsProcessor subscriptionJmsProcessor;

    @Test
    public void shouldProcessMessage() throws Exception {

        final TextMessage message = mock(TextMessage.class);
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        subscriptionJmsProcessor.process(message, subscriptionManager);

        verify(subscriptionJmsProcessorDelegate).process(message, subscriptionManager);
    }

}