package csi.server.business.visualization.map.mapserviceutil.typesorter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapLinkInfo;

public class AssociationTypeSorter {
	private List<String> associationNames;
	private Set<String> typenames;
	private Map<String, Integer> typenameToId;
	private Map<Integer, String> typeidToName;
	private int id;

	public AssociationTypeSorter(String mapViewDefUuid, List<String> associationNames) {
		this.associationNames = associationNames;
		typenames = Sets.newTreeSet();
		MapLinkInfo mapLinkInfo = MapCacheUtil.getCorrectMapLinkInfo(mapViewDefUuid);
		if (mapLinkInfo != null) {
			typenameToId = mapLinkInfo.getTypenameToId();
			typeidToName = mapLinkInfo.getTypeidToName();
		}
	}

	public void sort() {
		if (typenameToId != null) {
			init();
			applyToTypenameCache(associationNames.iterator());
			applyToTypenameCache(typenames.iterator());
		}
	}

	private void init() {
		initControlVariables();
		clearTypenameCache();
	}

	private void initControlVariables() {
		typenames.addAll(typenameToId.keySet());
		typenames.removeAll(associationNames);
		id = 0;
	}

	private void clearTypenameCache() {
		typenameToId.clear();
		typeidToName.clear();
	}

	private void applyToTypenameCache(Iterator<String> typeNames) {
		while (typeNames.hasNext()) {
			String typename = typeNames.next();
			typenameToId.put(typename, id);
			typeidToName.put(id, typename);
			id++;
		}
	}
}