package uk.gov.justice.services.healthcheck.registration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.collections.Sets.newSet;

import uk.gov.justice.services.framework.utilities.cdi.CdiInstanceResolver;
import uk.gov.justice.services.healthcheck.api.Healthcheck;
import uk.gov.justice.services.healthcheck.api.HealthcheckResult;

import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HealthcheckBootstrapperTest {

    @Mock
    private CdiInstanceResolver cdiInstanceResolver;

    @InjectMocks
    private HealthcheckBootstrapper healthcheckBootstrapper;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void shouldFindAllHealthchecksInCdiAndAddToTheRegistry() throws Exception {

        final HealthcheckRegistry healthcheckRegistry = mock(HealthcheckRegistry.class);

        final Healthcheck_1 healthcheck_1 = new Healthcheck_1();
        final Healthcheck_2 healthcheck_2 = new Healthcheck_2();

        final Class healthcheckClass_1 = healthcheck_1.getClass();
        final Class healthcheckClass_2 = healthcheck_2.getClass();

        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);
        final BeanManager beanManager = mock(BeanManager.class);

        final Bean<?> healthcheckBean_1 = mock(Bean.class);
        final Bean<?> healthcheckBean_2 = mock(Bean.class);

        when(cdiInstanceResolver.getInstanceOf(
                HealthcheckRegistry.class,
                beanManager)).thenReturn(healthcheckRegistry);
        when(beanManager.getBeans(Healthcheck.class)).thenReturn(newSet(healthcheckBean_1, healthcheckBean_2));
        when(healthcheckBean_1.getBeanClass()).thenReturn(healthcheckClass_1);
        when(healthcheckBean_2.getBeanClass()).thenReturn(healthcheckClass_2);
        when(cdiInstanceResolver.getInstanceOf(healthcheckClass_1, beanManager)).thenReturn(healthcheck_1);
        when(cdiInstanceResolver.getInstanceOf(healthcheckClass_2, beanManager)).thenReturn(healthcheck_2);

        healthcheckBootstrapper.afterDeploymentValidation(event, beanManager);

        verify(healthcheckRegistry).addHealthcheck(healthcheck_1);
        verify(healthcheckRegistry).addHealthcheck(healthcheck_2);
    }

    @SuppressWarnings("NewClassNamingConvention")
    private static class Healthcheck_1 implements Healthcheck {

        @Override
        public String getHealthcheckName() {
            return null;
        }

        @Override
        public String healthcheckDescription() {
            return null;
        }

        @Override
        public HealthcheckResult runHealthcheck() {
            return null;
        }
    }

    @SuppressWarnings("NewClassNamingConvention")
    private static class Healthcheck_2 implements Healthcheck {

        @Override
        public String getHealthcheckName() {
            return null;
        }

        @Override
        public String healthcheckDescription() {
            return null;
        }

        @Override
        public HealthcheckResult runHealthcheck() {
            return null;
        }
    }
}