package csi.server.business.service.matrix.storage.postgres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
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

import csi.server.business.service.matrix.storage.AbstractMatrixCacheStorageService;
import csi.server.business.service.matrix.storage.MatrixCacheStorage;
import csi.server.business.visualization.graph.data.SQLTimeTransformer;
import csi.server.common.exception.CentrifugeException;
import csi.server.dao.CsiPersistenceManager;

public class PostgresMatrixCacheStorageService extends AbstractMatrixCacheStorageService {
   private static final Logger LOG = LogManager.getLogger(PostgresMatrixCacheStorageService.class);

   private static final String DELETE_QUERY = "DELETE FROM public.matrixstorages WHERE visualizations_uuid = ?";
   private static final String HAS_QUERY = "SELECT COUNT(*) FROM public.matrixstorages WHERE visualizations_uuid = ?";
   private static final String GET_QUERY = "SELECT matrixinfo FROM public.matrixstorages WHERE visualizations_uuid = ?";
   private static final String INSERT_STATEMENT = "INSERT INTO public.matrixstorages VALUES (?,?)";

   public PostgresMatrixCacheStorageService() {
      initialize();
   }

   private void initialize() {
      // not sure about the encoding just yet, we probably don't need it
      BSON.addEncodingHook(java.sql.Time.class, new SQLTimeTransformer());
   }

   @Override
   public void resetData(String vizUuid) {
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
         LOG.error("Delete visualization of " + vizUuid + " failed.");
         LOG.error(e.getMessage(), e);
      }
   }

   @Override
   public boolean hasVisualizationData(String vizuuid) {
      boolean result = false;

      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           PreparedStatement ps = conn.prepareStatement(HAS_QUERY)) {
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
      return result;
   }

   @Override
   public MatrixCacheStorage<DBObject> getMatrixCacheStorage(String vizuuid) {
      MatrixCacheStorage<DBObject> result = new PostgresMatrixCacheStorage();

      if (hasVisualizationData(vizuuid)) {
         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();

               try (PreparedStatement ps = conn.prepareStatement(GET_QUERY)) {
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
                           Object o = ois.readObject();
                           PostgresMatrixCacheBlob storageBlob = (PostgresMatrixCacheBlob) o;

                           if (storageBlob != null) {
                              result = new PostgresMatrixCacheStorage(storageBlob);
                           }
                        }
                        conn.commit();
                     }
                  }
               }
            }
         } catch (CentrifugeException e) {
            LOG.error("Error getting connection.");
            LOG.error(e.getMessage(), e);
         } catch (SQLException e) {
            LOG.error("Error retrieving blob.");
            LOG.error(e.getMessage(), e);
         } catch (InvalidClassException e) {
            resetData(vizuuid);
            LOG.warn("Cache invalid. Creating new Matrix");
         } catch (IOException e) {
            LOG.error("Error serializing blob into an object. Creating new Matrix.");
            LOG.error(e.getMessage(), e);
         } catch (ClassNotFoundException e) {
            LOG.error("Error casting blob. Creating new Matrix.");
            LOG.error(e.getMessage(), e);
         }
      } else {
         result = new PostgresMatrixCacheStorage();
      }
      return result;
   }

   @Override
   public void saveMatrixCacheStorage(String vizUuid, MatrixCacheStorage<DBObject> matrixCacheStorage) {
      if (matrixCacheStorage instanceof PostgresMatrixCacheStorage) {
         if (hasVisualizationData(vizUuid)) {
            resetData(vizUuid);
         }
         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();
               long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
               LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

               try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos)) {
                  PostgresMatrixCacheStorage storage = (PostgresMatrixCacheStorage) matrixCacheStorage;
                  PostgresMatrixCacheBlob storageBlob = storage.getBlob();
                  out.writeObject(storageBlob);
                  byte[] buf = bos.toByteArray();

                  obj.write(buf, 0, buf.length);
                  obj.close();
               }
               try (PreparedStatement ps = conn.prepareStatement(INSERT_STATEMENT)) {
                  ps.setString(1, vizUuid);
                  ps.setLong(2, oid);

                  LOG.trace(ps.toString());

                  ps.executeUpdate();
               }
               conn.commit();
            }
         } catch (CentrifugeException e) {
            LOG.error("Error getting connection.");
            LOG.error(e.getMessage(), e);
         } catch (SQLException e) {
            LOG.error("Error saving blob.");
            LOG.error(e.getMessage(), e);
         } catch (IOException e) {
            LOG.error("Error writing an object into a blob.");
            LOG.error(e.getMessage(), e);
         }
      }
   }

   @Override
   public MatrixCacheStorage<DBObject> createEmptyStorage(String vizUuid) {
      return new PostgresMatrixCacheStorage();
   }
}
