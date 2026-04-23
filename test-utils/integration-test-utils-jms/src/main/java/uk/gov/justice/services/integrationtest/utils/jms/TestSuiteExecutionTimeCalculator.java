package uk.gov.justice.services.integrationtest.utils.jms;

import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestSuiteExecutionTimeCalculator implements ExtensionContext.Store.CloseableResource, AutoCloseable {

    private final StopWatch stopWatch;

    public TestSuiteExecutionTimeCalculator() {
        this(new StopWatch());
    }

    @VisibleForTesting
    TestSuiteExecutionTimeCalculator(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
        stopWatch.start();
    }

    @Override
    public void close() {
        stopWatch.stop();
        final long elapsedTimeSeconds = stopWatch.getTime(TimeUnit.SECONDS);
        final long minutes = elapsedTimeSeconds / 60;
        final long seconds = elapsedTimeSeconds % 60;
        System.out.printf("----------Test suite execution time (assuming all tests use Jms extension): %s:%s min -------------\n", minutes, seconds);
    }
}
