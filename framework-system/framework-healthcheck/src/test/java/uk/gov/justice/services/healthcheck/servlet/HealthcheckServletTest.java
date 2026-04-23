package uk.gov.justice.services.healthcheck.servlet;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.healthcheck.servlet.HealthcheckServlet.CUSTOM_HTTP_500_ERROR_RESPONSE_FOR_HEALTHCHECK_FAILURES;

import uk.gov.justice.services.healthcheck.run.HealthcheckProcessRunner;
import uk.gov.justice.services.healthcheck.run.HealthcheckRunResults;

import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class HealthcheckServletTest {

    @Mock
    private HealthcheckProcessRunner healthcheckProcessRunner;

    @Mock
    private HealthcheckToJsonConverter healthcheckToJsonConverter;

    @Mock
    private Logger logger;
    
    @InjectMocks
    private HealthcheckServlet healthcheckServlet;

    @Test
    public void shouldRunTheHealthchecksAndReturnJsonResponse() throws Exception {

        final String responseJson = "the response json";
        final HealthcheckRunResults healthcheckRunResults = mock(HealthcheckRunResults.class);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        final PrintWriter out = mock(PrintWriter.class);

        when(httpServletResponse.getWriter()).thenReturn(out);
        when(healthcheckProcessRunner.runAllHealthchecks()).thenReturn(healthcheckRunResults);
        when(healthcheckToJsonConverter.toJson(healthcheckRunResults)).thenReturn(responseJson);
        when(httpServletResponse.getWriter()).thenReturn(out);
        when(healthcheckRunResults.isAllHealthchecksPassed()).thenReturn(true);

        healthcheckServlet.doGet(httpServletRequest, httpServletResponse);

        final InOrder inOrder = inOrder(logger, httpServletResponse, out);

        inOrder.verify(logger).debug("Calling healthchecks..");
        inOrder.verify(httpServletResponse).setContentType("application/json; charset=UTF-8");
        inOrder.verify(httpServletResponse).setCharacterEncoding("UTF-8");
        inOrder.verify(httpServletResponse).setStatus(SC_OK);
        inOrder.verify(logger).debug("All healthchecks passed");
        inOrder.verify(out).println(responseJson);
        inOrder.verify(out).flush();
    }

    @Test
    public void shouldSendCustom500HttpStatusIfAnyOfTheHealthchecksFail() throws Exception {

        final String responseJson = "the response json";
        final HealthcheckRunResults healthcheckRunResults = mock(HealthcheckRunResults.class);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        final PrintWriter out = mock(PrintWriter.class);

        when(httpServletResponse.getWriter()).thenReturn(out);
        when(healthcheckProcessRunner.runAllHealthchecks()).thenReturn(healthcheckRunResults);
        when(healthcheckToJsonConverter.toJson(healthcheckRunResults)).thenReturn(responseJson);
        when(httpServletResponse.getWriter()).thenReturn(out);
        when(healthcheckRunResults.isAllHealthchecksPassed()).thenReturn(false);

        healthcheckServlet.doGet(httpServletRequest, httpServletResponse);

        final InOrder inOrder = inOrder(logger, httpServletResponse, out);

        inOrder.verify(logger).debug("Calling healthchecks..");
        inOrder.verify(httpServletResponse).setContentType("application/json; charset=UTF-8");
        inOrder.verify(httpServletResponse).setCharacterEncoding("UTF-8");
        inOrder.verify(httpServletResponse).setStatus(CUSTOM_HTTP_500_ERROR_RESPONSE_FOR_HEALTHCHECK_FAILURES);
        inOrder.verify(logger).error("Healthchecks failed: the response json");
        inOrder.verify(out).println(responseJson);
        inOrder.verify(out).flush();
    }
}