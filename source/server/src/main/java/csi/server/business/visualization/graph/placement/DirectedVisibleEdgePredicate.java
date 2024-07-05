package csi.server.business.visualization.graph.placement;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.shared.gwt.viz.graph.LinkDirection;

/**
 * Predicate for checking is a link allow communication over the given direction
 * and if the link is visible.
 * 
 * @author ciprian
 */
public class DirectedVisibleEdgePredicate extends AbstractPredicate {

	LinkDirection direction;
	
	
	public DirectedVisibleEdgePredicate(LinkDirection direction) {
		super();
		this.direction = LinkDirection.revert(direction);
	}


	public boolean getBoolean(Tuple t) {
        LinkStore ls = GraphManager.getEdgeDetails(t);
        // hack! need to get rid of the LinkStore/NodeStore for a more RDFish/direct tuple storage
        // if we don't have a link store, treat the link like as a standard unidirectional one!
       
        boolean forwardAndVisble = (ls == null) || 
        						   (ls.getDirection()!=direction && ls.isDisplayable());
        
        return forwardAndVisble;
    }
}
