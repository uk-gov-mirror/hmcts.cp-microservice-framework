package uk.gov.justice.services.common.configuration.subscription.pull;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventPullConfigurationTest {

    @InjectMocks
    private EventPullConfiguration eventPullConfiguration;

    @Test
    public void shouldParseTheJndiValueAsBooleanTrueIfTheConfigStringIsTrue() throws Exception {

        setField(eventPullConfiguration, "shouldProcessEventsByPullMechanism", "true");
        assertThat(eventPullConfiguration.shouldProcessEventsByPullMechanism(), is(true));
    }

    @Test
    public void shouldParseTheJndiValueAsBooleanFalseIfTheConfigStringIsNotTrue() throws Exception {

        setField(eventPullConfiguration, "shouldProcessEventsByPullMechanism", "something-not-true");
        assertThat(eventPullConfiguration.shouldProcessEventsByPullMechanism(), is(false));
    }

    @Test
    public void shouldCacheTheParsedBooleanInMemoryOnceParsed() throws Exception {

        setField(eventPullConfiguration, "shouldProcessEventsByPullMechanism", "true");
        assertThat(eventPullConfiguration.shouldProcessEventsByPullMechanism(), is(true));
        setField(eventPullConfiguration, "shouldProcessEventsByPullMechanism", "false");
        assertThat(eventPullConfiguration.shouldProcessEventsByPullMechanism(), is(true));
        setField(eventPullConfiguration, "shouldProcessEventsByPullMechanism", "something-silly");
        assertThat(eventPullConfiguration.shouldProcessEventsByPullMechanism(), is(true));
        setField(eventPullConfiguration, "shouldProcessEventsByPullMechanism", null);
        assertThat(eventPullConfiguration.shouldProcessEventsByPullMechanism(), is(true));
    }
}