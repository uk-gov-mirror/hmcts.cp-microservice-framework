package uk.gov.justice.services.persistence;

import static jakarta.transaction.Status.STATUS_ACTIVE;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

public class EntityManagerFlushInterceptor implements Interceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        try {
            return interceptorChain.processNext(interceptorContext);
        } finally {
            flushEntityManager();
        }
    }

    private void flushEntityManager() {
        try {
            if (userTransaction.getStatus() == STATUS_ACTIVE) {
                entityManager.flush();
            }
        } catch (final SystemException e) {
            throw new EntityManagerFlushException("Failed to get status of UserTransaction", e);
        }
    }
}
