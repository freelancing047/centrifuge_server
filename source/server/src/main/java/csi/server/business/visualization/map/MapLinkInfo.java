package csi.server.business.visualization.map;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import csi.server.common.model.map.LinkGeometry;

public class MapLinkInfo implements TypeInfo {
	private Map<Long, MapLink> mapById;
	private Map<LinkGeometry, MapLink> mapByGeometry;
	private Map<String, List<MapLink>> mapByType;
	private Map<String, Integer> typenameToId;
	private Map<Integer, String> typeidToName;
	private Map<String, String> typenameToColor;
	private Map<String, String> typenameToShape;
	private Map<String, Integer> typenameToWidth;
	private Map<String, Boolean> typenameToShowDirection;

	public MapLinkInfo() {
		mapById = Maps.newHashMap();
		mapByGeometry = Maps.newTreeMap();
		mapByType = Maps.newHashMap();
		typenameToId = Maps.newHashMap();
		typeidToName = Maps.newHashMap();
		typenameToColor = Maps.newHashMap();
		typenameToShape = Maps.newHashMap();
		typenameToWidth = Maps.newHashMap();
		typenameToShowDirection = Maps.newHashMap();
	}

	public Map<Long, MapLink> getMapById() {
		return mapById;
	}

	public Map<LinkGeometry, MapLink> getMapByGeometry() {
		return mapByGeometry;
	}

	public Map<String, List<MapLink>> getMapByType() {
		return mapByType;
	}

	public Map<String, Integer> getTypenameToId() {
		return typenameToId;
	}

	public Map<Integer, String> getTypeidToName() {
		return typeidToName;
	}

	public Map<String, String> getTypenameToColor() {
		return typenameToColor;
	}

	public Map<String, String> getTypenameToShape() {
		return typenameToShape;
	}

	public Map<String, Integer> getTypenameToWidth() {
		return typenameToWidth;
	}

	public Map<String, Boolean> getTypenameToShowDirection() {
		return typenameToShowDirection;
	}
}
