package io.collective.articles;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.collective.restsupport.BasicHandler;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for handling article-related endpoints.
 */
public class ArticlesController extends BasicHandler {
    private ArticleDataGateway gateway;
    Meter articleRequests;
    Meter articleAvailableRequests;

    /**
     * Constructs an ArticlesController with the specified ObjectMapper and ArticleDataGateway.
     *
     * @param mapper  The ObjectMapper for serialization.
     * @param gateway The ArticleDataGateway for retrieving article records.
     * @param registry The MetricRegistry for collecting metrics.
     */
    public ArticlesController(ObjectMapper mapper, ArticleDataGateway gateway, MetricRegistry registry) {
        // Call the constructor of the superclass (BasicHandler) passing the provided ObjectMapper
        super(mapper);
        // Set the ArticleDataGateway instance
        this.gateway = gateway;
        // Create a meter named "article-requests" in the MetricRegistry
        this.articleRequests = registry.meter("article-requests");
        // Create a meter named "article-available-requests" in the MetricRegistry
        this.articleAvailableRequests = registry.meter("article-available-requests");
    }

    /**
     * Handles the incoming HTTP request for the specified endpoint.
     *
     * @param target          The target of the request.
     * @param request         The Request object representing the incoming request.
     * @param servletRequest  The HttpServletRequest object representing the servlet request.
     * @param servletResponse The HttpServletResponse object representing the servlet response.
     */
    @Override
    public void handle(String target, Request request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        // Handle GET request for "/articles"
        get("/articles", Arrays.asList("application/json", "text/html"), request, servletResponse, () -> {
            { // todo - query the articles gateway for *all* articles, map record to infos, and send back a collection of article infos
                // Create an ArrayList to store the ArticleInfo objects
                ArrayList<ArticleInfo> articles = new ArrayList<>();
                // Retrieve all ArticleRecord objects from the gateway
                List<ArticleRecord> all = gateway.findAll();
                // Iterate over each ArticleRecord object
                for (ArticleRecord record : all) {
                    // Create a new ArticleInfo object using the record's ID and title, and add it to the articles list
                    articles.add(new ArticleInfo(record.getId(), record.getTitle()));
                }
                // Write the articles list as a JSON response body
                writeJsonBody(servletResponse, articles);
            }
            // Increment the meter for article requests
            articleRequests.mark();
        });

        // Handle GET request for "/available"
        get("/available", Arrays.asList("application/json", "text/html"), request, servletResponse, () -> {

            { // todo - query the articles gateway for *available* articles, map records to infos, and send back a collection of article infos
                // Create an ArrayList to store the ArticleInfo objects
                ArrayList<ArticleInfo> articles = new ArrayList<>();
                // Retrieve the available ArticleRecord objects from the gateway
                List<ArticleRecord> all = gateway.findAvailable();
                // Iterate over each available ArticleRecord object
                for (ArticleRecord record : all) {
                    // Create a new ArticleInfo object using the record's ID and title, and add it to the articles list
                    articles.add(new ArticleInfo(record.getId(), record.getTitle()));
                }
                // Write the articles list as a JSON response body
                writeJsonBody(servletResponse, articles);
            }
            // Increment the meter for available article requests
            articleAvailableRequests.mark();
        });
    }
}
