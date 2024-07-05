package csi.shared.gwt.viz.viewer.LensImage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;

import java.util.Map;

public class FieldLensImage implements LensImage {
    public HashBasedTable<String, String, String> table = HashBasedTable.create();
    public Map<String, Map<String, Integer>> fieldValueMapMap = Maps.newHashMap();
    private String label;

    public void add(String s, String func, String value) {
        if (value == null) {
            return;
        }
        table.put(s, func, value);

    }

    public String get(String a, String b) {
        return table.get(a, b);
    }

    public void addFieldValue(String uuid, String string, int anInt) {
        if (fieldValueMapMap == null) {
            fieldValueMapMap = Maps.newHashMap();
        }
        if (!fieldValueMapMap.containsKey(uuid)) {
            fieldValueMapMap.put(uuid, Maps.newHashMap());

        }
        fieldValueMapMap.get(uuid).put(string, anInt);
    }
    private String lensDef;
    @Override
    public String getLensDef() {
        return lensDef;
    }

    public void setLensDef(String lensDef) {
        this.lensDef = lensDef;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String s) {
        label = s;
    }
}
