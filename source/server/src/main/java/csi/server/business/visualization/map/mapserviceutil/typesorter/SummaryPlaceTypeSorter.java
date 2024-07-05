package csi.server.business.visualization.map.mapserviceutil.typesorter;

import static csi.server.business.visualization.map.MapServiceUtil.getPlaceDynamicTypeInfo;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.common.model.map.PlaceidTypenameDuple;

public class SummaryPlaceTypeSorter extends AbstractPlaceTypeSorter {
	public SummaryPlaceTypeSorter(String mapViewDefUuid, List<PlaceidTypenameDuple> keys) {
		super(keys);
		dynamicTypeInfo = getPlaceDynamicTypeInfo(mapViewDefUuid);
	}

	@Override
	public void sort() {
		init();
		applyToTypenameCache(keys.iterator());
		applyToDynamicTypenameCache(dynamicTypeInfoTypeNames.iterator());
	}

	private void init() {
		initControlVariables();
		clearTypenameCache();
	}

	private void initControlVariables() {
		dynamicTypeInfoTypeNames = new TreeSet<PlaceidTypenameDuple>();
		dynamicTypeInfoTypeNames.addAll(dynamicTypeInfo.getTypenameToId().keySet());
		dynamicTypeInfoTypeNames.removeAll(keys);
		id = 0;
	}

	private void clearTypenameCache() {
		MapServiceUtil.clearTypeInfo(dynamicTypeInfo);
	}

	private void applyToTypenameCache(Iterator<PlaceidTypenameDuple> typeNames) {
		while (typeNames.hasNext()) {
			PlaceidTypenameDuple typename = typeNames.next();
			MapServiceUtil.applyTypenameToTypeInfo(dynamicTypeInfo, typename, id);
			id++;
		}
	}
}
