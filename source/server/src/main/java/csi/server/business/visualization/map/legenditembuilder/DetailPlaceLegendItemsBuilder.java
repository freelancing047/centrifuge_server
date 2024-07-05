package csi.server.business.visualization.map.legenditembuilder;

import java.util.TreeSet;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNodeInfo;

public class DetailPlaceLegendItemsBuilder extends AbstractPlaceLegendItemsBuilder {
	public DetailPlaceLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
		super(mapCacheHandler);
	}

	public void build() {
		initControlVariables();
		buildLegendItems();
	}

	private void initControlVariables() {
		typeIds = new TreeSet<Integer>();

		MapNodeInfo mapNodeInfo = mapCacheHandler.getMapNodeInfo();
		if (mapNodeInfo != null) {
			typeIdToName = mapCacheHandler.getMapNodeTypeIdToName(mapNodeInfo);
			if (typeIdToName != null) {
				typeIds.addAll(typeIdToName.keySet());
				typenameToShape = mapNodeInfo.getTypenameToShape();
				typenameToColor = mapNodeInfo.getTypenameToColor();
				typenameToIconUrl = mapNodeInfo.getTypenameToIconUrl();
			}
		}
	}
}