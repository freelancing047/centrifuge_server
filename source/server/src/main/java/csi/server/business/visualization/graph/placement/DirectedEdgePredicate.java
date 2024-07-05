package csi.server.business.visualization.graph.placement;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.shared.gwt.viz.graph.LinkDirection;

/**
 * Predicate for checking is a link allow communication over the given direction.
 * 
 * @author ciprian
 */
public class DirectedEdgePredicate extends AbstractPredicate {
	
	LinkDirection direction;
	
	public DirectedEdgePredicate(LinkDirection direction) {
		super();
		this.direction = LinkDirection.revert(direction);
	}
	
    public boolean getBoolean(Tuple t) {
        LinkStore ls = GraphManager.getEdgeDetails(t);
        // if we don't have a link store, treat the link like as a standard unidirectional one!
        boolean forward = (ls == null || ls.getDirection() != direction); 
        return forward;
    }

}
