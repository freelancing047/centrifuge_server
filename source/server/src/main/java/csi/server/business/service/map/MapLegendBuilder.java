package csi.server.business.service.map;

import java.util.ArrayList;
import java.util.List;

import csi.server.business.visualization.legend.AssociationLegendItem;
import csi.server.business.visualization.legend.CombinedPlaceLegendItem;
import csi.server.business.visualization.legend.MapLegendInfo;
import csi.server.business.visualization.legend.NewPlaceLegendItem;
import csi.server.business.visualization.legend.PlaceLegendItem;
import csi.server.business.visualization.legend.TrackLegendItem;
import csi.server.business.visualization.legend.UpdatedPlaceLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.legenditembuilder.AbstractPlaceLegendItemsBuilder;
import csi.server.business.visualization.map.legenditembuilder.AssociationLegendItemsBuilder;
import csi.server.business.visualization.map.legenditembuilder.CombinedMapNodeLegendItemBuilder;
import csi.server.business.visualization.map.legenditembuilder.DetailPlaceLegendItemsBuilder;
import csi.server.business.visualization.map.legenditembuilder.NewMapNodeLegendItemBuilder;
import csi.server.business.visualization.map.legenditembuilder.SummaryPlaceLegendItemsBuilder;
import csi.server.business.visualization.map.legenditembuilder.TrackLegendItemsBuilder;
import csi.server.business.visualization.map.legenditembuilder.TrackMapSummaryPlaceLegendItemsBuilder;
import csi.server.business.visualization.map.legenditembuilder.UpdatedMapNodeLegendItemBuilder;

public class MapLegendBuilder {
    private final MapCacheHandler mapCacheHandler;
    private MapLegendInfo mapLegend;

    public MapLegendBuilder(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
    }

    public void build() {
        mapLegend = new MapLegendInfo();
        mapLegend.setLinkLimitReached(mapCacheHandler.isLinkLimitReached());
        mapLegend.setPlaceLegendItems(getPlaceLegendItems());
        mapLegend.setCombinedPlaceLegendItem(getCombinedPlaceLegendItem());
        mapLegend.setNewPlaceLegendItem(getNewPlaceLegendItem());
        mapLegend.setUpdatedPlaceLegendItem(getUpdatedPlaceLegendItem());
        mapLegend.setAssociationLegendItems(getAssociationLegendItems());
        mapLegend.setTrackLegendItems(getTrackLegendItems());
    }

    private List<PlaceLegendItem> getPlaceLegendItems() {
        if (mapCacheHandler.isUseTrackMap()) {
            if (mapCacheHandler.isCurrentlyAtDetailLevel()) {
                AbstractPlaceLegendItemsBuilder builder = new DetailPlaceLegendItemsBuilder(mapCacheHandler);
                builder.build();
                return builder.getLegendItems();
            } else {
                TrackMapSummaryPlaceLegendItemsBuilder builder = new TrackMapSummaryPlaceLegendItemsBuilder(mapCacheHandler);
                builder.build();
                return builder.getLegendItems();
            }
        } else {
            AbstractPlaceLegendItemsBuilder builder;
            if (!mapCacheHandler.isCurrentlyAtDetailLevel()) {
                builder = new SummaryPlaceLegendItemsBuilder(mapCacheHandler);
            } else {
                builder = new DetailPlaceLegendItemsBuilder(mapCacheHandler);
            }
            builder.build();
            return builder.getLegendItems();
        }
    }

    private CombinedPlaceLegendItem getCombinedPlaceLegendItem() {
        CombinedMapNodeLegendItemBuilder builder = new CombinedMapNodeLegendItemBuilder(mapCacheHandler);
        builder.build();
        return builder.getLegendItem();
    }

    private NewPlaceLegendItem getNewPlaceLegendItem() {
        NewMapNodeLegendItemBuilder builder = new NewMapNodeLegendItemBuilder(mapCacheHandler);
        builder.build();
        return builder.getLegendItem();
    }

    private UpdatedPlaceLegendItem getUpdatedPlaceLegendItem() {
        UpdatedMapNodeLegendItemBuilder builder = new UpdatedMapNodeLegendItemBuilder(mapCacheHandler);
        builder.build();
        return builder.getLegendItem();
    }

    private List<AssociationLegendItem> getAssociationLegendItems() {
        AssociationLegendItemsBuilder builder = new AssociationLegendItemsBuilder(mapCacheHandler);
        builder.build();
        return builder.getLegendItems();
    }

    private List<TrackLegendItem> getTrackLegendItems() {
        if (mapCacheHandler.isUseTrackMap() && mapCacheHandler.isCurrentlyAtDetailLevel()) {
            TrackLegendItemsBuilder builder = new TrackLegendItemsBuilder(mapCacheHandler);
            builder.build();
            return builder.getLegendItems();
        } else {
            return new ArrayList<TrackLegendItem>();
        }
    }

    public MapLegendInfo getMapLegendInfo() {
        if (!mapCacheHandler.usingLatestMapCache()) {
            return new MapLegendInfo();
        } else {
            return mapLegend;
        }
    }
}
