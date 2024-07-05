package csi.server.business.visualization.map;

import csi.server.common.model.map.Geometry;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.shared.core.visualization.map.MapSummaryExtent;

import java.util.Set;

public interface MapSummaryGrid {
    void setNewestInternalTypeId(int newestInternalTypeIdd);

    void addNode(Geometry geometry, int typeId, int internalStateId, PlaceidTypenameDuple key);

    Set<Geometry> getGeometriesOfType(PlaceidTypenameDuple key);

    Set<Integer> getRowIds(Geometry geometry);

    Set<Geometry> getDescendants(Geometry geometry);

    Set<Geometry> getDescendants(MapSummaryExtent mapSummaryExtent);

    Set<Geometry> getCombinedTypeGeometries();

    Set<Geometry> getNewTypeGeometries();

    Set<Geometry> getUpdatedTypeGeometries();

    Set<PlaceidTypenameDuple> getTypes();

    boolean hasCombinedType();

    int getNewCount();

    int getUpdateCount();

    Set<Geometry> getDescendants();
}
