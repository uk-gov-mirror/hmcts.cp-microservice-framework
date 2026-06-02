package uk.gov.justice.services.jmx.bootstrap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class JmxHandlesSystemCommandBootstrapperTest {

    @Mock
    private JmxCommandBootstrapObjectFactory jmxCommandBootstrapObjectFactory;

    @InjectMocks
    private JmxSystemCommandBootstrapper jmxSystemCommandBootstrapper;

    @Test
    public void shouldBootstrapTheJmxCommandBeanAndHandlers() throws Exception {

        final SystemCommandHandlerScanner systemCommandHandlerScanner = mock(SystemCommandHandlerScanner.class);
        final BeanManager beanManager = mock(BeanManager.class);

        when(jmxCommandBootstrapObjectFactory.systemCommandScanner()).thenReturn(systemCommandHandlerScanner);

        jmxSystemCommandBootstrapper.afterDeploymentValidation(mock(AfterDeploymentValidation.class), beanManager);

        verify(systemCommandHandlerScanner).registerSystemCommands(beanManager);
    }

    @Test
    public void shouldConstructItselfWithAnObjectFactory() throws Exception {

        assertThat(getValueOfField(new JmxSystemCommandBootstrapper(), "jmxCommandBootstrapObjectFactory", JmxCommandBootstrapObjectFactory.class), is(notNullValue()));
    }
}
