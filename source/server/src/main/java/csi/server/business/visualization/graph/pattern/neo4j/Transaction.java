package csi.server.business.visualization.graph.pattern.neo4j;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class Transaction {
    public Transaction() {
    }

    private static void sendCypherQuery(String query) {
        String txUri = Neo4jHelper.getRootUri() + "transaction/commit";
        WebResource resource = Client.create().resource(txUri);
        String payload = "{\"statements\" : [ {\"statement\" : \"" + query + "\"} ]}";
        ClientResponse response = (ClientResponse)((Builder)((Builder)resource.accept(new String[]{"application/json"}).type("application/json")).entity(payload)).post(ClientResponse.class);
        System.out.println(String.format("POST [%s] to [%s], status code [%d], returned data: " + System.getProperty("line.separator") + "%s", new Object[]{payload, txUri, Integer.valueOf(response.getStatus()), response.getEntity(String.class)}));
        response.close();
    }
}
