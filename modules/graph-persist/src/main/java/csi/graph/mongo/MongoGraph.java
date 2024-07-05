package csi.graph.mongo;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.MultiGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

@SuppressWarnings("serial")
public class MongoGraph
    extends AbstractGraph<DBObject, DBObject>
    implements MultiGraph<DBObject, DBObject>
//                GraphStorage<DBObject, DBObject>

{
    
    public MongoGraph(Mongo mongo) throws UnknownHostException, MongoException {
        this.mongo = mongo;
        db = mongo.getDB("centrifuge");

        vertices = db.getCollection("unittest.vertices");
        edges = db.getCollection("unittest.edges");

    }

    protected Mongo        mongo;
    protected DB           db;
    protected DBCollection vertices;
    protected DBCollection edges;

    @Override
    public Collection<DBObject> getInEdges(DBObject vertex) {
        Object id = Helper.getMongoId(vertex);
        if (id == null) {
            return Collections.emptySet();
        }

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY);
        builder.is(id);
        DBObject query = builder.get();
        DBObject keys = Helper.Empty;

        DBCursor cursor = edges.find(query, keys);
        Collection<DBObject> edges = Helper.asCollection(cursor);
        return edges;
    }

    @Override
    public Collection<DBObject> getOutEdges(DBObject vertex) {
        Object id = Helper.getMongoId(vertex);
        if (id == null) {
            return Collections.emptySet();
        }

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY);
        builder.is(id);
        DBObject query = builder.get();
        DBObject keys = Helper.Empty;

        DBCursor cursor = edges.find(query, keys);
        Collection<DBObject> edges = Helper.asCollection(cursor);
        return edges;
    }

    @Override
    public Collection<DBObject> getPredecessors(DBObject vertex) {
        Set<DBObject> vertices = new HashSet<DBObject>();

        Collection<DBObject> edges = getInEdges(vertex);
        for (DBObject edge : edges) {
            Pair<DBObject> endpoints = getEndpoints(edge);
            vertices.add(endpoints.getFirst());
        }
        return vertices;
    }

    @Override
    public Collection<DBObject> getSuccessors(DBObject vertex) {
        Set<DBObject> vertices = new HashSet<DBObject>();
        Collection<DBObject> edges = getOutEdges(vertex);
        for (DBObject edge : edges) {
            Pair<DBObject> endpoints = getEndpoints(edge);
            vertices.add(endpoints.getSecond());
        }
        return vertices;
    }

    @Override
    public DBObject getSource(DBObject directed_edge) {
        Object id = Helper.getMongoId(directed_edge);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(true);
        DBObject keys = builder.get();

        DBObject edge = edges.findOne(query, keys);
        BasicBSONObject data = (BasicBSONObject) edge.get(Helper.INTERNAL);
        ObjectId vertexId = (ObjectId) data.get(Helper.SOURCE);

        DBObject vertex = vertices.findOne(vertexId);
        return vertex;
    }

    @Override
    public DBObject getDest(DBObject directed_edge) {
        Object id = Helper.getMongoId(directed_edge);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).is(true);
        DBObject keys = builder.get();

        DBObject edge = edges.findOne(query, keys);
        BasicBSONObject data = (BasicBSONObject) edge.get(Helper.INTERNAL);
        ObjectId vertexId = (ObjectId) data.get(Helper.SOURCE);

        DBObject vertex = vertices.findOne(vertexId);
        return vertex;
    }

    @Override
    public boolean isSource(DBObject vertex, DBObject edge) {
        DBObject source = getSource(edge);
        Object sourceId = Helper.getMongoId(source);
        Object vertexId = Helper.getMongoId(vertex);

        boolean isSource = sourceId.equals(vertexId);
        return isSource;
    }

    @Override
    public boolean isDest(DBObject vertex, DBObject edge) {
        DBObject dest = getDest(edge);
        Object destId = Helper.getMongoId(dest);
        Object vertexId = Helper.getMongoId(vertex);

        boolean isDest = destId.equals(vertexId);
        return isDest;
    }

    @Override
    public Pair<DBObject> getEndpoints(DBObject edge) {
        Object id = Helper.getMongoId(edge);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(true);
        builder.put(Helper.EDGE_TARGET_QUERY).is(true);
        DBObject keys = builder.get();

        DBObject object = edges.findOne(query, keys);

        BasicBSONObject data = (BasicBSONObject) object.get(Helper.INTERNAL);
        ObjectId sId = (ObjectId) data.get(Helper.SOURCE);
        ObjectId tId = (ObjectId) data.get(Helper.TARGET);

        List<ObjectId> ids = Lists.newArrayList(sId, tId);

        builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).in(ids);
        query = builder.get();

        DBCursor cursor = vertices.find(query);
        int count = cursor.count();

        Pair<DBObject> endpoints;
        if (count != 2) {
            endpoints = new Pair<DBObject>(null, null);
        } else {
            // ordering of results is not guaranteed. determine
            // if one is our source or not.
            DBObject one = cursor.next();
            DBObject two = cursor.next();

            ObjectId test = Helper.getMongoId(one);
            if (one.equals(test)) {
                endpoints = new Pair<DBObject>(one, two);
            } else {
                endpoints = new Pair<DBObject>(two, one);
            }
        }

        return endpoints;
    }

    @Override
    public Collection<DBObject> getEdges() {
        DBCursor cursor = edges.find();
        Collection<DBObject> data = new MongoLazyCollection(cursor);
        return data;
    }

    @Override
    public Collection<DBObject> getVertices() {
        DBCursor cursor = vertices.find();
        Collection<DBObject> data = new MongoLazyCollection(cursor);
        return data;
    }

    @Override
    public boolean containsVertex(DBObject vertex) {
        DBObject query = Helper.getQueryFor(vertex);
        DBObject exists = vertices.findOne(query);

        if (exists != null) {
            Helper.ensureMongoId(vertex, exists);
        }

        return (exists != null);
    }

    @Override
    public boolean containsEdge(DBObject edge) {
        return containsEdge( edge, null );
    }

    @Override
    public int getEdgeCount() {
        long count = edges.count();
        return  (int) count;
    }

    @Override
    public int getVertexCount() {
        long count = vertices.count();
        return  (int) count;
    }

    @Override
    public Collection<DBObject> getNeighbors(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(id);
        DBObject asSource = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).is(id);
        DBObject asTarget = builder.get();

        builder = QueryBuilder.start();
        builder.or(asSource, asTarget);
        DBObject query = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(true);
        builder.put(Helper.EDGE_TARGET_QUERY).is(true);
        DBObject keys = builder.get();

        DBCursor cursor = edges.find(query, keys);

        Set<ObjectId> neighborIds = new HashSet<ObjectId>();
        while (cursor.hasNext()) {
            DBObject edge = cursor.next();
            BasicBSONObject data = (BasicBSONObject) edge.get(Helper.INTERNAL);
            neighborIds.add((ObjectId) data.get(Helper.SOURCE));
            neighborIds.add((ObjectId) data.get(Helper.TARGET));
        }

        cursor.close();

        // we have the neighbor vertex ids...fetch the payloads
        builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).in(neighborIds);
        query = builder.get();

        cursor = vertices.find(query);
        Collection<DBObject> neighbors = Helper.asCollection(cursor);
        return neighbors;
    }

    @Override
    public Collection<DBObject> getIncidentEdges(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).is(id);
        DBObject asSource = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).is(id);
        DBObject asTarget = builder.get();

        builder = QueryBuilder.start();
        builder.or(asSource, asTarget);
        DBObject query = builder.get();

        DBCursor cursor = edges.find(query);
        Collection<DBObject> edges = Helper.asCollection(cursor);
        return edges;
    }

    @Override
    public boolean addVertex(DBObject vertex) {

        if (containsVertex(vertex)) {
            return false;
        }

        DBObject payload = Helper.copy(vertex, true);
        WriteResult result = vertices.save(payload);
        boolean hasError = Helper.hasError(result);
        
        if( !hasError ) {
            Helper.ensureMongoId(vertex, payload);
        }
        
        return hasError;
    }

    @Override
    public boolean removeVertex(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        WriteResult writeResult = vertices.remove(query);
        boolean results = Helper.hasError(writeResult);
        
        if( results ) {
            // also need to remove all edges associated with the vertex...
            builder = QueryBuilder.start();
            builder.put(Helper.EDGE_SOURCE_QUERY).is(id);
            DBObject source = builder.get();
            
            builder = QueryBuilder.start();
            builder.put(Helper.EDGE_TARGET_QUERY).is(id);
            DBObject target = builder.get();
            
            builder = QueryBuilder.start();
            builder.or(source, target);
            DBObject edgeQuery = builder.get();
            
            WriteResult remove = edges.remove(edgeQuery);
            results = Helper.hasError(remove);
        }
        
        return results;
    }

    @Override
    public boolean removeEdge(DBObject edge) {
        ObjectId id = Helper.getMongoId(edge);
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        WriteResult result = edges.remove(query);
        return Helper.hasError(result);
    }

    @Override
    public EdgeType getEdgeType(DBObject edge) {
        ObjectId id = Helper.getMongoId(edge);
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TYPE_QUERY);
        DBObject keys = builder.get();

        DBObject payload = edges.findOne(query, keys);
        BasicBSONObject data = (BasicBSONObject) payload.get(Helper.INTERNAL);
        String value = data.getString(Helper.EDGE_TYPE_QUERY);
        EdgeType edgeType = (value != null) ? EdgeType.valueOf(value) : EdgeType.DIRECTED;
        return edgeType;
    }

    @Override
    public EdgeType getDefaultEdgeType() {
        return EdgeType.DIRECTED;
    }

    @Override
    public Collection<DBObject> getEdges(EdgeType edge_type) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TYPE_QUERY).is(edge_type.toString());
        DBObject query = builder.get();

        DBCursor cursor = edges.find(query);
        Collection<DBObject> edges = Helper.asCollection(cursor);
        return edges;
    }

    @Override
    public int getEdgeCount(EdgeType edge_type) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TYPE_QUERY).is(edge_type.toString());
        DBObject query = builder.get();

        int count = edges.find(query).count();
        return count;
    }

    @Override
    public boolean addEdge(DBObject edge, Pair<? extends DBObject> endpoints, EdgeType edgeType) {

        if (edgeType == null) {
            edgeType = getDefaultEdgeType();
        }

        DBObject source = endpoints.getFirst();
        DBObject target = endpoints.getSecond();

        if (!containsVertex(source)) {
            addVertex(source);
        }

        if (!containsVertex(target)) {
            addVertex(target);
        }

        ObjectId sourceId = Helper.getMongoId(source);
        ObjectId targetId = Helper.getMongoId(target);

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add(Helper.SOURCE, sourceId);
        builder.add(Helper.TARGET, targetId);
        builder.add(Helper.EDGE_TYPE, edgeType.toString());

        DBObject payload = Helper.copy(edge, false);
        payload.put(Helper.INTERNAL, builder.get());

        WriteResult writeResult = edges.save(payload);

        boolean hasError = Helper.hasError(writeResult);
        if( !hasError ) {
            Helper.ensureMongoId(edge, payload);
            
        }
        
        return hasError;
    }


//    @Override
    public boolean containsEdge(DBObject edge, EdgeType edgeType) {
        DBObject query = Helper.getEdgeExistsQuery(edge, edgeType );
        DBObject exists = edges.findOne(query, Helper.OnlyMongoId);

        if (exists != null) {
            Helper.ensureMongoId(edge, exists);
        }

        boolean contains = exists != null;
        return contains;

    }

//    @Override
    public DBObject findVertex(DBObject vertex) {
        DBObject query = Helper.getQueryFor(vertex);
        DBObject result = vertices.findOne(query);
        return result;
    }

  

//    @Override
    public DBObject findEdge(DBObject edge) {
        DBObject query = Helper.getEdgeExistsQuery(edge, null);
        DBObject result = edges.findOne(query);
        return result;
    }

//    @Override
    public boolean updateVertex(DBObject vertex) {
        // TODO Auto-generated method stub
        return false;
    }

//    @Override
    public boolean updateEdge(DBObject edge) {
        // TODO Auto-generated method stub
        return false;
    }

}
