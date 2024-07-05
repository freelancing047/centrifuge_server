/*
 * Copyright Centrifuge Systems, Inc.  2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package sample;

import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;
import sample.model.TaskStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import csi.server.ws.rest.wire.RestRelGraph;


/**
 * The RestSampleApp class is a sample Java client implementation that using the Centrifuge client helper
 * classes, 3rd party library "Jersey" for HTTP access, and the Centrifuge Server RestAPI to illustrate how to
 * use this interface.
 * <br><br> 
 * 
 */
public class RestSampleApp
{
    
    /** The Constant DATA_VIEW. */
    private static final String DATA_VIEW = "dataView";
    
    /** The Constant REL_GRAPH. */
    private static final String REL_GRAPH = "relGraph";

    /** The client. */
    private Client              client;
    
    /** The client id. */
    private String              clientId;

    /** The session id. */
    private String              sessionId = "Fallback-Session-ID";

    /** The hostname. */
    private String              hostname;
    
    /** The port. */
    private int                 port;

    /** The username. */
    private String              username;
    
    /** The password. */
    private String              password;

    /** The created dataview id. */
    private String createdDataviewId  = "rest-sample-dataview-id";
    private String copiedDataviewId  = "rest-sample-dataview-id-copy";

    /** The created graph id. */
    private String       createdGraphId = "r34t-s4mple";
    private String       copiedGraphId = "r34t-s4mple-copy";

    /** The sample graph. */
    private RestRelGraph sampleGraph;

    /**
     * Run samples.
     *
     * @param hostname the hostname
     * @param port the port
     * @param username the username
     * @param password the password
     */
    public void runSamples(String hostname, int port, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;

        client = Client.create();
        clientId = UUID.randomUUID().toString();

        sessionId = login();

        runDataviewSample();
        runRelgraphSample();

        logout();
    }

    /**
     * Run dataview sample.
     */
    private void runDataviewSample() {
        System.out.println("Running Centrifuge REST Dataview Sample");

        readDataview();
        copyDataview();
    }

    /**
     * Run relgraph sample.
     */
    private void runRelgraphSample() {
        System.out.println("Running Centrifuge REST Relgraph Sample");

        readGraph();
        copyGraph();
    }

    // ------------------------------------------

    /**
     * Login.
     *
     * @return the string
     */
    private String login() {
        System.out.println("Logging into Centrifuge as " + username + "...");

        WebResource res = client.resource(RestSampleUtils.getBaseUrl(hostname, port) + "/login");
        
        res.header("Content-Type", "application/x-www-form-urlencoded");
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("j_username", username);
        formData.add("j_password", password);
        ClientResponse response = res.post(ClientResponse.class, formData);

        System.out.println("Logged in.");

        MultivaluedMap<String, String> headers = response.getHeaders();

        String cookie = headers.getFirst("Set-Cookie");
        if (cookie == null) {
            cookie = headers.getFirst("Cookie");
        }

        if (cookie != null) {
            int index = cookie.indexOf(";");
            if (index > -1) {
                cookie = cookie.substring(0, index);
            }
            index = cookie.indexOf(":");
            if (index > -1) {
                cookie = cookie.substring(index + 1).trim();
            }
            sessionId = cookie;
        }

        return sessionId;
    }

    /**
     * Logout.
     */
    private void logout() {
        System.out.println("Logging out of Centrifuge...");
        WebResource res = client.resource(RestSampleUtils.getBaseUrl(hostname, port) + "/logout");
        res.header("Cookie", sessionId);
        res.post();
        System.out.println("Logged out.");
    }


    /**
     * Read dataview.
     */
    private void readDataview() {
        System.out.println("Attempting to retrieve dataview from server...");
        Builder connection = createDataViewRequest(createdDataviewId, DATA_VIEW);
        TaskStatus response = connection.get(TaskStatus.class);

        RestSampleUtils.handleResponse(response);
    }
    /**
     * Copy dataview.
     */
    private void copyDataview() {
        System.out.println("Attempting to copy dataview ...");
        Builder connection = createDataViewCopyRequest(createdDataviewId, copiedDataviewId, DATA_VIEW);
        TaskStatus response = connection.post(TaskStatus.class);

        RestSampleUtils.handleResponse(response);
    }

    /**
     * Read graph.
     */
    private void readGraph() {
        System.out.println("Attempting to retrieve relationship graph from server...");
        Builder connection = createRelGraphRequest(createdDataviewId, createdGraphId, REL_GRAPH);
        TaskStatus response = connection.get(TaskStatus.class);

        RestSampleUtils.handleResponse(response);

        if (response != null && response.resultData != null && response.resultData.graph != null) {
            sampleGraph = response.resultData.graph;
        }
    }
    /**
     * Read graph.
     */
    private void copyGraph() {
        System.out.println("Attempting to copy relationship graph ...");
        Builder connection = createRelGraphCopyRequest(createdDataviewId, createdGraphId, copiedGraphId, REL_GRAPH);
        TaskStatus response = connection.post(TaskStatus.class);

        RestSampleUtils.handleResponse(response);

    }

    /**
     * Creates the request builder.
     *
     * @param graphUuid the graph uuid
     * @param apiType the api type
     * @return the builder
     */
    private Builder createRequestBuilder(WebResource res) {
        String taskId = RestSampleUtils.createUuid(); // Get a new task Id

        Builder builder = null;
        builder = res.getRequestBuilder();
        builder.accept("application/xml");
        builder.header("Content-Type", "application/xml");
        builder.header("Cookie", sessionId);
        builder.header("X-Client-Id", clientId);
        builder.header("X-Task-Id", taskId);
        builder.header("X-Csi-Options", "sync");

        return builder;
    }
    
    private Builder createDataViewRequest(String dataViewId, String apiType) {
        WebResource res = client.resource(RestSampleUtils.getBaseUrl(hostname, port) + "/" + apiType + "/" + dataViewId );
        
        return createRequestBuilder(res);
    }
    
    private Builder createDataViewCopyRequest(String dataViewId, String copyDataViewId, String apiType) {
        WebResource res = client.resource(RestSampleUtils.getBaseUrl(hostname, port) + "/" + apiType + "/" + dataViewId + "/" + copyDataViewId );
        
        return createRequestBuilder(res);
    }
    
    private Builder createRelGraphRequest(String dataViewId, String graphUuid, String apiType) {
        WebResource res = client.resource(RestSampleUtils.getBaseUrl(hostname, port) + "/" + apiType + "/" + dataViewId + "/" + graphUuid);
        
        return createRequestBuilder(res);
    }
    private Builder createRelGraphCopyRequest(String dataViewId, String graphUuid, String copyGraphUuid, String apiType) {
        WebResource res = client.resource(RestSampleUtils.getBaseUrl(hostname, port) + "/" + apiType + "/" + dataViewId + "/" + graphUuid + "/" + copyGraphUuid);
        
        return createRequestBuilder(res);
    }
}