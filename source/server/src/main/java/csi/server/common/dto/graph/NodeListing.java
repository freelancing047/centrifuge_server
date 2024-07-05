package csi.server.common.dto.graph;

import java.util.function.Predicate;

import com.google.gwt.user.client.rpc.IsSerializable;

import prefuse.data.Graph;
import prefuse.data.Node;

/*
 * This is not really a DTO per-se.  This serves as a wrapper for the Graph object, so that
 * we can defer construction of an arbitrarily large set of nodes into DTOs.  The corresponding
 * converter sends back a minimal set of attributes--id, key, label, type(s), visible neighbor count,
 * and flags representing whether a node is: hidden and bundled.
 */

public class NodeListing implements IsSerializable {
   protected Graph graph;
   protected Predicate<Node> filter;

   public NodeListing(Graph data, Predicate<Node> filter) {
      this.graph = data;
      this.filter = filter;
   }

   public Graph getGraph() {
      return graph;
   }

   public Predicate<Node> getFilter() {
      return filter;
   }
}
