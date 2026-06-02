package uk.gov.justice.services.core.featurecontrol;

import uk.gov.justice.services.core.featurecontrol.domain.Feature;
import uk.gov.justice.services.core.featurecontrol.lookup.FeatureStore;

import jakarta.inject.Inject;

public class DefaultFeatureControlGuard implements FeatureControlGuard {

    @Inject
    private FeatureStore featureStore;

    @Override
    public boolean isFeatureEnabled(final String featureName) {
        final Boolean enabled = featureStore.lookup(featureName)
                .map(Feature::isEnabled)
                .orElse(false);
        return enabled;
    }
}
