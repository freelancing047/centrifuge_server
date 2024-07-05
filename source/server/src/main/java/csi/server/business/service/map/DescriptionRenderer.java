package csi.server.business.service.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;

import csi.server.business.visualization.map.AugmentedMapNode;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapViewDef;

class DescriptionRenderer {
   private DataModelDef modelDef;
   private List<MapPlace> mapPlaces;
   private AugmentedMapNode mapNode;
   private Set<String> headingsVisited = new TreeSet<String>();
   private Map<String,Map<String,Integer>> descriptions;
   private List<String> descriptionList = new ArrayList<String>();

   public DescriptionRenderer(DataView dataView, MapViewDef mapViewDef, AugmentedMapNode mapNode) {
      modelDef = dataView.getMeta().getModelDef();
      mapPlaces = mapViewDef.getMapSettings().getMapPlaces();
      this.mapNode = mapNode;
      descriptions = mapNode.getDescriptions();
   }

   public void render() {
      mapNode.getPlaceIds().forEach(this::renderUsingTooltipFields);
   }

   private void renderUsingTooltipFields(Integer placeId) {
      mapPlaces.get(placeId).getTooltipFields().forEach(tooltipField -> {
         FieldDef fieldDef = tooltipField.getFieldDef(modelDef);
         String fieldName = StringEscapeUtils.escapeHtml(fieldDef.getFieldName());

         if (!headingsVisited.contains(fieldName)) {
            headingsVisited.add(fieldName);
            renderUsingTooltipsField(fieldName);
         }
      });
   }

   private void renderUsingTooltipsField(String fieldName) {
      Map<String,Integer> description = descriptions.get(fieldName);

      if (description != null) {
         String stringVal = "\"" + fieldName + "\":[";
         stringVal += renderFieldValues(description);
         stringVal += "]";
         descriptionList.add(stringVal);
      }
   }

   private String renderFieldValues(Map<String,Integer> fieldValues) {
      List<String> list = new ArrayList<String>();

      if (fieldValues != null) {
         fieldValues.forEach((key, value) -> list.add(renderFieldValue(key, value)));
      }
      return list.stream().collect(Collectors.joining(", "));
   }

   private String renderFieldValue(String fieldValue, int fieldValueCount) {
      String retVal = "\"";
      retVal += StringEscapeUtils.escapeHtml(fieldValue);

      if (fieldValueCount > 1) {
         retVal += " (" + fieldValueCount + ")";
      }
      retVal += "\"";
      return retVal;
   }

   @Override
   public String toString() {
      return descriptionList.stream().collect(Collectors.joining(", "));
   }
}
