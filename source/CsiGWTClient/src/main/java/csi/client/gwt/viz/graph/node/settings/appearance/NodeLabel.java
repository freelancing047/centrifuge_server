package csi.client.gwt.viz.graph.node.settings.appearance;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;

public class NodeLabel {

    private boolean fixed;
    private boolean hidden;
    private String text;
    private FieldDef field;

    public NodeLabel(boolean fixed, boolean hidden, String text, FieldDef field) {
        this.fixed = fixed;
        this.hidden = hidden;
        this.text = text;
        this.field = field;
    }

    public NodeLabel(NodeProxy nodeProxy) {
        AttributeDef labelAttributeDef = nodeProxy.getLabelAttributeDef();
        FieldDef fieldDef = labelAttributeDef.getFieldDef();
        if (fieldDef != null) {
            // FIXME:This is incorrect.
            // The test for if an attribute is static is attributeDef.getKind().equals(AttributeKind.STATIC)
            this.fixed = fieldDef.getFieldType().equals(FieldType.STATIC);
            this.hidden = nodeProxy.getNodeDef().getHideLabels();
            this.text = fieldDef.getStaticText();
            this.field = fieldDef;
        }
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public FieldDef getField() {
        return field;
    }

    public void setField(FieldDef field) {
        this.field = field;
    }

    public void persist(NodeProxy nodeProxy) {
        AttributeDef labelAttributeDef = nodeProxy.getLabelAttributeDef();
        FieldDef labelField;
        if (fixed) {
            labelField = new FieldDef();
            labelField.setFieldType(FieldType.STATIC);
            labelField.setStaticText(text);
            labelField.setFieldName(null);
        } else {
            labelField = field;
        }
        labelAttributeDef.setFieldDef(labelField);
        nodeProxy.getNodeDef().setHideLabels(hidden);
    }
}
