package uk.gov.justice.services.metrics.micrometer.prometheus.web;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(
        name = "promethuesServlet",
        urlPatterns = "/internal/metrics/prometheus"
)
public class PrometheusMetricsServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(PrometheusMetricsServlet.class);

    @Inject
    private PrometheusMeterRegistry prometheusMeterRegistry;

    @Override
    public void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse response) throws IOException {
        final String metrics = prometheusMeterRegistry.scrape();

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try (final PrintWriter out = response.getWriter()) {
            out.print(metrics);
            out.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error("Error while writing metrics to response", e);
        }
    }
}
