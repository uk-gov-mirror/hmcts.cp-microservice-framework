package uk.gov.justice.services.core.featurecontrol.remote;

import uk.gov.justice.services.core.featurecontrol.domain.Feature;

import java.util.Optional;

import jakarta.inject.Inject;

public class RemoteFeatureStore {

    @Inject
    private CachingFeatureProviderTimerBean cachingFeatureProviderTimerBean;

    @Inject
    private NonCachingFeatureProvider nonCachingFeatureProvider;

    @Inject
    private FeatureControlConfiguration featureControlConfiguration;

    public Optional<Feature> lookup(final String featureName) {
        if(featureControlConfiguration.isFeatureControlCacheEnabled()) {
            return cachingFeatureProviderTimerBean.lookup(featureName);
        }

        return nonCachingFeatureProvider.lookup(featureName);
    }
}
