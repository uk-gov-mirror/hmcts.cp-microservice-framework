package uk.gov.justice.services.adapter.messaging;

import org.slf4j.Logger;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.jms.TextMessage;
import jakarta.json.JsonObjectBuilder;
import java.util.Optional;

import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;

/**
 * Gets the Metadata from the payload and adds metadata information to the Logger Mapped Diagnostic
 * Context.  This can be added to the log output by setting %X{frameworkRequestData} in the logger
 * pattern.
 */
public class JmsLoggerMetadataAdder {

    private static final String SERVICE_COMPONENT = "serviceComponent";

    @Inject
    private Logger logger;

    @Inject
    private JmsParameterChecker parameterChecker;

    @Inject
    private ServiceContextNameProvider serviceContextNameProvider;

    @Inject
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Inject
    private TraceLogger traceLogger;

    @Inject
    private MdcWrapper mdcWrapper;

    public Object addRequestDataToMdc(final InvocationContext invocationContext, final String componentName) throws Exception {
        traceLogger.trace(logger, () -> "Adding Request data to MDC");

        final Object[] parameters = invocationContext.getParameters();
        parameterChecker.check(parameters);
        final TextMessage message = (TextMessage) parameters[0];

        final JsonObjectBuilder builder = getJsonBuilderFactory().createObjectBuilder();

        addServiceContextNameIfPresent(builder);

        builder.add(SERVICE_COMPONENT, componentName);

        addMetaDataToBuilder(message, builder);

        mdcWrapper.put(REQUEST_DATA, builder.build().toString());

        traceLogger.trace(logger, () -> "Request data added to MDC");

        final Object result = invocationContext.proceed();

        traceLogger.trace(logger, () -> "Clearing MDC");

        mdcWrapper.clear();

        return result;
    }

    private void addServiceContextNameIfPresent(final JsonObjectBuilder builder) {
        Optional.ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> builder.add(SERVICE_CONTEXT, value));
    }

    private void addMetaDataToBuilder(final TextMessage message, final JsonObjectBuilder builder) {
        try {
            builder.add(METADATA, jmsMessageLoggerHelper.metadataAsJsonObject(message));
        } catch (Exception e) {
            builder.add(METADATA, "Could not find: _metadata in message");
        }
    }
}
