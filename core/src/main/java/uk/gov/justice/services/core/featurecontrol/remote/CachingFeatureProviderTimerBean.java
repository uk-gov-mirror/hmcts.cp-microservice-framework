package uk.gov.justice.services.core.featurecontrol.remote;

import static java.util.Optional.ofNullable;

import uk.gov.justice.services.core.featurecontrol.domain.Feature;
import uk.gov.justice.services.ejb.timer.TimerServiceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.TimerService;
import jakarta.inject.Inject;

@Singleton
@Startup
public class CachingFeatureProviderTimerBean {

    private static final String TIMER_JOB_NAME = "framework.feature-store-refresh.job";

    private AtomicReference<Map<String, Feature>> atomicFeatureMapReference = new AtomicReference<>(new HashMap<>());

    @Inject
    private FeatureFetcherFacade featureFetcherFacade;

    @Inject
    private TimerServiceManager timerServiceManager;

    @Resource
    private TimerService timerService;

    @Inject
    private FeatureControlConfiguration featureControlConfiguration;

    @PostConstruct
    public void startTimerService() {

        timerServiceManager.createIntervalTimer(
                TIMER_JOB_NAME,
                featureControlConfiguration.getTimerStartWaitMilliseconds(),
                featureControlConfiguration.getTimerIntervalMilliseconds(),
                timerService);
    }

    @Timeout
    public void reloadFeaturesOnTimeout() {
        reloadFeatures();
    }

    public void reloadFeatures() {
        final Map<String, Feature> featureMap = new HashMap<>();

        featureFetcherFacade
                .fetchFeatures()
                .forEach(feature -> featureMap.put(feature.getFeatureName(), feature));

        atomicFeatureMapReference.set(featureMap);
    }

    public Optional<Feature> lookup(final String featureName) {
        return ofNullable(atomicFeatureMapReference.get().get(featureName));
    }
}
