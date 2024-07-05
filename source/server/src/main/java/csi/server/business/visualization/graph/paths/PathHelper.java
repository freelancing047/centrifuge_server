package csi.server.business.visualization.graph.paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.shared.gwt.viz.graph.LinkDirection;

public class PathHelper {

    private Set<Node> _rem_vertex_id_set = new HashSet<Node>();
    private Set<Edge> _rem_edge_set = new HashSet<Edge>();
    private Map<Node, Double> weights = new HashMap<Node, Double>();
    private Graph _graph;
    private boolean includeHidden = false;
    private boolean includeDirection = false;

    public static String INBOUND = "inbound";
    public static String OUTBOUND = "outbound";
    public static String ANY = "any";

    public PathHelper(Graph graph, boolean includeDirection, boolean includeHidden) {
        super();
        this._graph = graph;
        this.includeHidden = includeHidden;
        this.includeDirection = includeDirection;
    }

    public void setNodeWeight(Node node, double weight) {
        weights.put(node, weight);
    }

    public double getNodeWeight(Node node) {
        return weights.get(node);
    }

    public double getEdgeWeight(Node source, Node sink) {
        //System.out.println("Getting edge weight between " + PathHelper.getLabel(source) + " and " + PathHelper.getLabel(sink));
        if (_rem_vertex_id_set.contains(source) || _rem_vertex_id_set.contains(sink) || _rem_edge_set.contains(_graph.getEdge(source, sink)) || _rem_edge_set.contains(_graph.getEdge(sink, source))) {
            //System.out.println("DISCONNECTED.  source removed: " + _rem_vertex_id_set.contains(source) + ", sink removed: " + _rem_vertex_id_set.contains(sink) + ", edge removed: "+ _rem_edge_set.contains(_graph.getEdge(source, sink)));
            return Constants.DISCONNECTED;
        }

        return getTrueEdgeWeight(source, sink);
    }

    public double getTrueEdgeWeight(Node source, Node sink) {
        //System.out.println("Getting TRUE edge weight between " + PathHelper.getLabel(source) + " and " + PathHelper.getLabel(sink));
        Iterator<?> iter = source.edges();
        if (nodeVisible(source) && nodeVisible(sink)) {
            while (iter.hasNext()) {
                Edge e = (Edge) iter.next();
                if (edgeVisible(e)) {
                    LinkStore details = (LinkStore) e.get(GraphConstants.LINK_DETAIL);
                    LinkDirection edgeDirection = details.getDirection();

                    if (!includeDirection && ((e.getSourceNode() == sink) || (e.getTargetNode() == sink))) {
                        return 1.0d;
                    } else if (includeDirection) {
                        if ((edgeDirection == LinkDirection.BOTH) || (edgeDirection == LinkDirection.NONE)) {
                            return 1.0d;
                        } else if ((edgeDirection == LinkDirection.FORWARD) && (e.getSourceNode() == source) && (e.getTargetNode() == sink)) {
                            return 1.0d;
                        } else if ((edgeDirection == LinkDirection.REVERSE) && (e.getTargetNode() == source) && (e.getSourceNode() == sink)) {
                            return 1.0d;
                        }
                    }
                }
            }
        }
        //System.out.println("TRULY DISCONNECTED.");
        return Constants.DISCONNECTED;
    }

    public static String getLabel(Node n) {
        if (n != null) {
            NodeStore details = (NodeStore) n.get(GraphConstants.NODE_DETAIL);
            return details.getLabel();
        }
        return "NULL";
    }

    public void remove_vertex(Node vertex_id) {
        //System.out.println("PathHelper.remove_vertex " + PathHelper.getLabel(vertex_id));
        _rem_vertex_id_set.add(vertex_id);
    }

    public void recover_removed_vertices() {
        //System.out.println("PathHelper.recover_removed_vertices");
        _rem_vertex_id_set.clear();
    }

    public void recover_removed_vertex(Node vertex_id) {
        //System.out.println("PathHelper.recover_removed_vertex");
        _rem_vertex_id_set.remove(vertex_id);
    }

    public void remove_edge(Node source, Node target) {
        //System.out.println("PathHelper.remove_edge between " + PathHelper.getLabel(source) + " and " + PathHelper.getLabel(target));
        Edge e = _graph.getEdge(source, target);
        if (e == null) {
            e = _graph.getEdge(target, source);
        }
        _rem_edge_set.add(e);
    }

    public void recover_removed_edges() {
        //System.out.println("PathHelper.recover_removed_edges");
        _rem_edge_set.clear();
    }

    public void recover_removed_edge(Node source, Node target) {
        //System.out.println("PathHelper.recover_removed_edge between " + PathHelper.getLabel(source) + " and " + PathHelper.getLabel(target));
        Edge e = _graph.getEdge(source, target);
        if (e == null) {
            e = _graph.getEdge(target, source);
        }
        _rem_edge_set.remove(e);
    }

    public Iterator<?> getNeighborNodes(Node n, String direction) {
        //System.out.println("getNeighborNodes for " + PathHelper.getLabel(n) + ", direction: " + direction);
        List<Node> visibleNodes = new ArrayList<Node>();
        if (!_rem_vertex_id_set.contains(n) && nodeVisible(n)) {
            Iterator<?> allEdges = n.edges();
            while (allEdges.hasNext()) {
                Edge e = (Edge) allEdges.next();
                if (!_rem_edge_set.contains(e) && edgeVisible(e)) {
                    LinkStore details = (LinkStore) e.get(GraphConstants.LINK_DETAIL);
                    LinkDirection edgeDirection = details.getDirection();
                    String linkDir = ANY;

                    //System.out.println("Edge: s: "+PathHelper.getLabel(e.getSourceNode())+", t: "+PathHelper.getLabel(e.getTargetNode())+", DIR: "+edgeDirection);
                    if (edgeDirection == LinkDirection.FORWARD) {
                        linkDir = (e.getSourceNode() == n) ? OUTBOUND : INBOUND;
                    } else if (edgeDirection == LinkDirection.REVERSE) {
                        linkDir = (e.getSourceNode() == n) ? INBOUND : OUTBOUND;
                    }

                    Node otherSide = e.getSourceNode();
                    if (otherSide == n) {
                        otherSide = e.getTargetNode();
                    }

                    NodeStore ns = GraphManager.getNodeDetails(otherSide);

                    if (!_rem_vertex_id_set.contains(otherSide) && nodeVisible(otherSide) && !ns.isBundled()) {
                        //System.out.println("Checking " + PathHelper.getLabel(otherSide) + ", inbound: " + linkDir);
                        if ((direction == null) || direction.equals(ANY)) {
                            //System.out.println("Adding " + PathHelper.getLabel(otherSide) + " NONE OR BOTH");
                            visibleNodes.add(otherSide);
                        } else if (direction.equals(linkDir) || linkDir.equals(ANY)) {
                            //System.out.println("Adding " + PathHelper.getLabel(otherSide) + " FOR " + direction);
                            visibleNodes.add(otherSide);
                        }
                    }
                }
            }
        }
        return visibleNodes.iterator();
    }

    private boolean nodeVisible(Node n) {
        NodeStore details = GraphManager.getNodeDetails(n);

        return includeHidden || !details.isHidden();
    }

    private boolean edgeVisible(Edge e) {
        LinkStore details = GraphManager.getEdgeDetails(e);

        return includeHidden || !details.isHidden();
    }
}
