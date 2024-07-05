package csi.server.business.visualization.graph.layout;

import java.awt.Point;
import java.util.Iterator;
import java.util.Vector;

import prefuse.action.layout.Layout;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.PrefuseLib;
import prefuse.visual.tuple.TableNodeItem;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.task.api.TaskController;

public class CircularLayout extends Layout {

    protected String m_nodeGroup;

    protected String m_edgeGroup;

    protected Vector<Ring> rings = new Vector<Ring>(); // ring info for Circular

    public CircularLayout(String group) {
        super(group);
        m_nodeGroup = PrefuseLib.getGroupName(group, Graph.NODES);
        m_edgeGroup = PrefuseLib.getGroupName(group, Graph.EDGES);
    }

    /**
     * @see prefuse.action.Action#run(double)
     */
    @SuppressWarnings("unchecked")
    public void run(double frac) {
        Iterator<Node> iter = m_vis.visibleItems(m_nodeGroup);
        while (iter.hasNext()) {
            // get NodeStore
            Node node = iter.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(node);

            nodeStore.initCircular();
        }

        iter = m_vis.visibleItems(m_nodeGroup);
        while (iter.hasNext()) {
            // get NodeStore
            Node node = iter.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(node);

            nodeStore.groupNeighbors(node);
        }

        NodeStore ring_master = identifyTangentNodes();
        circularPlacement(ring_master);
    }

    @SuppressWarnings("unchecked")
    private NodeStore identifyTangentNodes() {
        int nn = m_vis.getVisualGroup(m_nodeGroup).getTupleCount();
        int closest = nn;
        NodeStore winner = null;

        // find node with group that's about 1/2 number of nodes
        Iterator<Node> iter = m_vis.visibleItems(m_nodeGroup);
        while (iter.hasNext()) {
            // get NodeStore
            Node node = iter.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(node);

            // nodeStore.calcMaxGroup();
            if (nodeStore.getMaxGroup() > -1) {
                int maxTry = Math.abs(nodeStore.getMaxGroupSize() - (nn / 2));
                if (maxTry < closest) {
                    winner = nodeStore;
                    closest = maxTry;
                }
            }
        }

        if (winner != null) {
            // combine non max groups into one
            iter = m_vis.visibleItems(m_nodeGroup);
            while (iter.hasNext()) {
                // get NodeStore
                Node node = iter.next();
                NodeStore nodeStore = GraphManager.getNodeDetails(node);
                nodeStore.combineNonMaxGroups(winner, true);
            }

            // process winner
            winner.setRing(1);
            winner.setRingMaster(winner);
            winner.setGroupRing(winner.getMaxGroup(), 2, 0, false); // , graph->ringMinNodes,
            // graph->ringMasterMostConnected);
            winner.setGroupRing(winner.getNonMaxGroup(), 1, 0, false); // , graph->ringMinNodes,
            // graph->ringMasterMostConnected);
        }

        return winner;
    }

    @SuppressWarnings("unchecked")
    private void circularPlacement(NodeStore winner) {
        // int k, nn = nodes.size();

        if (winner == null) { // place all on one ring
            int nn = m_vis.size(m_nodeGroup);
            double radius = (60. * (nn < 4 ? 4 : nn)) / (2. * Math.PI);
            double angle = 0., astep = (2. * Math.PI) / nn;

            Ring ring = new Ring();
            ring.setRing(1);
            // set arbitrary ring master
            Node node = (Node) m_vis.visibleItems(m_nodeGroup).next();
            NodeStore master = GraphManager.getNodeDetails(node);
            ring.setRingMaster(master);
            ring.setNumMembers(nn);
            ring.setXCenter(400d);
            ring.setYCenter(400d);
            rings.add(ring);

            Iterator<Node> iter = m_vis.visibleItems(m_nodeGroup);
            while (iter.hasNext()) {
                TaskController.getInstance().checkForCancel();
                // get NodeStore
                node = iter.next();
                NodeStore nodeStore = GraphManager.getNodeDetails(node);

                Point point = new Point();
                point.setLocation(400. + (radius * Math.cos(angle)), 400. + (radius * Math.sin(angle)));
                nodeStore.setPosition(GraphConstants.eLayoutAlgorithms.circle, point);

                nodeStore.setRing(1);
                nodeStore.setRingMaster(master);
                angle += astep;
            }

        } else {
            int maxRing = 0;
            Iterator<Node> iter = m_vis.visibleItems(m_nodeGroup);
            while (iter.hasNext()) {
                TaskController.getInstance().checkForCancel();
                // get NodeStore
                Node node = iter.next();
                NodeStore nodeStore = GraphManager.getNodeDetails(node);
                if (nodeStore.getRing() > maxRing) {
                  maxRing = nodeStore.getRing();
               }
            }

            Point point = new Point();
            point.setLocation(400, 400);
            winner.setPosition(GraphConstants.eLayoutAlgorithms.circle, point);
            winner.setRadial(0d);
            winner.placeGroupRing(winner.getMaxGroup(), 2, maxRing, 25, false, this.rings);
            // graph->ringSubFactor, graph->ringSizeByRingNum);
            winner.placeGroupRing(winner.getNonMaxGroup(), 1, maxRing, 25, false, this.rings);
            // graph->ringSubFactor, graph->ringSizeByRingNum);
        }

        // transfer positions to Visualgraph's Node Table
        Iterator<TableNodeItem> iterator = m_vis.items(m_nodeGroup);
        while (iterator.hasNext()) {
            TaskController.getInstance().checkForCancel();
            TableNodeItem tni = iterator.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(tni);
            if (nodeStore.isDisplayable()) {
                Point point = nodeStore.getPosition(GraphConstants.eLayoutAlgorithms.circle);
                tni.setX(point.getX());
                tni.setY(point.getY());
            }
        }
    }
}
