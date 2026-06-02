package uk.gov.justice.services.core.featurecontrol.remote;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import uk.gov.justice.services.core.featurecontrol.FeatureFetcher;
import uk.gov.justice.services.core.featurecontrol.domain.Feature;

import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class FeatureFetcherFacade {

    @Inject
    private FeatureControlConfiguration featureControlConfiguration;

    @Inject
    private FeatureFetcher featureFetcher;

    @Inject
    private Logger logger;

    public List<Feature> fetchFeatures() {

        if (featureControlConfiguration.isFeatureControlEnabled()) {
            final List<Feature> features = featureFetcher.fetchFeatures();

            logger.info(format("Fetched list of Features from remote feature store: '%s'", features));

            return features;
        }

        logger.info("Fetched control disabled. No Features fetched from remote feature store");

        return emptyList();
    }
}
