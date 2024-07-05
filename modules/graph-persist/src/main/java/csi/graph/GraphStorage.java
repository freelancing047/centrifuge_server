package csi.graph;

import java.util.Collection;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public interface GraphStorage<V, E> {

    public boolean containsVertex(V vertex);
    public boolean addVertex(V vertex);
    public boolean removeVertex(V vertex);
    public boolean updateVertex(V vertex);
    public V findVertex(V vertex);

    public boolean containsEdge(E edge);
    public boolean containsEdge(E edge, EdgeType directed);
    
    public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType);
    public boolean removeEdge(E edge);
    public boolean updateEdge(E edge);
    public Collection<V> getEndpoints(E edge);
    public EdgeType getEdgeType(E edge);
    public Collection<E> getInEdges(V vertex);
    public Collection<E> getOutEdges(V vertex);
    public E findEdge(E edge);

    public long getVertexCount();
    public long getEdgeCount();
    public long getEdgeCount(EdgeType edgeType);

    public Collection<V> getVertices();
    public Collection<E> getEdges();
    public Collection<E> getEdges(EdgeType edgeType);
    public Collection<DBObject> getNeighbors(DBObject vertex);
    
    public DBCursor queryVertices(DBObject query);
    public DBCursor queryEdges(DBObject query);

	public boolean addSpanned(DBObject dbObject);
	public DBObject findSpanned(DBObject obj);
	public void addVisualProperty(DBObject dbObject);
	public DBObject getVisualProperty(DBObject dbObject);
	public void addOrUpdateVertex(DBObject data);
	public void addOrUpdateEdge(DBObject data, Pair<DBObject> pair, EdgeType edgeType);
}