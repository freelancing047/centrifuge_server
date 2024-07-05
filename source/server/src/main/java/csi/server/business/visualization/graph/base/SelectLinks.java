package csi.server.business.visualization.graph.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Table;

import csi.server.common.model.visualization.graph.GraphConstants;

public class SelectLinks implements Callable<Void> {
   protected static final Logger LOG = LogManager.getLogger(SelectLinks.class);

    protected Graph graph;

    protected List<Integer> links;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<Integer> getLinks() {
        return links;
    }

    public void setLinks(List<Integer> links) {
        this.links = links;
    }

    @SuppressWarnings("unchecked")
    public Void call() throws Exception {

        List<Edge> selectionList = (List<Edge>) graph.getClientProperty(GraphConstants.SELECTED_LINKS);
        if (selectionList == null) {
            selectionList = new ArrayList<Edge>();
            graph.putClientProperty(GraphConstants.SELECTED_LINKS, selectionList);
        }

        Table edgeTable = graph.getEdgeTable();
        for (int id : links) {
            if (!edgeTable.isValidRow(id)) {
               LOG.info("request to select a non-existent link with ID: " + id);
                continue;
            }

            Edge edge = graph.getEdge(id);
            selectionList.add(edge);
        }

        return null;
    }
}
