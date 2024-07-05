package csi.client.gwt.viz.graph.node.settings.type;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;

public class NodeType {
    private boolean fixed;
    private String text;
    private FieldDef fieldDef;


    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public boolean isFixed() {
        return fixed;
    }

    public String getText() {
        return text;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public NodeType(final NodeProxy nodeProxy) {
        AttributeDef def = nodeProxy.getTypeAttributeDef();
        FieldDef _fieldDef = def.getFieldDef();
        if (_fieldDef != null) {
            fixed = FieldType.STATIC == _fieldDef.getFieldType();
            text = _fieldDef.getStaticText();
            fieldDef = _fieldDef;
        }
    }

    public void persist(NodeProxy nodeProxy) {
        AttributeDef typeAttributeDef = nodeProxy.getTypeAttributeDef();
        FieldDef f;
        if (fixed) {
            f = new FieldDef();
            f.setFieldType(FieldType.STATIC);
            f.setStaticText(text);
            f.setFieldName(null);
        } else {
            f = fieldDef;
        }
        typeAttributeDef.setFieldDef(f);
    }
}
