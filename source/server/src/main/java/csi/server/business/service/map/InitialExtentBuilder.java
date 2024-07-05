package csi.server.business.service.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.config.Configuration;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNode;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.SpatialReference;

public class InitialExtentBuilder {
    private MapCacheHandler mapCacheHandler;
    private Map<Long, AugmentedMapNode> mapNodeMapById;
    private Extent initialExtent;

    public InitialExtentBuilder(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
    }

    public void build() {
        init();
        if (canBuild()) {
         buildInitialExtent();
      }
    }

    public void init() {
        getMapNodeMapById();
        initialExtent = null;
    }

    private void getMapNodeMapById() {
        if (mapCacheHandler.isUseTrackMap()) {
            int precision = mapCacheHandler.getCoarsestMapSummaryPrecision();
            if (precision == Configuration.getInstance().getMapConfig().getDetailLevel()) {
                mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
            } else {
                mapNodeMapById = mapCacheHandler.getHomeTrackMapNodeByIdMap();
            }
        } else {
            mapNodeMapById = mapCacheHandler.getMapNodeByIdMap();
        }
    }

    private boolean canBuild() {
        return (mapNodeMapById != null) && !mapNodeMapById.isEmpty();
    }

    private void buildInitialExtent() {
        createAndInitializeInitialExtent();
        calculateExtent();
        calculateZoom();
    }

    private void createAndInitializeInitialExtent() {
        initialExtent = new Extent();
        SpatialReference spatialReference = new SpatialReference();
        spatialReference.setWkid(4326);
        initialExtent.setSpatialReference(spatialReference);
    }

    private void calculateExtent() {
        addPointsToExtent();
        Extent.expandExtentIfTooSmall(initialExtent);
    }

    private void addPointsToExtent() {
        List<Long> keyList = generateKeyList();
        for (Long key : keyList) {
            MapNode mapNode = mapNodeMapById.get(key);
            addPointToExtent(mapNode);
        }
    }

    private List<Long> generateKeyList() {
        Set<Long> keys = mapNodeMapById.keySet();
        List<Long> keyList = new ArrayList<Long>(keys);
        Collections.sort(keyList);
        Collections.reverse(keyList);
        return keyList;
    }

    private void addPointToExtent(MapNode mapNode) {
        if (mapNode != null) {
         initialExtent.addPoint(mapNode.getGeometry());
      }
    }

    private void calculateZoom() {
//		int xZoom = calculateXZoom(initialExtent.getXMin(), initialExtent.getXMax());
//		int yZoom = calculateYZoom(initialExtent.getYMin(), initialExtent.getYMax());
//		initialExtent.setZoom(Math.min(xZoom, yZoom));
        initialExtent.setZoom(-1);
    }

    private int calculateXZoom(double min, double max) {
        return getZoom(getXPortionOfWorld(min, max));
    }

    private int getZoom(double portionOfWorld) {
        int zoom = 0;
        double zoomPortionOfWorld = 1 / Math.pow(2, zoom);
        while (zoomPortionOfWorld > portionOfWorld) {
            zoom++;
            zoomPortionOfWorld = 1 / Math.pow(2, zoom);
        }
        return zoom;
    }

    private double getXPortionOfWorld(double min, double max) {
        double diff = max - min;
        return diff / 360;
    }

    private int calculateYZoom(double min, double max) {
        return getZoom(getYPortionOfWorld(min, max));
    }

    private double getYPortionOfWorld(double min, double max) {
        double newMin = Math.log(Math.tan(((90 + min) * Math.PI) / 360));
        double newMax = Math.log(Math.tan(((90 + max) * Math.PI) / 360));
        double diff = newMax - newMin;
        return diff / 4;
    }

    public Extent getInitialExtent() {
        return initialExtent;
    }
}