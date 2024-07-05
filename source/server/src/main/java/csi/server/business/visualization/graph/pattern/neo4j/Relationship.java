package csi.server.business.visualization.graph.pattern.neo4j;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class Relationship {
    public Relationship() {
    }

    public static Relationship get(String id) {
        return null;
    }

    public static Relationship create(URI fromNode, URI toNode, String type) {
        String properties = "";
        return create(fromNode, toNode, type, properties);
    }

    public static Relationship create(URI fromNode, URI toNode, String type, String properties) {
        return null;
    }

    public static boolean delete(String ID) {
        return false;
    }

    public static void getProperties(Relationship relationship, String properties) {
    }

    public static void setProperties(Relationship relationship, String properties) {
    }

    public static void getProperty(Relationship relationship, String propertyKey) {
    }

    public static void setProperty(Relationship relationship, String propertyKey, String propertyValue) {
    }

    public static List<Relationship> getRelationships(URI node) {
        return Lists.newArrayList();
    }

    public static List<Relationship> getIncomingRelationships(URI node) {
        return Lists.newArrayList();
    }

    public static List<Relationship> getOutgoingRelationships(URI node) {
        return Lists.newArrayList();
    }

    public static List<Relationship> getRelationships(URI node, Collection<String> types) {
        return Lists.newArrayList();
    }
}
