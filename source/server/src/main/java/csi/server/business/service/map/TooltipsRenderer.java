package csi.server.business.service.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.business.visualization.map.BundleMapNode;
import csi.server.business.visualization.map.MapCacheHandler;
import csi.server.business.visualization.map.MapCacheStaleException;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.shared.core.visualization.map.MapSettingsDTO;
import csi.shared.core.visualization.map.PlaceSettingsDTO;

public class TooltipsRenderer {
   private MapCacheHandler mapCacheHandler;
   private DataView dataView;
   private MapViewDef mapViewDef;
   private List<Long> idList;
   private Map<Long,AugmentedMapNode> mapNodeMap;
   private String tooltipString = "";

   public TooltipsRenderer(MapCacheHandler mapCacheHandler, DataView dataView, MapViewDef mapViewDef, Long id) {
      this.mapCacheHandler = mapCacheHandler;
      this.dataView = dataView;
      this.mapViewDef = mapViewDef;
      idList = new ArrayList<Long>();
      idList.add(id);
   }

   public void exec() {
      init();

      tooltipString += "{";

      try {
         if (isRenderable()) {
            tooltipString += render();
         }
      } catch (MapCacheStaleException ignored) {
      }
      tooltipString += "}";
   }

   private void init() {
      mapNodeMap = mapCacheHandler.getMapNodeByIdMap();
   }

   private boolean isRenderable() {
      boolean currentlyAtDetailLevel = true;

      if (!mapCacheHandler.isBundleUsed()) {
         currentlyAtDetailLevel = mapCacheHandler.isCurrentlyAtDetailLevel();
      }
      return (dataView != null) && currentlyAtDetailLevel && (mapNodeMap != null);
   }

   private String render() {
      return renderNodes();
   }

   private String renderNodes() {
      List<String> nodeStrings = new ArrayList<String>();

      idList.forEach(id -> {
         String nodeString = renderNode(mapNodeMap.get(id));

         if (nodeString != null) {
            nodeStrings.add(nodeString);
         }
      });
      return nodeStrings.stream().collect(Collectors.joining(", "));
   }

   private String renderNode(AugmentedMapNode mapNode) {
      String result = null;

      if (mapNode != null) {
         if (mapNode instanceof BundleMapNode) {
            result = renderBundleMapNode((BundleMapNode) mapNode);
         } else {
            result = renderMapNode(mapNode);
         }
      }
      return result;
   }

   private String renderBundleMapNode(BundleMapNode bundleMapNode) {
      List<String> list = new ArrayList<String>();

      list.add(renderAsBundle("true"));
      list.add(renderAsBundleInfo(bundleMapNode));

      return new StringBuilder("\"").append(bundleMapNode.getBundleValue())
                       .append("\":{").append(list.stream().collect(Collectors.joining(", ")))
                       .append("}").toString();
   }

   private String renderAsBundle(String value) {
      return "\"renderAsBundle\":" + value;
   }

   private String renderAsBundleInfo(BundleMapNode bundleMapNode) {
      List<String> list = new ArrayList<String>();

      if (mapCacheHandler.isCountChildren()) {
         String childrenCount = renderChildrenCount(bundleMapNode);

         if (childrenCount != null) {
            list.add(childrenCount);
         }
      }
      list.add(renderRows(bundleMapNode));

      return new StringBuilder("\"renderAsBundleInfo\":[")
                       .append(list.stream().collect(Collectors.joining(", ")))
                       .append("]").toString();
   }

   private String renderChildrenCount(BundleMapNode bundleMapNode) {
      MapSettingsDTO mapSettings = mapCacheHandler.getMapSettings();
      List<Crumb> breadCrumb = mapCacheHandler.getBreadcrumb();

      return ((breadCrumb != null) && (bundleMapNode.getChildrenCount() != null))
                ? new StringBuilder("\"")
                            .append(mapSettings.getMapBundleDefinitions().get(breadCrumb.size() + 1).getFieldName())
                            .append(": ").append(bundleMapNode.getChildrenCount()).append("\"")
                            .toString()
                : null;
   }

   private String renderRows(BundleMapNode bundleMapNode) {
      return new StringBuilder("\"Rows: ").append((int) bundleMapNode.getHits()).append("\"").toString();
   }

   private String renderMapNode(AugmentedMapNode mapNode) {
      List<String> list = new ArrayList<String>();

      list.add(renderAsBundle("false"));
      list.add(renderCount(mapNode));

      String description = renderDescriptions(mapNode);

      if (!description.isEmpty()) {
         list.add(description);
      }
      return new StringBuilder("\"(").append(mapNode.getGeometry().getY()).append(",")
                       .append(mapNode.getGeometry().getX()).append(")\":{")
                       .append(list.stream().collect(Collectors.joining(", ")))
                       .append("}").toString();
   }

   private String renderCount(AugmentedMapNode mapNode) {
      return new StringBuilder("\"Type\":[").append(renderTypeNodeCounts(mapNode)).append("]").toString();
   }

   private String renderTypeNodeCounts(AugmentedMapNode mapNode) {
      List<String> list = new ArrayList<String>();

      for (Map.Entry<PlaceidTypenameDuple,Integer> entry : mapNode.getTypeNodeCounts().entrySet()) {
         list.add(renderTypeNodeCount(entry.getKey(), entry.getValue().intValue()));
      }
      return list.stream().collect(Collectors.joining(", "));
   }

   private String renderTypeNodeCount(PlaceidTypenameDuple duple, int typeNodeCount) {
      return new StringBuilder("\"").append(renderTypename(duple)).append(" (").append(typeNodeCount).append(")\"").toString();
   }

   private String renderTypename(PlaceidTypenameDuple duple) {
      PlaceSettingsDTO placeSettings = mapCacheHandler.getMapSettings().getPlaceSettings().get(duple.getPlaceid());
      String placeName = placeSettings.getName();
      String typename = duple.getTypename();

      return ((typename == null) || typename.isEmpty())
                ? placeName
                : new StringBuilder(typename).append("::").append(placeName).toString();
   }

   private String renderDescriptions(AugmentedMapNode mapNode) {
      DescriptionRenderer renderer = new DescriptionRenderer(dataView, mapViewDef, mapNode);

      renderer.render();
      return renderer.toString();
   }

   @Override
   public String toString() {
      return tooltipString;
   }
}
