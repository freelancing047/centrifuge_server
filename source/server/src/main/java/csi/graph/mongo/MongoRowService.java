package csi.graph.mongo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;

/*
 * Represents a service for supporting rows of graph data.
 * <p>
 * Supporting row data is stored in a single collection along-side
 * the vertex and edge collections i.e. following naming convention
 * of _rgUuid_.rows.  This data has the following JSON document structure:
 * 
 * <code>
 * {
 *      _id : row_id,
 *      node1_DocId : nodeDef1_Uuid,
 *      node2_DocId : nodeDef2_Uuid, ...
 *      nodeN_DocId : nodeDefN_Uuid,
 *      edge1_DocId : edgeDef1_Uuid, ...
 *      edgeM_DocId : edgeDefM_Uuid
 *  }
 *  
 * </code>
 *  
 *  The above structure enables the following queries:
 *      * find all rows for an object,
 *      * find all rows for an object defined by a specific definition
 *      * find all objects created on a set of rows
 *      
 *  This does not allow queries like: find all objects created on a subset of rows by 
 *  a specific definition.  This type of query must be done in two phases.
 *  
 *  <p>
 * 
 */
public class MongoRowService
{
    
    public static String getCollectionName(String vizUuid) {
        return vizUuid + ".rows";
    }

    
    protected Mongo mongo;
    protected DB db;
    protected DBCollection rowData;
    
    public MongoRowService(Mongo mongo, String dbName, String vizUuid) {
        this.mongo = mongo;
        db = mongo.getDB(dbName);
        String rowDataName = getCollectionName(vizUuid);
        rowData = db.getCollection(rowDataName);
    }

    /*
     * Find supporting rows for a given object.  This
     * method allows constraining the set of rows
     * to only those where a vertex/edge was defined
     * by a particular definition.
     */
    public Collection<Integer> getRows(Object docId, String defName) {
        QueryBuilder builder = QueryBuilder.start();
        
        
        builder.put(docId.toString());
        
        if( defName == null ) {
            builder.exists(true);
        } else {
            builder.is(defName);
        }
        
        DBObject query = builder.get();
        DBObject idOnly = getIdOnly();
        
        HashSet rowIds = new HashSet();
        
        DBCursor cursor = rowData.find(query, idOnly);
        while( cursor.hasNext()) {
            DBObject record = cursor.next();
            Integer row = (Integer) record.get(Helper.DOC_ID);
            rowIds.add(row);
        }
        
        return rowIds;
    }
    
    /*
     * Update row data for the graph.  The maps keys represent
     * the document Ids of the object (node, edge) and the
     * corresponding value is the definition which the object
     * was derived from.  
     * 
     */
    public void reportRow(Map<String, String> rowData) {
        
    }

    private DBObject getIdOnly() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add(Helper.DOC_ID, Boolean.TRUE);
        return builder.get();
    }
    
    

    
    
    


}
