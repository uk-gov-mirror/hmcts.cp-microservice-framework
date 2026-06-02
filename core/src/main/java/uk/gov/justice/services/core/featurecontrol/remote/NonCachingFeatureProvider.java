package uk.gov.justice.services.core.featurecontrol.remote;

import uk.gov.justice.services.core.featurecontrol.domain.Feature;

import java.util.Optional;

import jakarta.inject.Inject;

public class NonCachingFeatureProvider {

    @Inject
    private FeatureFetcherFacade featureFetcherFacade;

    public Optional<Feature> lookup(final String featureName) {
        return featureFetcherFacade.fetchFeatures()
                .stream()
                .filter(feature -> featureName.equals(feature.getFeatureName()))
                .findFirst();
    }
}
