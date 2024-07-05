package csi.server.business.visualization.map.cacheloader.pointcounter;

import java.util.Set;
import java.util.TreeSet;

import csi.config.Configuration;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.LinkGeometry;
import csi.shared.core.visualization.map.MapSummaryExtent;
import csi.shared.core.visualization.map.UBox;

public class TrackPointCounterByMapSummaryGrid extends AbstractPointCounter {
    private static final int DETAIL_LEVEL = Configuration.getInstance().getMapConfig().getDetailLevel();
    private static int LINK_LIMIT = Configuration.getInstance().getMapConfig().getLinkLimit();

    private TrackMapSummaryGrid mapSummaryGrid;
    private Set<Geometry> geometries;
    private Integer numLinks;
    private MapSummaryExtent mapSummaryExtent;
    private boolean hasRange = false;
    private TrackMapSummaryGrid.SequenceSortValue rangeStartCriterion;
    private TrackMapSummaryGrid.SequenceSortValue rangeEndCriterion;

    TrackPointCounterByMapSummaryGrid(MapCacheHandler mapCacheHandler, UBox uBox) {
        super(mapCacheHandler);
        Integer rangeStart;
        Integer rangeEnd;
        Integer rangeSize;
        if (uBox == null) {
            mapSummaryExtent = null;
            rangeStart = null;
            rangeEnd = null;
            rangeSize = null;
        } else {
            mapSummaryExtent = uBox.getMapSummaryExtent();
            rangeStart = uBox.getRangeStart();
            rangeEnd = uBox.getRangeEnd();
            rangeSize = uBox.getRangeSize();
        }
        if ((rangeStart != null) && (rangeEnd != null) && (rangeSize != null) && ((rangeStart != 0) || (rangeEnd != (rangeSize - 1)))) {
            hasRange = true;
        }
        mapSummaryGrid = mapCacheHandler.getTrackMapSummaryGrid();
        if (mapSummaryGrid != null) {
            if (hasRange) {
                rangeStartCriterion = mapSummaryGrid.getSequenceValue(rangeStart);
                rangeEndCriterion = mapSummaryGrid.getSequenceValue(rangeEnd);
                if ((rangeStartCriterion == null) || (rangeEndCriterion == null)) {
                    rangeStartCriterion = null;
                    rangeEndCriterion = null;
                    hasRange = false;
                }
            }

            if (mapSummaryExtent == null) {
                if (hasRange) {
                    geometries = mapSummaryGrid.getDescendants(rangeStartCriterion, rangeEndCriterion);
                } else {
                    geometries = mapSummaryGrid.getDescendants();
                }
            } else if (mapSummaryExtent.getXMin() < -180) {
                MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMax());
                MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin() + 360, mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                if (hasRange) {
                    geometries = mapSummaryGrid.getDescendants(fromNeg180, rangeStartCriterion, rangeEndCriterion);
                    geometries.addAll(mapSummaryGrid.getDescendants(toPos180, rangeStartCriterion, rangeEndCriterion));
                } else {
                    geometries = mapSummaryGrid.getDescendants(fromNeg180);
                    geometries.addAll(mapSummaryGrid.getDescendants(toPos180));
                }
            } else {
                if (hasRange) {
                    geometries = mapSummaryGrid.getDescendants(mapSummaryExtent, rangeStartCriterion, rangeEndCriterion);
                } else {
                    geometries = mapSummaryGrid.getDescendants(mapSummaryExtent);
                }
            }
        }

    }

    @Override
   public void calculatePrecision(int startingPrecision) {
        precision = DETAIL_LEVEL;
        numPoints = getNumPoints();
        if (numPoints == 0) {
            coarsestPrecision = DETAIL_LEVEL;
        } else {
            if (numPoints < POINT_LIMIT) {
//                if (getNumLinks() > LINK_LIMIT)
//                    precision--;
                coarsestPrecision = precision;
            } else {
                precision = startingPrecision;
                getCorrectPrecision();
                if ((coarsestPrecision == DETAIL_LEVEL) && (getNumLinks() > LINK_LIMIT)) {
                  coarsestPrecision--;
               }
            }
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

    private int getNumLinks() {
        if (numLinks == null) {
            if (mapSummaryGrid != null) {
                if (mapSummaryExtent == null) {
                    Set<LinkGeometry> linkGeometries;
                    if (hasRange) {
                        linkGeometries = mapSummaryGrid.getLinkDescendants(rangeStartCriterion, rangeEndCriterion);
                    } else {
                        linkGeometries = mapSummaryGrid.getAllLinks();
                    }
                    numLinks = linkGeometries.size();
                } else if (mapSummaryExtent.getXMin() < -180) {
                    MapSummaryExtent fromNeg180 = new MapSummaryExtent(-180.0, mapSummaryExtent.getYMin(), mapSummaryExtent.getXMax(), mapSummaryExtent.getYMax());
                    MapSummaryExtent toPos180 = new MapSummaryExtent(mapSummaryExtent.getXMin() + 360, mapSummaryExtent.getYMin(), 180.0, mapSummaryExtent.getYMax());
                    Set<LinkGeometry> linkGeometries;
                    if (hasRange) {
                        linkGeometries = mapSummaryGrid.getLinkDescendants(fromNeg180, rangeStartCriterion, rangeEndCriterion);
                        linkGeometries.addAll(mapSummaryGrid.getLinkDescendants(toPos180, rangeStartCriterion, rangeEndCriterion));
                    } else {
                        linkGeometries = mapSummaryGrid.getLinkDescendants(fromNeg180);
                        linkGeometries.addAll(mapSummaryGrid.getLinkDescendants(toPos180));
                    }
                    numLinks = linkGeometries.size();
                } else {
                    Set<LinkGeometry> linkGeometries;
                    if (hasRange) {
                        linkGeometries = mapSummaryGrid.getLinkDescendants(mapSummaryExtent, rangeStartCriterion, rangeEndCriterion);
                    } else {
                        linkGeometries = mapSummaryGrid.getLinkDescendants(mapSummaryExtent);
                    }
                    numLinks = linkGeometries.size();
                }
            }
        }
        if (numLinks == null) {
         return 0;
      }
        return numLinks;
    }
}
