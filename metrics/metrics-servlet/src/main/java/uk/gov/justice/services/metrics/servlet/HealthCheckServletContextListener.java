package uk.gov.justice.services.metrics.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Servlet context listener for health check registration.
 * Health checks are now managed via CDI through HealthcheckServlet.
 */
@WebListener
public class HealthCheckServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
    }
}
