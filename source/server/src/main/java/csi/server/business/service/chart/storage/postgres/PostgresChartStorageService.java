package csi.server.business.service.chart.storage.postgres;

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
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BSON;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.mongodb.DBObject;

import csi.server.business.service.chart.storage.AbstractChartStorageService;
import csi.server.business.service.chart.storage.ChartStorage;
import csi.server.business.visualization.graph.data.SQLTimeTransformer;
import csi.server.common.exception.CentrifugeException;
import csi.server.dao.CsiPersistenceManager;

public class PostgresChartStorageService extends AbstractChartStorageService {
   private static final Logger LOG = LogManager.getLogger(PostgresChartStorageService.class);

    private static final String MAGIC_HASH_STRING = "31";
    private static final String HAS_QUERY = "SELECT COUNT(*) FROM public.chartstorages WHERE visualizations_uuid = ?";
    private static final String HAS_AT_QUERY = "SELECT COUNT(*) FROM public.chartstorages WHERE visualizations_uuid = ? AND drillkey = ?";
    private static final String DELETE_QUERY = "DELETE FROM public.chartstorages WHERE visualizations_uuid = ?";
    private static final String DELETE_AT_QUERY = "DELETE FROM public.chartstorages WHERE visualizations_uuid = ? AND drillkey = ?";
    private static final String GET_CHART_QUERY = "SELECT chartinfo FROM public.chartstorages WHERE visualizations_uuid = ? AND drillkey = ?";
    private static final String INSERT_STATEMENT = "INSERT INTO public.chartstorages VALUES (?,?,?)";

    //+ "ON CONFLICT (visualizations_uuid) DO UPDATE SET chart_info = ?";

    public PostgresChartStorageService() {
        initialize();
    }

    private void initialize() {
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
         LOG.error("Delete visualization of " + vizUuid +" failed.");
         LOG.error(e.getMessage(), e);
      }
   }

   @Override
   public void initializeData(String vizUuid) {
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
   public boolean hasVisualizationDataAt(String vizUuid, Integer drillKey){
      boolean result = false;

      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           PreparedStatement ps = conn.prepareStatement(HAS_AT_QUERY)) {
         ps.setString(1, vizUuid);
         ps.setInt(2, drillKey.intValue());

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
   public PostgresChartStorage getChartStorage(String vizUuid, Integer drillKey) {
      PostgresChartStorage result = null;

      if (hasVisualizationData(vizUuid)) {
         PostgresChartStorageBlob storageBlob = null;

         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();

               try (PreparedStatement ps = conn.prepareStatement(GET_CHART_QUERY)) {
                  ps.setString(1, vizUuid);
                  ps.setInt(2, drillKey.intValue());

                  try (ResultSet rs = ps.executeQuery()) {
                     if (rs.next()) {
                        long oid = rs.getLong(1);
                        LargeObject obj = lobj.open(oid, LargeObjectManager.READ);

                        // Read the data
                        byte buf[] = new byte[obj.size()];
                        obj.read(buf, 0, obj.size());
                        obj.close();

                        // Do something with the data read here
                        try (ByteArrayInputStream bis = new ByteArrayInputStream (buf);
                             ObjectInputStream ois = new ObjectInputStream (bis)) {
                           Object o = ois.readObject();
                           storageBlob = (PostgresChartStorageBlob) o;

                           if (storageBlob != null) {
                              result = new PostgresChartStorage(storageBlob);
                           }
                        }
                        conn.commit();
                     }
                  }
               }
            }
         } catch (CentrifugeException e) {
             LOG.error("Error getting connection. Creating new Chart.");
             LOG.error(e.getMessage(), e);
         } catch (SQLException e) {
             LOG.error("Error retrieving blob. Creating new Chart.");
             LOG.error(e.getMessage(), e);
         } catch (ClassNotFoundException e) {
             LOG.error("Error casting blob. Creating new Chart.");
             LOG.error(e.getMessage(), e);
         } catch (IOException e) {
             LOG.error("Error serializing blob into an object. Creating new Chart.");
             LOG.error(e.getMessage(), e);
         }
      } else {
         result = new PostgresChartStorage();
      }
      return result;
   }

   @Override
   public void save(String vizUuid, Integer drillKey, ChartStorage<DBObject> chartStorage) {
      if (chartStorage instanceof PostgresChartStorage) {
         if (hasVisualizationDataAt(vizUuid, drillKey)) {
            resetDataAt(vizUuid, drillKey);
         }
         PostgresChartStorage storage = (PostgresChartStorage) chartStorage;

         try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
            if (conn.isWrapperFor(PGConnection.class)) {
               conn.setAutoCommit(false);

               PGConnection pgconn = conn.unwrap(PGConnection.class);
               LargeObjectManager lobj = pgconn.getLargeObjectAPI();
               long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
               LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

               try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = new ObjectOutputStream(bos)) {
                  PostgresChartStorageBlob storageBlob = storage.getBlob();
                  out.writeObject(storageBlob);
                  byte[] buf = bos.toByteArray();

                  obj.write(buf, 0, buf.length);
                  obj.close();
               }
               try (PreparedStatement ps = conn.prepareStatement(INSERT_STATEMENT)) {
                  ps.setString(1, vizUuid);
                  ps.setInt(2, drillKey.intValue());
                  ps.setLong(3, oid);
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
   public ChartStorage<DBObject> createEmptyStorage(String vizUuid) {
      return new PostgresChartStorage();
   }

   @Override
   public void resetDataAt(String vizUuid, Integer drillKey) {
      try (Connection conn = CsiPersistenceManager.getMetaConnection()) {
         conn.setAutoCommit(false);

         try (PreparedStatement ps = conn.prepareStatement(DELETE_AT_QUERY)) {
            ps.setString(1, vizUuid);
            ps.setInt(2, drillKey.intValue());
            ps.execute();
         }
         conn.commit();
      } catch (CentrifugeException e) {
         LOG.error("Error getting connection.");
         LOG.error(e.getMessage(), e);
      } catch (SQLException e) {
         LOG.error("Delete visualization of " + vizUuid +" failed.");
         LOG.error(e.getMessage(), e);
      }
   }

   @Override
   public int createDrillKey(List<String> drillDimensions) {
      return Objects.hash(drillDimensions, MAGIC_HASH_STRING);
   }
}
