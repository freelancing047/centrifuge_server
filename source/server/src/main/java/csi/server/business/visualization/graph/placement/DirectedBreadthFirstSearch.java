package csi.server.business.visualization.graph.placement;

import java.util.Iterator;

import prefuse.data.Node;
import prefuse.data.util.BreadthFirstIterator;

/**
 * Specialized graph iterator for performing a breadth-first search against a graph. 
 * An instance of this iterator is specialized to inspect each node's outgoing links.
 *
 * As the multiple links are merged in one link direction is determined by inspecting the LinkStore.
 *  
 * Each link is queried to determine if it is visible; determined by
 * inspecting the LinkStore of the link to identify if a node is visible.
 * <p>
 * A node's visiblity is determined if it is explicitly hidden or if the node is a member of a bundle.
 * <p>
 * 
 * @author ciprian 
 */
public class DirectedBreadthFirstSearch extends BreadthFirstIterator {
	@Override
	protected Iterator getEdges(Node node) {
		return EdgesIteratorFactory.iterateVisibleOutEdges(node);
	} 
}
