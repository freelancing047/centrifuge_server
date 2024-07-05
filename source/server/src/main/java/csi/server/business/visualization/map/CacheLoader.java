package csi.server.business.visualization.map;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.BundlemapMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.HeatmapMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.PointmapMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.TrackmapMapCacheLoader;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

public class CacheLoader {
    private static final Logger LOG = LogManager.getLogger(CacheLoader.class);

    private MapTheme mapTheme;

    private String status;
    private DataView dataView;
    private MapViewDef mapViewDef;
    private UBox uBox;
    private MapCacheHandler mapCacheHandler;

    public CacheLoader(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) {
        this.mapCacheHandler = mapCacheHandler;
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
        this.mapTheme = mapTheme;
        this.uBox = uBox;
    }

    public void load() {
        try {
            init();
            loadMap();
            status = "success";
        } catch (CentrifugeException e) {
           LOG.error(e);
        }
    }

    private void init() {
        status = "failure";
        mapCacheHandler.registerMapContextIfNotSet();
        getFromDataStore();
    }

    private void getFromDataStore() {
    }

    private void loadMap() throws CentrifugeException {
        if (mapCacheHandler.isUseHeatMap()) {
         loadHeatMap();
      } else if (MapServiceUtil.isHandleBundle(mapCacheHandler.getDvUuid(),mapCacheHandler.getVizUuid())) {
         loadBundleMap();
      } else if (mapCacheHandler.isUseTrackMap()) {
         loadTrackMap();
      } else {
         loadPointMap();
      }
    }

    private void loadHeatMap() throws CentrifugeException {
        AbstractMapCacheLoader mapCacheLoader = new HeatmapMapCacheLoader(mapCacheHandler, dataView, mapViewDef);
        mapCacheLoader.load();
        mapCacheHandler.setLegendEnabled(false);
        mapCacheHandler.setMultiTypeDecoratorEnabled(false);
    }

    private void loadBundleMap() throws CentrifugeException {
        AbstractMapCacheLoader mapCacheLoader = new BundlemapMapCacheLoader(mapCacheHandler, dataView, mapViewDef);
        mapCacheLoader.load();
        mapCacheHandler.setLegendEnabled(false);
        mapCacheHandler.setMultiTypeDecoratorEnabled(false);
    }

    private void loadTrackMap() throws CentrifugeException {
        mapCacheHandler.setLegendEnabled(true);
        AbstractMapCacheLoader mapCacheLoader = new TrackmapMapCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        mapCacheLoader.load();
        mapCacheHandler.setMultiTypeDecoratorEnabled(true);
    }

    private void loadPointMap() throws CentrifugeException {
        mapCacheHandler.setLegendEnabled(true);
        AbstractMapCacheLoader mapCacheLoader = new PointmapMapCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        mapCacheLoader.load();
        setMultiTypeDecoratorEnabled();
    }

    private void setMultiTypeDecoratorEnabled() {
        Set<AugmentedMapNode> combinedMapNodes = mapCacheHandler.getCombinedMapNodesSet();
        boolean canCreateCombinedMapNodeLegendItem = ((combinedMapNodes != null) && !combinedMapNodes.isEmpty());
        mapCacheHandler.setMultiTypeDecoratorEnabled(canCreateCombinedMapNodeLegendItem);
    }

    public String getStatus() {
        return status;
    }
}
