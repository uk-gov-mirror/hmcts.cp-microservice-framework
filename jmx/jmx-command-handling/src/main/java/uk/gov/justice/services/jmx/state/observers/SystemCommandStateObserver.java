package uk.gov.justice.services.jmx.state.observers;

import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

public class SystemCommandStateObserver {

    @Inject
    private SystemCommandStateHandler systemCommandStateHandler;

    public void onSystemCommandStateChanged(@Observes final SystemCommandStateChangedEvent systemCommandStateChangedEvent) {
        systemCommandStateHandler.handleSystemCommandStateChanged(systemCommandStateChangedEvent);
    }
}
