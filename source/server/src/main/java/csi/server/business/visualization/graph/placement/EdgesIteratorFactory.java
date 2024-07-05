package csi.server.business.visualization.graph.placement;

import java.util.Iterator;

import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.data.util.FilterIterator;
import prefuse.util.collections.CompositeIterator;
import csi.shared.gwt.viz.graph.LinkDirection;

public class EdgesIteratorFactory {

	static private Predicate VISIBLE_FORWARD_PREDICATE = new DirectedVisibleEdgePredicate(LinkDirection.FORWARD);
	static private Predicate VISIBLE_REVERSE_PREDICATE = new DirectedVisibleEdgePredicate(LinkDirection.REVERSE);
	
	static private Predicate FORWARD_PREDICATE = new DirectedEdgePredicate(LinkDirection.FORWARD);
	static private Predicate REVERSE_PREDICATE = new DirectedEdgePredicate(LinkDirection.REVERSE);
	
	static public Iterator iterateVisibleOutEdges(Node node) {
		return new CompositeIterator(
				new FilterIterator(node.outEdges(), VISIBLE_FORWARD_PREDICATE),
				new FilterIterator(node.inEdges(), VISIBLE_REVERSE_PREDICATE));
	}
	
	static public Iterator iterateVisibleInEdges(Node node) {
		return new CompositeIterator(
				new FilterIterator(node.outEdges(), VISIBLE_REVERSE_PREDICATE),
				new FilterIterator(node.inEdges(), VISIBLE_FORWARD_PREDICATE));
	} 
	
	static public Iterator iterateOutEdges(Node node) {
		return new CompositeIterator(
				new FilterIterator(node.outEdges(), FORWARD_PREDICATE),
				new FilterIterator(node.inEdges(), REVERSE_PREDICATE));
	}
	
	static public Iterator iterateInEdges(Node node) {
		return new CompositeIterator(
				new FilterIterator(node.outEdges(), REVERSE_PREDICATE),
				new FilterIterator(node.inEdges(), FORWARD_PREDICATE));
	} 
}
