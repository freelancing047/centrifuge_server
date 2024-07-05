package csi.server.business.visualization.deprecated.timeline;

import java.util.List;
import java.util.Map;

import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;

public class SimpleEventIdentifierStrategy implements IEventIDGenerator {

    public SimpleEventIdentifierStrategy() {
    }

    /**
     * Returns null if invalid e.g. no id or label.
     */
    @Override
    public String generate(Map<String, Property> data, int row) {

        String id;

        Object idOrLabel = getValue(data, ObjectAttributes.CSI_INTERNAL_ID);
        if (idOrLabel == null) {
            idOrLabel = getValue(data, ObjectAttributes.CSI_INTERNAL_LABEL);
        }

        if (idOrLabel == null) {
            //We want to return the same ID each time this graph is loaded,
            //so we attach thw row id to a known constant, instead of returning a new
            //uuid, as we did previously.
            return "csi-internal-"+row;
        }

        Object startTimestamp = getValue(data, ObjectAttributes.CSI_INTERNAL_START_DATE);
        Object endTimestamp = getValue(data, ObjectAttributes.CSI_INTERNAL_END_DATE);

        StringBuilder buf = new StringBuilder();
        buf.append(idOrLabel.toString());

        if (startTimestamp != null) {
            buf.append("::").append(startTimestamp.toString());
        }

        if (endTimestamp != null) {
            buf.append("::").append(endTimestamp.toString());
        }

        id = buf.toString();
        return id;
    }

   protected Object getValue(Map<String, Property> data, String propName) {
      Object value = null;

      if ((data != null) && (propName != null)) {
         Property property = data.get(propName);

         if (property != null) {
            List<Object> values = property.getValues();

            if ((values != null) && !values.isEmpty()) {
               value = values.get(0);
            }
         }
      }
      return value;
   }
}
