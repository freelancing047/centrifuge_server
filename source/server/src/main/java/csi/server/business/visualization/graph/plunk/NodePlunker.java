package csi.server.business.visualization.graph.plunk;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.common.base.Strings;
import com.mongodb.DBObject;

import prefuse.Display;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.graph.AbstractStorageService;
import csi.graph.GraphStorage;
import csi.graph.mongo.Helper;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.data.NodeToDataTransformer;
import csi.server.common.dto.graph.gwt.PlunkNodeDTO;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.FieldDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.PlunkedNode;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.dao.CsiPersistenceManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NodePlunker {
   private static final Logger LOG = LogManager.getLogger(NodePlunker.class);

    public PlunkedNode plunk(PlunkNodeDTO plunkNodeDTO) {
        validateInputParameters(plunkNodeDTO.getNodeName(), plunkNodeDTO.getNodeType());

        GraphContext graphContext = GraphServiceUtil.getGraphContext(plunkNodeDTO.getVizUuid());
        VisualGraph vGraph = graphContext.getVisualGraph();

        Node newNode = createPlunkedNode(graphContext, plunkNodeDTO);
        Point2D displayPoint = transformPointLocation(graphContext, plunkNodeDTO.getClientPositionX(), plunkNodeDTO.getClientPositionY());

        setNodePosition(vGraph, newNode, displayPoint);
        setNodeSize(vGraph, newNode);

        addNodeToGraphContext(graphContext, newNode);
        saveNodeToMongo(graphContext, vGraph.getNode(newNode.getRow()));

        return saveNodeToRelGraph(plunkNodeDTO.getVizUuid(), newNode);

    }

    @SuppressWarnings("unchecked")
    public void addNodesFromRelGraph(RelGraphViewDef rgDef, Graph graph) {
        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        for (PlunkedNode plunkedNode : rgDef.getPlunkedNodes()) {
            NodeStore nodeStore = PlunkedNodeConversion.convert(plunkedNode);

            NodeDef nodeDef = findMatchingNodeDef(plunkedNode.getNodeType(), rgDef);
            if((nodeDef != null) && !plunkedNode.isHasBeenEdited()) {
               setNodeValuesFromNodeDef(nodeStore, nodeDef);
            }

            Node node = makeNodeVisibleOnGraph(graph);
            GraphManager.setNodeDetails(node, nodeStore);
            idNodeMap.put(nodeStore.getKey(), node);
        }
    }

    public void editPlunkedNode(String vizUuid, PlunkedNode plunkedNode) {
        GraphContext graphContext = GraphServiceUtil.getGraphContext(vizUuid);
        Graph graph = graphContext.getVisibleGraph();
        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);
        Node node = idNodeMap.get(plunkedNode.getNodeKey());
        NodeStore nodeStore = PlunkedNodeConversion.convert(plunkedNode);
        GraphManager.setNodeDetails(node, nodeStore);

        VisualGraph vGraph = graphContext.getVisualGraph();
        setNodeSize(vGraph, node);
        addNodeToGraphContext(graphContext, node);

        saveNodeToMongo(graphContext, vGraph.getNode(node.getRow()));
    }

    private NodeDef findMatchingNodeDef(String nodeType, RelGraphViewDef rgDef) {
        for (NodeDef nodeDef : rgDef.getNodeDefs()) {
            String nodeDefType = getTypeFromNode(nodeDef);
            if(nodeDefType.equalsIgnoreCase(nodeType)) {
               return nodeDef;
            }
        }
        return null;
    }

    private String getTypeFromNode(NodeDef nodeDef) {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if((attributeDef != null) && (attributeDef.getFieldDef() != null) && attributeDef.getFieldDef().isAnonymous()){
            return attributeDef.getFieldDef().getStaticText();
        }
        return "";
    }

    private void saveNodeToMongo(GraphContext graphContext, Node newNode) {
    	AbstractStorageService service = AbstractStorageService.instance();
        GraphStorage storage = service.getGraphStorage(graphContext.getVizUuid());
        if(storage == null){
            storage = service.createEmptyStorage(graphContext.getVizUuid());
        }
        NodeToDataTransformer nodeTransform = new NodeToDataTransformer();
        nodeTransform.setGraphStorage(storage);
        DBObject data = nodeTransform.apply(newNode);
        storage.addOrUpdateVertex(data);
        Object docId = Helper.getId(data);
        newNode.set(GraphConstants.DOC_ID, docId);
        GraphManager.getNodeDetails(newNode).setDocId(docId);
        service.saveGraphStorage(graphContext.getVizUuid(), storage);
    }

    private PlunkedNode saveNodeToRelGraph(String vizUuid, Node newNode) {
        RelGraphViewDef relGraphViewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizUuid);
        PlunkedNode plunkedNode = PlunkedNodeConversion.convert(newNode);
        relGraphViewDef.getPlunkedNodes().add(plunkedNode);
        CsiPersistenceManager.merge(relGraphViewDef);
        return plunkedNode;
    }

    private void addNodeToGraphContext(GraphContext graphContext, Node newNode) {
        graphContext.showItems(new ArrayList<Node>(Arrays.asList(newNode)), false);
    }

    private void setNodeSize(VisualGraph vGraph, Node newNode) {
        NodeStore nodeDetails = GraphManager.getNodeDetails(newNode);
        VisualItem vi = (VisualItem) vGraph.getNode(newNode.getRow());
        vi.setSize(nodeDetails.getRelativeSize());
    }

    private void setNodePosition(VisualGraph vGraph, Node newNode, Point2D displayPoint) {
        NodeItem nodeItem = (NodeItem) vGraph.getNode(newNode.getRow());
        PrefuseLib.setX(nodeItem, null, displayPoint.getX());
        PrefuseLib.setY(nodeItem, null, displayPoint.getY());
    }

    private void validateInputParameters(String nodeName, String nodeType) {
        checkArgument(!Strings.isNullOrEmpty(nodeName), "Node name is required");
        checkArgument(!Strings.isNullOrEmpty(nodeType), "Node type is required");
    }

    private Point2D transformPointLocation(GraphContext graphContext, int clientX, int clientY) {
        Point2D point = new Point2D.Double(clientX, clientY);
        Display display = graphContext.getDisplay();
        AffineTransform inverseTransform = display.getInverseTransform();
        Point2D displayPoint = inverseTransform.transform(point, null);

        LOG.trace("Original point " + point);
        LOG.trace("Display point " + displayPoint);
        return displayPoint;
    }

    @SuppressWarnings("unchecked")
    private Node createPlunkedNode(GraphContext graphContext, PlunkNodeDTO plunkNodeDTO) {
        Graph graph = graphContext.getVisibleGraph();
        Map<String, Node> idNodeMap = (Map<String, Node>) graph.getClientProperty(GraphManager.NODE_HASH_TABLE);

        Node node = makeNodeVisibleOnGraph(graph);
        NodeStore nodeStore = createNodeDetails(plunkNodeDTO);
        GraphManager.setNodeDetails(node, nodeStore);

        idNodeMap.put(nodeStore.getKey(), node);

        return node;
    }

    private Node makeNodeVisibleOnGraph(Graph graph) {
        Node node = graph.addNode();
        node.setBoolean(GraphContext.IS_VISUALIZED, true);

        return node;
    }

    private NodeStore createNodeDetails(PlunkNodeDTO plunkNodeDTO) {
        NodeStore nodeStore = new NodeStore();
        nodeStore.setDocId(ObjectId.get());

        nodeStore.addLabel(plunkNodeDTO.getNodeName());
        nodeStore.setKey(new CsiUUID().toString());
        nodeStore.addType(plunkNodeDTO.getNodeType());
        nodeStore.setPlunked(true);
        setNodeValuesFromNodeDef(nodeStore, plunkNodeDTO.getNodeDef());

        return nodeStore;
    }

    private void setNodeValuesFromNodeDef(NodeStore nodeStore, NodeDef nodeDef) {
        Map<String, FieldDef> attributeDefsAsMap = nodeDef.getAttributeDefsAsMap();
        setSizeFromNodeDef(nodeStore, attributeDefsAsMap);
        setTransparencyFromNodeDef(nodeStore, attributeDefsAsMap);
    }

    private void setSizeFromNodeDef(NodeStore nodeStore, Map<String, FieldDef> attributeDefsAsMap) {
        FieldDef fieldDef = attributeDefsAsMap.get(ObjectAttributes.CSI_INTERNAL_SIZE);
        if (fieldDef == null) {
            return;
        }

        if (fieldDef.isAnonymous()) {
            nodeStore.setSizeMode(ObjectAttributes.CSI_INTERNAL_SIZE_BY_SIZE);
            nodeStore.setRelativeSize(Double.valueOf(fieldDef.getStaticText()));
        }
    }

    private void setTransparencyFromNodeDef(NodeStore nodeStore, Map<String, FieldDef> attributeDefsAsMap) {
        FieldDef fieldDef = attributeDefsAsMap.get(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);
        if (fieldDef == null) {
            return;
        }

        if (fieldDef.isAnonymous()) {
            nodeStore.setTransparency(Double.valueOf(fieldDef.getStaticText()));
        }
    }

}
