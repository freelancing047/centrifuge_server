package csi.server.business.service.widget.dataextractor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.business.service.widget.common.data.ClientRequest;
import csi.server.business.service.widget.dataextractor.api.DataExtractor;
import csi.server.business.service.widget.processor.core.GoogleMapsData;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.dao.CsiPersistenceManager;

/**
 * Extracts data in order to be processed and rendered by the google maps widget.
 */
class GoogleMapsDataExtractor implements DataExtractor {

    private Map<String, String> nodesImageUrl = new HashMap<String, String>();

    private static final String GRAPHS_MAP = "graphsMap";

    private static final String ICON = "csi.internal.Icon";
    private static final String SHAPE = "csi.internal.Shape";
    private static final String COLOR = "csi.internal.Color";

    // private static final String GLAT = "glat";
    // private static final String GLNG = "glng";

    public Object extract(HttpServletRequest request, ClientRequest clientRequest) {

        List<String> nodeImagesCache = null;

        // Visualization vis = (Visualization) request.getSession().getAttribute(GRAPH_VIS);
        GoogleMapsData mapsData = (GoogleMapsData) clientRequest.getContent();

        // Relationship Graph UUID received from client
        String rguuid = mapsData.getRguuid();

        Map<String, Visualization> graphsMap = (Map<String, Visualization>) request.getSession().getAttribute(GRAPHS_MAP);

        Visualization viz = graphsMap.get(rguuid);
        Graph graph = (Graph) viz.getSourceData("graph");
        System.out.println(graph);
        List<String[]> data = new ArrayList<String[]>();

        String[] lats = new String[graph.getNodeCount()];
        String[] lngs = new String[graph.getNodeCount()];
        String[] toolTips = new String[graph.getNodeCount()];
        String[] nodeImages = new String[graph.getNodeCount()];

        String dvUuid = (String) graph.getClientProperty("dvUuid");
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        DataModelDef modelDef = dv.getMeta().getModelDef();

        RelGraphViewDef graphDef = (RelGraphViewDef) modelDef.findVisualizationByUuid((String) graph.getClientProperty("viewUuid"));

        boolean selectionActive = false;
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph.getClientProperty("selections");
        if (selections != null) {
            for (SelectionModel selModel : selections.values()) {
                if (!selModel.nodes.isEmpty() || !selModel.links.isEmpty()) {
                    selectionActive = true;
                }

            }
        }

        for (NodeDef nodedef : graphDef.getNodeDefs()) {

            AttributeDef shapeAttribute = nodedef.getAttributeDef(SHAPE);
            AttributeDef attribute = nodedef.getAttributeDef(ICON);
            AttributeDef color = nodedef.getAttributeDef(COLOR);
            try {
                String im = attribute.getFieldDef().getStaticText();
                String sh = shapeAttribute.getFieldDef().getStaticText();
                String cl = color.getFieldDef().getStaticText();

                // Builds the image request for servlet as image=/path/to/image&shape=circle&color=256
                String finalImageRequest =
                   new StringBuilder("image=").append(im).append("&shape=").append(sh).append("&color=").append(cl).toString();

                nodesImageUrl.put(nodedef.getName(), finalImageRequest);
            } catch (NullPointerException e) {
                System.out.println(nodedef.getName());
            }
        }

        Set<String> nodesCacheSet = new HashSet<String>();
        int howMany = graph.getNodeCount();

        for (int id = 0; id < howMany; id++) {
            graph.getClientProperty("nodesByNodeDef");
            Node node = graph.getNode(id);

            NodeStore details = GraphManager.getNodeDetails(node);
            Property glat = details.getAttributes().get(ObjectAttributes.CSI_INTERNAL_LATITUDE);
            Property glong = details.getAttributes().get(ObjectAttributes.CSI_INTERNAL_LONGITUDE);

            lats[id] = null;
            lngs[id] = null;
            String cacheKey = null;
            Object message = (details.getLabel() != null) ? details.getLabel() : "";

            // If latitude and longitude values are not present for nodes then they will be set as null
            // in order to be skipped as node rendering on google map
            if ((glat != null) && (glong != null)) {
                if ((glat.getValues().get(0) != null) && (glong.getValues().get(0) != null)) {

                    cacheKey = String.valueOf(glat.getValues().get(0)) + ',' + String.valueOf(glong.getValues().get(0)) + ',' + message;
                    if (!nodesCacheSet.contains(cacheKey)) {
                        if (!selectionActive) {
                            lats[id] = String.valueOf(glat.getValues().get(0));
                            lngs[id] = String.valueOf(glong.getValues().get(0));
                        } else {
                            if (isSelected(node, graph)) {
                                lats[id] = String.valueOf(glat.getValues().get(0));
                                lngs[id] = String.valueOf(glong.getValues().get(0));
                            }
                        }
                        nodesCacheSet.add(cacheKey);
                    }
                }
            }

            nodeImagesCache = new ArrayList<String>(nodesImageUrl.values());
            // nodeImages[id] = nodesImageUrl.get(details.getSpecID());
            nodeImages[id] = String.valueOf(nodeImagesCache.indexOf(nodesImageUrl.get(details.getSpecID())));

            if ((lats[id] != null) && (lngs[id] != null)) {
                toolTips[id] = buildToolTip(lats[id], lngs[id], message.toString(), nodeImages[id]);
            } else {
                toolTips[id] = "";
            }

        }

        String[] startPoints = new String[graph.getEdgeCount()];
        String[] endPoints = new String[graph.getEdgeCount()];
        Set<String> connectionMap = new HashSet<String>();
        howMany = graph.getEdgeCount();

        for (int id = 0; id < howMany; id++) {
            Edge edge = graph.getEdge(id);
            int row1 = edge.getSourceNode().getRow();
            int row2 = edge.getTargetNode().getRow();

            if (!connectionMap.contains(row1 + "," + row2) && !connectionMap.contains(row2 + "," + row1)) {
                startPoints[id] = String.valueOf(row1);

                endPoints[id] = String.valueOf(row2);
                connectionMap.add(row1 + "," + row2);
            }
        }

        data.add(lats);
        data.add(lngs);
        data.add(startPoints);
        data.add(endPoints);
        data.add(toolTips);
        data.add(nodeImages);
        data.add(nodeImagesCache.toArray(new String[0]));

        return data;
    }

    private boolean isSelected(Node item, Graph graph) {
        boolean isSelected = false;
        int itemRow = item.getRow();
        Map<String, SelectionModel> selections = (Map<String, SelectionModel>) graph.getClientProperty("selections");
        if (selections != null) {
            SelectionModel defSelectionModel = selections.get("default.selection");
            if (defSelectionModel != null) {
                isSelected = defSelectionModel.nodes.contains(itemRow);
            }
        }
        return isSelected;
    }

    /**
     * Constructs the tooltip for nodes
     *
     * @param lat  latitude
     * @param lng  longitude
     * @param text text for node
     * @return HTML string
     */
   private static String buildToolTip(String lat, String lng, String text, String image) {
      return new StringBuilder(lat).append(",")
                       .append(lng).append(",")
                       .append(text.replace(",", "%%"))
                       .append(",").append(image)
                       .append(";").toString();
   }
}
