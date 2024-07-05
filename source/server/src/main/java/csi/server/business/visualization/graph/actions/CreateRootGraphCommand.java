package csi.server.business.visualization.graph.actions;

import java.sql.Connection;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import csi.server.common.model.dataview.DataView;
import csi.server.util.CacheUtil;

public class CreateRootGraphCommand implements Callable<Void> {

   private static final String ROOT_NODES_SQL = "INSERT INTO GraphNodes_{0} ( graph_id, node_id ) \n" +
         "\t(SELECT 0, n.node_id FROM Nodes_{0} n)";

   private static final String ROOT_LINKS_SQL = "INSERT INTO GraphLinks_{0} ( graph_id, link_id )\n" +
         "\t(SELECT 0, l.link_id FROM Links_{0} l)";

   protected DataView dataview;
   protected DataSource dataSource;

   public CreateRootGraphCommand(DataSource dataSource, DataView dataview) {
      this.dataSource = dataSource;
      this.dataview = dataview;
   }

   @Override
   public Void call() throws Exception {
      String uuid = CacheUtil.toDbUuid(dataview.getUuid());

      try (Connection connection = dataSource.getConnection();
           Statement statement = connection.createStatement()) {
         String command = MessageFormat.format(ROOT_NODES_SQL, uuid);
         statement.addBatch(command);

         command = MessageFormat.format(ROOT_LINKS_SQL, uuid);
         statement.addBatch(command);

         statement.executeBatch();
         connection.commit();
      }
      return null;
   }
}
