package uk.gov.justice.services.metrics.interceptor;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import jakarta.inject.Inject;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public abstract class AbstractMetricsInterceptor implements Interceptor {

    @Inject
    MetricRegistry metricsRegistry;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        final Timer.Context time = metricsRegistry.timer(timerNameOf(interceptorContext)).time();
        try {
            return interceptorChain.processNext(interceptorContext);
        } finally {
            time.stop();
        }
    }

    protected String componentName(final InterceptorContext interceptorContext) {
        return interceptorContext.getComponentName();
    }

    protected abstract String timerNameOf(final InterceptorContext interceptorContext);
}