package csi.server.business.visualization.graph.cluster;

import java.util.Set;
import java.util.function.Function;

import edu.uci.ics.jung.graph.Graph;

public interface GraphClusterer<V,E> extends Function<Graph<V,E>,Set<Set<V>>> {
}
