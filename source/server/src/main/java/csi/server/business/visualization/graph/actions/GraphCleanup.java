package csi.server.business.visualization.graph.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import csi.server.common.model.dataview.DataView;
import csi.server.util.CacheUtil;

public class GraphCleanup implements Callable<Void> {
   private DataView dataview;
   private DataSource dataSource;

   public GraphCleanup(DataView dataview) {
      this.dataview = dataview;
   }

   public synchronized DataSource getDataSource() {
      return dataSource;
   }

   public synchronized void setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
   }

   @Override
   public Void call() throws Exception {
      try (Connection connection = dataSource.getConnection()) {
         connection.setAutoCommit(false);

         try (Statement stmt = connection.createStatement()) {
            List<String> list = new ArrayList<String>();

            list.addAll(getDeleteCommands());

            String safeQuote = CacheUtil.toDbUuid(dataview.getUuid());

            for (String command : list) {
               String targetCommand = MessageFormat.format(command, safeQuote);

               stmt.addBatch(targetCommand);
            }
            stmt.executeBatch();
         }
         connection.commit();
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return null;
   }

   private Collection<? extends String> getDeleteCommands() {
      return Arrays.asList("DROP TABLE IF EXISTS Nodes_{0}",
                           "DROP TABLE IF EXISTS NodesXWalk_{0}",
                           "DROP TABLE IF EXISTS Links_{0}",
                           "DROP TABLE IF EXISTS LinksXWalk_{0}",
                           "DROP TABLE IF EXISTS GraphNodes_{0}",
                           "DROP TABLE IF EXISTS GraphLinks_{0}",
                           "DROP TABLE IF EXISTS NodeAttrs_{0}",
                           "DROP TABLE IF EXISTS NodePos_{0}",
                           "DROP TABLE IF EXISTS LinkAttrs_{0}");
   }
}
