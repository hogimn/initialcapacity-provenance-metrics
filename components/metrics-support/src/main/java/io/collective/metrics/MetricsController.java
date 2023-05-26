package io.collective.metrics;

import io.collective.restsupport.BasicHandler;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Handler class for exposing metrics data.
 */
public class MetricsController extends BasicHandler {
    private CollectorRegistry registry;

    /**
     * Constructs a MetricsController with the specified CollectorRegistry.
     *
     * @param registry The CollectorRegistry containing the metrics data.
     */
    public MetricsController(CollectorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Handles the GET request for the "/metrics" endpoint.
     *
     * @param target      The target of the request.
     * @param baseRequest The base request object.
     * @param request     The HTTP servlet request.
     * @param response    The HTTP servlet response.
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        // Call the get() method from the superclass BasicHandler to handle GET requests to "/metrics"
        get("/metrics", Arrays.asList("application/json", "text/html", "text/plain"), baseRequest, response, () -> {
            try {
                // Set the content type of the response to "text/plain"
                response.setContentType("text/plain");
                // Write the metrics data to the response
                writeMetrics(response);
                // Set the status of the response to 200 OK
                response.setStatus(HttpServletResponse.SC_OK);
                // Mark the base request as handled
                baseRequest.setHandled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Writes the metrics data to the HTTP servlet response.
     *
     * @param response The HTTP servlet response.
     * @throws IOException If an I/O error occurs while writing the metrics data.
     */
    private void writeMetrics(HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        // Write the metrics data in Prometheus text format to the writer
        TextFormat.write004(writer, registry.metricFamilySamples());
        // Flush the writer to ensure all data is written
        writer.flush();
    }
}
