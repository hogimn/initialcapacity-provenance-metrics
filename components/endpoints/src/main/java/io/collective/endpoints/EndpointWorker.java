package io.collective.endpoints;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.collective.articles.ArticleDataGateway;
import io.collective.articles.ArticleInfo;
import io.collective.restsupport.RestTemplate;
import io.collective.rss.Item;
import io.collective.rss.RSS;
import io.collective.workflow.Worker;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implementation of the Worker interface for executing endpoint tasks.
 */
public class EndpointWorker implements Worker<EndpointTask> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RestTemplate template;
    private ArticleDataGateway gateway;

    /**
     * Constructor that takes a RestTemplate and an ArticleDataGateway as parameters.
     *
     * @param template The RestTemplate used to perform HTTP requests.
     * @param gateway  The ArticleDataGateway used to save article information.
     */
    public EndpointWorker(RestTemplate template, ArticleDataGateway gateway) {
        this.template = template;
        this.gateway = gateway;
    }

    /**
     * Returns the name of the worker.
     *
     * @return The name of the worker.
     */
    @NotNull
    @Override
    public String getName() {
        return "ready";
    }

    /**
     * Executes the endpoint task.
     *
     * @param task The endpoint task to execute.
     */
    @Override
    public void execute(EndpointTask task) throws IOException {
        // Perform a GET request to the endpoint specified in the task using the RestTemplate
        String response = template.get(task.getEndpoint(), task.getAccept());
        // Clear the articles collection in the ArticleDataGateway
        ArticleDataGateway.articles.clear();

        { // todo - map rss results to an article infos collection and save articles infos to the article gateway
            // Map RSS results to an article infos collection and save article infos to the article gateway
            RSS rss = new XmlMapper().readValue(response, RSS.class);
            // Iterate over each item in the RSS channel
            for (Item item : rss.getChannel().getItem()) {
                // Log the title of the item
                logger.info("found title {}.", item.getTitle());
                // Create an ArticleInfo object with the title and save it to the ArticleDataGateway
                gateway.save(new ArticleInfo(0, item.getTitle()));
            }
        }
    }
}
