package csi.server.ws.support;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import csi.server.business.visualization.graph.DefaultImageProvider;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.business.visualization.graph.renderers.EdgeRenderer;
import csi.server.common.model.GraphConstants;
import csi.server.common.model.SelectionModel;
import csi.server.ws.actions.ForecastLinkEdgeRenderer;
import csi.server.ws.actions.ForecastLinkNodeRenderer;
import edu.uci.ics.jung.graph.util.Pair;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

public class GraphBuilder implements JsonGraphConstants {

    private ImageResolver imageResolver;

    public GraphBuilder(Properties images) {
        imageResolver = new ImageResolver(images);
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    public void fromJson(GraphContext context, Map<String, Object> graphPayload) {
        Collection<Map> nodes = (Collection<Map>) graphPayload.get(NODES);
        Collection<Map> links = (Collection<Map>) graphPayload.get(LINKS);
        updateGraph(context, nodes, links);

        if (graphPayload.containsKey(SELECTED)) {
            String nodeKey = (String) graphPayload.get(SELECTED);
            Map<String, Node> index = context.getNodeKeyIndex();
            Node node = index.get(nodeKey);
            if (node != null) {
                SelectionModel selection = context.getOrCreateSelection(GraphManager.DEFAULT_SELECTION);
                selection.nodes.add(node.getRow());
            }
        }

    }

    public GraphContext buildGraphContext(String identifier) {
        GraphContext context = new GraphContext(identifier, identifier);
        Visualization visualization = context.getVisualization();

        // override default rendering by allowing rendering of images with alpha
        // channel
        context.setImageProvider(new DefaultImageProvider(true));

        ForecastLinkNodeRenderer itemRenderer = new ForecastLinkNodeRenderer();
        EdgeRenderer edgeRenderer = new ForecastLinkEdgeRenderer();

        DefaultRendererFactory updatedFactory = new DefaultRendererFactory(itemRenderer, edgeRenderer);
        visualization.setRendererFactory(updatedFactory);

        Display display = context.getDisplay();
        display.setBackground(new Color(0x0FF3F3F3, true));
        Dimension viewDimension = new Dimension(800, 700);
        display.setSize(viewDimension);

        return context;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    private void updateGraph(GraphContext context, Collection<Map> nodes, Collection<Map> links) {

        Map<Object, Node> nodeLookup = new HashMap<Object, Node>(nodes.size());

        context.setShowLabels(true);
        Graph graph = context.getGraphData();
        SelectionModel selections = context.getOrCreateSelection(GraphManager.DEFAULT_SELECTION);

        for (Map map : nodes) {
            NodeStore details = createNodeDetails(map);
            if (details == null) {
                continue;
            }

            Node node = graph.addNode();
            node.set(GraphConstants.NODE_DETAIL, details);
            nodeLookup.put(details.getKey(), node);
            if (isSelected(map)) {
                selections.nodes.add(node.getRow());
            }
        }

        nodeLookup = (Map<Object, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);

        for (Map map : links) {
            LinkStore ls = createLinkDetails(map);
            Pair<Node> endpoints = getEndpoints(nodeLookup, map);
            if (endpoints == null) {
                continue;
            }
            Edge edge = graph.addEdge(endpoints.getFirst(), endpoints.getSecond());

            ls.setFirstEndpoint(GraphManager.getNodeDetails(endpoints.getFirst()));
            ls.setSecondEndpoint(GraphManager.getNodeDetails(endpoints.getSecond()));
            edge.set(GraphConstants.LINK_DETAIL, ls);
        }

        VisualGraph visualGraph = context.getVisualGraph();
        Iterator iterator = visualGraph.nodes();

        int black = Color.BLACK.getRGB();
        while (iterator.hasNext()) {
            NodeItem node = (NodeItem) iterator.next();
            node.setBoolean(GraphContext.IS_VISUALIZED, true);
            node.setVisible(true);
            node.setTextColor(black);

            NodeStore details = GraphManager.getNodeDetails(node);
            node.setSize(details.getRelativeSize());
        }

        iterator = visualGraph.edges();
        while (iterator.hasNext()) {
            EdgeItem edge = (EdgeItem) iterator.next();
            edge.setBoolean(GraphContext.IS_VISUALIZED, true);
            edge.setVisible(true);
        }

        // runComponentizedLayout(context);

    }

    private boolean isSelected(Map map) {
        boolean flag = Boolean.FALSE;

        if (map.containsKey(SELECTED)) {
            flag = Boolean.parseBoolean((String) map.get(SELECTED));
        }
        return flag;
    }

    @SuppressWarnings("rawtypes")
    private NodeStore createNodeDetails(Map map) {
        NodeStore details = new NodeStore();
        if (map == null) {
            return details;
        }

        if (map.containsKey(ID) == false) {
            System.out.println("Got a problem here. node w/o any id");
        }

        Object object = map.get(ID);

        String key = object == null ? null : object.toString();
        if (key == null) {
            System.out.println("Got a problem here. node w/o any id");
            System.out.println(map);
            return null;
        }

        details.setKey(map.get(ID).toString());
        details.addLabel((String) map.get(NAME));

        if (map.containsKey(TYPE)) {
            details.addType((String) map.get(TYPE));
        }

        if (map.containsKey(ICON)) {

            String alias = (String) map.get(ICON);
            Utility.addProperty(details, ICON_TYPE, alias);
            imageResolver.resolve(details);
        }

        if (map.containsKey(COLOR)) {
            String color = (String) map.get(COLOR);
            Utility.addProperty(details, STATUS_COLOR, color);
            imageResolver.resolve(details);
        }

        String s = (String) map.get(SIZE);
        double size = DEFAULT_NODE_SIZE;
        details.setRelativeSize(1.0);
        if (s != null) {
            try {
                size = Integer.parseInt(s);
                size = (size / DEFAULT_NODE_SIZE);
                details.setRelativeSize(size);
            } catch (Throwable t) {
            }
        }

        return details;
    }

    @SuppressWarnings("rawtypes")
    private LinkStore createLinkDetails(Map map) {
        LinkStore details = new LinkStore();

        double value = getAsDouble(map.get(THICKNESS));
        double upper = Math.ceil(value / 5.0);
        details.setWidth((int) upper);

        Property prop = new Property(ObjectAttributes.CSI_INTERNAL_COLOR);
        prop.getValues().add(new Integer(0x000000));
        details.getAttributes().put(ObjectAttributes.CSI_INTERNAL_COLOR, prop);

        return details;
    }

    @SuppressWarnings("rawtypes")
    private Pair<Node> getEndpoints(Map<Object, Node> nodeLookup, Map map) {
        Object skey = map.get(SOURCE);
        Object tkey = map.get(TARGET);

        if (skey == null || tkey == null) {
            return null;
        }
        Node source = nodeLookup.get(skey.toString());
        Node target = nodeLookup.get(tkey.toString());

        return new Pair<Node>(source, target);
    }

    private double getAsDouble(Object object) {

        if (object == null) {
            return 0.0;
        } else if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else {
            try {
                double value = Double.parseDouble((String) object);
                return value;
            } catch (Throwable t) {
            }
        }

        return 1.0;

    }

}
