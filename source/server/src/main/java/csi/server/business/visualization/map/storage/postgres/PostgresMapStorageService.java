package csi.server.business.visualization.map.storage.postgres;

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
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import csi.server.business.visualization.map.storage.AbstractMapStorageService;
import csi.server.business.visualization.map.storage.OutOfBandResources;
import csi.server.common.exception.CentrifugeException;
import csi.server.dao.CsiPersistenceManager;

public class PostgresMapStorageService extends AbstractMapStorageService {
   private static final Logger LOG = LogManager.getLogger(PostgresMapStorageService.class);

   private static final String TABLE_NAME = "public.mapstorages";
   private static final String HAS_QUERY = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE visualizations_uuid = ?";
   private static final String DELETE_QUERY = "DELETE FROM " + TABLE_NAME + " WHERE visualizations_uuid = ?";
   private static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " VALUES (?,?)";
   private static final String GET_QUERY = "SELECT mapinfo FROM " + TABLE_NAME + " WHERE visualizations_uuid = ?";

   public PostgresMapStorageService() {
      super();
   }

   @Override
   public void save(String vizuuid, OutOfBandResources resources) {
      if (hasVisualizationData(vizuuid)) {
         resetData(vizuuid);
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
               out.writeObject(resources);
               byte[] buf = bos.toByteArray();

               obj.write(buf, 0, buf.length);
               obj.close();
            }
            try (PreparedStatement ps = conn.prepareStatement(INSERT_STATEMENT)) {
               ps.setString(1, vizuuid);
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

   @Override
   public boolean hasVisualizationData(String vizUuid) {
      boolean result = false;

      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           PreparedStatement ps = conn.prepareStatement(HAS_QUERY)) {
         ps.setString(1, vizUuid);

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
   public OutOfBandResources get(String vizUuid) {
      OutOfBandResources outOfBandResources = null;

      if (hasVisualizationData(vizUuid)) {
         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();

               try (PreparedStatement ps = conn.prepareStatement(GET_QUERY)) {
                  ps.setString(1, vizUuid);

                  try (ResultSet rs = ps.executeQuery()) {
                     if (rs.next()) {
                        long oid = rs.getLong(1);
                        LargeObject obj = lobj.open(oid, LargeObjectManager.READ);

                        // Read the data
                        byte[] buf = new byte[obj.size()];
                        obj.read(buf, 0, obj.size());
                        obj.close();

                        // Do something with the data read here
                        try (ByteArrayInputStream bis = new ByteArrayInputStream(buf);
                             ObjectInputStream ois = new ObjectInputStream(bis)) {
                           Object o = ois.readObject();
                           outOfBandResources = (OutOfBandResources) o;
                        }
                        conn.commit();
                     }
                  }
               }
            }
         } catch (CentrifugeException e) {
            LOG.error("Error getting connection. Creating new Map.");
            LOG.error(e.getMessage(), e);
         } catch (SQLException e) {
            LOG.error("Error retrieving blob. Creating new Map.");
            LOG.error(e.getMessage(), e);
         } catch (ClassNotFoundException e) {
            LOG.error("Error casting blob. Creating new Map.");
            LOG.error(e.getMessage(), e);
         } catch (IOException e) {
            LOG.error("Error serializing blob into an object. Creating new Map.");
            LOG.error(e.getMessage(), e);
         }
      }
      return outOfBandResources;
   }
}
