package csi.server.business.visualization.map.cacheloader.pointcounter;

import java.util.Set;
import java.util.TreeSet;

import csi.config.Configuration;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.common.model.map.Geometry;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.UBox;

public class PointCounterByMapSummaryGrid extends AbstractPointCounter {
    private Set<Geometry> geometries;

    PointCounterByMapSummaryGrid(MapCacheHandler mapCacheHandler, UBox uBox) {
        super(mapCacheHandler);

        MapSummaryExtent mapSummaryExtent;
        if (uBox == null) {
            mapSummaryExtent = null;
        } else {
            mapSummaryExtent = uBox.getMapSummaryExtent();
        }
        MapSummaryGrid mapSummaryGrid = mapCacheHandler.getMapSummaryGrid();
        if (mapSummaryGrid != null) {
            if (mapSummaryExtent == null) {
                geometries = mapSummaryGrid.getDescendants();
            } else if (mapSummaryExtent.getXMin() < -180) {
                MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMax());
                MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin() + 360, mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                geometries = mapSummaryGrid.getDescendants(fromNeg180);
                geometries.addAll(mapSummaryGrid.getDescendants(toPos180));
            } else {
                geometries = mapSummaryGrid.getDescendants(mapSummaryExtent);
            }
        }
    }

    @Override
   public void calculatePrecision(int startingPrecision) {
        precision = Configuration.getInstance().getMapConfig().getDetailLevel();
        numPoints = getNumPoints();
        if (numPoints < POINT_LIMIT) {
         coarsestPrecision = precision;
      } else {
            precision = startingPrecision;
            getCorrectPrecision();
        }
    }

    @Override
   int getNumPoints() {
        if (precision == Configuration.getInstance().getMapConfig().getDetailLevel()) {
         return geometries.size();
      } else {
            int count = 0;
            Set<Long> uniqueValues = new TreeSet<Long>();
            for (Geometry geometry : geometries) {
                double x = geometry.getX();
                x = Math.floor(x * Math.pow(10, precision));
                double y = geometry.getY();
                y = Math.floor(y * Math.pow(10, precision));
                double number = (x * Math.pow(10, 3 + precision)) + y;
                if (uniqueValues.add(Double.doubleToLongBits(number))) {
                  count++;
               }
                if (count > POINT_LIMIT) {
                  break;
               }
            }
            return count;
        }
    }
}
