package csi.server.business.visualization.map.legenditembuilder;

import csi.server.business.visualization.legend.UpdatedPlaceLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;

public class UpdatedMapNodeLegendItemBuilder {
    private MapCacheHandler mapCacheHandler;
    private UpdatedPlaceLegendItem legendItem;
    private int updatedCount = 0;

    public UpdatedMapNodeLegendItemBuilder(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
        legendItem = null;
    }

    public void build() {
        init();
        if (canCreateLegendItem())
            createLegendItem();
    }

    private void init() {
        MapSummaryGrid mapSummaryGrid = mapCacheHandler.getMapSummaryGrid();
        if (mapSummaryGrid != null)
            updatedCount = mapSummaryGrid.getUpdateCount();
    }

    private boolean canCreateLegendItem() {
        return updatedCount > 0 && mapCacheHandler.isLinkupDecoratorShown();
    }

    private void createLegendItem() {
        legendItem = new UpdatedPlaceLegendItem();
        legendItem.count = updatedCount;
        legendItem.visible = true;
        legendItem.clickable = mapCacheHandler.isCurrentlyAtDetailLevel();
    }

    public UpdatedPlaceLegendItem getLegendItem() {
        return legendItem;
    }
}