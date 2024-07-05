package csi.server.common.codec.xstream.converter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.visualization.graph.GraphConstants;

public class GraphConverter implements Converter {
   private static final String[] LINK_ATTRS = { Tokens.ID, Tokens.SOURCE, Tokens.TARGET };

   public class Tokens implements GraphMLReader.Tokens {
      public static final String BGCOLOR = "bgcolor";
      public static final String CSI_RELGRAPH_BACKGROUND_COLOR = "csi.relgraph.backgroundColor";
   }

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {

        Graph graph = (Graph) o;
        Schema ns = graph.getNodeTable().getSchema();
        Schema es = graph.getEdgeTable().getSchema();
        Map<String, Node> nodeTable = (Map<String, Node>) graph.getClientProperty("nodeHashTable");

        writer.startNode(Tokens.GRAPH);

        String backgroundColor = (String) graph.getClientProperty(Tokens.CSI_RELGRAPH_BACKGROUND_COLOR);
        if (backgroundColor != null) {
            writer.addAttribute(Tokens.BGCOLOR, backgroundColor);
        }
        writer.endNode();
        writer.startNode(Tokens.GRAPH);
        writer.addAttribute(Tokens.EDGEDEF, String.valueOf(graph.isDirected() ? Tokens.DIRECTED : Tokens.UNDIRECTED));

        for (Iterator<Node> nodes = graph.nodes(); nodes.hasNext();) {
           Node n = nodes.next();

           if (GraphContext.Predicates.IsNodeVisualized.test(n)) {
              // this is not writing any other in-table fields
              // such as component id so it can easily recurse
              // through the bundle hierarchy in a proper order
              NodeStore nodeStore = GraphManager.getNodeDetails(n);

              if (!nodeStore.isBundled()) {
                 writeNode(nodeStore, ns, nodeTable, writer);
              }
           }
        }
        for (Iterator<Edge> edges = graph.edges(); edges.hasNext();) {
           Edge e = edges.next();

           if (GraphContext.Predicates.IsEdgeVisualizedAndDisplayable.test(e) &&
               isLinkRelevant(e)) {
              writeLink(graph, es, writer, e);
           }
        }
        writer.endNode();
    }

   private boolean isLinkRelevant(Edge e) {
      LinkStore details = GraphManager.getEdgeDetails(e);

      return (!details.getFirstEndpoint().isBundled() && !details.getSecondEndpoint().isBundled());
   }

    private void writeNode(HierarchicalStreamWriter writer, NodeStore nodeStore, Schema ns, Map<String, Node> nodeTable) {
        Node n = nodeTable.get(nodeStore.getKey());
        writer.startNode("object");
        writer.addAttribute("type", "NodeStore");
        int howMany = ns.getColumnCount();

        if (howMany > 0) {
           for (int i = 0; i < howMany; i++) {
                String field = ns.getColumnName(i);

                if (field.equalsIgnoreCase(GraphConstants.NODE_DETAIL)) {
                    writeGraphML(writer, nodeStore);
                } else { // just another field
                    writer.startNode(Tokens.DATA);
                    writer.addAttribute(Tokens.KEY, field);
                    writer.setValue(n.getString(field));
                    writer.endNode();
                }
            }
        } else {
            writer.startNode(Tokens.NODE);
            writer.addAttribute(Tokens.ID, String.valueOf(n.getRow()));
            writer.endNode();
        }

        // then recurse through children
        if (nodeStore.getChildren() != null) {
            for (AbstractGraphObjectStore child : nodeStore.getChildren()) {
                writeNode(writer, ((NodeStore) child), ns, nodeTable);
            }
        }

        writer.endNode();
    }

    private void writeGraphML(HierarchicalStreamWriter writer, NodeStore nodeStore) {

        writer.startNode("object");

        writer.addAttribute("type", "NodeStore");

        if (nodeStore.getIcon() != null) {
         writeTag(writer, "icon", nodeStore.getIcon());
      }
        if (nodeStore.getShape() != null) {
         writeTag(writer, "shape", nodeStore.getShape());
      }
        if (nodeStore.isAnchored()) {
         writeTag(writer, "anchored", "true");
      }
        if (nodeStore.isHideLabels()) {
            writeTag(writer, "hideLabels", "false");
        }

        writeGraphStore(writer, nodeStore);
        writer.endNode();
    }

    private void writeLink(Graph graph, Schema es, HierarchicalStreamWriter writer, Edge e) {
        String[] vals = new String[3];
        vals[0] = String.valueOf(e.getRow());
        vals[1] = String.valueOf(e.getSourceNode().getRow());
        vals[2] = String.valueOf(e.getTargetNode().getRow());
        int howMany = es.getColumnCount();

        if (howMany > 2) {
            writeForAttrs(writer, Tokens.EDGE, LINK_ATTRS, vals, 3);

            for (int i = 0; i < howMany; i++) {
                String field = es.getColumnName(i);
                if (field.equals(graph.getEdgeSourceField()) || field.equals(graph.getEdgeTargetField())) {
                  continue;
               }

                if (field.equalsIgnoreCase(GraphConstants.LINK_DETAIL)) {
                    LinkStore linkStore = (LinkStore) e.get(i);
                    writeLinkStore(writer, linkStore);
                    // linkStore.writeGraphML(writer);
                } else { // just another field
                    writer.startNode(Tokens.DATA);
                    writer.addAttribute(Tokens.KEY, field);
                    writer.setValue(e.getString(field));
                    writer.endNode();
                }
            }
            writer.endNode();
        } else {
            writeForAttrs(writer, Tokens.EDGE, LINK_ATTRS, vals, 3);
            writer.endNode();
        }
    }

