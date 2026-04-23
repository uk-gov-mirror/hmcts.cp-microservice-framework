package uk.gov.justice.services.core.featurecontrol;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;

import uk.gov.justice.services.core.annotation.FeatureControl;
import uk.gov.justice.services.core.featurecontrol.local.LocalFeatureStore;
import uk.gov.justice.services.core.handler.HandlerMethod;
import uk.gov.justice.services.core.handler.registry.HandlerRegistryCache;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Interceptor for applying feature control. If any {@link FeatureControl} annotations are
 * found on the handler method that will handle the action, these are compared against a list
 * of enabled/disabled feature names. If any of the names match the annotations then a
 * {@link DisabledFeatureException} is thrown. This will result in an HTTP 403 FORBIDDEN
 * response.
 *
 * The features are stored in azure, however they can be overridden by putting feature-control.yaml
 * either on the classpath or in the deployment directory of wildfly.
 *
 * The wildfly yaml file will override the classpath yaml which will override the azure settings
 *
 * See {@link LocalFeatureStore} for details of using feature-control.yaml and refer to
 * feature-control.yaml in the test resources of this module for an example
 */
public class FeatureControlInterceptor implements Interceptor {

    @Inject
    private FeatureControlGuard featureControlGuard;

    @Inject
    private HandlerRegistryCache handlerRegistryCache;

    @Inject
    private Logger logger;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {

        final String actionName = interceptorContext.inputEnvelope().metadata().name();
        final String componentName = interceptorContext.getComponentName();

        final HandlerMethod handlerMethod = handlerRegistryCache
                .handlerRegistryFor(componentName, LOCAL)
                .get(actionName);

        final List<String> featureNames = handlerMethod.getFeatureNames();

        featureNames.forEach(featureName -> {
            final boolean featureEnabled = featureControlGuard.isFeatureEnabled(featureName);

            logger.info(format("'%s' feature is enabled '%s' for action '%s' in %s", featureName, featureEnabled, actionName, componentName));

            if (!featureEnabled) {
                throw new DisabledFeatureException(format("The feature '%s' is disabled for the action '%s' in %s", featureName, actionName, componentName));
            }
        });

        return interceptorChain.processNext(interceptorContext);
    }
}
