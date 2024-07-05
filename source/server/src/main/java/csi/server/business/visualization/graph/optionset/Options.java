package csi.server.business.visualization.graph.optionset;

import java.util.Map;

public class Options {

    public static final String ICON_ATTRIBUTE = "icon";
    public static final String NODE_TYPE_ATTRIBUTE = "nodetype";
    public static final String COLOR_ATTRIBUTE = "color";
    public static final String SHAPE_ATTRIBUTE = "shape";
    public static final String SUBTYPE_ATTRIBUTE = "subtype";
    public static final String WIDTH_ATTRIBUTE = "width";

    public String optionType;
    public String key;
    public Map<String, String> properties;

    public String getOption(String key) {
        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }
}
