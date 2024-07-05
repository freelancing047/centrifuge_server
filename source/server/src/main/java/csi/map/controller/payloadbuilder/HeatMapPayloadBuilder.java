package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.map.controller.model.Feature;
import csi.map.controller.model.XYPair;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.HeatMapInfo;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class HeatMapPayloadBuilder extends AbstractPayloadBuilder {
    private HeatMapInfo heatMapInfo;

    public HeatMapPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent) {
        super(mapCacheHandler, extent);
    }

    public HeatMapPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
    }

    public void setHeatMapInfo(HeatMapInfo heatMapInfo) {
        this.heatMapInfo = heatMapInfo;
    }

    @Override
    protected void decorateWithMapCache() {
        AbstractMapSelection mapSelection = mapCacheHandler.getMapSelection();
        Integer sn = null;
        if (mapSelection != null) {
            sn = mapSelection.getSequenceNumber();
            mapSelection.registerReader(sn);
        }
        List<Feature> features = payload.getLayer().getFeatures();
        Map<Long, AugmentedMapNode> mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
        Map<Integer, List<Feature>> selectedUncombined = new HashMap<Integer, List<Feature>>();
        Map<Integer, List<Feature>> unselectedUncombined = new HashMap<Integer, List<Feature>>();

        if (mapNodeMapById != null) {
            Set<Long> keys = mapNodeMapById.keySet();
            List<Long> keyList = new ArrayList<Long>(keys);
            Collections.sort(keyList);
            Collections.reverse(keyList);
            for (Long key : keyList) {
                if ((mapSelection != null) && mapSelection.isLockedByWriter()) {
                    break;
                }
                AugmentedMapNode mapNode = mapNodeMapById.get(key);
                if (mapNode != null) {
                    XYPair geometry = new XYPair(mapNode.getGeometry().getX(), mapNode.getGeometry().getY());
                    Feature feature = new Feature(geometry, key.toString(), 0);
                    feature.getAttributes().setHits(mapNode.getHits());
                    attachLabel(mapNode, feature);
                    if (mapCacheHandler.isSelected(mapSelection, mapNode)) {
                        layerHolder.addSelectedFeature(key.toString());
                        addFeatureInto(selectedUncombined, feature);
                    } else {
                        addFeatureInto(unselectedUncombined, feature);
                    }
                }
            }
        }

        if (mapSelection != null) {
            mapSelection.deregisterReader(sn);
        }
        incorporateInto(features, unselectedUncombined, mapCacheHandler);
        incorporateInto(features, selectedUncombined, mapCacheHandler);
        transfer(layerHolder, payload.getLayer());
        payload.setItemsInViz(mapCacheHandler.getItemsInViz());
        payload.setPointLimitReached(mapCacheHandler.isPointLimitReached());
    }

    @Override
    protected void decorateWithOthers() {
        payload.setBlurValue(heatMapInfo.getBlurValue());
        payload.setMaxValue(heatMapInfo.getMaxValue());
        payload.setMinValue(heatMapInfo.getMinValue());
    }

    @Override
    protected void decorateWithMapSettings() {
        payload.setHeatmapColors(mapCacheHandler.getMapSettings().getHeatmapColors());
    }
}
