package csi.server.business.selection.storage.postgres;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BSON;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.mongodb.DBObject;

import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.visualization.graph.data.SQLTimeTransformer;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.dao.CsiPersistenceManager;

public class PostgresBroadcastStorageService extends AbstractBroadcastStorageService {
   private static final Logger LOG = LogManager.getLogger(PostgresBroadcastStorageService.class);

    public static final String BROADCAST_KEY = "broadcast";

    private static final String TABLE_NAME = "public.broadcaststorages";
    private static final String HAS_QUERY = "SELECT COUNT(*) FROM "+ TABLE_NAME +" WHERE visualizations_uuid = ?";
    private static final String DELETE_QUERY = "DELETE FROM "+ TABLE_NAME +" WHERE visualizations_uuid = ?";
    private static final String GET_BROADCAST_QUERY = "SELECT broadcast FROM "+ TABLE_NAME +" WHERE visualizations_uuid = ?";
    private static final String INSERT_STATEMENT = "INSERT INTO "+ TABLE_NAME +" VALUES (?,?)";

    public PostgresBroadcastStorageService() {
        initialize();
    }

    private void initialize() {
        BSON.addEncodingHook(java.sql.Time.class, new SQLTimeTransformer());
    }

   @Override
   public void addBroadcast(String vizUuid, IntegerRowsSelection selection, boolean excludeRows) {
      if (hasBroadcast(vizUuid)) {
         clearBroadcast(vizUuid);
      }
      if (selection.isCleared()) {
//        broadcastResult = BroadcastResult.EMPTY_BROADCAST_RESULT;
         // we do not want to save this broadcast, quit.
      } else {
         BroadcastResult broadcastResult = new BroadcastResult(selection, excludeRows);
         BroadcastToDataTransformer transformer = new BroadcastToDataTransformer();
         DBObject object = transformer.apply(broadcastResult);
         PostgresBroadcastStorage storage = createEmptyStorage(vizUuid);

         storage.addResult(object);

         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();
               long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
               LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

               try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos)) {
                  PostgresBroadcastStorageBlob storageBlob = storage.getBlob();
                  out.writeObject(storageBlob);
                  byte[] buf = bos.toByteArray();

                  obj.write(buf, 0, buf.length);
                  obj.close();
               }
               try (PreparedStatement ps = conn.prepareStatement(INSERT_STATEMENT)) {
                  ps.setString(1, vizUuid);
                  ps.setLong(2, oid);
                  ps.executeUpdate();
               }
               conn.commit();
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
   public BroadcastResult getBroadcast(String vizUuid) {
      BroadcastResult result = BroadcastResult.NULL_BROADCAST;

      if (hasBroadcast(vizUuid)) {
         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();

               try (PreparedStatement ps = conn.prepareStatement(GET_BROADCAST_QUERY)) {
                  ps.setString(1, vizUuid);

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
                           Object o = ois.readObject();
                           PostgresBroadcastStorageBlob storageBlob = (PostgresBroadcastStorageBlob) o;

                           if (storageBlob != null) {
                              result = new DataToBroadcastTransformer().apply(storageBlob.getBroadcastResult());
                           }
                        }
                        conn.commit();
                     }
                  }
               }
            }
         } catch (CentrifugeException e) {
            LOG.error("Error getting connection. Creating new Broadcast.");
            LOG.error(e.getMessage(), e);
         } catch (SQLException e) {
            LOG.error("Error retrieving blob. Creating new Broadcast.");
            LOG.error(e.getMessage(), e);
         } catch (ClassNotFoundException e) {
            LOG.error("Error casting blob. Creating new Broadcast.");
            LOG.error(e.getMessage(), e);
         } catch (IOException e) {
            LOG.error("Error serializing blob into an object. Creating new Broadcast.");
            LOG.error(e.getMessage(), e);
         }
      }
      return result;
   }

   @Override
   public void clearBroadcast(String vizUuid) {
      try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
         conn.setAutoCommit(false);

         try (PreparedStatement ps = conn.prepareStatement(DELETE_QUERY)) {
            ps.setString(1, vizUuid);
            ps.execute();
         }
         conn.commit();
      } catch (CentrifugeException e) {
         LOG.error("Error getting connection.");
         LOG.error(e.getMessage(), e);
      } catch (SQLException e) {
         LOG.error("Delete broadcast of " + vizUuid +" failed.");
         LOG.error(e.getMessage(), e);
      }
   }

   @Override
   public void copy(String vizUuid, String targetUuid) {
      BroadcastResult broadcast = getBroadcast(vizUuid);

      if ((broadcast != BroadcastResult.EMPTY_BROADCAST_RESULT) && (broadcast != BroadcastResult.NULL_BROADCAST)) {
         addBroadcast(targetUuid, broadcast.getBroadcastFilter(), broadcast.isExcludeRows());
      }
   }

   @Override
   public boolean hasBroadcast(String vizUuid) {
      boolean result = false;

      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           PreparedStatement ps = conn.prepareStatement(HAS_QUERY)) {
         ps.setString(1, vizUuid);

         try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            result = rs.getInt(1) > 0;
         }
      } catch (CentrifugeException e) {
         LOG.error("Error getting connection.");
         LOG.error(e.getMessage(), e);
      } catch (SQLException e) {
         LOG.error("Could not perform a COUNT query for hasBroadcast.");
         LOG.error(e.getMessage(), e);
      }
      return result;
   }

   private static PostgresBroadcastStorage createEmptyStorage(String vizUuid) {
      return new PostgresBroadcastStorage();
   }
}
