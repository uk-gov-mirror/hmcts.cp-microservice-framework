package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import jakarta.json.JsonValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DispatcherDelegateTest {

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private SystemUserUtil systemUserUtil;

    @Mock
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Mock
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    private RequestResponseEnvelopeValidator requestResponseEnvelopeValidator;

    @InjectMocks
    private DispatcherDelegate dispatcherDelegate;

    @Test
    public void shouldDispatchRequestAndValidateResponse() throws Exception {

        final JsonEnvelope requestEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope convertedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope repackedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope responseEnvelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(
                requestEnvelope,
                JsonValue.class)).thenReturn(convertedEnvelope);
        when(jsonEnvelopeRepacker.repack(convertedEnvelope)).thenReturn(repackedEnvelope);
        when(dispatcher.dispatch(repackedEnvelope)).thenReturn(responseEnvelope);

        assertThat(dispatcherDelegate.request(requestEnvelope), is(responseEnvelope));

        verify(requestResponseEnvelopeValidator).validateResponse(responseEnvelope);
    }

    @Test
    public void shouldDispatchTypedRequestAndValidateResponse() throws Exception {

        final Class<JsonValue> type = JsonValue.class;

        final JsonEnvelope requestEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope convertedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope repackedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope responseEnvelope = mock(JsonEnvelope.class);
        final Envelope<JsonValue> typedResponseEnvelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(
                requestEnvelope,
                type)).thenReturn(convertedEnvelope);
        when(jsonEnvelopeRepacker.repack(convertedEnvelope)).thenReturn(repackedEnvelope);
        when(dispatcher.dispatch(repackedEnvelope)).thenReturn(responseEnvelope);
        when(envelopePayloadTypeConverter.convert(responseEnvelope, type)).thenReturn(typedResponseEnvelope);

        assertThat(dispatcherDelegate.request(requestEnvelope, type), is(typedResponseEnvelope));

        verify(requestResponseEnvelopeValidator).validateResponse(responseEnvelope);
    }

    @Test
    public void shouldSendRequestAsAdmin() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope adminEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope responseEnvelope = mock(JsonEnvelope.class);

        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(adminEnvelope);
        when(dispatcher.dispatch(adminEnvelope)).thenReturn(responseEnvelope);

        assertThat(dispatcherDelegate.requestAsAdmin(envelope), is(responseEnvelope));

        verify(requestResponseEnvelopeValidator).validateResponse(responseEnvelope);
    }

    @Test
    public void shouldSendTypedRequestAsAdmin() throws Exception {

        final Class<JsonValue> type = JsonValue.class;

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final Envelope<JsonValue> convertedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope repackedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope adminEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope responseEnvelope = mock(JsonEnvelope.class);
        final Envelope<JsonValue> typedResponseEnvelope = mock(JsonEnvelope.class);


        when(envelopePayloadTypeConverter.convert(
                envelope,
                type)).thenReturn(convertedEnvelope);
        when(jsonEnvelopeRepacker.repack(convertedEnvelope)).thenReturn(repackedEnvelope);
        when(systemUserUtil.asEnvelopeWithSystemUserId(repackedEnvelope)).thenReturn(adminEnvelope);
        when(dispatcher.dispatch(adminEnvelope)).thenReturn(responseEnvelope);
        when(envelopePayloadTypeConverter.convert(responseEnvelope, type)).thenReturn(typedResponseEnvelope);

        assertThat(dispatcherDelegate.requestAsAdmin(envelope, type), is(typedResponseEnvelope));

        verify(requestResponseEnvelopeValidator).validateResponse(responseEnvelope);
    }

    @Test
    public void shouldSendUntypedEnvelopeAsJsonValueType() throws Exception {

        final Envelope<?> envelope = mock(Envelope.class);
        final Envelope<JsonValue> convertedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope repackedEnvelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(
                envelope,
                JsonValue.class)).thenReturn(convertedEnvelope);

        when(jsonEnvelopeRepacker.repack(convertedEnvelope)).thenReturn(repackedEnvelope);

        dispatcherDelegate.send(envelope);

        final InOrder inOrder = inOrder(requestResponseEnvelopeValidator, dispatcher);

        inOrder.verify(requestResponseEnvelopeValidator).validateRequest(repackedEnvelope);
        inOrder.verify(dispatcher).dispatch(repackedEnvelope);
    }

    @Test
    public void shouldSendJsonEnvelopeAsAdmin() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final JsonEnvelope adminEnvelope = mock(JsonEnvelope.class);

        when(systemUserUtil.asEnvelopeWithSystemUserId(envelope)).thenReturn(adminEnvelope);

        dispatcherDelegate.sendAsAdmin(envelope);

        final InOrder inOrder = inOrder(requestResponseEnvelopeValidator, dispatcher);

        inOrder.verify(requestResponseEnvelopeValidator).validateRequest(envelope);
        inOrder.verify(dispatcher).dispatch(adminEnvelope);
    }

    @Test
    public void shouldSendUntypedEnvelopeAsAdminWithTypeOfJsonEnvelope() throws Exception {

        final Envelope<?> envelope = mock(Envelope.class);
        final JsonEnvelope adminEnvelope = mock(JsonEnvelope.class);
        final Envelope<JsonValue> convertedEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope repackedEnvelope = mock(JsonEnvelope.class);

        when(envelopePayloadTypeConverter.convert(envelope, JsonValue.class)).thenReturn(convertedEnvelope);
        when(jsonEnvelopeRepacker.repack(convertedEnvelope)).thenReturn(repackedEnvelope);
        when(systemUserUtil.asEnvelopeWithSystemUserId(repackedEnvelope)).thenReturn(adminEnvelope);

        dispatcherDelegate.sendAsAdmin(envelope);

        final InOrder inOrder = inOrder(requestResponseEnvelopeValidator, dispatcher);

        inOrder.verify(requestResponseEnvelopeValidator).validateRequest(repackedEnvelope);
        inOrder.verify(dispatcher).dispatch(adminEnvelope);

    }
}
