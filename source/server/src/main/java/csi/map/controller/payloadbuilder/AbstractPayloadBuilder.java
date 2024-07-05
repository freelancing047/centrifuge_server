package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.config.Configuration;
import csi.config.MapConfig;
import csi.map.controller.model.AssociationSymbol;
import csi.map.controller.model.Feature;
import csi.map.controller.model.Layer;
import csi.map.controller.model.LayerHolder;
import csi.map.controller.model.Payload;
import csi.map.controller.model.TypeSymbol;
import csi.server.business.service.map.ExtentInfoBuilder;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapLinkInfo;
import csi.server.business.visualization.map.MapNodeInfo;
import csi.server.business.visualization.map.MapSummaryGrid;
import csi.server.business.visualization.map.PlaceDynamicTypeInfo;
import csi.server.common.model.map.Extent;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.shared.core.visualization.map.MapSummaryExtent;

public abstract class AbstractPayloadBuilder {
    protected Extent extent;
    protected Payload payload;
    protected MapSummaryExtent mapSummaryExtent = null;
    protected Integer rangeStart;
    protected Integer rangeEnd;
    protected Integer rangeSize;
    MapCacheHandler mapCacheHandler;
    LayerHolder layerHolder = new LayerHolder();

    public AbstractPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent) {
        this.mapCacheHandler = mapCacheHandler;
        this.extent = extent;
    }

    public AbstractPayloadBuilder(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        this.mapCacheHandler = mapCacheHandler;
        this.extent = extent;
        this.mapSummaryExtent = mapSummaryExtent;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.rangeSize = rangeSize;
    }

    static void attachLabel(AugmentedMapNode mapNode, Feature feature) {
        MapConfig mapConfig = Configuration.getInstance().getMapConfig();
        int maxLabelLength = mapConfig.getMaxLabelLength();
        String label = mapNode.getLabel().trim();
        if (label.length() > maxLabelLength) {
            feature.getAttributes().setLabel(label.substring(0, maxLabelLength - 3) + "...");
        } else {
            feature.getAttributes().setLabel(label);
        }
    }

    static void addFeatureInto(Map<Integer, List<Feature>> target, Feature feature) {
        int key = feature.getAttributes().getTypeId();
        if (!target.containsKey(key)) {
            target.put(key, new ArrayList<>());
        }
        target.get(key).add(feature);
    }

    static void incorporateInto(List<Feature> features, Map<Integer, List<Feature>> selectedUncombined, Map<Integer, List<Feature>> unselectedUncombined, MapCacheHandler mapCacheHandler) {
        if (mapCacheHandler.usingLatestMapCache()) {
            incorporateInto(features, unselectedUncombined, mapCacheHandler);
        }
        if (mapCacheHandler.usingLatestMapCache()) {
            incorporateInto(features, selectedUncombined, mapCacheHandler);
        }
    }

    static void incorporateInto(List<Feature> target, Map<Integer, List<Feature>> source, MapCacheHandler mapCacheHandler) {
        Set<Integer> keys = source.keySet();
        List<Integer> keyList = new ArrayList<Integer>(keys);

        Collections.sort(keyList);
        Collections.reverse(keyList);

        for (Integer key : keyList) {
            mapCacheHandler.iterate();
            target.addAll(source.get(key));
        }
    }

    static void addTypeSymbol(List<TypeSymbol> typeSymbols, MapNodeInfo mapNodeInfo, MapCacheHandler mapCacheHandler) {
        if (mapNodeInfo != null) {
            for (PlaceidTypenameDuple typename : mapNodeInfo.getTypenameToColor().keySet()) {
                PlaceDynamicTypeInfo placeDynamicTypeInfo = mapCacheHandler.getPlaceDynamicTypeInfo();
                if (placeDynamicTypeInfo.getTypenameToId().containsKey(typename)) {
                    Integer typeId = placeDynamicTypeInfo.getTypenameToId().get(typename);
                    TypeSymbol typeSymbol = new TypeSymbol();
                    typeSymbol.setId(typeId);
                    typeSymbol.setShape(mapNodeInfo.getTypenameToShape().get(typename));
                    typeSymbol.setColor(mapNodeInfo.getTypenameToColor().get(typename));
                    typeSymbols.add(typeSymbol);
                }
                mapCacheHandler.iterate();
            }
        }
    }

    public static void transfer(LayerHolder layerHolder, Layer layer) {
        layer.getSelectedFeatures().addAll(layerHolder.getSelectedFeatures());
        layer.getCombinedFeatures().addAll(layerHolder.getCombinedFeatures());
        layer.getNewFeatures().addAll(layerHolder.getNewFeatures());
        layer.getUpdatedFeatures().addAll(layerHolder.getUpdatedFeatures());
        layer.getTypeIdIconUrlPairs().addAll(layerHolder.getTypeIdIconUrlPairs());
    }

    private void initPayload() {
        payload = new Payload();
    }

    protected abstract void decorateWithMapCache();

    protected abstract void decorateWithOthers();

    protected void decorateWithExtentInfo() {
        payload.setExtentInfo(ExtentInfoBuilder.getExtentInfo(mapCacheHandler));
    }

    protected abstract void decorateWithMapSettings();

    private void decorateWithMapConfig() {
    }

    public void build() {
        initPayload();

        if (mapCacheHandler.usingLatestMapCache()) {
            decorateWithMapCache();

            if (payload.getLayer().getFeatures().isEmpty()) {
                try {
                    while (mapCacheHandler.outOfBandResourceBuilding()) {
                        Thread.sleep(200);
                    }
                    MapSummaryGrid mapSummaryGrid = mapCacheHandler.getMapSummaryGrid();
                    if ((mapSummaryGrid == null) || mapSummaryGrid.getDescendants().isEmpty()) {
                        payload.setNoData(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mapCacheHandler.usingLatestMapCache()) {
            decorateWithOthers();
        }
        if (mapCacheHandler.usingLatestMapCache()) {
            decorateWithExtentInfo();
        }
        decorateWithMapSettings();
        decorateWithMapConfig();
    }

    public Payload getPayload() {
        if (!mapCacheHandler.usingLatestMapCache()) {
            payload.setDeferToNewCache();
            payload.getLayer().clear();
        }
        return payload;
    }

    protected void decoratePayloadWithPlaceInfo(Payload payload) {
        List<TypeSymbol> typeSymbols = new ArrayList<TypeSymbol>();
        MapNodeInfo mapNodeInfo = mapCacheHandler.getMapNodeInfo();

        addTypeSymbol(typeSymbols, mapNodeInfo, mapCacheHandler);
        payload.setDefaultShapeString(mapCacheHandler.getMapSettings().getDefaultShapeString());
        payload.setTypeSymbols(typeSymbols);
    }

    protected void decoratePayloadWithAssociationInfo(Payload payload) {
        if (mapCacheHandler.usingLatestMapCache()) {
            List<AssociationSymbol> associationSymbols = new ArrayList<AssociationSymbol>();
            MapLinkInfo mapLinkInfo = mapCacheHandler.getMapLinkInfo();

            if ((mapLinkInfo != null) && (mapLinkInfo.getTypeidToName() != null)) {
               int typeId = 0;

               for (String typename : mapLinkInfo.getTypeidToName().values()) {
                    AssociationSymbol associationSymbol = new AssociationSymbol();

                    associationSymbol.setId(typeId++);
                    associationSymbol.setLineStyle(mapLinkInfo.getTypenameToShape().get(typename));
                    associationSymbol.setWidth(mapLinkInfo.getTypenameToWidth().get(typename).intValue());
                    associationSymbol.setColor(mapLinkInfo.getTypenameToColor().get(typename));
                    associationSymbol.setShowDirection(mapLinkInfo.getTypenameToShowDirection().get(typename).booleanValue());
                    associationSymbols.add(associationSymbol);
                }
            }
            payload.setAssociationSymbols(associationSymbols);
        }
    }
}
