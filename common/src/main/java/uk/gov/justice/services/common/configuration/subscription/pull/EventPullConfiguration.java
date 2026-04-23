package uk.gov.justice.services.common.configuration.subscription.pull;

import static java.lang.Boolean.parseBoolean;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.common.util.LazyValue;

import jakarta.inject.Inject;

public class EventPullConfiguration {

    private final LazyValue lazyValue = new LazyValue();

    @Inject
    @Value(key = "event.processing.by.pull.mechanism.enabled", defaultValue = "false")
    private String shouldProcessEventsByPullMechanism;


    public boolean shouldProcessEventsByPullMechanism() {
        return  lazyValue.createIfAbsent(() -> parseBoolean(shouldProcessEventsByPullMechanism));
    }
}
