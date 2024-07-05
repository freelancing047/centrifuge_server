package csi.server.business.visualization.graph.layout;

import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Graph;
import prefuse.data.SpanningTree;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.NodeItem;
import csi.server.business.visualization.graph.placement.DirectedSpanningTree;

public class DirectedNodeLinkTreeLayout extends NodeLinkTreeLayout{

	public DirectedNodeLinkTreeLayout(String group) {
		super(group);
	}

	@Override
	public NodeItem getLayoutRoot() {
        TupleSet ts = m_vis.getGroup(m_group);
        if ( ts instanceof Graph ) {
        	Graph g = (Graph)ts;
        	SpanningTree tree;
        	if (m_root == null) {
        		tree = new DirectedSpanningTree(g);
        	} else {
        		tree = new DirectedSpanningTree(g, m_root);
        	}
        	//the spanning tree will be used later
        	g.setSpanningTree(tree);
            return (NodeItem)tree.getRoot();
        } else {
            throw new IllegalStateException("This action's data group does" +
                    "not resolve to a Graph instance.");
        }
	}

	
}
