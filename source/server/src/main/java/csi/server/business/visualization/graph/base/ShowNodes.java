package csi.server.business.visualization.graph.base;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import csi.server.business.visualization.graph.GraphManager;

public class ShowNodes implements Callable<Void> {
   protected static final Logger LOG = LogManager.getLogger(ShowNodes.class);

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

    public Void call() throws Exception {
        Table nodeTable = graph.getNodeTable();

        for (int id : nodes) {
            if (!nodeTable.isValidRow(id)) {
               LOG.info("request to display a non-existent node with ID: " + id);
                continue;
            }

            Node node = graph.getNode(id);
            NodeStore nodeStore = GraphManager.getNodeDetails(node);
            nodeStore.setHidden(false);
        }

        graph.putClientProperty("dirty", true);

        return null;
    }

}
