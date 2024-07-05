package csi.server.business.visualization.map.mapserviceutil.typesorter;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.common.model.map.PlaceidTypenameDuple;

class PlaceDynamicTypeInfoReconciler {
	private MapCacheHandler mapCacheHandler;
	private MapNodeInfo mapNodeInfo;
	private PlaceDynamicTypeInfo dynamicTypeInfo;
	private int maxId;
	private Set<PlaceidTypenameDuple> typeNamesNotInMapNodeInfo;

	PlaceDynamicTypeInfoReconciler(MapCacheHandler mapCacheHandler, MapNodeInfo mapNodeInfo, PlaceDynamicTypeInfo dynamicTypeInfo) {
		this.mapCacheHandler = mapCacheHandler;
		this.mapNodeInfo = mapNodeInfo;
		this.dynamicTypeInfo = dynamicTypeInfo;
	}

	void reconcile() {
		init();
		populateDynamicTypeInfoWithMapNodeInfo();
		populateDynamicTypeInfoWithTypeNamesNotInMapNodeInfo();
	}

	private void init() {
		maxId = 0;
		typeNamesNotInMapNodeInfo = createTypeNamesNotInMapNodeInfo();
		mapCacheHandler.clearTypeInfo(dynamicTypeInfo);
	}

	private Set<PlaceidTypenameDuple> createTypeNamesNotInMapNodeInfo() {
		Set<PlaceidTypenameDuple> dynamicTypeInfoTypeNames = Sets.newTreeSet();
		dynamicTypeInfoTypeNames.addAll(dynamicTypeInfo.getTypenameToId().keySet());
		dynamicTypeInfoTypeNames.removeAll(mapNodeInfo.getTypenameToId().keySet());
		return dynamicTypeInfoTypeNames;
	}

	private void populateDynamicTypeInfoWithMapNodeInfo() {
		for (Map.Entry<Integer, PlaceidTypenameDuple> integerStringEntry : mapNodeInfo.getTypeIdToName().entrySet()) {
			PlaceidTypenameDuple key = integerStringEntry.getValue();
			int typeID = integerStringEntry.getKey();
			mapCacheHandler.applyTypenameToTypeInfo(dynamicTypeInfo, key, typeID);
			if (maxId < typeID)
				maxId = typeID;
		}
	}

	private void populateDynamicTypeInfoWithTypeNamesNotInMapNodeInfo() {
		int typeId = maxId + 1;
		for (PlaceidTypenameDuple key : getSortedTypeNames(typeNamesNotInMapNodeInfo)) {
			mapCacheHandler.applyTypenameToTypeInfo(dynamicTypeInfo, key, typeId);
			typeId++;
		}
	}

	private Set<PlaceidTypenameDuple> getSortedTypeNames(Set<PlaceidTypenameDuple> keys) {
		return Sets.newTreeSet(keys);
	}
}