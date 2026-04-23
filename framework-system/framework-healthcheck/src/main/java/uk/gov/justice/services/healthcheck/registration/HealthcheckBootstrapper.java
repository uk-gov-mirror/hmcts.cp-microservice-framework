package uk.gov.justice.services.healthcheck.registration;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.healthcheck.api.Healthcheck;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import com.google.common.annotations.VisibleForTesting;

public class HealthcheckBootstrapper implements Extension {

    private final CdiInstanceResolver cdiInstanceResolver;

    @SuppressWarnings("unused")
    public HealthcheckBootstrapper() {
        this(new CdiInstanceResolver());
    }

    @VisibleForTesting
    HealthcheckBootstrapper(final CdiInstanceResolver cdiInstanceResolver) {
        this.cdiInstanceResolver = cdiInstanceResolver;
    }

    public void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        final HealthcheckRegistry healthcheckRegistry = cdiInstanceResolver.getInstanceOf(
                HealthcheckRegistry.class,
                beanManager);

        beanManager.getBeans(Healthcheck.class)
                .stream()
                .map(bean -> (Healthcheck) cdiInstanceResolver.getInstanceOf(bean.getBeanClass(), beanManager))
                .forEach(healthcheckRegistry::addHealthcheck);
    }
}