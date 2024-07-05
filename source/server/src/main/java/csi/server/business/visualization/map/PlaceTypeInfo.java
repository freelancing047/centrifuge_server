package csi.server.business.visualization.map;

import java.util.Map;

import csi.server.common.model.map.PlaceidTypenameDuple;

public interface PlaceTypeInfo {
	Map<PlaceidTypenameDuple, Integer> getTypenameToId();
	Map<Integer, PlaceidTypenameDuple> getTypeIdToName();
}
