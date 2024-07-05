package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import csi.map.controller.model.Association;
import csi.map.controller.model.Feature;
import csi.map.controller.model.Layer;
import csi.map.controller.model.LayerHolder;
import csi.map.controller.model.Payload;
import csi.map.controller.model.TypeIdIconUrlPair;
import csi.map.controller.model.XYPair;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLink;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.shared.core.visualization.map.MapSummaryExtent;

class TrackDetailPointBuilder extends PointBuilder {
    private List<Feature> features;
    private List<Association> associations;
    private LayerHolder layerHolder = new LayerHolder();

    TrackDetailPointBuilder(Payload payload, MapCacheHandler mapCacheHandler, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
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
        processNodesAndLinks2();
        payload.setItemsInViz(mapCacheHandler.getItemsInViz());
        payload.setUseSummary();
        payload.setSummaryLevel(mapCacheHandler.getCurrentMapSummaryPrecision());
    }

    private void processNodesAndLinks2() {
        AbstractMapSelection mapSelection = mapCacheHandler.getMapSelection();
        Integer sn = null;
        if (mapSelection != null) {
            sn = mapSelection.getSequenceNumber();
            mapSelection.registerReader(sn);
        }
        if (mapCacheHandler.isLinkLimitReached()) {
            payload.setLinkLimitReached(true);
        } else {
            Association selectedAssociation = new Association(-1);
            Map<TrackidTracknameDuple, List<MapLink>> mapLinkByKey = mapCacheHandler.getMapLinkByKey();
            Map<TrackidTracknameDuple, Integer> trackTypenameToId = mapCacheHandler.getTrackTypenameToId();
            if (mapLinkByKey != null) {
                Map<Integer, TrackidTracknameDuple> trackIdToKey = new TreeMap<Integer, TrackidTracknameDuple>();
                mapLinkByKey.keySet().forEach(key -> {
                    Integer trackId = trackTypenameToId.get(key);
                    trackIdToKey.put(trackId, key);
                });
                List<Long> keyList = new ArrayList<Long>();
                Map<Long, TrackidTracknameDuple> nodeIdToTrackType = new HashMap<Long, TrackidTracknameDuple>();
                Set<Long> used = new HashSet<Long>();

                Map<Integer, Association> mapAssociationByTypeId = new TreeMap<Integer, Association>(Collections.reverseOrder());
                trackIdToKey.forEach((trackId, key) -> {
                    Association association = new Association(trackId);
                    mapAssociationByTypeId.put(trackId, association);
                    for (MapLink link : mapLinkByKey.get(key)) {
                        long sourceNodeId = link.getSourceNode().getNodeId();
                        if (!used.contains(sourceNodeId)) {
                            used.add(sourceNodeId);
                            keyList.add(sourceNodeId);
                            nodeIdToTrackType.put(sourceNodeId, key);
                        }
                        long destinationNodeId = link.getDestinationNode().getNodeId();
                        if (!used.contains(destinationNodeId)) {
                            used.add(destinationNodeId);
                            keyList.add(destinationNodeId);
                            nodeIdToTrackType.put(destinationNodeId, key);
                        }
                        if (mapCacheHandler.isSelected(mapSelection, link)) {
                            selectedAssociation.addSegment(sourceNodeId, destinationNodeId);
                        }
                        association.addSegment(sourceNodeId, destinationNodeId);
                    }
                });
                associations.addAll(mapAssociationByTypeId.values());

                Collections.reverse(keyList);
                for (Long key : keyList) {
                    if ((mapSelection != null) && mapSelection.isLockedByWriter()) {
                     break;
                  }
                    AugmentedMapNode mapNode = mapNodeMapById.get(key);
                    if (mapNode != null) {
                        TrackidTracknameDuple trackType = nodeIdToTrackType.get(key);
                        addNode(mapSelection, key, mapNode, trackType);
                    }
                }
            }
            associations.add(selectedAssociation);
        }
        AbstractPayloadBuilder.transfer(layerHolder, payload.getLayer());
        if (mapSelection != null) {
         mapSelection.deregisterReader(sn);
      }
    }

    private void addNode(AbstractMapSelection mapSelection, Long key, AugmentedMapNode mapNode, TrackidTracknameDuple track) {
        XYPair geometry = new XYPair(mapNode.getGeometry().getX(), mapNode.getGeometry().getY());
        PlaceidTypenameDuple placeType = mapNode.getPlaceTypeForTrack(track);
        Integer typeId = placeTypenameToId.get(placeType);
        Feature feature = new Feature(geometry, key.toString(), typeId);
        feature.getAttributes().setHits(mapNode.getHits());
        AbstractPayloadBuilder.attachLabel(mapNode, feature);
        String iconUrl = mapNode.getIconUri(placeType);
        if (iconUrl != null) {
         layerHolder.getTypeIdIconUrlPairs().add(new TypeIdIconUrlPair(typeId, iconUrl));
      }
        PointBuilder.addToLayerHolder(mapCacheHandler, mapSelection, key, mapNode, layerHolder, feature, mapNode.getSize(placeType));
        features.add(feature);
    }
}
