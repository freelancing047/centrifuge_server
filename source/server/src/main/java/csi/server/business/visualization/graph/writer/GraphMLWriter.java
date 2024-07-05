/**
 * Copyright (c) 2004-2006 Regents of the University of California.
 * See "license-prefuse.txt" for licensing terms.
 */
package csi.server.business.visualization.graph.writer;

import java.awt.Point;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.io.GraphMLReader;
import prefuse.util.io.XMLWriter;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.AbstractGraphObjectStore;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.TypeInfo;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.util.DateUtil;

/**
 * GraphWriter instance that writes a graph file formatted using the GraphML file format. GraphML is an XML format
 * supporting graph structure and typed data schemas for both nodes and edges. For more information about the format,
 * please see the <a href="http://graphml.graphdrawing.org/">GraphML home page</a>.
 *
 * <p>
 * The GraphML spec only supports the data types <code>int</code>, <code>long</code>, <code>float</code>,
 * <code>double</code>, <code>boolean</code>, and <code>string</code>. An exception will be thrown if a data
 * type outside these allowed types is encountered.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GraphMLWriter {
   private static final String[] LINK_ATTRS = { Tokens.ID, Tokens.SOURCE, Tokens.TARGET };

   /**
    * String tokens used in the GraphML format.
    */
   public class Tokens implements GraphMLReader.Tokens {
      public static final String BGCOLOR = "bgcolor";
      public static final String CSI_RELGRAPH_BACKGROUND_COLOR = "csi.relgraph.backgroundColor";
      public static final String GRAPHML = "graphml";
   }

    /**
     * Map containing legal data types and their names in the GraphML spec
     */
    @SuppressWarnings("unchecked")
    private static final HashMap<Class, String> TYPES = new HashMap<Class, String>();
    static {
        TYPES.put(int.class, Tokens.INT);
        TYPES.put(long.class, Tokens.LONG);
        TYPES.put(float.class, Tokens.FLOAT);
        TYPES.put(double.class, Tokens.DOUBLE);
        TYPES.put(boolean.class, Tokens.BOOLEAN);
        TYPES.put(String.class, Tokens.STRING);
    }

    /**
     * @see prefuse.data.io.GraphWriter#writeGraph(prefuse.data.Graph, java.io.OutputStream)
     */
    @SuppressWarnings("unchecked")
    public void writeNodesAndLinks(Graph graph, PrintWriter out) {
        // get the schemas
        Schema ns = graph.getNodeTable().getSchema();
        Schema es = graph.getEdgeTable().getSchema();

        Map<String, Node> nodeTable = (Map<String, Node>) graph.getClientProperty("nodeHashTable");

        XMLWriter xml = new XMLWriter(out);

        xml.begin();

        String backgroundColor = (String) graph.getClientProperty(Tokens.CSI_RELGRAPH_BACKGROUND_COLOR);
        if (backgroundColor != null) {
            xml.start(Tokens.GRAPHML, Tokens.BGCOLOR, backgroundColor);
        } else {
            xml.start(Tokens.GRAPHML);
        }

        xml.comment("prefuse GraphML Writer | " + ZonedDateTime.now().format(DateUtil.JAVA_UTIL_DATE_TOSTRING_FORMATTER));

        // Print graph contents
        xml.start(Tokens.GRAPH, Tokens.EDGEDEF, graph.isDirected() ? Tokens.DIRECTED : Tokens.UNDIRECTED);

        Map<String, TypeInfo> styles = (Map<String, TypeInfo>) graph.getClientProperty(NodeStore.NODE_LEGEND_INFO);
        // print the nodes
        xml.comment("nodes");
        Iterator<Node> nodes = graph.nodes();
        // boolean always = false;
        while (nodes.hasNext()) {
            // this is not writing any other in-table fields
            // such as component id so it can easily recurse
            // through the bundle hierarchy in a proper order
            Node n = nodes.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(n);

            applyStyleInfo(styles, nodeStore);

            if (!nodeStore.isBundled()) {
                writeNode(nodeStore, ns, nodeTable, xml);
            }
        }

        // add a blank line
        xml.println();

        // print the edges
        // String[] attr = new String[] {
        // Tokens.ID, Tokens.SOURCE, Tokens.TARGET
        // };

        xml.comment("edges");
        Iterator<Edge> edges = graph.edges();
        while (edges.hasNext()) {
            Edge e = edges.next();
            writeLink(graph, es, xml, e);
        }
        xml.end();

        // finish writing file
        xml.end();

        xml.finish();
    }

    private void applyStyleInfo(Map<String, TypeInfo> styles, NodeStore nodeStore) {
        String type = nodeStore.getType();
        if (type == null) {
            return;
        }

        TypeInfo typeInfo = styles.get(type);
        if (typeInfo == null) {
            return;
        }

        nodeStore.setColor(typeInfo.color);
        nodeStore.setIcon(typeInfo.iconId);
        nodeStore.setShape(typeInfo.shape.toString());

    }

    @SuppressWarnings("unchecked")
    public void writeLinks(Graph graph, PrintWriter writer) {
        Schema es = graph.getEdgeTable().getSchema();
        XMLWriter xml = new XMLWriter(writer);
        xml.begin();
        xml.start(Tokens.GRAPHML);
        xml.start(Tokens.GRAPH, Tokens.EDGEDEF, graph.isDirected() ? Tokens.DIRECTED : Tokens.UNDIRECTED);

        {
            Iterator<Edge> edges = graph.edges();
            while (edges.hasNext()) {
                Edge e = edges.next();
                writeLink(graph, es, xml, e);
            }
        }

        // end graph element.
        xml.end();

        // finish writing file
        xml.end(); // end GRAPHML element

        xml.finish();
    }

    private void writeLink(Graph graph, Schema es, XMLWriter xml, Edge e) {
        String[] vals = new String[3];
        vals[0] = String.valueOf(e.getRow());
        vals[1] = String.valueOf(e.getSourceNode().getRow());
        vals[2] = String.valueOf(e.getTargetNode().getRow());

        if (es.getColumnCount() > 2) {
            xml.start(Tokens.EDGE, LINK_ATTRS, vals, 3);
            for (int i = 0; i < es.getColumnCount(); ++i) {
                String field = es.getColumnName(i);
                if (field.equals(graph.getEdgeSourceField()) || field.equals(graph.getEdgeTargetField())) {
                  continue;
               }

                if (field.equalsIgnoreCase(GraphConstants.LINK_DETAIL)) {
                    LinkStore linkStore = (LinkStore) e.get(i);
                    linkStore.writeGraphML(xml);
                } else { // just another field
                    xml.contentTag(Tokens.DATA, Tokens.KEY, field, e.getString(field));
                }
            }
            xml.end();
        } else {
            xml.tag(Tokens.EDGE, LINK_ATTRS, vals, 3);
        }
    }

    // depth-first order
    private void writeNode(NodeStore nodeStore, Schema ns, Map<String, Node> nodeTable, XMLWriter xml) {
        // write the node
        // xml.start(Tokens.NODE, Tokens.KEY, nodeStore.getKey());
        // nodeStore.writeGraphML(xml);
        // xml.end();

        Node n = nodeTable.get(nodeStore.getKey());

        if (ns.getColumnCount() > 0) {
            xml.start(Tokens.NODE, Tokens.ID, String.valueOf(n.getRow()));
            for (int i = 0; i < ns.getColumnCount(); ++i) {
                String field = ns.getColumnName(i);
                if (field.equalsIgnoreCase(GraphConstants.NODE_DETAIL)) {
                    nodeStore.writeGraphML(xml);
                } else { // just another field
                    xml.contentTag(Tokens.DATA, Tokens.KEY, field, n.getString(field));
                }
            }
            xml.end();
        } else {
            xml.tag(Tokens.NODE, Tokens.ID, String.valueOf(n.getRow()));
        }

        // then recurse through children
        if (nodeStore.getChildren() != null) {
            for (AbstractGraphObjectStore child : nodeStore.getChildren()) {
                writeNode(((NodeStore) child), ns, nodeTable, xml);
            }
        }
    }

    /**
     * @see prefuse.data.io.GraphWriter#writeGraph(prefuse.data.Graph, java.io.OutputStream)
     */
    @SuppressWarnings("unchecked")
    public void writeNodePlacement(Graph graph, PrintWriter out, GraphConstants.eLayoutAlgorithms layout) {
        XMLWriter xml = new XMLWriter(out);
        xml.begin();

        xml.start(Tokens.GRAPHML);

        xml.comment("prefuse GraphML Writer | " + ZonedDateTime.now().format(DateUtil.JAVA_UTIL_DATE_TOSTRING_FORMATTER));

        // print graph contents
        xml.start(Tokens.GRAPH, Tokens.EDGEDEF, graph.isDirected() ? Tokens.DIRECTED : Tokens.UNDIRECTED);

        // print the nodes
        xml.comment("nodes");
        Iterator<Node> nodes = graph.nodes();
        while (nodes.hasNext()) {
            Node n = nodes.next();
            NodeStore nodeStore = GraphManager.getNodeDetails(n);
            // TODO if node not hidden nor bundled
            Point point = nodeStore.getPosition(layout);
            if (point != null) {
                xml.start(Tokens.NODE, Tokens.ID, String.valueOf(n.getRow()));
                xml.contentTag("X", ((Integer) point.x).toString());
                xml.contentTag("Y", ((Integer) point.y).toString());
                xml.end();
            }
        }

        xml.end();

        // finish writing file

        xml.end(); // end GRAPHML

        xml.finish();
    }

} // end of class GraphMLWriter
