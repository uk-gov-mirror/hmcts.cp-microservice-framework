package uk.gov.justice.services.test.utils.common;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWithMemberAsFirstMethodOf;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

public class MemberInjectionPointTest {

    @Test
    public void shouldCreateInjectionPointWithMemberAsFirstMethodOfClass() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.getMember().getName(), is("testMethod"));
    }

    @Test
    public void shouldCreateInjectionPointWithGivenMember() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWith(FieldClass.class.getDeclaredField("field"));
        assertThat(injectionPoint.getMember().getName(), is("field"));
    }

    @Test
    public void shouldReturnNullForGetType() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.getType(), nullValue());
    }

    @Test
    public void shouldReturnNullForGetQualifiers() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.getQualifiers(), nullValue());
    }

    @Test
    public void shouldReturnNullForGetBean() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.getBean(), nullValue());
    }

    @Test
    public void shouldReturnNullForGetAnnotated() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.getAnnotated(), nullValue());
    }

    @Test
    public void shouldReturnFalseForIsDelegate() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.isDelegate(), is(false));
    }

    @Test
    public void shouldReturnFalseForIIsTransient() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWithMemberAsFirstMethodOf(FirstMethodClass.class);
        assertThat(injectionPoint.isTransient(), is(false));
    }

    public static class FirstMethodClass {

        public void testMethod() {
        }
    }

    public static class FieldClass {

        @Inject
        Object field;
    }

}