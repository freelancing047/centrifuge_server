package csi.server.business.visualization.map.cacheloader.pointcounter;

import csi.config.Configuration;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.UBox;

public abstract class AbstractPointCounter {
    static final int POINT_LIMIT = Configuration.getInstance().getMapConfig().getPointLimit();

    int numPoints;
    int precision;
    int coarsestPrecision;
    MapCacheHandler mapCacheHandler;
    private int lowerLimit = -1;
    private int upperLimit = 5;

    AbstractPointCounter(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
    }

    public static AbstractPointCounter make(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, UBox uBox) {
        if (mapCacheHandler.isUseTrackMap()) {
            if (mapCacheHandler.outOfBandResourceBuilding() || (mapCacheHandler.getMapSummaryGrid() == null)) {
                return new TrackPointCounterByCountGrid(mapCacheHandler, dataView, mapViewDef, uBox);
            } else {
                return new TrackPointCounterByMapSummaryGrid(mapCacheHandler, uBox);
            }
        } else {
            if (mapCacheHandler.outOfBandResourceBuilding() || (mapCacheHandler.getMapSummaryGrid() == null)) {
                return new PointCounterByCountGrid(mapCacheHandler, dataView, mapViewDef, uBox);
            } else {
                return new PointCounterByMapSummaryGrid(mapCacheHandler, uBox);
            }
        }
    }

    public void calculatePrecision() {
        int levels = (upperLimit - lowerLimit) + 1;
        calculatePrecision(lowerLimit + (levels / 2));
    }

    public abstract void calculatePrecision(int startingPrecision);

    void getCorrectPrecision() {
        numPoints = getNumPoints();
        if (numPoints != 0) {
            if (numPoints > POINT_LIMIT) {
                upperLimit = precision - 1;
                getLessPrecise();
            } else {
                lowerLimit = precision;
                getMorePrecise();
            }
        } else {
            coarsestPrecision = Configuration.getInstance().getMapConfig().getDetailLevel();
        }
    }

    abstract int getNumPoints();

    private void getLessPrecise() {
        while ((numPoints > POINT_LIMIT) && (precision >= -2)) {
            precision--;
            if (precision == -2) {
               break;
            }
            numPoints = getNumPoints();
        }
        coarsestPrecision = precision;
    }

    private void getMorePrecise() {
        while ((numPoints < POINT_LIMIT) && (precision <= Configuration.getInstance().getMapConfig().getDetailLevel())) {
            precision++;
            if (precision == 7) {
               break;
            }
            numPoints = getNumPoints();
        }
        coarsestPrecision = precision - 1;
    }

    public int getCoarsestPrecision() {
        return coarsestPrecision;
    }
}
