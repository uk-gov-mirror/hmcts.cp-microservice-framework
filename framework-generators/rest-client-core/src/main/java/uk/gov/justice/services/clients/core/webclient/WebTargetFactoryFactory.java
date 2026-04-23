package uk.gov.justice.services.clients.core.webclient;

import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

@Default
public class WebTargetFactoryFactory {

    @Inject
    BaseUriFactory baseUriFactory;

    public WebTargetFactory create() {
        return new WebTargetFactory(baseUriFactory);
    }
}
