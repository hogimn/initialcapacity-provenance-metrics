package io.collective.metrics;

import io.collective.restsupport.BasicHandler;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Handler class for handling the health check endpoint.
 */
public class HealthCheck extends BasicHandler {

    /**
     * Handles the GET request for the "/health-check" endpoint.
     *
     * @param target   The target of the request.
     * @param baseRequest The base request object.
     * @param request  The HTTP servlet request.
     * @param response The HTTP servlet response.
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        // Call the get() method from the superclass BasicHandler to handle GET requests to "/health-check"
        get("/health-check", Arrays.asList("application/json", "text/html"), baseRequest, response, () -> {
            try {
                // Set the content type of the response to "text/html; charset=UTF-8"
                response.setContentType("text/html; charset=UTF-8");
                // Write the response message "i'm healthy." to the output stream
                response.getOutputStream().write("i'm healthy.".getBytes());
                // Set the status of the response to 200 OK
                response.setStatus(HttpServletResponse.SC_OK);
                // Mark the base request as handled
                baseRequest.setHandled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
