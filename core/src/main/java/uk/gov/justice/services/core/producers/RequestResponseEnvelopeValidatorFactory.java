package uk.gov.justice.services.core.producers;

import uk.gov.justice.services.core.dispatcher.DispatcherConfiguration;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

import jakarta.inject.Inject;

public class RequestResponseEnvelopeValidatorFactory {

    @Inject
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    @Inject
    private MediaTypeProvider mediaTypeProvider;

    @Inject
    private EnvelopeInspector envelopeInspector;

    @Inject
    private EnvelopeValidatorFactory envelopeValidatorFactory;

    @Inject
    private DispatcherConfiguration dispatcherConfiguration;

    public RequestResponseEnvelopeValidator createNew() {

        final EnvelopeValidator envelopeValidator = envelopeValidatorFactory.createNew();

        return new RequestResponseEnvelopeValidator(
                envelopeValidator,
                nameToMediaTypeConverter,
                mediaTypeProvider,
                envelopeInspector,
                dispatcherConfiguration);
    }
}
