package uk.gov.justice.services.messaging.jms;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

import uk.gov.justice.services.common.annotation.ComponentNameExtractor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

@ApplicationScoped
@Default
public class JmsEnvelopeSenderProducer {
    public static final String AUDIT_CLIENT = "AUDIT_CLIENT";

    @Inject
    private JmsSender jmsSender;

    @Inject
    private AuditJmsSender auditJmsSender;

    @Inject
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Inject
    private ComponentNameExtractor componentNameExtractor;

    @Produces
    public JmsEnvelopeSender createJmsEnvelopeSender(final InjectionPoint injectionPoint) {

        if (componentNameExtractor.hasComponentAnnotation(injectionPoint) && isAuditClient(injectionPoint)) {
            return new AuditJmsEnvelopeSender(auditJmsSender);
        }
        if (componentNameExtractor.hasComponentAnnotation(injectionPoint) && !isCommandController(injectionPoint)) {
            return new ShutteringJmsEnvelopeSender(envelopeSenderSelector);
        }

        return new DefaultJmsEnvelopeSender(jmsSender);
    }

    private boolean isCommandController(final InjectionPoint injectionPoint) {
        return COMMAND_CONTROLLER.equals(componentNameExtractor.componentFrom(injectionPoint));
    }

    private boolean isAuditClient(final InjectionPoint injectionPoint) {
        return AUDIT_CLIENT.equals(componentNameExtractor.componentFrom(injectionPoint));
    }

}
