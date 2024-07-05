package csi.server.business.visualization.deprecated.timeline;

import java.util.Map;

import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.business.visualization.graph.base.property.Property;

public class GroupingIdentifierStrategy extends SimpleEventIdentifierStrategy {

    static String SEPARATOR = "::";
    static int SEPARATOR_LEN = SEPARATOR.length();

    @Override
    public String generate(Map<String, Property> data, int row) {
        String id = super.generate(data, row);

        if (data.containsKey(ObjectAttributes.CSI_INTERNAL_GROUP_ID)) {
            Object value = getValue(data, ObjectAttributes.CSI_INTERNAL_GROUP_ID);
            String group = "";
            if (value != null) {
                group = value.toString();
                group.trim();
            }

            if (group.length() > 0) {
                StringBuilder builder = new StringBuilder(id.length() + group.length() + SEPARATOR_LEN);
                builder.append(id).append(SEPARATOR).append(group);
                id = builder.toString();
            }

        }
        return id;
    }

}
