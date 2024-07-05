package csi.server.business.visualization.graph;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.legend.GraphLinkLegendItem;

/**
 * Listener which updates the legend whenever the graph is changed. The update done here is for links.
 */
public class GraphLinkListener implements GraphListener {
   private static final Logger LOG = LogManager.getLogger(GraphLinkListener.class);

   private final int detailColumn;
    Map<String, Edge> index;
    Map<String, GraphLinkLegendItem> legend;

    public GraphLinkListener(Map<String, GraphLinkLegendItem> legendData, int detailColumn, Map<String, Edge> idEdgeIndex) {
        this.detailColumn = detailColumn;
        this.index = idEdgeIndex;
        this.legend = legendData;
    }

    @Override
    public void graphChanged(Graph g, String table, int start, int end, int col, int type) {
        boolean isLink = Graph.EDGES.equals(table);
        boolean isDetailUpdate = (col == detailColumn && type == EventConstants.UPDATE);
        Table linkTable = g.getEdgeTable();
        boolean schemaUpdate = (linkTable.getColumnCount() - 1) == col && EventConstants.INSERT == type && ((linkTable.getRowCount() - 1) == (end - start));

        if (isLink && !schemaUpdate) {
            boolean linkAdded = (start == end) && col == -1 && EventConstants.INSERT == type;
            Edge edge = g.getEdge(start);
            if (isDetailUpdate) {
                LinkStore details = GraphManager.getEdgeDetails(edge);
                if (LOG.isTraceEnabled()) {
                   LOG.trace("Indexing edge : " + details.getKey());
                }
                index.put(details.getKey(), edge);
                String edgeType = details.getType();
                if (edgeType != null && legend.containsKey(edgeType)) {
                    legend.get(edgeType).totalCount++;
                }

            } else if (linkAdded) {

            } else if (type == EventConstants.DELETE) {
                LinkStore details = GraphManager.getEdgeDetails(edge);
                if (LOG.isTraceEnabled()) {
                   LOG.trace("Drop link from index: " + details.getKey());
                }
                index.remove(details.getKey());
            }
        }
    }
}
