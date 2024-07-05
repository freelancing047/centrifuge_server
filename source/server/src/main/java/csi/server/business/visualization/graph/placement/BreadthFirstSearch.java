package csi.server.business.visualization.graph.placement;

import java.util.Iterator;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.data.util.BreadthFirstIterator;
import prefuse.data.util.FilterIterator;

/**
 * Specialized graph iterator for performing a breadth-first search against a graph. An instance of this iterator is
 * specialized to inspect each node's links. Each link is queried to determine if it is visible; determined by
 * inspecting the LinkStore of the link to identify if a node is visible.
 * <p>
 * A node's visiblity is determined if it is explicitly hidden or if the node is a member of a bundle.
 * <p>
 * 
 * @author Tildenwoods
 * 
 */
public class BreadthFirstSearch extends BreadthFirstIterator {

	static private Predicate VISIBLE_PREDICATE = new VisibleEdgePredicate();

    /**
     * Overrides BreadthFirstIterator by eliminating edges that are not displayable. Determines which edges are
     * traversed for a given node.
     * 
     * @param n
     *            a node
     * @return an iterator over edges incident on the node
     */
    @SuppressWarnings("unchecked")
    protected Iterator<Edge> getEdges(Node n) {
		return new FilterIterator(n.edges(),VISIBLE_PREDICATE);
    }
}
