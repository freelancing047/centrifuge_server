package csi.startup;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import csi.server.common.exception.CentrifugeException;
import csi.server.util.FileUtil;
import csi.server.util.JndiUtil;
import csi.server.util.SqlUtil;

public class HibernateAutoGenCacheInitializer extends AbstractCacheInitializer {
   private static final String METADB_RESOURCE_URL = "java:comp/env/jdbc/MetaDB";

   private File createUserTable;
   private File createUsers;

   public HibernateAutoGenCacheInitializer() {
      super();
      ddl = new File(dbroot, "ddl");
      this.createUserTable = new File(ddl, "CreateUserTables.sql");
      this.createUsers = new File(ddl, "CreateUsers.sql");
   }

   void createIfDontExist(Connection unused) throws CentrifugeException {
      // install the users if the db does not have it yet
      DataSource ds = JndiUtil.lookupResource(DataSource.class, METADB_RESOURCE_URL);

      try (Connection conn = ds.getConnection()) {
           DatabaseMetaData meta = conn.getMetaData();
         try (ResultSet tables = meta.getTables(null, null, "usersview", new String[] { "VIEW" })) {
            if (!tables.next()) {
               try (Statement stmt = conn.createStatement()) {
                  // we need to install the default users
                  LOG.info("Installing user tables");
                  String tablesql = FileUtil.readFile(createUserTable);
                  stmt.executeUpdate(tablesql);

                  LOG.info("Installing default users");
                  String usersql = FileUtil.readFile(createUsers);
                  stmt.executeUpdate(usersql);
               }
            }
            conn.commit();
         } catch (Exception e) {
            SqlUtil.quietRollback(conn);
            throw new CentrifugeException(e);
         }
      } catch (SQLException sqle) {
      }
      try (Connection conn = ds.getConnection()) {
         try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("create schema data");

            if (!conn.getAutoCommit()) {
               conn.commit();
            }
            LOG.info("Installed data schema.");
         } catch (Exception e) {
            SqlUtil.quietRollback(conn);
         }
      } catch (SQLException sqle) {
      }
   }
}
