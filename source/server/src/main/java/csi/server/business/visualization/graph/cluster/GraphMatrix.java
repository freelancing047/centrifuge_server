package csi.server.business.visualization.graph.cluster;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 *
 * This class represents a (sparse) 2-dimensional matrix for a graph.
 * <p>
 * Places where this is really useful is when you need to perform matrix
 * operations on a graph, but are feeling lazy in having to track the node <-->
 * row/col index.
 * <p>
 * The resulting matrix that an instance of this class represents is a square n
 * by n matrix; where n represents the number of vertices in the graph.
 *
 * @param <V>
 * @param <E>
 */

public class GraphMatrix<V,E> extends SparseDoubleMatrix2D {
   protected Graph<V,E> graph;
   protected BiMap<V,Integer> nodeIndexMap;

   public GraphMatrix(Graph<V,E> graph) {
      super(graph.getVertexCount(), graph.getVertexCount());
      initialize(graph);
   }

   private void initialize(Graph<V,E> graph) {
      this.graph = graph;

      nodeIndexMap = HashBiMap.create(graph.getVertexCount());

      // build our node <--> index map first
      int id = 0;
      for (V v : graph.getVertices()) {
         Integer k = Integer.valueOf(id++);
         nodeIndexMap.put(v, k);
      }

      boolean isDirected = DirectedGraph.class.isAssignableFrom(graph.getClass());
      for (E edge : graph.getEdges()) {
         Pair<V> endpoints = graph.getEndpoints(edge);

         V first = endpoints.getFirst();
         V second = endpoints.getSecond();

         int i = nodeIndexMap.get(first).intValue();
         int j = nodeIndexMap.get(second).intValue();

         // TODO: add edge weights here...
         double weight = 1.0d;
         this.setQuick(i, j, weight);

         if (!isDirected) {
            this.setQuick(j, i, weight);
         }
      }
   }

   public V getNode(int index) {
      return nodeIndexMap.inverse().get(Integer.valueOf(index));
   }

   public V getNode(Integer index) {
      return nodeIndexMap.inverse().get(index);
   }
}
