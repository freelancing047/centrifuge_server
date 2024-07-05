package csi.server.business.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.base.property.AggregateProperty;
import csi.server.business.visualization.graph.base.property.Property;
import csi.server.common.dto.CsiMap;
import csi.shared.gwt.viz.graph.LinkDirection;

public class DirectionAggregator {
   private static final String REGEX = "^(.*)\\.(FORWARD|REVERSE|NONE)$";
   private static final Pattern PATTERN = Pattern.compile(REGEX);

   public CsiMap<String,CsiMap<String,String>> aggregateComputedFields(LinkStore linkStore) {
      CsiMap<String,CsiMap<String,String>> result = new CsiMap<String,CsiMap<String,String>>();
      Map<String,Property> attributes = linkStore.getAttributes();

      for (Map.Entry<String,Property> entry : attributes.entrySet()) {
         if (entry.getValue() instanceof AggregateProperty) {
            AggregateProperty prop = (AggregateProperty) entry.getValue();

            if ((prop != null) && prop.isIncludeInTooltip()) {
               String attributeName = entry.getKey();
               String attributeValue = String.valueOf(prop.getValue());
               Matcher matcher = PATTERN.matcher(attributeName);
               String key = attributeName;
               String direction = "ALL";

               if (matcher.matches()) {
                  key = matcher.group(1);
                  direction = matcher.group(2);
               }
               CsiMap<String,String> valuesByDirection = result.get(key);

               if (valuesByDirection == null) {
                  valuesByDirection = new CsiMap<String,String>();
               }
               valuesByDirection.put(direction, attributeValue);
               result.put(key, valuesByDirection);
            }
         }
      }
      return result;
   }

    public CsiMap<String, String> aggregateDirectionTypeStrings(LinkStore linkStore) {
        // Compute a Map of form: {REVERSE=A to B (3), NONE=Undefined (1), ...}
        NodeStore firstEndpoint = linkStore.getFirstEndpoint();
        NodeStore secondEndpoint = linkStore.getSecondEndpoint();

        String sourceKey = firstEndpoint.getLabel();
        String targetKey = secondEndpoint.getLabel();

        String forwardKey = new StringBuffer(sourceKey).append(" to ").append(targetKey).append(" ").toString();
        String reverseKey = new StringBuffer(targetKey).append(" to ").append(sourceKey).append(" ").toString();

        int countForward = linkStore.getCountForward();
        int countReverse = linkStore.getCountReverse();
        int countNone = linkStore.getCountNone();

        CsiMap<String, String> directions = new CsiMap<String, String>();

        if (countForward > 0) {
            directions.put(LinkDirection.FORWARD.toString(), composeDirectionTypeString(forwardKey, countForward));
        }
        if (countReverse > 0) {
            directions.put(LinkDirection.REVERSE.toString(), composeDirectionTypeString(reverseKey, countReverse));
        }
        if (countNone > 0) {
            directions.put(LinkDirection.NONE.toString(), composeDirectionTypeString("Undirected ", countNone));
        }

        return directions;
    }


   private static String composeDirectionTypeString(String directionKey, int countForward) {
      String result = directionKey;

      if (countForward > 1) {
         result = new StringBuffer(directionKey).append("(").append(countForward).append(")").toString();
      }
      return result;
   }
}