    private void writeLinkStore(HierarchicalStreamWriter writer, LinkStore linkStore) {
        writer.startNode("object");
        writer.addAttribute("type", "LinkStore");

        if (linkStore.getStyle() != null) {
            writer.startNode("style");
            writer.setValue(linkStore.getStyle());
            writer.endNode();
        }

        if (linkStore.getWidth() > 0) {
            writer.startNode("width");
            writer.setValue(((Double) linkStore.getWidth()).toString());
            writer.endNode();
        }

        writeGraphStore(writer, linkStore);
        // super.writeSuperML( xml );
        writer.endNode();
    }

    private void writeGraphStore(HierarchicalStreamWriter writer, AbstractGraphObjectStore graph) {
        writeTag(writer, "key", graph.getKey());
        writeTag(writer, "type", graph.getType());

        if (graph.getColor() != null) {
            writeTag(writer, "color", graph.getColor().toString());
        }

        if (graph.isHidden()) {
            writeTag(writer, "hidden", "true");
        }

        if (graph.isBundled()) {
            writeTag(writer, "bundled", "true");
        }

        String compositeLabel = graph.getLabel();
        if (compositeLabel.length() > 0) {
            writeTag(writer, "label", compositeLabel);
        }

        writer.startNode("types");

        Iterator<Map.Entry<String, Integer>> iter = graph.getTypes().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = iter.next();
            writer.startNode(GraphMLWriter.Tokens.DATA);
            writer.addAttribute("type", entry.getKey());
            writer.setValue(entry.getValue().toString());
            writer.endNode();
        }
        writer.endNode();

        int nestedLevel = 0;
        AbstractGraphObjectStore myParent = graph.getParent();
        while (myParent != null) {
            nestedLevel += 1;
            myParent = myParent.getParent();
        }

        writeTag(writer, "nestedLevel", ((Integer) nestedLevel).toString());

        for (Map.Entry<String, Property> entry : graph.getAttributes().entrySet()) {
            Property prop = entry.getValue();
            List<Object> values = prop.getValues();

            writer.startNode(GraphMLWriter.Tokens.DATA);
            writer.addAttribute("key", prop.getName());
            for (Object o : values) {
                if (o != null) {
                    writeTag(writer, GraphMLWriter.Tokens.DATA, o.toString());
                }
            }
            writer.endNode();

        }
    }

    private void writeTag(HierarchicalStreamWriter writer, String tag, String value) {
        writer.startNode(tag);
        writer.setValue(value);
        writer.endNode();
    }

    private void writeForAttrs(HierarchicalStreamWriter xml, String tag, String[] names, String[] values, int attrs) {
        xml.startNode(tag);
        for (int i = 0; i < attrs; i++) {
            xml.addAttribute(names[i], values[i]);
        }

    }

    private void writeNode(NodeStore nodeStore, Schema ns, Map<String, Node> nodeTable, HierarchicalStreamWriter writer) {
        // write the node
        // writer.start(Tokens.NODE, Tokens.KEY, nodeStore.getKey());
        // nodeStore.writeGraphML(writer);
        // writer.end();

        Node n = nodeTable.get(nodeStore.getKey());
        int howMany = ns.getColumnCount();

        if (howMany > 0) {
            writer.startNode(Tokens.NODE);
            writer.addAttribute(Tokens.ID, String.valueOf(n.getRow()));
            for (int i = 0; i < howMany; i++) {
                String field = ns.getColumnName(i);
                if( field.equalsIgnoreCase(GraphConstants.DOC_ID)) {
                    continue;
                }

                if (field.equalsIgnoreCase(GraphConstants.NODE_DETAIL)) {
                    writeGraphML(writer, nodeStore);
                } else { // just another field
                    writer.startNode(Tokens.DATA);
                    writer.addAttribute(Tokens.KEY, field);
                    writer.setValue(n.getString(field));
                    writer.endNode();
                }
            }
            writer.endNode();
        } else {
            writer.startNode(Tokens.NODE);
            // writer.addAttribute(Tokens.ID, String.valueOf(n.getRow()));
            writer.endNode();
        }

        // then recurse through children
        if (nodeStore.getChildren() != null) {
            for (AbstractGraphObjectStore child : nodeStore.getChildren()) {
                writeNode(((NodeStore) child), ns, nodeTable, writer);
            }
        }
    }

    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        return null;
    }

    public boolean canConvert(Class aClass) {
        return aClass.equals(Graph.class);
    }
}
