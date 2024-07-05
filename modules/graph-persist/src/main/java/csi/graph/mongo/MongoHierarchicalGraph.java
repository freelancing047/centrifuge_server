package csi.graph.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

import csi.graph.HierarchicalGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

@SuppressWarnings("serial")
public class MongoHierarchicalGraph
    extends MongoGraph
    implements HierarchicalGraph<DBObject, DBObject>
{

    public MongoHierarchicalGraph(Mongo mongo) throws UnknownHostException, MongoException {
        super(mongo);
    }

    @Override
    public boolean addNode(DBObject child, DBObject parent) {
        // ensure we have the data for the child and parent...
        child = findVertex(child);
        parent = findVertex(parent);
        ObjectId pId = Helper.getMongoId(parent);
        boolean parentChanged = Helper.markAsContainer(parent);

        List<Object> parents = Helper.getAncestors(parent);

        List<Object> ancestors = new ArrayList<Object>(parents);
        ancestors.add(pId);
        
        DBObject childData = Helper.getInternalPayload(child);
        childData.put(Helper.GROUP_ID, pId);
        childData.put(Helper.ANCESTORS, ancestors);

        // fix up edges as well.
        // edges are added only if they are linked to a vertex outside of
        // the parent.
        Collection<DBObject> inEdges = this.getInEdges(child);
        Iterator<DBObject> iterator = inEdges.iterator();
        boolean edgeAdded = true;
        while (edgeAdded && iterator.hasNext()) {
            DBObject edge = iterator.next();
            DBObject edgeQuery = Helper.getQueryFor(edge);
            DBObject other = getOpposite(child, edge);
            if (isChildOf(parent, other)) {
                DBObject edgeData = Helper.getInternalPayload(edge);
                edgeData.put(Helper.GROUP_ID, parent);
                edges.update(edgeQuery, edge);
                continue;
            }

            DBObject metaEdge = this.findEdge(other, parent);
            if (metaEdge == null) {
                BasicDBObject newEdge = new BasicDBObject();
                Helper.markAsContainer(newEdge);
                EdgeType edgeType = Helper.getEdgeType(edge);
                edgeAdded = addEdge(newEdge, other, parent, edgeType);
                metaEdge = this.findEdge(newEdge);
            }

            ObjectId metaId = Helper.getMongoId(metaEdge);
            DBObject edgeData = Helper.getInternalPayload(edge);
            edgeData.put(Helper.GROUP_ID, metaId);

            edges.update(edgeQuery, edge);
        }

        boolean added = false;
        boolean parentUpdated = false;

        if (parentChanged) {
            DBObject query = Helper.getQueryFor(parent);
            WriteResult writeResult = vertices.update(query, parent);
            parentUpdated = !Helper.hasError(writeResult);
        }

        if (parentUpdated) {
            DBObject query = Helper.getQueryFor(child);
            WriteResult childResult = vertices.update(query, child);
            added = !Helper.hasError(childResult);
        }

        return added;
    }

    @Override
    public int getChildrenCount(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        DBObject query = getChildrenQuery(id);
        int count = vertices.find(query).count();
        return count;
    }

    private DBObject getChildrenQuery(ObjectId id) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.PARENT_QUERY).is(id);
        DBObject query = builder.get();
        return query;
    }

    @Override
    public int getDescendantCount(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        DBObject query = getDescendantQuery(id);
        int count = vertices.find(query).count();
        return count;
    }

    private DBObject getDescendantQuery(ObjectId id) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.ANCESTOR_QUERY).is(id);
        DBObject query = builder.get();
        return query;
    }

    @Override
    public DBObject getParent(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(id);
        DBObject query = builder.get();

        builder = QueryBuilder.start();
        builder.put(Helper.PARENT_QUERY).is(true);
        DBObject keys = builder.get();

        DBObject parent = vertices.findOne(query, keys);
        return parent;
    }

    @Override
    public Iterable<DBObject> getChildren(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);

        DBObject query = getChildrenQuery(id);
        DBCursor cursor = vertices.find(query);
        return cursor;
    }

    @Override
    public Iterable<DBObject> getDescendants(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        DBObject query = getDescendantQuery(id);

        DBCursor cursor = vertices.find(query);
        return cursor;
    }

    @Override
    public Iterable<DBObject> getInteriorEdges(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.PARENT_QUERY).is(id);
        DBObject query = builder.get();

        DBCursor cursor = edges.find(query);
        return cursor;
    }

    @Override
    public Iterable<DBObject> getExteriorEdges(DBObject vertex) {
        ObjectId id = Helper.getMongoId(vertex);
        DBObject parent = getParent(vertex);
        ObjectId cId = Helper.getMongoId(parent);

        QueryBuilder builder = Helper.getEdgeQueryBuilder(id);
        builder.put(Helper.PARENT_QUERY).notEquals(cId);
        DBObject query = builder.get();

        DBCursor cursor = edges.find(query);
        return cursor;
    }

    @Override
    public Iterable<DBObject> getMetaEdges() {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.META_QUERY).exists(true);
        DBObject query = builder.get();
        DBCursor cursor = edges.find(query);
        return cursor;
    }

    @Override
    public Iterable<DBObject> getMetaEdges(DBObject container) {
//        Collection<DBObject> edges = this.getIncidentEdges(container);
        ObjectId id = Helper.getMongoId(container);
        QueryBuilder builder = Helper.getEdgeQueryBuilder(id);
        builder.put(Helper.META_QUERY).exists(true);
        DBObject query = builder.get();
        
        DBCursor cursor = edges.find(query);
        return cursor;
    }

    @Override
    public int getMetaDegree(DBObject container) {
        DBCursor cursor = (DBCursor) this.getMetaEdges(container);
        int degree = cursor.size();
        return degree;
    }

    @Override
    public void removeMetaEdges(DBObject container) {
        
        // FIXME: should this resurrect any contained edges?
        
        ObjectId id = Helper.getMongoId(container);
        QueryBuilder builder = Helper.getEdgeQueryBuilder(id);
        DBObject query = builder.get();

        WriteResult writeResult = edges.remove(query);
        if (Helper.hasError(writeResult)) {
            // TODO log this
        }
    }

    @Override
    public Iterable<DBObject> getTopVertices() {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.PARENT_QUERY).exists(false);
        DBObject query = builder.get();

        DBCursor cursor = vertices.find(query);
        return cursor;
    }

    @Override
    public Iterable<DBObject> getNodes(int level) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.ANCESTORS).size(level);
        DBObject query = builder.get();

        DBCursor cursor = vertices.find(query);
        return cursor;
    }

    @Override
    public boolean isDescendant(DBObject vertex, DBObject descendant) {
        QueryBuilder builder = QueryBuilder.start();
        ObjectId id = Helper.getMongoId(vertex);
        ObjectId descendantId = Helper.getMongoId(descendant);

        if (id == null || descendantId == null) {
            return false;
        }

        builder.put(Helper.DOC_ID).is(descendantId);
        builder.put(Helper.ANCESTOR_QUERY).is(id);
        DBObject query = builder.get();

        DBObject results = vertices.findOne(query);

        boolean isDescendant = (results != null);
        return isDescendant;
    }

    @Override
    public boolean isAncestor(DBObject vertex, DBObject ancestor) {
        return isDescendant(ancestor, vertex);
    }

    @Override
    public boolean isParent(DBObject vertex, DBObject parent) {
        return isChildOf(parent, vertex);
    }

    @Override
    public int getHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLevelSize(int level) {
        
        // query should be any node that has 'level' ancestors
        // or no ancestors at all.
        
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.ANCESTOR_QUERY).size(level);
        DBObject hasAncestors = builder.get();
        
        builder = QueryBuilder.start();
        builder.put(Helper.PARENT_QUERY).exists(false);
        DBObject noAncestors = builder.get();
        
        builder = QueryBuilder.start();
        builder.or(hasAncestors, noAncestors);
        DBObject query = builder.get();
        
        int levelSize = vertices.find(query).count();
        return levelSize;
    }

    @Override
    public int getLevel(DBObject vertex) {
        vertex = findVertex(vertex);
        
        if( vertex == null ) {
            return -1;
        } else {
            List<Object> ancestors = Helper.getAncestors(vertex);
            return ancestors.size();
        }
    }

    @Override
    public void moveTo(DBObject vertex, DBObject container) {
        removeFromParent( vertex );
        addNode(vertex, container);
    }

    @Override
    public void removeFromParent(DBObject vertex) {
        
        // identify the vertex's container
        // 
        // find all edges for vertex that are not 'owned' by the container
        // this represents set of edges that have meta-edges wrapping them
        // fix up this set of edges by doing the following:
        //
        // get the containing meta-edge info.
        // search for an existing edge replacing the container with the current vertex
        // if an edge doesn't exist create a new metaEdge
        
        vertex = findVertex(vertex);
        DBObject container = getParent(vertex);
        Helper.clearContainer(vertex);
        Helper.copyAncestorTo( vertex, container );
        
        ObjectId id = Helper.getMongoId(vertex);
        ObjectId containerId = Helper.getMongoId(container);
        
        QueryBuilder builder = Helper.getEdgeQueryBuilder(id);
        builder.put(Helper.PARENT_QUERY).notEquals(containerId);
        DBObject externalQuery = builder.get();
        
        DBCursor cursor = edges.find(externalQuery);
        while( cursor.hasNext()) {
            DBObject edge = cursor.next();
            ObjectId existingMetaEdgeId  = Helper.getParentId(edge);
            if( existingMetaEdgeId == null ) {
                // shouldn't happen
                continue;
            }
            
            DBObject edgeQuery = Helper.getIdQuery( existingMetaEdgeId );
            
            Pair<DBObject> endpoints = getEndpoints(edgeQuery);
            Pair<DBObject> testQuery;
            if( endpoints.getFirst().equals(container)) {
                testQuery = new Pair<DBObject>(vertex, endpoints.getSecond());
            } else {
                testQuery = new Pair<DBObject>(endpoints.getFirst(), vertex);
            }
            
            DBObject existingEdge = findEdge(endpoints.getFirst(), endpoints.getSecond());
            if( existingEdge == null ) {
                existingEdge = new BasicDBObject();
                Helper.markAsContainer(existingEdge);
                EdgeType edgeType = Helper.getEdgeType(edge);
                addEdge(existingEdge, endpoints, edgeType );
            }
            
            if( Helper.hasEqualIds( edge, existingEdge) ) {
                DBObject metaEdge = findEdge(edgeQuery);
                Helper.setContainer(edge, Helper.getParentId(metaEdge));
            } else {
                Helper.setContainer(edge, Helper.getMongoId(existingEdge));
            }
            
            // remove the existing meta edge if there are no more edges referencing it as a parent
            builder = QueryBuilder.start();
            builder.put(Helper.PARENT_QUERY).is(existingMetaEdgeId);
            long refs = edges.count(builder.get());
            if( refs == 0 ) {
                removeEdge(edgeQuery);
            }
        }
        

        // find all edges from the vertex that are current internal to the
        // container.  we'll fix these up
        builder = Helper.getEdgeQueryBuilder(id);
        builder.put(Helper.PARENT_QUERY).is(containerId);
        cursor = edges.find(builder.get());
        while( cursor.hasNext()) {
            DBObject edge = cursor.next();
            
            
        }
        
        
    }
    
    public void removeFromContainer( DBObject[] vertices ) {
        
    }

    @Override
    public void group(DBObject container, DBObject[] vertices) {
        Set<DBObject> nodeUpdates = new HashSet<DBObject>();
        Set<DBObject> edgeUpdates = new HashSet<DBObject>();
        
        DBObject existing = findVertex(container);
        
        if( existing == null) {
            addVertex(container);
            container = findVertex(existing);
            Helper.markAsContainer(container);
            nodeUpdates.add(container);
        } else if(!Helper.isContainer( existing)) {
            // don't allow a simple vertex to be a container
            // TODO: msg key
            throw new IllegalStateException();
        }
        
        ObjectId containerId = Helper.getMongoId(container);
        
        Set<ObjectId> ids = new HashSet<ObjectId>();
        for (int i = 0; i < vertices.length; i++) {
            DBObject dbObject = vertices[i];
            ObjectId id = Helper.getMongoId(dbObject);
            ids.add(id);
        }

        
        // find edges that occur between the nodes that are 
        // in the container -- these are owned by the container
        DBCursor cursor = findIncidentEdges(ids);
        while( cursor.hasNext()) {
            DBObject edge = cursor.next();
            Helper.setContainer( edge, containerId );
            edgeUpdates.add(edge);
        }
        
        // find edges that 'reach out' of the container. 
        // for these edges we'll create a new 'meta-edge'
        // the meta-edge becomes the parent/owner of
        // these edges.  Note that multiple edges may have the
        // same parent in instances where two vertices are linked to the
        // same external vertex.
        
        
    }

    // Find the set of edges that exist between the provided vertices
    protected DBCursor findIncidentEdges(Collection<ObjectId> vertices) {
        
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).in(vertices);
        DBObject sourceQuery = builder.get();
        
        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).in(vertices);
        DBObject targetQuery = builder.get();
        
        builder = QueryBuilder.start();
        builder.put(Helper.AND).is( Lists.newArrayList(sourceQuery, targetQuery) );
        DBObject query = builder.get();
        
        DBCursor cursor = edges.find(query);
        return cursor;
    }
    
    // Finds edges for the set of vertices.  The set of edges are those
    // that do not connect between the provided vertices.  This
    // method provides the inverse behavior of findIncidentEdges.
    protected DBCursor findExternalEdges(Collection<ObjectId> vertices ) {
        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.EDGE_SOURCE_QUERY).in(vertices);
        builder.put(Helper.EDGE_TARGET_QUERY).notIn(vertices);
        DBObject asSource = builder.get();
        
        builder = QueryBuilder.start();
        builder.put(Helper.EDGE_TARGET_QUERY).in(vertices);
        builder.put(Helper.EDGE_SOURCE_QUERY).notIn(vertices);
        DBObject asTarget = builder.get();
        
        builder = QueryBuilder.start();
        builder.or(asSource, asTarget);
        DBObject query = builder.get();
        
        DBCursor cursor = edges.find( query );
        return cursor;
    }
    
    @Override
    public void ungroup(DBObject container) {
        // TODO Auto-generated method stub

    }

    protected boolean isChildOf(DBObject parent, DBObject child) {
        ObjectId parentId = Helper.getMongoId(parent);
        ObjectId childId = Helper.getMongoId(child);

        QueryBuilder builder = QueryBuilder.start();
        builder.put(Helper.DOC_ID).is(childId);
        builder.put(Helper.PARENT_QUERY).is(parentId);
        DBObject query = builder.get();
        DBObject keys = Helper.OnlyMongoId;

        DBObject result = vertices.findOne(query, keys);
        return result != null;
    }

}
