package uk.gov.justice.services.messaging.jms.cdi;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import uk.gov.justice.services.messaging.jms.annotation.ConnectionFactoryName;

import jakarta.inject.Inject;

public class JmsConnectionFactoryJndiNameProvider {

    public static final String DEFAULT_CONNECTION_FACTORY_JNDI_NAME = "java:comp/DefaultJMSConnectionFactory";

    @Inject
    private JmsConnectionConfig jmsConnectionConfig;

    public String determineConnectionFactoryName(final ConnectionFactoryName connectionFactoryName) {

        final String nameFromAnnotation = connectionFactoryName.value();
        if (isNotBlank(nameFromAnnotation) &&
            jmsConnectionConfig.shouldUseSeparateAuditMessageBroker()) {
                return nameFromAnnotation;
        }

        return defaultConnectionFactoryJndiName();
    }

    public String defaultConnectionFactoryJndiName() {
        return DEFAULT_CONNECTION_FACTORY_JNDI_NAME;
    }
}
