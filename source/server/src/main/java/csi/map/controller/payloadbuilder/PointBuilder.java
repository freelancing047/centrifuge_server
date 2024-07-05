package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.map.controller.model.Feature;
import csi.map.controller.model.Layer;
import csi.map.controller.model.LayerHolder;
import csi.map.controller.model.Payload;
import csi.map.controller.model.TypeIdIconUrlPair;
import csi.map.controller.model.XYPair;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapCacheNotAvailable;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.server.common.model.visualization.selection.TrackmapSelection;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class PointBuilder {
    private static final Logger LOG = LogManager.getLogger(PointBuilder.class);

    protected MapSummaryExtent mapSummaryExtent;
    protected Integer rangeStart;
    protected Integer rangeEnd;
    protected Integer rangeSize;
    protected Payload payload;
    MapCacheHandler mapCacheHandler;
    Map<PlaceidTypenameDuple, Integer> placeTypenameToId;
    Map<Integer, List<Feature>> unselectedUncombined = new HashMap<Integer,List<Feature>>();
    Map<Long, AugmentedMapNode> mapNodeMapById;
    private LayerHolder layerHolder = new LayerHolder();

    PointBuilder(MapCacheHandler mapCacheHandler, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize, Payload payload) {
        this.mapCacheHandler = mapCacheHandler;
        this.mapSummaryExtent = mapSummaryExtent;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.rangeSize = rangeSize;
        this.payload = payload;
    }

    public static void transfer(LayerHolder layerHolder, Layer layer) {
        layer.getSelectedFeatures().addAll(layerHolder.getSelectedFeatures());
        layer.getCombinedFeatures().addAll(layerHolder.getCombinedFeatures());
        layer.getNewFeatures().addAll(layerHolder.getNewFeatures());
        layer.getUpdatedFeatures().addAll(layerHolder.getUpdatedFeatures());
        layer.getTypeIdIconUrlPairs().addAll(layerHolder.getTypeIdIconUrlPairs());
    }

    static Geometry getSummaryGeometry(double scale, Geometry geometry) {
        double x = Math.round(geometry.getX() * scale) / scale;
        double y = Math.round(geometry.getY() * scale) / scale;
        return new Geometry(x, y);
    }

    private static void addNode(MapCacheHandler mapCacheHandler, AbstractMapSelection mapSelection, Long key, AugmentedMapNode mapNode, TreeSet<Integer> typeIds, LayerHolder layerHolder, Map<PlaceidTypenameDuple, Integer> placeTypenameToId, Map<Integer, List<Feature>> unselectedUncombined) {
        XYPair geometry = new XYPair(mapNode.getGeometry().getX(), mapNode.getGeometry().getY());
        Feature feature = new Feature(geometry, key.toString(), typeIds.first());
        feature.getAttributes().setHits(mapNode.getHits());
        AbstractPayloadBuilder.attachLabel(mapNode, feature);
        String iconUrl = mapNode.getIconUri(placeTypenameToId);
		if (iconUrl != null) {
            iconUrl = iconUrl.replace("?", "_CQMC_");
            iconUrl = iconUrl.replace("&", "_CAMPC_");
            iconUrl = iconUrl.replace("%", "_CPERC_");
		}
        if (iconUrl != null) {
            layerHolder.getTypeIdIconUrlPairs().add(new TypeIdIconUrlPair(typeIds.first(), iconUrl));
		}
        addToLayerHolder(mapCacheHandler, mapSelection, key, mapNode, layerHolder, feature, mapNode.getSize(placeTypenameToId));
        AbstractPayloadBuilder.addFeatureInto(unselectedUncombined, feature);
    }

    static void addToLayerHolder(MapCacheHandler mapCacheHandler, AbstractMapSelection mapSelection, Long key, AugmentedMapNode mapNode, LayerHolder layerHolder, Feature feature, Integer size) {
        feature.getAttributes().setSize(size);
        if (mapCacheHandler.isSelected(mapSelection, mapNode) && !(mapSelection instanceof TrackmapSelection)) {
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
    }

    static List<Long> getSortedKeyList(Map<Long, AugmentedMapNode> mapNodeMapById) {
        if (mapNodeMapById == null) {
            throw new MapCacheNotAvailable();
        }
        Set<Long> keys = mapNodeMapById.keySet();
        List<Long> keyList = new ArrayList<Long>(keys);
        Collections.sort(keyList);
        Collections.reverse(keyList);
        return keyList;
    }

    void processNodesAndLinks() {
        AbstractMapSelection mapSelection = mapCacheHandler.getMapSelection();
        Integer sn = null;
        if (mapSelection != null) {
            sn = mapSelection.getSequenceNumber();
            mapSelection.registerReader(sn);
        }
        processNodes(mapSelection);
        processLinks(mapSelection);
        AbstractPayloadBuilder.transfer(layerHolder, payload.getLayer());
        if (mapSelection != null) {
         mapSelection.deregisterReader(sn);
      }
    }

    private void processNodes(AbstractMapSelection mapSelection) {
        List<Long> keyList = getSortedKeyList(mapNodeMapById);

//        if (mapSelection != null && mapSelection.isLockedByWriter())
//            break;

        for (Long key : keyList) {
            AugmentedMapNode mapNode = mapNodeMapById.get(key);
            if (mapNode != null) {
                TreeSet<Integer> typeIds = mapNode.getTypeIds(placeTypenameToId);
                if (typeIds.isEmpty()) {
                    LOG.debug("mapNode with no type found");
                }
                addNode(mapSelection, key, mapNode, typeIds);
            }
        }
    }

    protected void addNode(AbstractMapSelection mapSelection, Long key, AugmentedMapNode mapNode, TreeSet<Integer> typeIds) {
        addNode(mapCacheHandler, mapSelection, key, mapNode, typeIds, layerHolder, placeTypenameToId, unselectedUncombined);
    }

    protected void processLinks(AbstractMapSelection mapSelection) {
    }
}
