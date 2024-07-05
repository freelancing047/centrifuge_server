package csi.server.business.visualization.map.legenditembuilder;

import csi.server.business.visualization.legend.NewPlaceLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapSummaryGrid;

public class NewMapNodeLegendItemBuilder {
    private MapCacheHandler mapCacheHandler;
    private NewPlaceLegendItem legendItem;
    private int newCount = 0;

    public NewMapNodeLegendItemBuilder(MapCacheHandler mapCacheHandler) {
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
            newCount = mapSummaryGrid.getNewCount();
    }

    private boolean canCreateLegendItem() {
        return newCount > 0 && mapCacheHandler.isLinkupDecoratorShown();
    }

    private void createLegendItem() {
        legendItem = new NewPlaceLegendItem();
        legendItem.count = newCount;
        legendItem.visible = true;
        legendItem.clickable = mapCacheHandler.isCurrentlyAtDetailLevel();
    }

    public NewPlaceLegendItem getLegendItem() {
        return legendItem;
    }
}