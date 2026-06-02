package uk.gov.justice.services.jmx.logging;

import org.slf4j.MDC;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.common.log.LoggerConstants.SERVICE_CONTEXT;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonBuilderFactory;

public class MdcLoggerInterceptor {

    @Inject
    private ServiceContextNameProvider serviceContextNameProvider;

    @AroundInvoke
    public Object addRequestDataToMdc(final InvocationContext invocationContext) throws Exception {

        ofNullable(serviceContextNameProvider.getServiceContextName())
                .ifPresent(value -> {
                    final String jsonAsString = getJsonBuilderFactory().createObjectBuilder().add(SERVICE_CONTEXT, value).build().toString();
                    MDC.put(REQUEST_DATA, jsonAsString);
                });

        final Object result = invocationContext.proceed();

        MDC.remove(REQUEST_DATA);

        return result;
    }
}
