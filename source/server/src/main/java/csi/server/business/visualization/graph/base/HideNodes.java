/** 
 *  Copyright (c) 2009 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.business.visualization.graph.base;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import csi.server.business.visualization.graph.GraphManager;

/**
 * @author Centrifuge Systems, Inc.
 * 
 */
public class HideNodes implements Callable<Void> {
    protected static final Logger LOG = LogManager.getLogger(HideNodes.class);

    protected Graph graph;

    protected List<Integer> nodeIDs;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<Integer> getNodeIDs() {
        return nodeIDs;
    }

    public void setNodeIDs(List<Integer> nodeIDs) {
        this.nodeIDs = nodeIDs;
    }

    public Void call() throws Exception {
        Table nodeTable = graph.getNodeTable();

        for (int nodeID : nodeIDs) {
            if (!nodeTable.isValidRow(nodeID)) {
               LOG.info("request to display a non-existent node with ID: " + nodeID);
                continue;
            }

            Node node = graph.getNode(nodeID);
            NodeStore nodeStore = GraphManager.getNodeDetails(node);
            nodeStore.setHidden(true);
        }

        graph.putClientProperty("dirty", true);

        return null;
    }
}
