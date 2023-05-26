package io.collective.restsupport

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair

/**
 * The RestTemplate class provides a simple HTTP client for making GET and POST requests to
 * remote endpoints.
 *
 * The class supports performing GET requests with optional query parameters, specifying the
 * desired response format using the "Accept" header, and retrieving the response as a String.
 * It also supports performing POST requests with the ability to set the "Content-type" header
 * and send data in the request body.
 *
 * @constructor Creates a new instance of the RestTemplate class.
 */
open class RestTemplate {

    /**
     * Perform a GET request to the specified endpoint with optional query parameters.
     *
     * @param endpoint The URL of the endpoint to send the GET request to.
     * @param accept The desired response format, specified using the "Accept" header.
     * @param pairs Optional query parameters to be appended to the endpoint URL.
     * @return The response from the server as a String.
     */
    open fun get(endpoint: String, accept: String, vararg pairs: BasicNameValuePair) = execute {
        val builder = URIBuilder(endpoint)
        // Add query parameters to the URI builder
        pairs.forEach { pair -> builder.addParameter(pair.name, pair.value) }
        HttpGet(builder.build()).apply {
            // Add the "Accept" header to specify the desired response format
            addHeader("Accept", accept)
        }
    }

    /**
     * Perform a GET request to the specified endpoint with optional query parameters.
     *
     * @param endpoint The URL of the endpoint to send the GET request to.
     * @param accept The desired response format, specified using the "Accept" header.
     * @param queryParams Optional query parameters to be appended to the endpoint URL.
     * @return The response from the server as a String.
     */
    fun post(endpoint: String, accept: String, data: String) = execute {
        // Create an HttpPost request with the endpoint URL
        HttpPost(endpoint).apply {
            // Add headers for the request, including "Accept" and "Content-type"
            addHeader("Accept", accept)
            // Add the "Content-type" header to specify the content type as JSON
            addHeader("Content-type", "application/json")
            // Set the request entity to the provided data
            entity = StringEntity(data)
        }
    }

    // Execute an HTTP request using the provided block to build the request
    open fun execute(block: () -> HttpUriRequest): String {
        // Create an HttpClients instance and execute the request, returning the response as a String
        return HttpClients.createDefault().execute(block(), BasicResponseHandler())
    }
}
