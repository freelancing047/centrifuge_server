package csi.map.controller.payloadbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import csi.map.controller.model.Feature;
import csi.map.controller.model.FeatureAttributes;
import csi.map.controller.model.Payload;
import csi.map.controller.model.TypeSymbol;
import csi.map.controller.model.XYPair;
import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapNode;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.Extent;
import csi.server.common.model.visualization.selection.AbstractMapSelection;
import csi.shared.core.visualization.map.MapBundleDefinitionDTO;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.MapSummaryExtent;

public class BundlePayloadLoader extends AbstractPayloadBuilder {
    private List<Crumb> breadcrumb;
    private boolean showLeaves;

    public BundlePayloadLoader(MapCacheHandler mapCacheHandler, Extent extent) {
        super(mapCacheHandler, extent);
    }

    public BundlePayloadLoader(MapCacheHandler mapCacheHandler, Extent extent, MapSummaryExtent mapSummaryExtent, Integer rangeStart, Integer rangeEnd, Integer rangeSize) {
        super(mapCacheHandler, extent, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
    }

    public void setBreadcrumb(List<Crumb> breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public void setShowLeaves(boolean showLeaves) {
        this.showLeaves = showLeaves;
    }

    @Override
    protected void decorateWithMapCache() {
        int index = 0;
        if (breadcrumb != null) {
         index = breadcrumb.size();
      }
        if (!showLeaves && (index < mapCacheHandler.getMapSettings().getMapBundleDefinitions().size())) {
            buildBundles();
            transfer(layerHolder, payload.getLayer());
        } else {
            DetailPointBuilder pointBuilder = new DetailPointBuilder(payload, mapCacheHandler, mapSummaryExtent, rangeStart, rangeEnd, rangeSize);
            pointBuilder.build();
        }
        payload.setPointLimitReached(mapCacheHandler.isPointLimitReached());
    }

    private void buildBundles() {
        AbstractMapSelection mapSelection = mapCacheHandler.getMapSelection();
        Integer sn = null;
        if (mapSelection != null) {
            sn = mapSelection.getSequenceNumber();
            mapSelection.registerReader(sn);
        }
        List<Feature> features = payload.getLayer().getFeatures();
        List<Feature> selectedFeatures = new ArrayList<Feature>();
        Map<Long, AugmentedMapNode> childrenBundleMapNodes = mapCacheHandler.getMapNodeByIdMap();
        if (childrenBundleMapNodes != null) {
            for (Map.Entry<Long, AugmentedMapNode> entry : childrenBundleMapNodes.entrySet()) {
                AugmentedMapNode mapNode = childrenBundleMapNodes.get(entry.getKey());
                if (mapNode != null) {
                    XYPair geometry = new XYPair(mapNode.getGeometry().getX(), mapNode.getGeometry().getY());
                    Feature feature = new Feature(geometry, entry.getKey().toString(), 0);
                    setHitsAndSize(mapNode, feature);
                    attachLabel(mapNode, feature);
                    if (mapCacheHandler.isSelected(mapSelection, mapNode)) {
                        layerHolder.addSelectedFeature(entry.getKey().toString());
                        selectedFeatures.add(feature);
                    } else {
                        features.add(feature);
                    }
                }
            }
        }
        if (mapSelection != null) {
         mapSelection.deregisterReader(sn);
      }
        features.addAll(selectedFeatures);
        payload.setItemsInViz(mapCacheHandler.getItemsInViz());
    }

    private void setHitsAndSize(MapNode mapNode, Feature feature) {
        double hits = mapNode.getHits();
        FeatureAttributes attributes = feature.getAttributes();
        attributes.setHits(hits);
        attributes.setSize(1);
        if ((hits >= 10) && (hits < 100)) {
            attributes.setSize(2);
        } else if ((hits >= 100) && (hits < 1000)) {
            attributes.setSize(3);
        } else if ((hits >= 1000) && (hits < 10000)) {
            attributes.setSize(4);
        } else if (hits >= 10000) {
            attributes.setSize(5);
        }
    }

    @Override
    protected void decorateWithOthers() {
        MapSettingsDTO mapSettingsDTO = mapCacheHandler.getMapSettings();
        payload.setUseBundle(true);
        int index = 0;
        if (breadcrumb != null) {
            while (index < breadcrumb.size()) {
                Crumb crumb = breadcrumb.get(index);
                MapBundleDefinitionDTO mapBundleDefinition = mapSettingsDTO.getMapBundleDefinitions().get(index);
                payload.getBreadcrumb().add(mapBundleDefinition.getFieldName() + "(" + crumb.getCriterion() + ")");
                index++;
            }
        }
        if (!showLeaves && (index < mapSettingsDTO.getMapBundleDefinitions().size())) {
            MapBundleDefinitionDTO mapBundleDefinition = mapSettingsDTO.getMapBundleDefinitions().get(index);
            payload.getBreadcrumb().add(mapBundleDefinition.getFieldName() + " &#187;");
            payload.setShowLabel(mapBundleDefinition.isShowLabel());
            decoratePayloadWithBundleInfo(mapSettingsDTO, payload, mapBundleDefinition);
        } else {
            decoratePayloadWithPlaceInfo(payload);
            decoratePayloadWithAssociationInfo(payload);
            payload.setUseLinkupDecorator(mapCacheHandler.isLinkupDecoratorShown());
        }
        if (showLeaves) {
            payload.setShowLeaves(true);
        }
        if (index < mapSettingsDTO.getMapBundleDefinitions().size()) {
            payload.setDrilledToBottom(false);
        }
    }

    private void decoratePayloadWithBundleInfo(MapSettingsDTO mapSettings, Payload payload, MapBundleDefinitionDTO mapBundleDefinition) {
        List<TypeSymbol> typeSymbols = new ArrayList<TypeSymbol>();
        TypeSymbol typeSymbol = new TypeSymbol();
        typeSymbol.setId(0);
        typeSymbol.setShape(mapBundleDefinition.getShapeString());
        typeSymbol.setColor(mapBundleDefinition.getColorString());
        typeSymbols.add(typeSymbol);
        payload.setDefaultShapeString(mapSettings.getDefaultShapeString());
        payload.setTypeSymbols(typeSymbols);
    }

    @Override
    protected void decorateWithMapSettings() {
    }
}
