package uk.gov.justice.services.metrics.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Servlet context listener for metrics registry initialisation.
 * Metrics are now accessed directly via CDI.
 */
@WebListener
public class MetricsServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
    }
}
