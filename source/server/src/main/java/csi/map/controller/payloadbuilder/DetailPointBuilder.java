package csi.map.controller.payloadbuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.map.controller.model.Association;
import csi.map.controller.model.Feature;
import csi.map.controller.model.Layer;
import csi.map.controller.model.Payload;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLink;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.shared.core.visualization.map.MapSummaryExtent;

class DetailPointBuilder extends PointBuilder {
    private List<Feature> features;
    private List<Association> associations;
    private Map<Integer,List<Feature>> selectedUncombined = new HashMap<Integer,List<Feature>>();

    DetailPointBuilder(Payload payload, MapCacheHandler mapCacheHandler, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        super(mapCacheHandler, mapSummaryExtent, rangeStart, rangeEnd, rangeSize, payload);
        init();
    }

    private void init() {
        mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
        Layer layer = payload.getLayer();
        features = layer.getFeatures();
        associations = layer.getAssociations();
        placeTypenameToId = mapCacheHandler.getPlaceTypenameToId();
    }

    public void build() {
        processNodesAndLinks();
        payload.setItemsInViz(mapCacheHandler.getItemsInViz());
        AbstractPayloadBuilder.incorporateInto(features, selectedUncombined, unselectedUncombined, mapCacheHandler);
        payload.setUseSummary();
        payload.setSummaryLevel(mapCacheHandler.getCurrentMapSummaryPrecision());
    }

    @Override
    protected void processLinks(AbstractMapSelection mapSelection) {
        if (mapCacheHandler.isLinkLimitReached()) {
            payload.setLinkLimitReached(true);
        } else {
            Association selectedAssociation = new Association(-1);
            Map<String, List<MapLink>> mapLinkByType = mapCacheHandler.getMapLinkByTypeMap();
            if (mapLinkByType != null) {
                for (List<MapLink> value : mapLinkByType.values()) {
                    Integer typeId = null;
                    Association association = null;
                    for (MapLink link : value) {
                        if (typeId == null) {
                            typeId = link.getTypeId();
                            association = new Association(typeId);
                            associations.add(association);
                        }
                        if (mapCacheHandler.isSelected(mapSelection, link)) {
                            selectedAssociation.addSegment(link.getSourceNode().getNodeId(), link.getDestinationNode().getNodeId());
                        }
                        association.addSegment(link.getSourceNode().getNodeId(), link.getDestinationNode().getNodeId());
                    }
                }
            }
            associations.add(selectedAssociation);
        }
    }
}
