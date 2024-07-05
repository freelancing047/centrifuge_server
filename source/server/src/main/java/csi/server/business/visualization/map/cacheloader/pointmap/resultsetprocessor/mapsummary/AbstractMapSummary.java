package csi.server.business.visualization.map.cacheloader.pointmap.resultsetprocessor.mapsummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;

public abstract class AbstractMapSummary {
   private Double min = Double.MAX_VALUE;
   private Double max = Double.MIN_VALUE;
   private Map<Long,Double> idToValue = new TreeMap<Long,Double>();
   private List<AugmentedMapNode> mapNodeList = new ArrayList<AugmentedMapNode>();
   private double gate1;
   private double gate2;
   private double gate3;
   private double gate4;

   public boolean nodeValueAlreadySubmitted(Long id) {
      return idToValue.containsKey(id);
   }

   public void addMapNode(AugmentedMapNode mapNode, Double value) {
      idToValue.put(mapNode.getNodeId(), value);
      mapNodeList.add(mapNode);
   }

   public void updateMinMax(Double value) {
      if (value != null) {
         if (value < min) {
            min = value;
         }
         if (value > max) {
            max = value;
         }
      }
   }

   List<AugmentedMapNode> getMapNodeList() {
      return mapNodeList;
   }

   public boolean hasMapNodes() {
      return !mapNodeList.isEmpty();
   }

   void setupGates() {
      double step = (max - min) / 5;
      gate1 = min + step;
      gate2 = gate1 + step;
      gate3 = gate2 + step;
      gate4 = gate3 + step;
   }

   public abstract void setMapNodeSizes();

   public void setMapNodeSizes(PlaceidTypenameDuple key) {
      for (AugmentedMapNode mapNode : mapNodeList) {
         int size = getSize(mapNode);
         for (PlaceidTypenameDuple placeidTypenameDuple : mapNode.getTypeNodeCounts().keySet()) {
            mapNode.setSize(placeidTypenameDuple, size);
         }
      }
   }

   void setMapNodeSizes(TrackidTracknameDuple key) {
      for (AugmentedMapNode mapNode : mapNodeList) {
         mapNode.setIdentitySize(key, getSize(mapNode));
      }
   }

   int getSize(AugmentedMapNode mapNode) {
      Double currentValue = idToValue.get(mapNode.getNodeId());
      int size = 1;
      if (currentValue != null) {
         if (currentValue > gate4) {
            size = 5;
         } else if (currentValue > gate3) {
            size = 4;
         } else if (currentValue > gate2) {
            size = 3;
         } else if (currentValue > gate1) {
            size = 2;
         }
      }
      return size;
   }
}
