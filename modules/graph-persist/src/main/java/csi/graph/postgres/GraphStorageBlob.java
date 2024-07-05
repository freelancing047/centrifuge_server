package csi.graph.postgres;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mongodb.DBObject;

public class GraphStorageBlob implements Serializable {
    protected Set<DBObject> vertices;
    protected Set<DBObject> edges;
    protected Map<Object, DBObject> visualProperties;

	public GraphStorageBlob() {
        vertices = new HashSet<DBObject>();
        edges = new HashSet<DBObject>();
        visualProperties = new HashMap<Object,DBObject>();
	}

	public Set<DBObject> getVertices() {
		return vertices;
	}

	public Set<DBObject> getEdges() {
		return edges;
	}

	public Map<Object, DBObject> getVisualProperties() {
		return visualProperties;
	}
}
