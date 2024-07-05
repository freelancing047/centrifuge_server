package csi.graph.mongo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

import csi.graph.GraphStorage;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class MongoGraphStorage
    implements GraphStorage<DBObject, DBObject>
{

    protected Mongo mongo;
    protected DB db;
    protected DBCollection vertices;
    protected DBCollection edges;
    //CTWO-7105 Graph does not retain size when reloaded.
    protected DBCollection visualProperties;
    protected DBCollection spannedDocs;
    
    public MongoGraphStorage(Mongo mongo, String dbName, String graphName) {
        this.mongo = mongo;
        initialize( dbName, graphName );
    }
    
    protected void initialize(String dbName, String graphName) {
        db = mongo.getDB(dbName);
        vertices = db.getCollection(graphName+".vertices");
        edges = db.getCollection(graphName+".edges");
        //CTWO-7105 Graph does not retain size when reloaded.
        visualProperties = db.getCollection(graphName+".properties");
        spannedDocs = db.getCollection(graphName+".spanned");
    }

    @Override
    public boolean containsVertex(DBObject vertex) {
        DBObject query = Helper.generateQueryFor(vertex);
        DBObject exists = vertices.findOne(query);

        if (exists != null) {
            Helper.ensureMongoId(vertex, exists);
        }

        return (exists != null);
    }

    @Override
    public boolean addVertex(DBObject vertex) {
        if (containsVertex(vertex)) {
            return false;
        }

        boolean added = false;
        WriteResult writeResult = vertices.save(vertex);
        boolean hasError = Helper.hasError(writeResult);

        return !hasError;
    }

    @Override
    public boolean addSpanned(DBObject spanned) {
        if (containsSpanned(spanned)) {
            return false;
        }

         WriteResult writeResult = spannedDocs.save(spanned);
        boolean hasError = Helper.hasError(writeResult);

        return !hasError;
    }
    
    public boolean containsSpanned(DBObject spanned) {
        DBObject query = Helper.generateQueryFor(spanned);
        DBObject exists = spannedDocs.findOne(query);

        if (exists != null) {
            Helper.ensureMongoId(spanned, exists);
        }

        return (exists != null);
    }
    
    public boolean removeSpanned(DBObject spanned) {
        ObjectId id = Helper.getMongoId(spanned);

        if (id == null) {
        	spanned = findSpanned(spanned);
            if (spanned != null) {
                id = Helper.getMongoId(spanned);
            }
        }

        if (id == null) {
            // non-existent 
            return false;
        }

        DBObject query = Helper.generateQueryFor(spanned);

        WriteResult remoteResult = spannedDocs.remove(query);
        boolean ok = Helper.hasError(remoteResult);
        return ok;
    }
    
    public DBObject findSpanned(DBObject spanned) {
        DBObject query = Helper.generateQueryFor(spanned, true);
        DBObject result = spannedDocs.findOne(query);
        return result;
    }
    
    @Override
    public boolean removeVertex(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);

        if (id == null) {
            vertex = findVertex(vertex);
            if (vertex != null) {
                id = Helper.getMongoId(vertex);
            }
        }

        if (id == null) {
            // non-existent edge
            return false;
        }

        DBObject query = Helper.generateQueryFor(vertex);

        WriteResult remoteResult = vertices.remove(query);
        boolean ok = Helper.hasError(remoteResult);
        if (ok) {
            // now we can remote edges as well
            DBObject edgeQuery = Helper.getEdgeQueryBuilder(id).get();
            WriteResult result = edges.remove(edgeQuery);

            if (Helper.hasError(result)) {
                // warn on failure to remove edges for vertex.
            }

        }
        return ok;
    }

    @Override
    public boolean updateVertex(DBObject vertex) {
        DBObject query = Helper.generateQueryFor(vertex);

        WriteResult result = vertices.update(query, vertex);
        boolean updated = Helper.hasSuccess(result);
        return updated;
    }

    @Override
    public DBObject findVertex(DBObject vertex) {
        DBObject query = Helper.generateQueryFor(vertex, true);
        DBObject result = vertices.findOne(query);
        return result;
    }

    @Override
    public boolean containsEdge(DBObject edge) {
        return containsEdge(edge, null);
    }

    @Override
    public boolean containsEdge(DBObject edge, EdgeType directed) {
        DBObject query = Helper.getEdgeExistsQuery(edge, directed);
        DBObject exists = edges.findOne(query);

        if (exists != null) {
            Helper.ensureMongoId(edge, exists);
        }

        return exists != null;
    }

    protected EdgeType getDefaultEdgeType() {
        return EdgeType.DIRECTED;
    }

    @Override
    public boolean addEdge(DBObject edge, Pair<? extends DBObject> endpoints, EdgeType edgeType) {
        if (edgeType == null) {
            edgeType = getDefaultEdgeType();
        }

        DBObject source = endpoints.getFirst();
        DBObject target = endpoints.getSecond();

        
        /*
         * ctwo-7287 
         * Disabling vertex check for now.  In our usage of this storage service,
         * we always save the vertices of the graph, then follow up
         * with the edges.  We should never have a case where a graph's edge
         * references nodes that the graph doesn't know about.
         * 
         * This has a _very_ significant impact on performance; please test
         * results before resurrecting this code!
         */
//        if (!containsVertex(source)) {
//            addVertex(source);
//        }
//        if (!containsVertex(target)) {
//            addVertex(target);
//        }

        ObjectId sourceId = Helper.getMongoId(source);
        ObjectId targetId = Helper.getMongoId(target);


        DBObject payload = Helper.copy(edge, true);

        WriteResult writeResult = edges.save(payload);

        boolean hasError = Helper.hasError(writeResult);
        if (!hasError) {
            Helper.ensureMongoId(edge, payload);

        }

        return !hasError;
    }

    @Override
    public boolean removeEdge(DBObject edge) {

        DBObject query = Helper.generateQueryFor(edge);
        WriteResult result = edges.remove(query);
        boolean removed = Helper.hasSuccess(result);
        return removed;
    }

    @Override
    public boolean updateEdge(DBObject edge) {
        DBObject query = Helper.generateQueryFor(edge);

        WriteResult result = edges.update(query, edge);
        boolean updated = Helper.hasSuccess(result);
        return updated;
    }

    @Override
    public Collection<DBObject> getEndpoints(DBObject edge) {

        if (!Helper.hasEdgeData(edge)) {
            DBObject query = Helper.generateEdgeQueryFor(edge, false);
            edge = edges.findOne(query);
        }

        DBObject data = Helper.getInternalPayload(edge);
        ObjectId sid = (ObjectId) data.get(Helper.SOURCE);
        ObjectId tid = (ObjectId) data.get(Helper.TARGET);

        DBObject query = Helper.getAnyQuery(sid, tid);
        DBCursor cursor = vertices.find(query);
        int count = cursor.count();
        Pair<DBObject> endpoints;
        if (count != 2) {
            endpoints = new Pair<DBObject>(null, null);
        } else {
            DBObject one = cursor.next();
            DBObject two = cursor.next();
            if (one.equals(sid)) {
                endpoints = new Pair<DBObject>(one, two);
            } else {
                endpoints = new Pair<DBObject>(two, one);
            }
        }
        return endpoints;
    }

    @Override
    public EdgeType getEdgeType(DBObject edge) {
        if (!Helper.hasEdgeData(edge)) {
            DBObject query = Helper.generateEdgeQueryFor(edge, false);
            edge = edges.findOne(query);
        }

        EdgeType edgeType = Helper.getEdgeType(edge);
        if (edgeType == null) {
            edgeType = getDefaultEdgeType();
        }
        return edgeType;
    }

    @Override
    public Collection<DBObject> getInEdges(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        if (id == null) {
            vertex = findVertex(vertex);
            if (vertex != null) {
                id = Helper.getMongoId(vertex);
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("non-existent.vertex");
        }

        DBObject query = Helper.getInEdgeQuery(id);
        DBCursor cursor = edges.find(query);
        Collection<DBObject> edges = Helper.asCollection(cursor);
        return edges;
    }

    @Override
    public Collection<DBObject> getOutEdges(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        if (id == null) {
            vertex = findVertex(vertex);
            if (vertex != null) {
                id = Helper.getMongoId(vertex);
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("non-existent.vertex");
        }

        DBObject query = Helper.getOutEdgeQuery(id);
        DBCursor cursor = edges.find(query);
        Collection<DBObject> edges = Helper.asCollection(cursor);
        return edges;
    }

    @Override
    public DBObject findEdge(DBObject edge) {
        DBObject query = Helper.getEdgeExistsQuery(edge, null);
        DBObject result = edges.findOne(query);
        return result;
    }

    @Override
    public long getVertexCount() {
        long count = vertices.count();
        return (int) count;
    }

    @Override
    public long getEdgeCount() {
        long count = edges.getCount();
        return count;
    }

    @Override
    public long getEdgeCount(EdgeType edgeType) {
        if (edgeType != null) {
            QueryBuilder builder = QueryBuilder.start();
            builder.put(Helper.EDGE_TYPE_QUERY).is(edgeType.toString());
            DBObject query = builder.get();
            return edges.count(query);

        } else {
            return getEdgeCount();
        }
    }

    @Override
    public Collection<DBObject> getVertices() {
        DBCursor cursor = vertices.find();
        return new MongoLazyCollection(cursor);
    }

    @Override
    public Collection<DBObject> getEdges() {
        DBCursor cursor = edges.find();
        return new MongoLazyCollection(cursor);
    }

    @Override
    public Collection<DBObject> getEdges(EdgeType edgeType) {
        DBCursor cursor;
        if (edgeType != null) {
            QueryBuilder builder = QueryBuilder.start();
            builder.put(Helper.EDGE_TYPE_QUERY).is(edgeType.toString());
            DBObject query = builder.get();
            cursor = edges.find(query);
        } else {
            cursor = edges.find();
        }

        return new MongoLazyCollection(cursor);
    }

    @Override
    public Collection<DBObject> getNeighbors(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        if (id == null) {
            vertex = findVertex(vertex);
            if (vertex != null) {
                id = Helper.getMongoId(vertex);
            }
        }

        if (id == null) {
            throw new IllegalArgumentException("non-existent.vertex");
        }

        QueryBuilder builder = Helper.getEdgeQueryBuilder(id);
        DBObject query = builder.get();

        // TODO: optimization required to reduce data returned from query?
        // requires restricted returned keys to only edge.source and edge.target
        DBCursor cursor = edges.find(query);

        Set<ObjectId> ids = new HashSet<ObjectId>();
        while (cursor.hasNext()) {
            DBObject edge = cursor.next();
            ObjectId sid = Helper.getEdgeSource(edge);
            ObjectId tid = Helper.getEdgeTarget(edge);

            if (sid != null && !sid.equals(id)) {
                ids.add(sid);
            }

            if (tid != null && !tid.equals(id)) {
                ids.add(tid);
            }
        }

        query = Helper.getAnyQuery(ids.toArray(new ObjectId[0]));
        cursor = vertices.find(query);

        Collection<DBObject> result = Helper.asCollection(cursor);
        return result;

    }

    @Override
    public DBCursor queryVertices(DBObject query) {
        DBCursor cursor = vertices.find(query);
        return cursor;
    }

    @Override
    public DBCursor queryEdges(DBObject query) {
        DBCursor cursor = edges.find(query);
        return cursor;
    }

    /** Get the VisualProperty described in the specified DBObject
     * @param visualProperty - the DBObject describing the property to get
     * @return the retrieved visualProperty
     */
    public DBObject getVisualProperty(DBObject visualProperty) {
        DBObject query = Helper.generateQueryFor(visualProperty);
        DBObject result = visualProperties.findOne(query);
        return result;
    }

    /** Add the VisualProperty described in the specified DBObject to the collection
     * @param visualProperty the visualProperty to add
     */
    public void addVisualProperty(DBObject visualProperty) {
        DBObject payload = Helper.copy(visualProperty, true);

        WriteResult writeResult = visualProperties.save(payload);

        boolean hasError = Helper.hasError(writeResult);
        if (!hasError) {
            Helper.ensureMongoId(visualProperty, payload);

        }
    }

	@Override
	public void addOrUpdateVertex(DBObject data) {
		boolean added = addVertex(data);
		if (!added) {
		    updateVertex(data);
		}
	}

	@Override
	public void addOrUpdateEdge(DBObject data, Pair<DBObject> pair,
			EdgeType edgeType) {
        boolean added = addEdge(data, pair, edgeType);
        if (!added) {
            updateEdge(data);
        }
	}
}
