package csi.server.business.visualization.graph;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;

import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.TypeInfo;

public final class GraphModelListener implements GraphListener {
   private static final Logger LOG = LogManager.getLogger(GraphModelListener.class);

   private final int detailColumn;
    Map<String, Node> index;
    Map<String, TypeInfo> legend;

    public GraphModelListener(Map<String, TypeInfo> legendData, int detailColumn, Map<String, Node> idNodeIndex) {
        this.detailColumn = detailColumn;
        this.index = idNodeIndex;
        this.legend = legendData;
    }

    @Override
    public void graphChanged(Graph g, String table, int start, int end, int col, int type) {
        boolean isNode = Graph.NODES.equals(table);
        boolean isDetailUpdate = (col == detailColumn && type == EventConstants.UPDATE);
        Table nodeTable = g.getNodeTable();
        boolean schemaUpdate = (nodeTable.getColumnCount() - 1) == col && EventConstants.INSERT == type && ((nodeTable.getRowCount() - 1) == (end - start));

        if (isNode && !schemaUpdate) {
            boolean nodeAdded = (start == end) && col == -1 && EventConstants.INSERT == type;
            Node node = g.getNode(start);
            if (isDetailUpdate) {
                NodeStore details = GraphManager.getNodeDetails(node);
                if (LOG.isTraceEnabled()) {
                   LOG.trace("Indexing node : " + details.getKey());
                }
                index.put(details.getKey(), node);
                String nodeType = details.getType();
                if (nodeType != null && legend.containsKey(nodeType)) {
                    legend.get(nodeType).totalCount++;
                }

            } else if (nodeAdded) {

            } else if (type == EventConstants.DELETE) {
                NodeStore details = GraphManager.getNodeDetails(node);
                if (LOG.isTraceEnabled()) {
                   LOG.trace("Drop node from index: " + details.getKey());
                }
                index.remove(details.getKey());
            }
        }
    }
}
