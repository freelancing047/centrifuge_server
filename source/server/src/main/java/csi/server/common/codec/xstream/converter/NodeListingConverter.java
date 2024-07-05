package csi.server.common.codec.xstream.converter;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.dto.graph.NodeListing;

public class NodeListingConverter implements Converter {
   private static final Logger LOG = LogManager.getLogger(NodeListingConverter.class);

   public class Tokens implements GraphMLReader.Tokens {
      public static final String BGCOLOR = "bgcolor";
      public static final String CSI_RELGRAPH_BACKGROUND_COLOR = "csi.relgraph.backgroundColor";
   }

   @Override
   public boolean canConvert(Class clazz) {
      return NodeListing.class.isAssignableFrom(clazz);
   }

   @Override
   public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
      NodeListing listing = (NodeListing) o;

      if (o != null) {
         Graph graph = listing.getGraph();
         Map<String, Node> idNodeIndex = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
         Predicate<Node> filter = listing.getFilter();

         openGraph(writer, graph);

         for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
            Node node = nodes.next();

            if (filter.test(node)) {
               try {
                  if (shouldEmitNode(node)) {
                     writeNode(writer, node, idNodeIndex);
                  }
               } catch (IllegalArgumentException iae) {
                  LOG.warn("Encountered transient error while writing node information.  Not all data may have been conveyed", iae);
               }
            }
         }
         closeGraph(writer, graph);
      }
   }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    private void openGraph(HierarchicalStreamWriter writer, Graph graph) {
        writer.startNode(Tokens.GRAPH);
    }

    private void closeGraph(HierarchicalStreamWriter writer, Graph graph) {
        writer.endNode();
    }

    private void writeNode(HierarchicalStreamWriter writer, Node node, Map<String, Node> idNodeIndex) {

        NodeStore details = GraphManager.getNodeDetails(node);

        writer.startNode(Tokens.NODE);
        writer.addAttribute(Tokens.ID, String.valueOf(node.getRow()));

        {
            writer.startNode("object");
            writer.addAttribute("type", "NodeStore");

            writeTag(writer, "key", details.getKey());
            writeTag(writer, "type", details.getType());
            if (details.isHidden()) {
                writeTag(writer, "hidden", "true");
            }

            if (details.isBundled()) {
                writeTag(writer, "bundled", "true");
            }

            String compositeLabel = details.getLabel();
            if (compositeLabel.length() > 0) {
                writeTag(writer, "label", compositeLabel);
            }

            writer.startNode("types");

            Iterator<Map.Entry<String, Integer>> iter = details.getTypes().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                writer.startNode(GraphMLWriter.Tokens.DATA);
                writer.addAttribute("type", entry.getKey());
                writer.setValue(entry.getValue().toString());
                writer.endNode();
            }
            writer.endNode();

            int nestedLevel = 0;
            AbstractGraphObjectStore myParent = details.getParent();
            while (myParent != null) {
                nestedLevel += 1;
                myParent = myParent.getParent();
            }

            writeTag(writer, "nestedLevel", ((Integer) nestedLevel).toString());

            int visibleNeighbors = 0;

            for (Iterator<Edge> edges = node.edges(); edges.hasNext();) {
               Edge edge = edges.next();

               if ((edge != null) && GraphContext.Predicates.IsEdgeVisualizedAndDisplayable.test(edge) &&
                   !GraphManager.getEdgeDetails(edge).isHidden()) {
                  visibleNeighbors++;
               }
            }
            writeTag(writer, "visibleNeighbors", Integer.valueOf(visibleNeighbors));

            writer.endNode();
        }

        writer.endNode();

        if (details.hasChildren()) {
            for (AbstractGraphObjectStore store : details.getChildren()) {
                Node child = idNodeIndex.get(store.getKey());
                writeNode(writer, child, idNodeIndex);

            }
        }

    }

    private void writeTag(HierarchicalStreamWriter writer, String tag, String value) {
        writer.startNode(tag);
        writer.setValue(value);
        writer.endNode();
    }

    private void writeTag(HierarchicalStreamWriter writer, String tag, Object value) {
        writeTag(writer, tag, (value == null) ? "" : value.toString());
    }

   private boolean shouldEmitNode(Node node) {
      boolean result = false;

      if (node != null) {
         NodeStore nodeStore = GraphManager.getNodeDetails(node);
         result = ((nodeStore != null) && !nodeStore.isBundled());
      }
      return result;
   }
}
