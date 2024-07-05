package csi.server.business.visualization.graph.placement;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;

public class VisibleEdgePredicate extends AbstractPredicate {
	public boolean getBoolean(Tuple t) {
        LinkStore ls = GraphManager.getEdgeDetails(t);
        // hack! need to get rid of the LinkStore/NodeStore for a more RDFish/direct tuple storage
        // if we don't have a link store, treat the link like as a standard unidirectional one!
       
        boolean visble = (ls == null) || ls.isDisplayable();
        
        return visble;
    }
}
