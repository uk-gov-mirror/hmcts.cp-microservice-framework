package uk.gov.justice.services.jmx;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import javax.management.MBeanServer;

@ApplicationScoped
public class MBeanServerProducer {

    @Produces
    public MBeanServer mBeanServer() {
        return getPlatformMBeanServer();
    }
}
