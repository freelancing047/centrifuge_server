package csi.server.business.visualization.graph.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.util.Index;
import csi.server.common.model.dataview.DataView;
import csi.server.util.CacheUtil;

public class LoadPrefuseGraphCommand implements Callable<Graph> {

    static final String NODE_QUERY = "SELECT node_id, node_key from Nodes_{0}";

    private static final String EDGE_QUERY = "SELECT link_id, source_id, target_id from Links_{0}";

    private DataSource dataSource;

    private DataView dataview;

    protected JdbcTemplate db;

    public LoadPrefuseGraphCommand(DataSource dataSource, DataView dataview) {
        this.dataSource = dataSource;
        this.dataview = dataview;

        db = new JdbcTemplate(dataSource);
    }

    @Override
    public Graph call() throws Exception {
        String nodeQuery = getNodeQuery();

        Graph graph = new Graph();

        Table nodeTable = graph.getNodeTable();
        nodeTable.addColumn("node_id", long.class);
        nodeTable.addColumn("node_key", String.class);

        db.query(nodeQuery, new NodeHandler(graph));

        nodeTable.index("node_id");

        Table edges = graph.getEdgeTable();
        edges.addColumn("edge_id", long.class);

        String edgeQuery = getEdgeQuery();

        LinkHandler linkHandler = new LinkHandler(graph);
        db.query(edgeQuery, linkHandler);

        return graph;
    }

    private String getNodeQuery() {
        String quotedUUID = CacheUtil.toDbUuid(dataview.getUuid());
        String query = MessageFormat.format(NODE_QUERY, quotedUUID);
        return query;
    }

    private String getEdgeQuery() {
        String quotedUUID = CacheUtil.toDbUuid(dataview.getUuid());
        String query = MessageFormat.format(EDGE_QUERY, quotedUUID);
        return query;
    }

    class NodeHandler implements RowCallbackHandler {

        private static final int NODE_ID_POS = 1;
        private static final int NODE_KEY_POS = 2;
        protected Graph graph;

        public NodeHandler(Graph graph) {
            this.graph = graph;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            long nodeId = rs.getLong(NODE_ID_POS);
            String key = rs.getString(NODE_KEY_POS);

            Node node = graph.addNode();
            node.set("node_id", nodeId);
            node.set("node_key", key);
        }
    }

    class LinkHandler implements RowCallbackHandler {

        static final int EDGE_ID_COL = 1;
        static final int SOURCE_COL = 2;
        static final int TARGET_COL = 3;

        protected Graph graph;
        protected Index nodeIndex;

        public int count = 0;

        public LinkHandler(Graph graph) {
            this.graph = graph;

            Table nodeTable = graph.getNodeTable();
            nodeIndex = nodeTable.getIndex("node_id");

        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {

            int source = nodeIndex.get(rs.getLong(SOURCE_COL));
            int target = nodeIndex.get(rs.getLong(TARGET_COL));

            int edge_id = graph.addEdge(source, target);
            Edge edge = graph.getEdge(edge_id);
            edge.set("edge_id", rs.getLong(EDGE_ID_COL));

            count++;
        }
    }
}
