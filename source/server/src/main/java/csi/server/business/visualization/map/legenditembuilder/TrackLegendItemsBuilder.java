package csi.server.business.visualization.map.legenditembuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import csi.server.business.visualization.legend.TrackLegendItem;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapTrackInfo;
import csi.server.business.visualization.map.TrackMapSummaryGrid;
import csi.server.common.model.map.TrackidTracknameDuple;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrackLegendItemsBuilder {
    private final TrackMapSummaryGrid trackMapSummaryGrid;
    protected Map<TrackidTracknameDuple, String> typenameToShape;
    protected Map<TrackidTracknameDuple, String> typenameToColor;
    private Map<Integer, TrackidTracknameDuple> typeIdToName;
    private MapCacheHandler mapCacheHandler;
    private List<TrackLegendItem> legendItems;
    private Set<Integer> typeIds;

    public TrackLegendItemsBuilder(MapCacheHandler mapCacheHandler) {
        this.mapCacheHandler = mapCacheHandler;
        legendItems = Lists.newArrayList();
        trackMapSummaryGrid = mapCacheHandler.getTrackMapSummaryGrid();
    }

    public void build() {
        initControlVariables();
        buildLegendItems();
    }

    private void initControlVariables() {
        typeIds = Sets.newTreeSet();

        MapTrackInfo mapTrackInfo = mapCacheHandler.getMapTrackInfo();
        if (mapTrackInfo != null) {
            typeIdToName = mapCacheHandler.getMapTrackTypeIdToName(mapTrackInfo);
            if (typeIdToName != null) {
                typeIds.addAll(typeIdToName.keySet());
                typenameToShape = mapTrackInfo.getTrackkeyToShape();
                typenameToColor = mapTrackInfo.getTrackkeyToColor();
            }
        }
    }

    private void buildLegendItems() {
        for (Integer typeId : typeIds)
            addToLegendItems(typeId);
    }

    private void addToLegendItems(Integer typeId) {
        TrackidTracknameDuple key = typeIdToName.get(typeId);
        if (!trackMapSummaryGrid.isTrackTypeEmpty(key)) {
            if (typenameToColor.containsKey(key)) {
                TrackLegendItem legendItem = getLegendItem(key);
                legendItems.add(legendItem);
            }
        }
    }

    private TrackLegendItem getLegendItem(TrackidTracknameDuple key) {
        return createAndInitializeLegendItem(key);
    }

    private TrackLegendItem createAndInitializeLegendItem(TrackidTracknameDuple key) {
        TrackLegendItem legendItem = new TrackLegendItem();
        legendItem.key = key.getTrackname() + "::" + key.getTrackid();
        legendItem.trackId = key.getTrackid();
        legendItem.typeName = key.getTrackname();
        legendItem.shape = typenameToShape.get(key);
        String color = typenameToColor.get(key);
        if (color.startsWith("#")) {
            color = color.substring(1);
        }
        legendItem.color = Integer.decode("0x" + color);
        return legendItem;
    }

    public List<TrackLegendItem> getLegendItems() {
        return legendItems;
    }
}