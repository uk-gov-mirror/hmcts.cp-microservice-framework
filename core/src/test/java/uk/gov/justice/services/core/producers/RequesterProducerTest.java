package uk.gov.justice.services.core.producers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherDelegate;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.requester.Requester;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RequesterProducerTest {

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private SystemUserUtil systemUserUtil;

    @Mock
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Mock
    private RequestResponseEnvelopeValidatorFactory requestResponseEnvelopeValidatorFactory;

    @InjectMocks
    private RequesterProducer requesterProducer;

    @Test
    public void requesterShouldDelegateAdminRequestSubstitutingUserId() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final Dispatcher dispatcher = mock(Dispatcher.class);
        final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator = mock(RequestResponseEnvelopeValidator.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(requestResponseEnvelopeValidatorFactory.createNew()).thenReturn(requestResponseEnvelopeValidator);

        final Requester requester = requesterProducer.produceRequester(injectionPoint);

        assertThat(requester, is(instanceOf(DispatcherDelegate.class)));

        assertThat(getValueOfField(requester, "dispatcher", Dispatcher.class), is(dispatcher));
        assertThat(getValueOfField(requester, "systemUserUtil", SystemUserUtil.class), is(systemUserUtil));
        assertThat(getValueOfField(requester, "requestResponseEnvelopeValidator", RequestResponseEnvelopeValidator.class), is(requestResponseEnvelopeValidator));
        assertThat(getValueOfField(requester, "envelopePayloadTypeConverter", EnvelopePayloadTypeConverter.class), is(envelopePayloadTypeConverter));
        assertThat(getValueOfField(requester, "jsonEnvelopeRepacker", JsonEnvelopeRepacker.class), is(jsonEnvelopeRepacker));
    }
}
