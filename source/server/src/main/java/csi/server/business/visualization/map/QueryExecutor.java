package csi.server.business.visualization.map;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.Logger;

import csi.server.business.helper.QueryHelper;
import csi.server.dao.CsiPersistenceManager;

public class QueryExecutor {
   public static void execute(Logger logger, String query, ResultSetProcessor processor) {
      try (Connection conn = CsiPersistenceManager.getCacheConnection();
           Statement stmt = conn.createStatement();
           ResultSet rs = QueryHelper.executeStatement(stmt, query)) {
         processor.process(rs);
      } catch (MapCacheStaleException ignore) {
      } catch (Exception se) {
         if (logger != null) {
            logger.error(se);

            for (StackTraceElement stackTraceElement : se.getStackTrace()) {
               logger.error(stackTraceElement.toString());
            }
            logger.error("query: " + query);
         }
      } finally {
         CsiPersistenceManager.releaseCacheConnection();
      }
   }

   public interface ResultSetProcessor {
      void process(ResultSet rs) throws SQLException;
   }
}
