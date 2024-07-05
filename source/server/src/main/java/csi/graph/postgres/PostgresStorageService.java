package csi.graph.postgres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BSON;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.mongodb.DBObject;

import csi.graph.AbstractStorageService;
import csi.graph.GraphStorage;
import csi.server.business.visualization.graph.data.SQLTimeTransformer;
import csi.server.common.exception.CentrifugeException;
import csi.server.dao.CsiPersistenceManager;

public class PostgresStorageService extends AbstractStorageService {
	private static final Logger LOG = LogManager.getLogger(PostgresStorageService.class);

	private ConcurrentHashMap<String, PostgresGraphStorage> tempStorage = new ConcurrentHashMap<String, PostgresGraphStorage>();

	public PostgresStorageService() {
        initialize();
    }

    private void initialize() {
        BSON.addEncodingHook(java.sql.Time.class, new SQLTimeTransformer());
    }

   @Override
	public void resetData(String vizuuid) {
		try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
		   tempStorage.remove(vizuuid);
			conn.setAutoCommit(false);

	      String sql = "DELETE FROM public.graphstorages WHERE visualizations_uuid = ?";

			try (PreparedStatement ps = conn.prepareStatement(sql)) {
			   ps.setString(1, vizuuid);
			   ps.execute();
			}
			conn.commit();
		} catch (CentrifugeException e) {
		   LOG.error("Error getting connection.");
		   LOG.error(e.getMessage(), e);
		} catch (SQLException e) {
		   LOG.error("Delete visualization of " + vizuuid +" failed.");
		   LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void initializeData(String vizuuid) {
	}

	@Override
	public boolean hasVisualizationData(String vizuuid) {
		boolean result = false;

		if (tempStorage.containsKey(vizuuid)) {
		   result = true;
		} else {
		   String sql = "SELECT COUNT(*) FROM public.graphstorages WHERE visualizations_uuid = ?";

		   try (Connection conn = CsiPersistenceManager.getCacheConnection();
		        PreparedStatement ps = conn.prepareStatement(sql)) {
		      ps.setString(1, vizuuid);

		      try (ResultSet rs = ps.executeQuery()) {
		         rs.next();
		         result = (rs.getInt(1) > 0);
		      }
		   } catch (CentrifugeException e) {
		      LOG.error("Error getting connection.");
		      LOG.error(e.getMessage(), e);
		   } catch (SQLException e) {
		      LOG.error("Could not perform a COUNT query.");
		      LOG.error(e.getMessage(), e);
		   }
		}
		return result;
	}

	@Override
	public PostgresGraphStorage getGraphStorage(String vizuuid) {
	   PostgresGraphStorage storage = null;

	   if (hasVisualizationData(vizuuid)) {
	      storage = tempStorage.get(vizuuid);

	      if (storage == null) {
	         GraphStorageBlob storageBlob = null;

	         try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
	            if (conn.isWrapperFor(PGConnection.class)) {
	               conn.setAutoCommit(false);

	               PGConnection pgconn = conn.unwrap(PGConnection.class);
	               LargeObjectManager lobj = pgconn.getLargeObjectAPI();
	               String sql = "SELECT graph_info FROM public.graphstorages WHERE visualizations_uuid = ?";

	               try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                  ps.setString(1, vizuuid);

	                  try (ResultSet rs = ps.executeQuery()) {
	                     if (rs.next()) {
	                        long oid = rs.getLong(1);
	                        LargeObject obj = lobj.open(oid, LargeObjectManager.READ);

	                        // Read the data
	                        byte buf[] = new byte[obj.size()];
	                        obj.read(buf, 0, obj.size());
	                        obj.close();

	                        // Do something with the data read here
	                        try (ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	                             ObjectInputStream ois = new ObjectInputStream(bis)) {
	                           storageBlob = (GraphStorageBlob) ois.readObject();
	                        }
	                     }
	                     conn.commit();
	                  }
	               }
	            }
	         } catch (CentrifugeException e) {
	            LOG.error("Error getting connection. Creating new Graph.");
	            LOG.error(e.getMessage(), e);
	         } catch (SQLException e) {
	            LOG.error("Error retrieving blob. Creating new Graph.");
	            LOG.error(e.getMessage(), e);
	         } catch (ClassNotFoundException e) {
	            LOG.error("Error casting blob. Creating new Graph.");
	            LOG.error(e.getMessage(), e);
	         } catch (IOException e) {
	            LOG.error("Error serializing blob into an object. Creating new Graph.");
	            LOG.error(e.getMessage(), e);
	         }
	         storage = (storageBlob == null) ? null : new PostgresGraphStorage(storageBlob);
	      }
		} else {
		   storage = new PostgresGraphStorage();
		}
	   return storage;
    }

	@Override
	public boolean copy(String srcVizUuid, String targetVizUuid) {
	   boolean result = false;
		GraphStorage<DBObject, DBObject> graphStorage = getGraphStorage(srcVizUuid);

		if (graphStorage != null) {
			saveGraphStorage(targetVizUuid, graphStorage);
			result = true;
		}
		return result;
	}

	@Override
	public void saveGraphStorage(String vizuuid, GraphStorage<DBObject, DBObject> graphStorage) {
		if (graphStorage instanceof PostgresGraphStorage) {
//			if (hasVisualizationData(vizuuid)) {
//				resetData(vizuuid);
//			}

			tempStorage.put(vizuuid, (PostgresGraphStorage) graphStorage);

			PostgresGraphStorage storage = (PostgresGraphStorage) graphStorage;

			try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
			   if (conn.isWrapperFor(PGConnection.class)) {
			      conn.setAutoCommit(false);

					PGConnection pgconn = conn.unwrap(PGConnection.class);
					LargeObjectManager lobj = pgconn.getLargeObjectAPI();
					long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
					LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

					try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
					     ObjectOutput out = new ObjectOutputStream(bos)) {
					   GraphStorageBlob storageBlob = storage.getGraphStorageBlog();

					   out.writeObject(storageBlob);
					   byte[] buf = bos.toByteArray();
					   obj.write(buf, 0, buf.length);
					   obj.close();
					}
		         String sql = "INSERT INTO public.graphstorages VALUES (?, ?)" +
		                        "ON CONFLICT (visualizations_uuid) DO UPDATE SET graph_info = ?";

		         try (PreparedStatement ps = conn.prepareStatement(sql)) {
					   ps.setString(1, vizuuid);
					   ps.setLong(2, oid);
					   ps.setLong(3, oid);
					   ps.executeUpdate();
					}
					conn.commit();
					tempStorage.remove(vizuuid);
			   }
			} catch (CentrifugeException e) {
			   LOG.error("Error getting connection.");
			   LOG.error(e.getMessage(), e);
			} catch (IOException e) {
			   LOG.error("Error writing an object into a blob.");
			   LOG.error(e.getMessage(), e);
			} catch (SQLException e) {
			   LOG.error("Error saving blob.");
			   LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public GraphStorage<DBObject, DBObject> createEmptyStorage(String vizUuid) {
	   return new PostgresGraphStorage();
	}
}
