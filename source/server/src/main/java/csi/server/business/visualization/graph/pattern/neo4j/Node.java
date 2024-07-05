package csi.server.business.visualization.graph.pattern.neo4j;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Node {
    public static final String NODE = "node";
    private URI location;
    Set<Node.NodeProperty> properties = Sets.newHashSet();

    public Node(URI location) {
        this.location = location;
    }

    public static Node createNode() {
        return createNode("{}");
    }

    public static Node createNode(String properties) {
        if(Strings.isNullOrEmpty(properties)) {
            return null;
        } else {
            String nodeEntryPointUri = Neo4jHelper.getRootUri() + NODE;
            WebResource resource = Client.create().resource(nodeEntryPointUri);
            ClientResponse response = resource.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).entity(properties).post(ClientResponse.class);
            URI location = response.getLocation();
            response.close();
            return new Node(location);
        }
    }

    public static Node getNode(String id) {
        if(Strings.isNullOrEmpty(id)) {
            return null;
        } else {
            String nodeEntryPointUri = Neo4jHelper.getRootUri() + NODE + "/" + id;
            WebResource resource = Client.create().resource(nodeEntryPointUri);
            ClientResponse response = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
            if(response.getStatus() == 404) {
                return null;
            } else {
                try {
                    URI e = new URI(nodeEntryPointUri);
                    return new Node(e);
                } catch (Exception var5) {
                    return null;
                }
            }
        }
    }

    public static boolean deleteNode(String id) {
        if(Strings.isNullOrEmpty(id)) {
            return false;
        } else {
            String nodeEntryPointUri = Neo4jHelper.getRootUri() + NODE + "/" + id;
            WebResource resource = Client.create().resource(nodeEntryPointUri);
            ClientResponse response = resource.accept(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            return response.getStatus() == 204;
        }
    }

    public URI getLocation() {
        return this.location;
    }

    public void setLocation(URI location) {
        this.location = location;
    }

    public void setProperty(Node.NodeProperty property) {
        String key = property.getKey();
        String value = property.getValue();
        if(!Strings.isNullOrEmpty(key)) {
            if(value == null) {
                String nodeEntryPointUri = this.getLocation() + "/properties/" + key;
                WebResource resource = Client.create().resource(nodeEntryPointUri);
                ClientResponse response = resource.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
                if(response.getStatus() != 404) {
                    try {
                        new URI(nodeEntryPointUri);
                    } catch (Exception ignored) {
                    }

                }
            }
        }
    }

    public static void updateProperty(Node node) {
    }

    public static void getProperty(Node node) {
    }

    public static void deletaAllProperties(Node node) {
    }

    public static void deleteProperty(Node node) {
    }

    public void addProperty(String key, String value) {
        String propertyUri =  location + "/properties/" + key;
        WebResource resource = Client.create().resource(propertyUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).entity("\"" + value + "\"").put(ClientResponse.class);
        response.close();
    }

    public static class NodeProperty {
        private final Node node;
        private String key;
        private String value;

        public NodeProperty(Node node) {
            this.node = node;
        }

        public static Node.NodeProperty create(Node node, String key, String Value) {
            return null;
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class NodeLabel {
        String label;
        private Node node;

        public NodeLabel(Node node) {
            this.node = node;
        }
    }

    public void addLabel(String label){
        String propertyUri =  location + "/labels";
        WebResource resource = Client.create().resource(propertyUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).entity("\"" + label + "\"").post(ClientResponse.class);
        response.close();
    }
}
