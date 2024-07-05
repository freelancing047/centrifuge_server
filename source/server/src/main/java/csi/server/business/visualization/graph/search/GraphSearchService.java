package csi.server.business.visualization.graph.search;

import java.util.function.Predicate;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.common.dto.graph.search.GraphSearch;
import csi.server.common.dto.graph.search.GraphSearchResults;

public interface GraphSearchService {
   public GraphSearchResults search(Graph graph, GraphSearch search);
   public Predicate<Node> getNodeFilter();
   public void setNodeFilter(Predicate<Node> filter);
   public void setEdgeFilter(Predicate<Edge> filter);
}
