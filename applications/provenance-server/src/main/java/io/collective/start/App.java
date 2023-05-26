package io.collective.start;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.collective.articles.ArticleDataGateway;
import io.collective.articles.ArticleRecord;
import io.collective.articles.ArticlesController;
import io.collective.endpoints.EndpointDataGateway;
import io.collective.endpoints.EndpointTask;
import io.collective.endpoints.EndpointWorkFinder;
import io.collective.endpoints.EndpointWorker;
import io.collective.metrics.HealthCheck;
import io.collective.metrics.MetricsController;
import io.collective.restsupport.BasicApp;
import io.collective.restsupport.NoopController;
import io.collective.restsupport.RestTemplate;
import io.collective.workflow.WorkScheduler;
import io.collective.workflow.Worker;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import org.eclipse.jetty.server.handler.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * The main application class that extends BasicApp and configures the server and its handlers.
 * It starts the MetricsController, initializes components for endpoint work, and provides the entry point for the program.
 */
public class App extends BasicApp {
    // Create a MetricRegistry to hold metrics
    MetricRegistry registry = new MetricRegistry();
    // Create a ArticleDataGateway with a MetricRegistry to provide some metrics
    ArticleDataGateway gateway = new ArticleDataGateway(registry);
    // Create a Prometheus CollectorRegistry
    CollectorRegistry prometheus = CollectorRegistry.defaultRegistry;

    // Create an instance of Slf4jReporter for reporting metrics to SLF4J (Simple Logging Facade for Java)
    Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
            // Set the SLF4J logger to which the metrics will be logged
            .outputTo(LoggerFactory.getLogger("io.collective.start"))
            // Set the time unit for reporting rates (e.g., requests per second)
            .convertRatesTo(TimeUnit.SECONDS)
            // Set the time unit for reporting durations (e.g., response time in milliseconds)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            // Build the Slf4jReporter instance
            .build();

    /**
     * Starts the MetricsController by registering Dropwizard metrics with Prometheus,
     * starting the Slf4jReporter to report metrics periodically, and initializing
     * the necessary components for endpoint work.
     */
    @Override
    public void start() {
        // Collect Dropwizard metrics from a MetricRegistry and register Dropwizard metrics with Prometheus
        prometheus.register(new DropwizardExports(registry));
        // Start the Slf4jReporter to report metrics periodically
        reporter.start(5, TimeUnit.SECONDS);
        // Call the start method of the superclass (BasicApp)
        super.start();

        // Initialize the articleDataGateway with some sample data
        ArticleDataGateway.articles = new ArrayList<>();
        ArticleDataGateway.articles.add(new ArticleRecord(10101, "Programming Languages InfoQ Trends Report - October 2019 4", true));
        ArticleDataGateway.articles.add(new ArticleRecord(10106, "Ryan Kitchens on Learning from Incidents at Netflix, the Role of SRE, and Sociotechnical Systems", true));

        { // todo - start the endpoint worker
            // Create an EndpointWorkFinder with an EndpointDataGateway
            EndpointWorkFinder finder = new EndpointWorkFinder(new EndpointDataGateway());
            // Create an EndpointWorker with a RestTemplate and the ArticleDataGateway
            EndpointWorker worker = new EndpointWorker(new RestTemplate(), gateway);
            // Create a list of workers to do work when EndpointWorkFinder finds a work
            List<Worker<EndpointTask>> workers = Collections.singletonList(worker);
            // Create a WorkScheduler with the finder, workers, and a delay of 300 seconds
            WorkScheduler<EndpointTask> scheduler = new WorkScheduler<>(finder, workers, 300);
            // Start the work scheduler
            scheduler.start();
        }
    }

    /**
     * Constructs an instance of the App class with the specified port.
     *
     * @param port The port number on which the server should listen.
     */
    public App(int port) {
        // Call the constructor of the superclass (BasicApp) with the provided port
        super(port);
    }

    /**
     * Retrieves the list of handlers to be registered with the server.
     *
     * @return The HandlerList containing the registered handlers.
     */
    @NotNull
    @Override
    protected HandlerList handlerList() {
        // Create a new HandlerList to hold the handlers
        HandlerList list = new HandlerList();
        // Add an instance of ArticlesController to the handler list to handle article-related request
        // It updates article-related metrics
        list.addHandler(new ArticlesController(new ObjectMapper(), gateway, registry));
        // Add an instance of HealthCheck to the handler list to check the healthiness
        list.addHandler(new HealthCheck());
        // Add an instance of MetricsController to the handler list to provide metrics data
        list.addHandler(new MetricsController(prometheus));
        // Add an instance of NoopController to the handler list
        list.addHandler(new NoopController());
        return list;
    }

    /**
     * Start of the program
     * @param args Command-line arguments passed to the program
     */
    public static void main(String[] args) {
        // Set the default timezone to UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        // Get the port from the environment variable or use the default value "8881"
        String port = System.getenv("PORT") != null ? System.getenv("PORT") : "8881";
        // Create an instance of App with the parsed port as an integer
        App app = new App(Integer.parseInt(port));
        // Start the application
        app.start();
    }
}
