package csi.server.common.codec.xstream.converter;

import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.common.dto.graph.EdgeListing;

public class EdgeListingConverter implements Converter {
   private static final Logger LOG = LogManager.getLogger(EdgeListingConverter.class);

    static String[] LINK_ATTRS = { Tokens.ID, Tokens.SOURCE, Tokens.TARGET };


    public interface Tokens extends GraphMLReader.Tokens {
    }

   @Override
   public boolean canConvert(Class clazz) {
      return EdgeListing.class.isAssignableFrom(clazz);
   }

   @Override
   public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
      EdgeListing listing = (EdgeListing) o;

      if (listing != null) {
         Graph graph = listing.getGraph();

         openGraph(writer, graph);

         for (Iterator<Edge> edges = graph.edges(); edges.hasNext();) {
            Edge edge = edges.next();

            if (GraphContext.Predicates.IsEdgeVisualized.test(edge)) {
               try {
                  if (shouldEmitEdge(edge)) {
                     writeEdge(writer, edge);
                  }
               } catch (IllegalArgumentException iae) {
                  LOG.warn("Encountered transient error while writing edge information.  Not all data may have been conveyed", iae);
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

   private boolean shouldEmitEdge(Edge edge) {
      LinkStore details = GraphManager.getEdgeDetails(edge);

      return (!details.getFirstEndpoint().isBundled() && !details.getSecondEndpoint().isBundled());
   }

    private void writeEdge(HierarchicalStreamWriter writer, Edge edge) {
        writer.startNode(Tokens.EDGE);
        {
            String[] values = new String[LINK_ATTRS.length];
            values[0] = String.valueOf(edge.getRow());
            values[1] = String.valueOf(edge.getSourceNode().getRow());
            values[2] = String.valueOf(edge.getTargetNode().getRow());
            for (int i = 0; i < values.length; i++) {
                writer.addAttribute(LINK_ATTRS[i], values[i]);
            }

            writeEdgeDetails(writer, edge);
        }
        writer.endNode();
    }

    private void writeEdgeDetails(HierarchicalStreamWriter writer, Edge edge) {

        LinkStore details = GraphManager.getEdgeDetails(edge);
        writer.startNode("object");
        {
            writer.addAttribute("type", "LinkStore");

            writeTag(writer, "key", details.getKey());
            writeTag(writer, "type", details.getType());

            if (details.isHidden()) {
                writeTag(writer, "hidden", "true");
            }

            if (details.isBundled()) {
                writeTag(writer, "bundled", "true");
            }

            if (!details.isDisplayable()) {
                writeTag(writer, "displayable", Boolean.FALSE);
            }
            if (!details.isVisualized()) {
            	writeTag(writer, "isVisualized", Boolean.FALSE);
            }
            if (!details.isEditable()) {
            	writeTag( writer, "editable", Boolean.FALSE);
            }
            String compositeLabel = details.getLabel();

            if (compositeLabel.length() > 0) {
                writeTag(writer, "label", compositeLabel);
            }

            writeTypes(writer, details);

            writeEndpointLabels(writer, edge, details);

        }
        writer.endNode();
    }

    private void writeEndpointLabels(HierarchicalStreamWriter writer, Edge edge, LinkStore details) {
        String sLabel = details.getFirstEndpoint().getLabel();
        String tLabel = details.getSecondEndpoint().getLabel();
        writeTag(writer, "source", sLabel);
        writeTag(writer, "target", tLabel);
        writeTag(writer, "sourceId", edge.getSourceNode().getRow());
        writeTag(writer, "targetId", edge.getTargetNode().getRow());
    }

    private void writeTypes(HierarchicalStreamWriter writer, LinkStore details) {
        writer.startNode("types");
        {
            Iterator<Map.Entry<String, Integer>> iter = details.getTypes().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                writer.startNode(GraphMLWriter.Tokens.DATA);
                writer.addAttribute("type", entry.getKey());
                writer.setValue(entry.getValue().toString());
                writer.endNode();
            }
        }
        writer.endNode();
    }

    private void writeTag(HierarchicalStreamWriter writer, String tag, String value) {
        writer.startNode(tag);
        writer.setValue(value);
        writer.endNode();
    }

    private void writeTag(HierarchicalStreamWriter writer, String tag, Object value) {
        writeTag(writer, tag, (value == null) ? "" : value.toString());
    }
}
