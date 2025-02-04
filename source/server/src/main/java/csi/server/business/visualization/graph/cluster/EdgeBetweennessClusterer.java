/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

package csi.server.business.visualization.graph.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * An algorithm for computing clusters (community structure) in graphs based on
 * edge betweenness. The betweenness of an edge is defined as the extent to
 * which that edge lies along shortest paths between all pairs of nodes.
 *
 * This algorithm works by iteratively following the 2 step process:
 * <ul>
 * <li>Compute edge betweenness for all edges in current graph
 * <li>Remove edge with highest betweenness
 * </ul>
 * <p>
 * Running time is: O(kmn) where k is the number of edges to remove, m is the
 * total number of edges, and n is the total number of vertices. For very sparse
 * graphs the running time is closer to O(kn^2) and for graphs with strong
 * community structure, the complexity is even lower.
 * <p>
 * This algorithm is a slight modification of the algorithm discussed below in
 * that the number of edges to be removed is parameterized.
 *
 * @author Scott White
 * @author Tom Nelson (converted to jung2)
 * @see "Community structure in social and biological networks by Michelle Girvan and Mark Newman"
 *      <p>
 *      This extension augments the original implementation of JUNG's
 *      EdgeBetweennessClusterer by allowing previous state to be re-used. The
 *      original implementation always recomputed the betweenness values for
 *      edges after each cut. Unfortunately given this behavior, there is no way
 *      to determine how many edges are required to be cut to get to the next
 *      step in cluster decomposition.
 *      <p>
 *      By consuming previously cut edges this implementation allows by-passing
 *      the previous <i>n</i> steps of betweenness computations.
 *
 */
public class EdgeBetweennessClusterer<V,E> implements GraphClusterer<V,E> {
   private int mNumEdgesToRemove;
   private Map<E,Pair<V>> edgesRemoved;
   private List<E> priorEdges;

   /**
    * Constructs a new clusterer for the specified graph.
    *
    * @param numEdgesToRemove the number of edges to be progressively removed from
    *                         the graph
    */
   public EdgeBetweennessClusterer(int numEdgesToRemove) {
      mNumEdgesToRemove = numEdgesToRemove;
      edgesRemoved = new LinkedHashMap<E,Pair<V>>();
      priorEdges = Collections.emptyList();
   }

   @Override
   public Set<Set<V>> apply(Graph<V,E> graph) {
      if ((mNumEdgesToRemove < 0) || (mNumEdgesToRemove > graph.getEdgeCount())) {
         throw new IllegalArgumentException("Invalid number of edges passed in.");
      }
      recoverPreviousState(graph);

      int iterations = computeIterationCount();

      for (int k = 0; k < iterations; k++) {
         BetweennessCentrality<V,E> bc = new BetweennessCentrality<V,E>(graph);
         E to_remove = null;
         double score = 0;

         for (E e : graph.getEdges()) {
            if (bc.getEdgeScore(e) > score) {
               to_remove = e;
               score = bc.getEdgeScore(e);
            }
         }
         edgesRemoved.put(to_remove, graph.getEndpoints(to_remove));
         graph.removeEdge(to_remove);
      }
      WeakComponentClusterer<V,E> wcSearch = new WeakComponentClusterer<V,E>();
      Set<Set<V>> clusterSet = wcSearch.transform(graph);

      for (Map.Entry<E,Pair<V>> entry : edgesRemoved.entrySet()) {
         Pair<V> endpoints = entry.getValue();

         graph.addEdge(entry.getKey(), endpoints.getFirst(), endpoints.getSecond());
      }
      return clusterSet;
   }

   /**
    * Finds the set of clusters which have the strongest "community structure". The
    * more edges removed the smaller and more cohesive the clusters.
    *
    * @param graph the graph
    */
   public Set<Set<V>> transform(Graph<V,E> graph) {
      throw new UnsupportedOperationException();

   }

   /*
    * Reconstitute the graph to a previous state. This involves either replaying
    * one or more of the prior edges up to the desired edges to remove.
    */
   protected void recoverPreviousState(Graph<V,E> graph) {
      edgesRemoved.clear();

      int count = Math.min(mNumEdgesToRemove, priorEdges.size());

      for (int i = 0; i < count; i++) {
         E edge = priorEdges.get(i);

         edgesRemoved.put(edge, graph.getEndpoints(edge));
         graph.removeEdge(edge);
      }
   }

   protected int computeIterationCount() {
      return (mNumEdgesToRemove - Math.min(mNumEdgesToRemove, priorEdges.size()));
   }

   public void setEdgesRemoved(List<E> edges) {
      this.priorEdges = edges;
   }

   /**
    * Retrieves the list of all edges that were removed (assuming extract(...) was
    * previously called). The edges returned are stored in order in which they were
    * removed.
    *
    * @return the edges in the original graph
    */
   public List<E> getEdgesRemoved() {
      return new ArrayList<E>(edgesRemoved.keySet());
   }
}
