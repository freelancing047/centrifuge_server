package csi.server.business.visualization.graph.plunk;

import java.awt.Point;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.DBObject;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;

import csi.graph.AbstractStorageService;
import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.data.EdgeToDataTransformer;
import csi.server.common.dto.graph.gwt.PlunkLinkDTO;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.PlunkedLink;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class LinkPlunker {
    public PlunkedLink plunk(PlunkLinkDTO plunkLinkDTO) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(plunkLinkDTO.getVizUuid());
        VisualGraph vGraph = graphContext.getVisualGraph();
        Display display = getDisplay(vGraph);


        VisualItem startNodeItem = getVisualItem(display, plunkLinkDTO.getSourceNodeCenterX(), plunkLinkDTO.getSourceNodeCenterY());
        VisualItem endNodeItem = getVisualItem(display, plunkLinkDTO.getTargetNodeX(), plunkLinkDTO.getTargetNodeY());

        if(nodesAreNotFoundAtBothLocations(startNodeItem, endNodeItem)) {
         return null;
      }

        Node startNode = vGraph.getNode(startNodeItem.getRow());
        Node endNode = vGraph.getNode(endNodeItem.getRow());

        Edge edge = addNewEdge(graphContext, startNode, endNode, plunkLinkDTO.getLinkDef());

        if(edge == null) {
         return null;
      }

        addEdgeToMongo(graphContext, edge);
        return saveEdgeToRelGraph(plunkLinkDTO.getVizUuid(), edge);
    }

    @SuppressWarnings("unchecked")
    public void addEdgesFromRelGraph(RelGraphViewDef rgDef, Graph graph) {
        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        for (PlunkedLink plunkedLink : rgDef.getPlunkedLinks()) {
            Node sourceNode = idNodeMap.get(plunkedLink.getSourceNodeKey());
            Node targetNode = idNodeMap.get(plunkedLink.getTargetNodeKey());
            if((sourceNode != null) && (targetNode != null)) {
                LinkDef linkDef = findMatchingLinkDef(plunkedLink.getLinkType(), rgDef);
                if(linkDef == null) {
                    linkDef = new LinkDef();
                }
                Edge edge = createEdgeFromNodes(graph, sourceNode, targetNode, linkDef);
                if (edge != null) {
                    String edgeKey = GraphManager.getNodeDetails(sourceNode).getKey() + "+" + GraphManager.getNodeDetails(targetNode).getKey();
                    addEdgeToGraphEdgeMap(graph, edge, edgeKey);
                    updateLinkStore(plunkedLink, edge);
                }
            }
        }
    }

    private LinkDef findMatchingLinkDef(String linkType, RelGraphViewDef rgDef) {
        for (LinkDef linkDef : rgDef.getLinkDefs()) {
            String linkDefType = getTypeFromLink(linkDef);
            if((linkDefType != null) && linkDefType.equalsIgnoreCase(linkType)) {
               return linkDef;
            }
        }
        return null;
    }

    private String getTypeFromLink(LinkDef linkDef) {
        AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if((attributeDef != null) && (attributeDef.getFieldDef() != null) && attributeDef.getFieldDef().isAnonymous()){
            return attributeDef.getFieldDef().getStaticText();
        }
        return "";
    }

    public void editPlunkedLink(String vizUuid, PlunkedLink plunkedLink) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        Graph graph = graphContext.getVisibleGraph();
        Map<String, Edge> linkIndex = (Map<String, Edge>) graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        Edge edge = linkIndex.get(plunkedLink.buildItemKey());
        LinkStore linkStore = updateLinkStore(plunkedLink, edge);
        VisualGraph vGraph = graphContext.getVisualGraph();
        TableEdgeItem tableEdgeItem = (TableEdgeItem) vGraph.getEdge(edge.getRow());
        tableEdgeItem.setSize(plunkedLink.getSize());
        GraphManager.setLinkDetails(tableEdgeItem, linkStore);

        updateEdgeToMongo(graphContext, tableEdgeItem);
    }

    private LinkStore updateLinkStore(PlunkedLink plunkedLink, Edge edge) {
        LinkStore linkStore = (LinkStore)edge.get(GraphConstants.LINK_DETAIL);

        linkStore.setWidth(plunkedLink.getSize());
        linkStore.setTransparency(plunkedLink.getTransparency());
        linkStore.getLabels().clear();
        linkStore.addLabel(plunkedLink.getLabel());
        linkStore.clearIncrementCounts();
        linkStore.addDirectedEdge(plunkedLink.getLinkDirection());
        linkStore.setType(plunkedLink.getLinkType());
        linkStore.setColor(plunkedLink.getColor());
        linkStore.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE);

        GraphManager.setLinkDetails(edge, linkStore);
        return linkStore;
    }

    private void addEdgeToMongo(GraphContext graphContext, Edge edge) {
        AbstractStorageService service = AbstractStorageService.instance();
        GraphStorage storage = service.getGraphStorage(graphContext.getVizUuid());
        if(storage == null){
            storage = service.createEmptyStorage(graphContext.getVizUuid());
        }
        EdgeToDataTransformer edgeToDataTransformer = new EdgeToDataTransformer();
        edgeToDataTransformer.setGraphStorage(storage);
        DBObject data = edgeToDataTransformer.apply(edge);
        DBObject sourceRef = buildEdgeNodeRef(edge.getSourceNode());
        DBObject targetRef = buildEdgeNodeRef(edge.getTargetNode());
        storage.addEdge(data, new Pair<DBObject>(sourceRef, targetRef), EdgeType.UNDIRECTED);
        service.saveGraphStorage(graphContext.getVizUuid(), storage);
    }

    private void updateEdgeToMongo(GraphContext graphContext, Edge edge) {
        AbstractStorageService service = AbstractStorageService.instance();
        GraphStorage storage = service.getGraphStorage(graphContext.getVizUuid());
        if(storage == null){
            storage = service.createEmptyStorage(graphContext.getVizUuid());
        }
        EdgeToDataTransformer edgeToDataTransformer = new EdgeToDataTransformer();
        edgeToDataTransformer.setGraphStorage(storage);
        DBObject data = edgeToDataTransformer.apply(edge);
        storage.updateEdge(data);
        service.saveGraphStorage(graphContext.getVizUuid(), storage);
    }

    private PlunkedLink saveEdgeToRelGraph(String vizUuid, Edge edge) {
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
        PlunkedLink plunkedLink = PlunkedLinkConversion.convert(edge);
        plunkedLink.setLinkType(GraphConstants.UNSPECIFIED_LINK_TYPE);
        relGraphViewDef.getPlunkedLinks().add(plunkedLink);
        CsiPersistenceManager.merge(relGraphViewDef);
        return plunkedLink;
    }

    private static DBObject buildEdgeNodeRef(Node sourceNode) {
        ObjectId id = (ObjectId) sourceNode.get(GraphConstants.DOC_ID);
        return Helper.getIdQuery(id);
    }

    private Display getDisplay(VisualGraph vGraph) {
        Visualization visualization = vGraph.getVisualization();
        return visualization.getDisplay(0);
    }

    private Edge addNewEdge(GraphContext graphContext, Node startNode, Node endNode, LinkDef linkDef) {
        Graph graph = graphContext.getVisibleGraph();
        Edge edge = createEdgeFromNodes(graph, startNode, endNode, linkDef);
        if(edge == null) {
         return null;
      }

        addEdgeToVisualGraph(graphContext, edge);

        String edgeKey = GraphManager.getNodeDetails(startNode).getKey() + "+" + GraphManager.getNodeDetails(endNode).getKey();
        addEdgeToGraphEdgeMap(graph, edge, edgeKey);
        return edge;
    }

    private void addEdgeToVisualGraph(GraphContext graphContext, Edge edge) {
        VisualGraph visualGraph = graphContext.getVisualGraph();
        EdgeItem visualGraphEdge = (EdgeItem) visualGraph.getEdge(edge.getRow());
        visualGraphEdge.setBoolean(GraphContext.IS_VISUALIZED, true);
        visualGraphEdge.setVisible(true);
    }

    private Edge createEdgeFromNodes(Graph graph, Node startNode, Node endNode, LinkDef linkDef) {
        LinkStore ls = createLinkDetails(startNode, endNode, linkDef);

        Map<String, Edge> linkIndex = (Map<String, Edge>) graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        if(plunkedLinkAlreadyExists(ls, linkIndex)){
            return null;
        }

        int edgeRow = graph.addEdge(startNode.getRow(), endNode.getRow());
        Edge edge = graph.getEdge(edgeRow);
        GraphManager.setLinkDetails(edge, ls);
        return edge;
    }

    private boolean plunkedLinkAlreadyExists(LinkStore ls, Map<String, Edge> linkIndex) {
        return linkIndex.containsKey(ls.getKey()) || linkIndex.containsKey(reverseKey(ls.getKey()));
    }

    private String reverseKey(String key) {
        String [] keyparts = key.split("\\+");
        return keyparts[1] + "+" + keyparts[0];
    }

    @SuppressWarnings("unchecked")
    private void addEdgeToGraphEdgeMap(Graph graph, Edge edge, String edgeKey) {
        Map<String, Edge> linkIndex = (Map<String, Edge>) graph.getClientProperty(GraphManager.EDGE_HASH_TABLE);
        linkIndex.put(edgeKey, edge);
    }

    private boolean nodesAreNotFoundAtBothLocations(VisualItem startNodeItem, VisualItem endNodeItem) {
        return (startNodeItem == null) || (endNodeItem == null) || (endNodeItem instanceof TableEdgeItem);
    }

    private VisualItem getVisualItem(Display display, double centerOfNodeX, double centerOfNodeY) {
        Point startPoint = new Point(Double.valueOf(centerOfNodeX).intValue(), Double.valueOf(centerOfNodeY).intValue());
        return display.findItem(startPoint);
    }

    private LinkStore createLinkDetails(Node startNode, Node endNode, LinkDef linkDef) {
        LinkStore linkStore =  new LinkStore();
        linkStore.setDocId(ObjectId.get());

        NodeStore sourceDetails = GraphManager.getNodeDetails(startNode);
        NodeStore targetDetails = GraphManager.getNodeDetails(endNode);
        linkStore.setKey(sourceDetails.getKey() + "+" + targetDetails.getKey());
        linkStore.setFirstEndpoint(sourceDetails);
        linkStore.setSecondEndpoint(targetDetails);
        linkStore.setPlunked(true);
        linkStore.addType(GraphConstants.UNSPECIFIED_LINK_TYPE);
        setLinkValuesFromLinkDef(linkStore, linkDef);

        return linkStore;
    }

    private void setLinkValuesFromLinkDef(LinkStore linkStore, LinkDef linkDef) {
        //TODO: Link should inherit from the type.
    }

}
