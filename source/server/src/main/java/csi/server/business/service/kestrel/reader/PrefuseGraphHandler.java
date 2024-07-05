package csi.server.business.service.kestrel.reader;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import prefuse.Display;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;

public class PrefuseGraphHandler extends DefaultHandler {

//    private String currentValue;
    private StringBuilder currentValue;
    private String graphId;
    private Map<String, String> currentMap = new HashMap<String, String>();
    private GraphContext graphContext;

    public PrefuseGraphHandler(String graphId) {
        this.graphId = graphId;
        currentValue = new StringBuilder();
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // reset
//        currentValue = "";
        currentValue.setLength(0);
        if (qName.equalsIgnoreCase("graph")) {
            graphContext = createBaseGraphContext(graphId);

        } else if (qName.equalsIgnoreCase("node")) {
            currentMap.clear();
        } else if (qName.equalsIgnoreCase("link")) {
            currentMap.clear();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
//        currentValue = new String(ch, start, length);
        currentValue.append( new String( ch, start, length));
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("graphid")) {
            if (graphId == null) {
                graphId = currentValue.toString();
                graphContext.resetIds(graphId, graphId);
            }
        } else if (qName.equalsIgnoreCase("optionset")) {
            graphContext.getGraphData().putClientProperty(GraphManager.OPTION_SET_NAME, currentValue);

        } else if (qName.equalsIgnoreCase("background-color")) {
//            if (currentValue != null && !currentValue.trim().isEmpty()) {
            if( currentValue.length() > 0 ) {
                try {
                    graphContext.getVisualGraph().getVisualization().getDisplay(0).setBackground(ColorLib.getColor(Integer.parseInt(currentValue.toString()) | 0xff000000));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        } else if (qName.equalsIgnoreCase("node")) {
            handleNode(currentMap);

        } else if (qName.equalsIgnoreCase("link")) {
            handleLink(currentMap);

        } else {
            currentMap.put(qName.toLowerCase(), currentValue.toString());
        }

    }

    private void handleLink(Map<String, String> map) {

        Map<String, Node> nodeLookup = graphContext.getNodeKeyIndex();

        String skey = map.get("source");
        String tkey = map.get("target");

        if (skey == null || tkey == null) {
            throw new RuntimeException("Invalid link.  Source or Target is null");
        }

        Node source = nodeLookup.get(skey);
        Node target = nodeLookup.get(tkey);

        if (source == null || target == null) {
            throw new RuntimeException("Invalid link.  Source or Target is null");
        }

        Edge edge = graphContext.getGraphData().addEdge(source, target);

        LinkStore ls = createLinkDetails(map);
        ls.setFirstEndpoint(GraphManager.getNodeDetails(source));
        ls.setSecondEndpoint(GraphManager.getNodeDetails(target));
        GraphManager.setLinkDetails(edge, ls);
        

        VisualGraph vgraph = graphContext.getVisualGraph();
        EdgeItem vedge = (EdgeItem) vgraph.getEdge(edge.getRow());
        vedge.setBoolean(GraphContext.IS_VISUALIZED, true);
        vedge.setVisible(true);

    }

    private void handleNode(Map<String, String> map) {
        String key = map.get("id");
        if (key == null) {
            throw new RuntimeException("Encountered node with null id");
        }

        Map<String, Node> nodeLookup = graphContext.getNodeKeyIndex();
        if (nodeLookup.containsKey(key)) {
            throw new RuntimeException("Encountered duplicate node id: " + key);
        }

        NodeStore details = createNodeDetails(map);
        if (details == null) {
            return;
        }

        Graph graph = graphContext.getGraphData();
        Node node = graph.addNode();
        nodeLookup.put(details.getKey(), node);
        GraphManager.setNodeDetails(node, details);

        VisualGraph vgraph = graphContext.getVisualGraph();
        NodeItem vnode = (NodeItem) vgraph.getNode(node.getRow());
        vnode.setBoolean(GraphContext.IS_VISUALIZED, true);
        vnode.setVisible(true);
        vnode.setSize(details.getRelativeSize());
    }

    private GraphContext createBaseGraphContext(String graphId) {
        GraphContext context = new GraphContext(graphId, graphId, null);
        Display display = context.getDisplay();
        Dimension viewDimension = new Dimension(800, 700);
        display.setSize(viewDimension);
        return context;
    }

    //
    private LinkStore createLinkDetails(Map<String, String> map) {
        LinkStore details = new LinkStore();
        details.setDocId(ObjectId.get());

        String scale = map.get("scale");
        if (scale != null && !scale.isEmpty()) {
            try {
                // TODO: scale needs to be a double
                int intscale = Integer.parseInt(scale);
                details.setScale(intscale);
            } catch (NumberFormatException ne) {
                // ignore;
            }
        }

        String color = map.get("color");
        if (color != null && !color.isEmpty()) {
            try {
                int intcolor = Integer.parseInt(color);
                details.setColor(intcolor);
            } catch (NumberFormatException ne) {
                // ignore;
            }
        }

        return details;
    }

    private NodeStore createNodeDetails(Map<String, String> map) {
        NodeStore details = new NodeStore();
        details.setDocId(ObjectId.get());
        
        if (map == null) {
            return details;
        }

        String key = map.get("id");
        if (key != null) {
            details.setKey(key);
        }

        String label = map.get("label");
        if (label != null) {
            details.addLabel(label);
        }

        String type = map.get("type");
        if (type != null && !type.isEmpty()) {
            details.addType(type);
        }

        String color = map.get("color");
        if (color != null && !color.isEmpty()) {
            try {
                int intcolor = Integer.parseInt(color);
                details.setColor(intcolor);
            } catch (NumberFormatException ne) {
                // ignore;
            }
        }

        String icon = map.get("icon");
        if (icon != null && !icon.isEmpty()) {
            icon = "/Centrifuge/resources/icons/" + graphContext.getOptionSetName() +"/" + icon;
            details.setIcon(icon);
        }

        String shape = map.get("shape");
        if (shape != null && !shape.isEmpty()) {
            details.setShape(shape);
        }

        String s = map.get("scale");
        details.setRelativeSize(1.0);
        if (s != null && !s.isEmpty()) {
            try {
                double scale = Double.parseDouble(s);
                details.setRelativeSize(scale);
            } catch (Throwable t) {

            }
        }

        return details;
    }

    public GraphContext getGraphContext() {
        return graphContext;
    }

}
