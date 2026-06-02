package uk.gov.justice.services.persistence;

import static jakarta.transaction.Status.STATUS_ACTIVE;
import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import jakarta.persistence.EntityManager;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntityManagerFlushInterceptorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private UserTransaction userTransaction;

    @InjectMocks
    private EntityManagerFlushInterceptor entityManagerFlushInterceptor;

    @Test
    public void shouldCallFlushOnTheHibernateEntityManagerToCommitTheTransaction() throws Exception {

        final InterceptorContext incomingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorContext outgoingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);

        when(interceptorChain.processNext(incomingInterceptorContext)).thenReturn(outgoingInterceptorContext);
        when(userTransaction.getStatus()).thenReturn(STATUS_ACTIVE);

        assertThat(entityManagerFlushInterceptor.process(incomingInterceptorContext, interceptorChain), is(outgoingInterceptorContext));

        final InOrder inOrder = inOrder(entityManager, interceptorChain);

        inOrder.verify(interceptorChain).processNext(incomingInterceptorContext);
        inOrder.verify(entityManager).flush();
    }

    @Test
    public void shouldNotCallFlushOnTheHibernateEntityManagerIfTheTransactionIsNotActive() throws Exception {

        final InterceptorContext incomingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorContext outgoingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);

        when(interceptorChain.processNext(incomingInterceptorContext)).thenReturn(outgoingInterceptorContext);
        when(userTransaction.getStatus()).thenReturn(STATUS_MARKED_ROLLBACK);

        assertThat(entityManagerFlushInterceptor.process(incomingInterceptorContext, interceptorChain), is(outgoingInterceptorContext));

        verify(interceptorChain).processNext(incomingInterceptorContext);
        verify(entityManager, never()).flush();
    }

    @Test
    public void shouldCallFlushEvenOnFailureIfTheTransactionIsActive() throws Exception {

        final RuntimeException runtimeException = new RuntimeException("Oops");

        final InterceptorContext incomingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);

        when(interceptorChain.processNext(incomingInterceptorContext)).thenThrow(runtimeException);
        when(userTransaction.getStatus()).thenReturn(STATUS_ACTIVE);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> entityManagerFlushInterceptor.process(incomingInterceptorContext, interceptorChain));

        assertThat(exception, is(sameInstance(runtimeException)));

        final InOrder inOrder = inOrder(interceptorChain, entityManager);

        inOrder.verify(interceptorChain).processNext(incomingInterceptorContext);
        inOrder.verify(entityManager).flush();
    }

    @Test
    public void shouldNotCallFlushOnFailureIfTheTransactionIsNotActive() throws Exception {

        final RuntimeException runtimeException = new RuntimeException("Oops");

        final InterceptorContext incomingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);

        when(interceptorChain.processNext(incomingInterceptorContext)).thenThrow(runtimeException);
        when(userTransaction.getStatus()).thenReturn(STATUS_MARKED_ROLLBACK);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> entityManagerFlushInterceptor.process(incomingInterceptorContext, interceptorChain));

        assertThat(exception, is(sameInstance(runtimeException)));

        verify(interceptorChain).processNext(incomingInterceptorContext);
        verify(entityManager, never()).flush();
    }

    @Test
    public void shouldThrowEntityManagerFlushExceptionIfGettingStatusOfUserTransactionFails() throws Exception {

        final SystemException systemException = new SystemException("Ooops");

        final InterceptorContext incomingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorContext outgoingInterceptorContext = mock(InterceptorContext.class);
        final InterceptorChain interceptorChain = mock(InterceptorChain.class);

        when(interceptorChain.processNext(incomingInterceptorContext)).thenReturn(outgoingInterceptorContext);
        when(userTransaction.getStatus()).thenThrow(systemException);

        final EntityManagerFlushException entityManagerFlushException = assertThrows(
                EntityManagerFlushException.class,
                () -> entityManagerFlushInterceptor.process(incomingInterceptorContext, interceptorChain));

        assertThat(entityManagerFlushException.getCause(), is(systemException));
        assertThat(entityManagerFlushException.getMessage(), is("Failed to get status of UserTransaction"));
    }
}