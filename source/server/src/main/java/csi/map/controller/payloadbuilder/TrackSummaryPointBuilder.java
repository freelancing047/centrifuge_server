package csi.map.controller.payloadbuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.map.controller.model.Feature;
import csi.map.controller.model.Layer;
import csi.map.controller.model.LayerHolder;
import csi.map.controller.model.Payload;
import csi.map.controller.model.XYPair;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.model.visualization.selection.AbstractMapSelection;

class TrackSummaryPointBuilder {
   private static final Logger LOG = LogManager.getLogger(TrackSummaryPointBuilder.class);

   private Payload payload;
    private LayerHolder layerHolder = new LayerHolder();
    private MapCacheHandler mapCacheHandler;
    private List<Feature> features;
    private Map<Integer, List<Feature>> selectedUncombined = new HashMap<Integer, List<Feature>>();
    private Map<Integer, List<Feature>> unselectedUncombined = new HashMap<Integer, List<Feature>>();

    TrackSummaryPointBuilder(Payload payload, MapCacheHandler mapCacheHandler) {
        this.payload = payload;
        this.mapCacheHandler = mapCacheHandler;
        init();
    }

    private void init() {
        if (mapCacheHandler.usingLatestMapCache()) {
            Layer layer = payload.getLayer();
            features = layer.getFeatures();
        }
    }

    public void build() {
        processNodesAndLinks();
        payload.setItemsInViz(mapCacheHandler.getItemsInViz());
        AbstractPayloadBuilder.incorporateInto(features, selectedUncombined, unselectedUncombined, mapCacheHandler);
        payload.setUseSummary();
        payload.setSummaryLevel(mapCacheHandler.getCurrentMapSummaryPrecision());
    }

    private void processNodesAndLinks() {
        if (mapCacheHandler.usingLatestMapCache()) {
            Map<Long, AugmentedMapNode> mapNodeMapById = mapCacheHandler.getCurrentTrackMapSummaryMapNodeByIdMap();
            List<Long> keyList = PointBuilder.getSortedKeyList(mapNodeMapById);

            AbstractMapSelection mapSelection = mapCacheHandler.getMapSelection();
            Integer sn = null;
            if (mapSelection != null) {
                sn = mapSelection.getSequenceNumber();
                mapSelection.registerReader(sn);
            }
            Set<Geometry> selectedSummaryGeometries = new HashSet<Geometry>();
            int summaryLevel = mapCacheHandler.getCurrentMapSummaryPrecision();
            double scale = Math.pow(10, summaryLevel);
            if ((mapSelection != null) && (mapSelection.getLinks() != null)) {
                Set<LinkGeometry> linkGeometries = mapSelection.getLinks();
                Set<Geometry> node1Geometries = linkGeometries.stream().map(LinkGeometry::getNode1Geometry).collect(Collectors.toSet());
                Set<Geometry> node2Geometries = linkGeometries.stream().map(LinkGeometry::getNode2Geometry).collect(Collectors.toSet());
                node1Geometries.addAll(node2Geometries);
                node1Geometries.forEach(geometry -> selectedSummaryGeometries.add(getSummaryGeometry(scale, geometry)));
            }
            Map<TrackidTracknameDuple, Integer> trackKeyToId = mapCacheHandler.getTrackDynamicTypeInfo().getTrackKeyToId();
            for (Long key : keyList) {
                if ((mapSelection != null) && mapSelection.isLockedByWriter()) {
                  break;
               }
                AugmentedMapNode mapNode = mapNodeMapById.get(key);
                if (mapNode != null) {
                    TreeSet<Integer> typeIds = mapNode.getIdentityIds(trackKeyToId);
                    if (typeIds.isEmpty()) {
                        LOG.debug("mapNode with no type found");
                    } else {
                        XYPair geometry = new XYPair(mapNode.getGeometry().getX(), mapNode.getGeometry().getY());
                        Feature feature = new Feature(geometry, key.toString(), typeIds.first());
                        feature.getAttributes().setHits(mapNode.getHits());
                        AbstractPayloadBuilder.attachLabel(mapNode, feature);
                        feature.getAttributes().setSize(mapNode.getIdentitySize(trackKeyToId));
                        if (selectedSummaryGeometries.contains(getSummaryGeometry(scale, mapNode.getGeometry()))) {
                           layerHolder.addSelectedFeature(key.toString());
                        }
                        if (mapNode.isCombined()) {
                           layerHolder.addCombinedFeature(key.toString());
                        }
                        if (mapNode.isNew()) {
                           layerHolder.addNewFeature(key.toString());
                        }
                        if (mapNode.isUpdated()) {
                           layerHolder.addUpdatedFeature(key.toString());
                        }
                        AbstractPayloadBuilder.addFeatureInto(unselectedUncombined, feature);
                    }
                }
            }
            AbstractPayloadBuilder.transfer(layerHolder, payload.getLayer());
            if (mapSelection != null) {
               mapSelection.deregisterReader(sn);
            }
        }
    }

    private Geometry getSummaryGeometry(double scale, Geometry geometry) {
        double x = Math.floor(geometry.getX() * scale) / scale;
        double y = Math.floor(geometry.getY() * scale) / scale;
        return new Geometry(x, y);
    }
}
