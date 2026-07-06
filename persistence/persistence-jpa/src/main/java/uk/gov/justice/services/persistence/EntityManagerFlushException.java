package uk.gov.justice.services.persistence;

public class EntityManagerFlushException extends RuntimeException {

    public EntityManagerFlushException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
