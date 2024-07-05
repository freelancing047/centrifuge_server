package csi.graph.mongo;

import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BSON;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBDecoder;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

import csi.config.Configuration;
import csi.config.MongoConfig;
import csi.graph.AbstractStorageService;
import csi.graph.GraphStorage;
import csi.server.business.visualization.graph.data.CsiDefaultDBDecoder;
import csi.server.business.visualization.graph.data.SQLTimeTransformer;

public class MongoStorageService extends AbstractStorageService {
   protected static final Logger LOG = LogManager.getLogger(MongoStorageService.class);

   protected Mongo  mongo;

    public MongoStorageService() {
        try {
            initialize();
        } catch (Throwable t) {
           LOG.warn("Failed to initialize Mongo Storage Service.\nPlease verify your configuration settings", t);
        }
    }

    private void initialize() throws MongoException, UnknownHostException {
        MongoConfig mongoConfig = Configuration.getInstance().getMongoConfig();
        String value = mongoConfig.getMongoUrl();

        DefaultDBDecoder.FACTORY =  new CsiDefaultDBDecoder.CsiDefaultFactory();

        MongoURI uri = new MongoURI(value);
        mongo = uri.connect();

        BSON.addEncodingHook(java.sql.Time.class, new SQLTimeTransformer());
    }

    @Override
   public void resetData(String id) {

        DB db = getCentrifugeDB();

        DBCollection vertices = db.getCollection(getVerticesName(id));
        DBCollection edges = db.getCollection(getEdgesName(id));

        DBCollection spanned = db.getCollection(getSpannedName(id));
        DBCollection visualProperties = db.getCollection(getPropertiesName(id));

        vertices.dropIndexes();
        edges.dropIndexes();
        spanned.dropIndexes();
        visualProperties.dropIndexes();

        vertices.drop();
        edges.drop();
        spanned.drop();
        visualProperties.drop();
    }

    protected String getEdgesName(String id) {
        return id + ".edges";
    }

    protected String getVerticesName(String id) {
        return id + ".vertices";
    }

    protected String getSpannedName(String id) {
        return id + ".spanned";
    }

    protected String getPropertiesName(String id) {
        return id + ".properties";
    }

    protected DB getCentrifugeDB() {
        return mongo.getDB("centrifuge");
    }

    @Override
   public void initializeData(String id) {

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add(Helper.APP_ID, true);
        DBObject indexKeys = builder.get();

        DB db = getCentrifugeDB();
        DBCollection vertices = db.getCollection(getVerticesName(id));
        vertices.ensureIndex(indexKeys, null, true);

    }

    @Override
   public boolean hasVisualizationData(String id) {
        DB db = getCentrifugeDB();
        DBCollection vertices = db.getCollection(getVerticesName(id));

        long count = vertices.count();
        return count > 0;
    }

    @Override
   public MongoGraphStorage getGraphStorage(String vizuuid) {
        return new MongoGraphStorage(mongo, "centrifuge", vizuuid);
    }

    /*
     * TODO: Not used yet. Leave this in the attic if we hit
     * the per doc size limit.
     */
    public MongoRowService getSupportingRowsService(String vizUuid) {
        return new MongoRowService(mongo, "centrifuge", vizUuid);
    }

    @Override
   public boolean copy(String from, String to) {
        DB db = getCentrifugeDB();

        String srcVertices = getVerticesName(from);
        String srcEdges = getEdgesName(from);
        String srcVisualProperties = getPropertiesName(from);
        String srcSpanned = getSpannedName(from);

        String destVertices = getVerticesName(to);
        String destEdges = getEdgesName(to);
        String destVisualProperties = getPropertiesName(to);
        String destSpanned = getSpannedName(to);

        String script = "function(s, d) {" +
        		"var src = db.getCollection(s);" +
        		"var dest = db.getCollection(d);" +
        		"src.find().forEach( function(doc) {" +
        		"dest.save( doc );" +
        		"});" +
        		"}";

        boolean ok = true;

        try {
            db.eval(script, srcVertices, destVertices);
            db.eval(script, srcEdges, destEdges);
            db.eval(script, srcVisualProperties, destVisualProperties);
            db.eval(script, srcSpanned, destSpanned);
        } catch (MongoException me) {
           LOG.warn("Failed to copy data: " + me.getMessage());
            ok = false;
        }
        return ok;
    }

	@Override
	public void saveGraphStorage(String vizuuid, GraphStorage<DBObject, DBObject> graphStorage) {
	}

    @Override
    public GraphStorage<DBObject, DBObject> createEmptyStorage(String vizUuid) {
        return new MongoGraphStorage(mongo, "centrifuge", vizUuid);
    }
}
