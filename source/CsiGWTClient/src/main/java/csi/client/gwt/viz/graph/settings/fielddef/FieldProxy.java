package csi.client.gwt.viz.graph.settings.fielddef;

import csi.server.common.model.FieldDef;

public class FieldProxy {

    public String name;
    public FieldDef fieldDef;

    public FieldProxy(FieldDef fieldDef, String name) {
        this.fieldDef = fieldDef;
        this.name = name;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }
}