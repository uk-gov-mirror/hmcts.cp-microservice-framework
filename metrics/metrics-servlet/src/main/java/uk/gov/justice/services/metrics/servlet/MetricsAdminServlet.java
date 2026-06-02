package uk.gov.justice.services.metrics.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet providing internal metrics endpoints. Handles /internal/metrics/ping
 * to support health check probes.
 */
@WebServlet(
        name = "metrics",
        value = "/internal/metrics/*"
)
public class MetricsAdminServlet extends HttpServlet {

    private static final long serialVersionUID = 8926448900805363286L;

    private static final String PING_PATH = "/ping";
    private static final String PONG = "pong";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain;charset=UTF-8";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (PING_PATH.equals(request.getPathInfo())) {
            response.setContentType(CONTENT_TYPE_TEXT_PLAIN);
            response.setStatus(HttpServletResponse.SC_OK);
            try (final PrintWriter writer = response.getWriter()) {
                writer.write(PONG);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
