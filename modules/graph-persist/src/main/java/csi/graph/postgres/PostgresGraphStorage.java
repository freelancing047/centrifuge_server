package csi.graph.postgres;

import java.util.Collection;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class PostgresGraphStorage 
    implements GraphStorage<DBObject, DBObject>
{
	private GraphStorageBlob storageBlob;
	
	public PostgresGraphStorage() {
        storageBlob = new GraphStorageBlob();
    }
	
	public PostgresGraphStorage(GraphStorageBlob storageBlob) {
		this.storageBlob = storageBlob;
	}
	
	public GraphStorageBlob getGraphStorageBlog() {
		return storageBlob;
	}

    @Override
    public boolean addVertex(DBObject vertex) {
    	ObjectId oid = (ObjectId) vertex.get(Helper.DOC_ID);
    	if (oid == null) {
    		vertex.put(Helper.DOC_ID, ObjectId.get());
    	}
        return storageBlob.getVertices().add(vertex);
    }

    @Override
    public boolean addSpanned(DBObject spanned) {
    	return true;
    }
    
    @Override
    public boolean removeVertex(DBObject vertex) {
        //This isn't working, DBObject does not resolve the vertices properly
		//return storageBlob.getVertices().remove(vertex);
        
        DBObject toRemove = null;
        for(DBObject dbVertex : storageBlob.getVertices()){
            if(vertex.get(Helper.DOC_ID).equals(dbVertex.get(Helper.DOC_ID))){
                toRemove = dbVertex;
                break;
            }
        }
        
        if(toRemove == null){
            return false;
        }
        
        return storageBlob.getVertices().remove(toRemove);
    }

	@Override
    public DBObject findSpanned(DBObject spanned) {
		// TODO Auto-generated method stub
		return new BasicDBList();
    }

    @Override
    public boolean updateVertex(DBObject vertex) {
    	return storageBlob.getVertices().add(vertex);
    }

    @Override
    public DBObject findVertex(DBObject vertex) {
    	return findDBObject(storageBlob.getVertices(), vertex);
    }
    
    private DBObject findDBObject(Set<DBObject> set, DBObject criteria) {
    	ObjectId criteriaObjectId = (ObjectId) criteria.get(Helper.DOC_ID);
    	for (DBObject member : set) {
    		ObjectId memberObjectId = (ObjectId) member.get(Helper.DOC_ID);
    		if (memberObjectId.equals(criteriaObjectId)) {
    			return member;
    		}
    	}
    	return null;
    }

    @Override
    public boolean addEdge(DBObject edge, Pair<? extends DBObject> endpoints, EdgeType edgeType) {
        DBObject payload = Helper.copy(edge, true);
        ObjectId oid = (ObjectId) payload.get(Helper.DOC_ID);
        if (oid == null) {
        	payload.put(Helper.DOC_ID, ObjectId.get());
        }
        return storageBlob.getEdges().add(payload);
    }

    @Override
    public boolean removeEdge(DBObject edge) {
		return storageBlob.getEdges().remove(edge);
    }

    @Override
    public boolean updateEdge(DBObject edge) {
    	return storageBlob.getEdges().add(edge);
    }

    @Override
    public Collection<DBObject> getVertices() {
    	return storageBlob.getVertices();
    }

    @Override
    public Collection<DBObject> getEdges() {
		return storageBlob.getEdges();
    }

	@Override
    public DBObject getVisualProperty(DBObject visualProperty) {
		return storageBlob.getVisualProperties().get(visualProperty.get(Helper.DOC_ID));
    }

	@Override
    public void addVisualProperty(DBObject visualProperty) {
		storageBlob.getVisualProperties().put(visualProperty.get(Helper.DOC_ID), visualProperty);
	}

	@Override
	public void addOrUpdateVertex(DBObject data) {
		addVertex(data);
	}

	@Override
	public void addOrUpdateEdge(DBObject data, Pair<DBObject> pair,
			EdgeType edgeType) {
		addEdge(data, pair, edgeType);
	}

	
	
	
	
	



    @Override
    public boolean containsVertex(DBObject vertex) {
    	DBObject result = findVertex(vertex);
    	return result == null;
    }
    
    public boolean containsSpanned(DBObject spanned) {
        return true;
    }
    
    @Override
    public boolean containsEdge(DBObject edge) {
    	DBObject result = findVertex(edge);
    	return result == null;
    }

    @Override
    public boolean containsEdge(DBObject edge, EdgeType directed) {
		// TODO Auto-generated method stub
		return false;
    }

    @Override
    public DBObject findEdge(DBObject edge) {
    	return findDBObject(storageBlob.getEdges(), edge);
    }
    

    @Override
    public Collection<DBObject> getEndpoints(DBObject edge) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public EdgeType getEdgeType(DBObject edge) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public Collection<DBObject> getInEdges(DBObject vertex) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public Collection<DBObject> getOutEdges(DBObject vertex) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public long getVertexCount() {
		// TODO Auto-generated method stub
		return 0;
    }

    @Override
    public long getEdgeCount() {
		// TODO Auto-generated method stub
		return 0;
    }

    @Override
    public long getEdgeCount(EdgeType edgeType) {
		// TODO Auto-generated method stub
		return 0;
    }

    @Override
    public Collection<DBObject> getEdges(EdgeType edgeType) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public Collection<DBObject> getNeighbors(DBObject vertex) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public DBCursor queryVertices(DBObject query) {
		// TODO Auto-generated method stub
		return null;
    }

    @Override
    public DBCursor queryEdges(DBObject query) {
		// TODO Auto-generated method stub
		return null;
    }
}
