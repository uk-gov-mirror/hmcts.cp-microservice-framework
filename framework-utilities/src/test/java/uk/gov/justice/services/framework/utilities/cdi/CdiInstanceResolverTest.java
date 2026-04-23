package uk.gov.justice.services.framework.utilities.cdi;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CdiInstanceResolverTest {

    @InjectMocks
    private CdiInstanceResolver cdiInstanceResolver;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldResolveTheInstanceOfACdiBean() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final Bean bean = mock(Bean.class);
        final Set<Bean<?>> beans = newHashSet(bean);
        final CreationalContext<?> context = mock(CreationalContext.class);
        final DummyCdiBean systemCommandScanner = new DummyCdiBean();

        final Class<DummyCdiBean> beanClass = DummyCdiBean.class;
        when(beanManager.getBeans(beanClass)).thenReturn(beans);
        when(beanManager.resolve(beans)).thenReturn(bean);
        when(beanManager.createCreationalContext(bean)).thenReturn(context);
        when(beanManager.getReference(
                bean,
                beanClass,
                context)).thenReturn(systemCommandScanner);

        assertThat(cdiInstanceResolver.getInstanceOf(beanClass, beanManager), is(systemCommandScanner));
    }
}
