package uk.gov.justice.services.core.featurecontrol.local;

import static java.lang.String.format;
import static java.util.Optional.empty;

import uk.gov.justice.services.core.featurecontrol.domain.Feature;
import uk.gov.justice.services.core.featurecontrol.domain.FeatureControl;
import uk.gov.justice.services.yaml.YamlParser;

import java.net.URL;
import java.util.Optional;

import jakarta.inject.Inject;

import org.slf4j.Logger;

public class LocalFeatureStore {

    public static final String FEATURE_CONTROL_FILE_NAME = "feature-control.yaml";

    @Inject
    private YamlParser yamlParser;

    @Inject
    private LocalFeatureFileLocator localFeatureFileLocator;

    @Inject
    private Logger logger;

    public Optional<Feature> lookup(final String featureName) {

        final Optional<URL> url = localFeatureFileLocator.findLocalFeatureFileLocation(FEATURE_CONTROL_FILE_NAME);

        if (url.isPresent()) {

            final URL localFeatureFileLocation = url.get();
            logger.warn(format("Loading FeatureControl list from local file: '%s'", localFeatureFileLocation));

            final FeatureControl featureControlList = yamlParser.parseYamlFrom(
                    localFeatureFileLocation,
                    FeatureControl.class);

            return featureControlList
                    .getFeatures()
                    .stream()
                    .filter(feature -> feature.getFeatureName().equals(featureName))
                    .findFirst();
        }

        return empty();
    }
}
