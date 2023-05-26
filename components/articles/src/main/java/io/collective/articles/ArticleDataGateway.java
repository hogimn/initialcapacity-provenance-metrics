package io.collective.articles;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The ArticleDataGateway class provides data access methods for managing articles.
 */
public class ArticleDataGateway {
    /**
     * Static list to store ArticleRecords
     */
    public static List<ArticleRecord> articles = new ArrayList<>();

    /**
     * Random object to generate sequence numbers
     */
    private Random sequence = new Random();

    /**
     * Constructor that registers a metric for the number of articles in the MetricRegistry
     *
     * @param registry The MetricRegistry to register the metric in.
     */
    public ArticleDataGateway(MetricRegistry registry) {
        registry.register("articles",
                new CachedGauge<Integer>(10, TimeUnit.MINUTES) {
                    // Override the loadValue method to return the current size of the articles list
                    @Override
                    protected Integer loadValue() {
                        return articles.size();
                    }
                });
    }

    /**
     * Returns all articles.
     *
     * @return The list of all ArticleRecord objects.
     */
    public List<ArticleRecord> findAll() {
        return articles;
    }

    /**
     * Returns available articles (articles with isAvailable = true).
     *
     * @return The list of available ArticleRecord objects.
     */
    public List<ArticleRecord> findAvailable() {
        // Filter the articles based on availability and collect them into a new list
        return articles.stream().filter(ArticleRecord::isAvailable).collect(Collectors.toList());
    }

    /**
     * Saves a new article
     * Generates a random sequence number, creates a new ArticleRecord, and adds it to the articles list.
     *
     * @param info The new article.
     */
    public void save(ArticleInfo info) {
        // Generate a random ID and create a new ArticleRecord with the given title and availability set to true
        articles.add(new ArticleRecord(sequence.nextInt(), info.getTitle(), true));
    }
}
