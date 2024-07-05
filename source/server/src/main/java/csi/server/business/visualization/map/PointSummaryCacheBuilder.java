package csi.server.business.visualization.map;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.pointcounter.AbstractPointCounter;
import csi.server.business.visualization.map.cacheloader.pointmap.AbstractDetailPointCacheLoader;
import csi.server.business.visualization.map.cacheloader.pointmap.SummaryPointCacheLoader;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

public class PointSummaryCacheBuilder implements SummaryCacheBuilder {
    private static final Logger LOG = LogManager.getLogger(PointSummaryCacheBuilder.class);

    private long id;

    private MapCacheHandler mapCacheHandler;
    private DataView dataView;
    private MapTheme mapTheme;

    private String status;
    private int lowestMapSummaryPrecision;
    private MapViewDef mapViewDef;
    private int currentMapPrecision;
    private AbstractMapCacheLoader mapCacheLoader;
    private UBox uBox;

    public PointSummaryCacheBuilder(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) {
        this.mapCacheHandler = mapCacheHandler;
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
        this.mapTheme = mapTheme;
        this.uBox = uBox;
        id = UUIDUtil.getUUIDLong();
    }

    public void build() {
        init();
        if (needToCalculateAndSetCurrentMapPrecision()) {
         calculateAndSetCurrentMapPrecision();
      }
        loadCache();
    }

    private void init() {
        status = "failure";
        lowestMapSummaryPrecision = mapCacheHandler.getCoarsestMapSummaryPrecision();
    }

    private boolean needToCalculateAndSetCurrentMapPrecision() {
        return uBox.getMapSummaryExtent() != null;
    }

    private void calculateAndSetCurrentMapPrecision() {
        AbstractPointCounter counter = AbstractPointCounter.make(mapCacheHandler, dataView, mapViewDef, uBox);
        counter.calculatePrecision(lowestMapSummaryPrecision);
        currentMapPrecision = counter.getCoarsestPrecision();
        mapCacheHandler.setCurrentMapSummaryPrecision(currentMapPrecision);
    }

    private void loadCache() {
        try {
            if (shouldLoadDetailCache()) {
               loadDetailCache();
            } else {
               loadSummaryCache();
            }
            status = "success";
        } catch (CentrifugeException e) {
           LOG.error(e.getMessage());
        }
    }

    private boolean shouldLoadDetailCache() {
        return mapCacheHandler.isCurrentlyAtDetailLevel();
    }

    private void loadDetailCache() throws CentrifugeException {
        mapCacheHandler.invalidateMapNodeInfoAndMapLinkInfo();
        mapCacheLoader = AbstractDetailPointCacheLoader.make(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        mapCacheLoader.load();
    }

    private void loadSummaryCache() throws CentrifugeException {
        mapCacheLoader = new SummaryPointCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        ((SummaryPointCacheLoader) mapCacheLoader).setPrecision(currentMapPrecision);
        mapCacheLoader.load();
    }

    public String getStatus() {
        return status;
    }

    public boolean equals(Object other) {
        if (other instanceof PointSummaryCacheBuilder) {
            PointSummaryCacheBuilder other2 = (PointSummaryCacheBuilder) other;
            return this.id == other2.id;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public UBox getUBox() {
        return uBox;
    }

}
