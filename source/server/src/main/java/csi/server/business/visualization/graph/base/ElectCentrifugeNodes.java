package csi.server.business.visualization.graph.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;

public class ElectCentrifugeNodes {

    protected Graph _graph;

    protected float _factor;

    public ElectCentrifugeNodes(Graph graph) {
        _graph = graph;
        _factor = 1.75f;
    }

    public void setThreshold(float value) {
        _factor = value;
    }

    @SuppressWarnings("unchecked")
    public List<Node> getNodes() {

        List<Node> centrifugeNodes = new ArrayList<Node>();
        List<Node> componentElection = new ArrayList<Node>();

        componentElection.clear();

        Iterator<Node> nodes = _graph.nodes();

        int totalVisibleEdges = 0;
        int nodeCount = 0;

        while (nodes.hasNext()) {
            Node node = nodes.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(node);
            if (!nodeStore.isHidden()) {
                totalVisibleEdges += nodeStore.calcVisibleNeighbors(node);
                nodeCount++;
            }

        }

        // NB: protect against / 0 since all nodes are not visible!
        if (nodeCount <= 2) {
            return centrifugeNodes;
        }

        float threshold = ((_factor * totalVisibleEdges) / nodeCount);
        if (threshold < (1 / _factor)) {
            return centrifugeNodes;
        }

        nodes = _graph.nodes();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(node);

            if (nodeStore.isDisplayable()) {
                if (nodeStore.getNumVisibleNeighbors() > threshold) {
                    componentElection.add(node);
                }
            }
        }

        centrifugeNodes.addAll(componentElection);

        return centrifugeNodes;

    }

}
