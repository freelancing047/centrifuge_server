package csi.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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
 * 3.5 disables co-occurance and directed co-occurance matrix types, going
 * forward they will be converted to HEAT MAP type.
 *
 */
public class MatrixTypeMigrate implements CustomTaskChange {
   private static final Logger LOG = LogManager.getLogger(MatrixSortOrderMigrate.class);

   private int numberRowsFixed = 0;

   @Override
   public void execute(Database database) throws CustomChangeException {
      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           Statement stmt = conn.createStatement()) {
         Map<String,String> matrixMigrate = new HashMap<String,String>();
         String sql = "SELECT matrixtype, uuid FROM matrixsettings;";

         try (ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
               String matrixType = rs.getString("matrixtype");
               String uuid = rs.getString("uuid");

               if (matrixType.equals("CO_OCCURRENCE") || matrixType.equals("CO_OCCURRENCE_DIR")) {
                  matrixMigrate.put(uuid, "HEAT_MAP");
               }
            }
         }
         for (Map.Entry<String,String> entry : matrixMigrate.entrySet()) {
            sql = "UPDATE matrixsettings SET matrixtype = '" + entry.getValue() +
                  "' WHERE uuid = '" + entry.getKey() + "';";

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
      return "Fixed " + numberRowsFixed + " matrix definitions to be HEAT MAP ";
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
