package uk.gov.justice.services.healthcheck.servlet;

import static java.lang.String.format;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

import uk.gov.justice.services.healthcheck.run.HealthcheckProcessRunner;
import uk.gov.justice.services.healthcheck.run.HealthcheckRunResults;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

@WebServlet(
        name = "healthcheckServlet",
        urlPatterns = "/internal/healthchecks/all"
)
public class HealthcheckServlet extends HttpServlet {

    public static final int CUSTOM_HTTP_500_ERROR_RESPONSE_FOR_HEALTHCHECK_FAILURES = 523;

    @Inject
    private HealthcheckProcessRunner healthcheckProcessRunner;

    @Inject
    private HealthcheckToJsonConverter healthcheckToJsonConverter;

    @Inject
    private Logger logger;

    @Override
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws IOException {

        logger.debug("Calling healthchecks..");

        final HealthcheckRunResults healthcheckRunResults = healthcheckProcessRunner.runAllHealthchecks();
        final String json = healthcheckToJsonConverter.toJson(healthcheckRunResults);
        httpServletResponse.setContentType("application/json; charset=UTF-8");
        httpServletResponse.setCharacterEncoding("UTF-8");

        final PrintWriter out = httpServletResponse.getWriter();

        if (healthcheckRunResults.isAllHealthchecksPassed()) {
            httpServletResponse.setStatus(SC_OK);
            logger.debug("All healthchecks passed");
        } else {
            httpServletResponse.setStatus(CUSTOM_HTTP_500_ERROR_RESPONSE_FOR_HEALTHCHECK_FAILURES);
            logger.error(format("Healthchecks failed: %s", json));
        }

        out.println(json);
        out.flush();
    }
}
