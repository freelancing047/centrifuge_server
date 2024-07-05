package csi.server.business.visualization.map;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.config.Configuration;
import csi.server.business.visualization.map.cacheloader.AbstractMapCacheLoader;
import csi.server.business.visualization.map.cacheloader.pointcounter.AbstractPointCounter;
import csi.server.business.visualization.map.cacheloader.trackmap.AbstractTrackDetailPointCacheLoader;
import csi.server.business.visualization.map.cacheloader.trackmap.TrackSummaryPointCacheLoader;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

public class TrackSummaryCacheBuilder implements SummaryCacheBuilder {
    private static final Logger LOG = LogManager.getLogger(TrackSummaryCacheBuilder.class);

    private long id;

    private DataView dataView;
    private MapTheme mapTheme;

    private String status;
    private int lowestMapSummaryPrecision;
    private MapViewDef mapViewDef;
    private int currentMapPrecision;
    private AbstractMapCacheLoader mapCacheLoader;

    private UBox uBox;
    private MapCacheHandler mapCacheHandler;

    public TrackSummaryCacheBuilder(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, MapTheme mapTheme, UBox uBox) {
        this.mapCacheHandler = mapCacheHandler;
        this.dataView = dataView;
        this.mapViewDef = mapViewDef;
        this.mapTheme = mapTheme;
        this.uBox = uBox;
        id = UUIDUtil.getUUIDLong();
    }

    @Override
   public void build() {
        init();
        calculateAndSetCurrentMapPrecision();
        loadCache();
    }

    private void init() {
        status = "failure";
        lowestMapSummaryPrecision = mapCacheHandler.getCoarsestMapSummaryPrecision();
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
                if (mapCacheHandler.isPointLimitReached()) {
                    mapCacheHandler.setPointLimitReached(false);
                    currentMapPrecision = Configuration.getInstance().getMapConfig().getDetailLevel() - 1;
                    mapCacheHandler.setCurrentMapSummaryPrecision(currentMapPrecision);
                    loadSummaryCache();
                }
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
        mapCacheLoader = AbstractTrackDetailPointCacheLoader.make(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        mapCacheLoader.load();
    }

    private void loadSummaryCache() throws CentrifugeException {
        mapCacheLoader = new TrackSummaryPointCacheLoader(mapCacheHandler, dataView, mapViewDef, mapTheme, uBox);
        ((TrackSummaryPointCacheLoader) mapCacheLoader).setPrecision(currentMapPrecision);
        mapCacheLoader.load();
    }

    @Override
   public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
         return true;
      }
        if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }
        TrackSummaryCacheBuilder that = (TrackSummaryCacheBuilder) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
   public UBox getUBox() {
        return uBox;
    }

}
