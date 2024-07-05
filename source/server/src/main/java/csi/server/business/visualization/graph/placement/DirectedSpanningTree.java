package csi.server.business.visualization.graph.placement;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.SpanningTree;

/**
 * Specialized SpanningTree that take the direction of the links in consideration when is builded.
 * 
 * Direction is from root to the first level of nodes and so on. 
 * @author ciprian
 *
 */
public class DirectedSpanningTree extends SpanningTree {

	public DirectedSpanningTree(Graph g) {
		super(g, (Node)g.nodes().next());
	}
	
	public DirectedSpanningTree(Graph g, Node root) {
		super(g, root);
	}
	
	@Override
	protected Iterator getEdges(Node node) {
		return EdgesIteratorFactory.iterateOutEdges(node);
	} 

}
