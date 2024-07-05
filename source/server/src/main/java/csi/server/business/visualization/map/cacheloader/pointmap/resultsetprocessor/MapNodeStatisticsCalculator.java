package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor;

import java.util.Map;
import java.util.Set;

import com.google.gwt.thirdparty.guava.common.collect.Sets;

import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapCacheNotAvailable;
import csi.server.business.visualization.map.NodeSizeCalculator;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.PlaceSummary;
import csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary.TypeSummary;
import csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue.TypeSizeValue;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class MapNodeStatisticsCalculator {
    private MapSettingsDTO mapSettings;
    private Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType;
    private Map<Integer, Set<String>> placeIdToTypeNames;
    private Map<Integer, Map<String, NodeSizeCalculator>> registry;

    public MapNodeStatisticsCalculator(MapCacheHandler mapCacheHandler, Map<PlaceidTypenameDuple, Set<AugmentedMapNode>> mapNodeByType, Map<Integer, Set<String>> placeIdToTypeNames, boolean waitForRegistry) {
        this.mapSettings = mapCacheHandler.getMapSettings();
        this.mapNodeByType = mapNodeByType;
        this.placeIdToTypeNames = placeIdToTypeNames;
        if (registryNecessary()) {
            if (mapCacheHandler.outOfBandResourceBuilding() && !waitForRegistry) {
                throw new MapCacheNotAvailable();
            }
            registry = mapCacheHandler.getNodeSizeCalculatorRegistry();
        }
    }

    public static boolean shouldCreateCalculators(int placeId, MapSettingsDTO mapSettings) {
        PlaceSettingsDTO placeSettings = mapSettings.getPlaceSettings().get(placeId);
        if ((placeSettings.getSizeColumn() == null) || (placeSettings.getSizeFunction() == null)) {
         return false;
      }
        String sizeFunction = placeSettings.getSizeFunction();
        if (!sizeFunction.equals("COUNT") && !sizeFunction.equals("COUNT_DIST")) {
         return (mapSettings.getPlaceSettings().get(placeId).getSizeColumnNumerical() != null)
                 && mapSettings.getPlaceSettings().get(placeId).getSizeColumnNumerical().booleanValue();
      }
        return true;
    }

    private boolean registryNecessary() {
        for (int placeId = 0; placeId < mapSettings.getPlaceSettings().size(); placeId++) {
         if (shouldCreateCalculators(placeId, mapSettings)) {
            return true;
         }
      }
        return false;
    }

    public void calculate() {
        for (int placeId = mapSettings.getPlaceSettings().size() - 1; placeId >= 0; placeId--) {
         calculateMapNodeStatisticsForPlace(placeId);
      }
    }

   private void calculateMapNodeStatisticsForPlace(int placeId) {
      if (mapSettings.getPlaceSettings().get(placeId).isSizedByDynamicType().booleanValue()) {
         calculateMapNodeTypeStatistics(placeId);
      } else {
         calculateMapNodePlaceStatistics(placeId);
      }
   }

    private void calculateMapNodeTypeStatistics(int placeId) {
        if (mapSettings.getPlaceSettings().get(placeId) != null) {
            if (placeIdToTypeNames.isEmpty() || !placeIdToTypeNames.containsKey(placeId)) {
                return;
            }
            for (String typename : placeIdToTypeNames.get(placeId)) {
                gatherTypeSummaryForTypename(placeId, typename);
            }
        }
    }

    private void gatherTypeSummaryForTypename(int placeId, String typename) {
        PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
        Set<AugmentedMapNode> mapNodes = mapNodeByType.get(key);
        if (mapNodes != null) {
            NodeSizeCalculator calculator = null;
            if (registry != null) {
                Map<String, NodeSizeCalculator> typenameToCalculator = registry.get(placeId);
                if (typenameToCalculator != null) {
                  calculator = typenameToCalculator.get(typename);
               }
            }
            if (calculator != null) {
                for (AugmentedMapNode mapNode : mapNodes) {
                    TypeSizeValue typeSizeValue = mapNode.getTypeSizeValue(key);
                    Double doubleValue = null;
                    if (typeSizeValue != null) {
                     doubleValue = typeSizeValue.getValue();
                  }
                    mapNode.setSize(key, calculator.calculate(doubleValue));
                }
            } else {
                TypeSummary typeSummary = gatherTypeSummary(key, mapNodes);
                if (typeSummary.hasMapNodes()) {
                  typeSummary.setMapNodeSizes();
               }
            }
        }
    }

    private TypeSummary gatherTypeSummary(PlaceidTypenameDuple key, Set<AugmentedMapNode> mapNodes) {
        TypeSummary typeSummary = new TypeSummary(key);
        for (AugmentedMapNode mapNode : mapNodes) {
         addMapNodeToTypeSummary(key, typeSummary, mapNode);
      }
        return typeSummary;
    }

    private void addMapNodeToTypeSummary(PlaceidTypenameDuple key, TypeSummary typeSummary, AugmentedMapNode mapNode) {
        TypeSizeValue typeSizeValue = mapNode.getTypeSizeValue(key);
        if (typeSizeValue != null) {
            Double doubleValue = typeSizeValue.getValue();
            typeSummary.addMapNode(mapNode, doubleValue);
            typeSummary.updateMinMax(doubleValue);
        }
    }

    private void calculateMapNodePlaceStatistics(int placeId) {
        if (placeIdToTypeNames.isEmpty() || !placeIdToTypeNames.containsKey(placeId)) {
            return;
        }

        NodeSizeCalculator calculator = null;
        if (registry != null) {
            Map<String, NodeSizeCalculator> typenameToCalculator = registry.get(placeId);
            if (typenameToCalculator != null) {
               calculator = typenameToCalculator.get("");
            }
        }
        if (calculator != null) {
            if (mapSettings.getPlaceSettings().get(placeId).getSizeColumn() != null) {
                for (String typename : placeIdToTypeNames.get(placeId)) {
                    PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
                    Set<AugmentedMapNode> mapNodes = mapNodeByType.get(key);
                    if (mapNodes != null) {
                        for (AugmentedMapNode mapNode : mapNodes) {
                            TypeSizeValue typeSizeValue = mapNode.getPlaceSizeValue(placeId);
                            Double doubleValue = null;
                            if (typeSizeValue != null) {
                              doubleValue = typeSizeValue.getValue();
                           }
                            mapNode.setSize(key, calculator.calculate(doubleValue));
                        }
                    }
                }
            }
        } else {
            Set<String> typeNames = placeIdToTypeNames.get(placeId);
            PlaceSummary placeSummary = gatherPlaceSummary(placeId, typeNames);
            if (placeSummary.hasMapNodes()) {
               placeSummary.setMapNodeSizes();
            }
        }
    }

    private PlaceSummary gatherPlaceSummary(int placeId, Set<String> typeNames) {
        Set<PlaceidTypenameDuple> keys = Sets.newTreeSet();
        for (String typename : typeNames) {
            PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
            keys.add(key);
        }
        PlaceSummary placeSummary = new PlaceSummary();
        if (mapSettings.getPlaceSettings().get(placeId).getSizeColumn() != null) {
         for (String typename : placeIdToTypeNames.get(placeId)) {
             PlaceidTypenameDuple key = new PlaceidTypenameDuple(placeId, typename);
             Set<AugmentedMapNode> mapNodes = mapNodeByType.get(key);
             if (mapNodes != null) {
               for (AugmentedMapNode mapNode : mapNodes) {
                  addMapNodeToPlaceSummary(placeSummary, mapNode, placeId);
               }
            }

         }
      }
        return placeSummary;
    }

    private void addMapNodeToPlaceSummary(PlaceSummary placeSummary, AugmentedMapNode mapNode, int placeId) {
        if (placeSummary.nodeValueAlreadySubmitted(mapNode.getNodeId())) {
         return;
      }

        TypeSizeValue typeSizeValue = mapNode.getPlaceSizeValue(placeId);
        if (typeSizeValue != null) {
            Double doubleValue = typeSizeValue.getValue();
            placeSummary.addMapNode(mapNode, doubleValue);
            placeSummary.updateMinMax(doubleValue);
        }
    }
}
