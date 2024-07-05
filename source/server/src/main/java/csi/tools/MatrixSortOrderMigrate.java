package csi.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import csi.server.dao.CsiPersistenceManager;

/**
 * Change NULL measureSortOrders to empty string, which resolves into SortOrder.NONE enum
 */
public class MatrixSortOrderMigrate implements CustomTaskChange {
   private static final Logger LOG = LogManager.getLogger(MatrixSortOrderMigrate.class);

   private int numberRowsFixed = 0;

   @Override
   public void execute(Database database) throws CustomChangeException {
      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           Statement stmt = conn.createStatement()) {
         Set<String> nullSortOrders = new HashSet<String>();
         String sql = "SELECT measuresortorder, uuid FROM matrixsettings;";

         try (ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
               String mso = rs.getString("measuresortorder");
               String uuid = rs.getString("uuid");

               if ((mso == null) && (uuid != null)) {
                  nullSortOrders.add(uuid);
               }
            }
         }
         for (String currUuid : nullSortOrders) {
            sql = "UPDATE matrixsettings SET measuresortorder = '' WHERE uuid = '" + currUuid + "';";

            if (LOG.isDebugEnabled()) {
               LOG.debug(sql);
            }
            stmt.executeUpdate(sql);
            numberRowsFixed++;
         }
         conn.commit();
      } catch (Exception e) {
         LOG.error(e);
      } finally {
         CsiPersistenceManager.close();
      }
   }

   @Override
   public String getConfirmationMessage() {
      return "Fixed " + numberRowsFixed + " rows in matrixsettings that had a NULL measuresortorder ";
   }

   @Override
   public void setUp() throws SetupException {

   }

   @Override
   public void setFileOpener(ResourceAccessor resourceAccessor) {

   }

   @Override
   public ValidationErrors validate(Database database) {
      return null;
   }
}
