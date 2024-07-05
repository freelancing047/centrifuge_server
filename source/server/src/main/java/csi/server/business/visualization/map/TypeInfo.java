package csi.server.business.visualization.map;

import java.util.Map;

public interface TypeInfo {
	Map<String, Integer> getTypenameToId();
	Map<Integer, String> getTypeidToName();
}
