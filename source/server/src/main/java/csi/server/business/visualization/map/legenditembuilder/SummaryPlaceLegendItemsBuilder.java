package csi.server.business.visualization.map.legenditembuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.common.model.map.PlaceidTypenameDuple;

public class SummaryPlaceLegendItemsBuilder extends AbstractPlaceLegendItemsBuilder {
    public SummaryPlaceLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
        super(mapCacheHandler);
    }

    public void build() {
        initControlVariables();
        buildLegendItems();
    }

    private void initControlVariables() {
       typeIds = new TreeSet<Integer>();

        PlaceDynamicTypeInfo dynamicTypeInfo = mapCacheHandler.getPlaceDynamicTypeInfo();
        typeIdToName = dynamicTypeInfo.getTypeIdToName();
        removeUnusedTypes();

        if (typeIdToName != null) {
            typeIds.addAll(typeIdToName.keySet());
            typenameToShape = dynamicTypeInfo.getTypenameToShape();
            typenameToColor = dynamicTypeInfo.getTypenameToColor();
            typenameToIconUrl = dynamicTypeInfo.getTypenameToIconUrl();
        }
    }

    private void removeUnusedTypes() {
        MapSummaryGrid grid = mapCacheHandler.getMapSummaryGrid();
        if (grid != null) {
            Set<PlaceidTypenameDuple> types = grid.getTypes();
            Map<Integer, PlaceidTypenameDuple> isMap = new HashMap<Integer, PlaceidTypenameDuple>();

            isMap.putAll(typeIdToName);
            for (Map.Entry<Integer, PlaceidTypenameDuple> integerStringEntry : isMap.entrySet()) {
                if (!types.contains(integerStringEntry.getValue())) {
                    typeIdToName.remove(integerStringEntry.getKey());
                }
            }
        }
    }
}
