package csi.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;

import csi.graph.mongo.Helper;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.MultiGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class MultiGraphWithStorage
    extends AbstractGraph<DBObject, DBObject>
    implements MultiGraph<DBObject, DBObject>
{
    
    protected GraphStorage<DBObject, DBObject> storage;

    public MultiGraphWithStorage(GraphStorage<DBObject, DBObject> storage) {
        this.storage = storage;
    }

    @Override
    public Collection<DBObject> getInEdges(DBObject vertex) {
        return storage.getInEdges(vertex);
    }

    @Override
    public Collection<DBObject> getOutEdges(DBObject vertex) {
        return storage.getOutEdges(vertex);
    }

    @Override
    public Collection<DBObject> getPredecessors(DBObject vertex) {
        Set<DBObject> preds = new HashSet<DBObject>();
        
        Collection<DBObject> edges = getInEdges(vertex);
        for (DBObject edge : edges) {
            ObjectId source = Helper.getEdgeSource(edge);
            DBObject query = Helper.getIdQuery(source);
            DBObject sourceVertex = storage.findVertex(query);
            preds.add(sourceVertex);
        }
        
        return preds;
    }

    @Override
    public Collection<DBObject> getSuccessors(DBObject vertex) {
        Set<DBObject> preds = new HashSet<DBObject>();
        
        Collection<DBObject> edges = getInEdges(vertex);
        for (DBObject edge : edges) {
            ObjectId target = Helper.getEdgeTarget(edge);
            DBObject query = Helper.getIdQuery(target);
            DBObject sourceVertex = storage.findVertex(query);
            preds.add(sourceVertex);
        }
        
        return preds;

    }

    @Override
    public DBObject getSource(DBObject edge) {
        edge = storage.findEdge(edge);
        ObjectId sourceId = Helper.getEdgeSource(edge);
        DBObject query = Helper.getIdQuery(sourceId);
        DBObject vertex = storage.findVertex(query);
        return vertex;
    }

    @Override
    public DBObject getDest(DBObject edge) {
        edge = storage.findEdge(edge);
        ObjectId target = Helper.getEdgeTarget(edge);
        DBObject query = Helper.getIdQuery(target);
        DBObject vertex = storage.findVertex(query);
        return vertex;
    }

    @Override
    public boolean isSource(DBObject vertex, DBObject edge) {
        DBObject source = getSource(edge);
        boolean equal = Helper.hasEqualIds(vertex, source);
        return equal;
    }

    @Override
    public boolean isDest(DBObject vertex, DBObject edge) {
        DBObject dest = getDest(edge);
        boolean equal = Helper.hasEqualIds(vertex, dest);
        return equal;
    }

    @Override
    public Pair<DBObject> getEndpoints(DBObject edge) {
        Collection<DBObject> endpoints = storage.getEndpoints(edge);
        if( endpoints instanceof Pair) {
            return (Pair<DBObject>)endpoints;
        } else {
            Iterator<DBObject> iterator = endpoints.iterator();
            return new Pair<DBObject>(iterator.next(), iterator.next());
        }
    }

    @Override
    public Collection<DBObject> getEdges() {
        Collection<DBObject> edges = storage.getEdges();
        return edges;
    }

    @Override
    public Collection<DBObject> getVertices() {
        Collection<DBObject> vertices = storage.getVertices();
        return vertices;
    }

    @Override
    public boolean containsVertex(DBObject vertex) {
        boolean contains = storage.containsVertex(vertex);
        return contains;
    }

    @Override
    public boolean containsEdge(DBObject edge) {
        boolean contains = storage.containsEdge(edge);
        return contains;
    }

    @Override
    public int getEdgeCount() {
        int count = (int) storage.getEdgeCount();
        return count;
    }

    @Override
    public int getVertexCount() {
        int count = (int) storage.getVertexCount();
        return count;
    }

    @Override
    public Collection<DBObject> getNeighbors(DBObject vertex) {
        Collection<DBObject> neighbors = storage.getNeighbors(vertex);
        return neighbors;
    }

    @Override
    public Collection<DBObject> getIncidentEdges(DBObject vertex) {
        Collection<DBObject> inEdges = storage.getInEdges(vertex);
        Collection<DBObject> outEdges = storage.getOutEdges(vertex);
        
        ArrayList<DBObject> incident = new ArrayList<DBObject>(inEdges.size()+outEdges.size());
        incident.addAll(inEdges);
        incident.addAll(outEdges);
        return incident;
    }

    @Override
    public boolean addVertex(DBObject vertex) {
        return storage.addVertex(vertex);
    }

    @Override
    public boolean removeVertex(DBObject vertex) {
        return storage.removeVertex(vertex);
    }

    @Override
    public boolean removeEdge(DBObject edge) {
        return storage.removeEdge(edge);
    }

    @Override
    public EdgeType getEdgeType(DBObject edge) {
        return storage.getEdgeType(edge);
    }

    @Override
    public EdgeType getDefaultEdgeType() {
        return EdgeType.DIRECTED;
    }

    @Override
    public Collection<DBObject> getEdges(EdgeType edge_type) {
        return storage.getEdges(edge_type);
    }

    @Override
    public int getEdgeCount(EdgeType edge_type) {
        return (int) storage.getEdgeCount(edge_type);
    }

    @Override
    public boolean addEdge(DBObject edge, Pair<? extends DBObject> endpoints, EdgeType edgeType) {
        return storage.addEdge(edge, endpoints, edgeType);
    }

}
