package csi.server.business.visualization.graph.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import csi.server.common.model.visualization.graph.GraphConstants;

/*
 * Clears out the current selection and resets to the list provided.
 */
public class SelectNodes implements Callable<Void> {
   protected static final Logger LOG = LogManager.getLogger(SelectNodes.class);

    protected Graph graph;

    protected List<Integer> nodes;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public void setNodes(List<Integer> nodes) {
        this.nodes = nodes;
    }

    @SuppressWarnings("unchecked")
    public Void call() throws Exception {
        List<Node> selectionList = (List<Node>) graph.getClientProperty(GraphConstants.SELECTED_NODES);
        if (selectionList == null) {
            selectionList = new ArrayList<Node>();
            graph.putClientProperty(GraphConstants.SELECTED_NODES, selectionList);
        } else {
            selectionList.clear();
        }

        Table nodeTable = graph.getNodeTable();
        for (int id : nodes) {

            if (!nodeTable.isValidRow(id)) {
               LOG.info("request to display a non-existent node with ID: " + id);
                continue;
            }

            Node node = graph.getNode(id);
            selectionList.add(node);
        }

        return null;

    }

}
