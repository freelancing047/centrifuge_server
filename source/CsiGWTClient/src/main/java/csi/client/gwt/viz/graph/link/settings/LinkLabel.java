package csi.client.gwt.viz.graph.link.settings;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;

public class LinkLabel {

    private boolean fixed;
    private boolean hidden;
    private String text;
    private FieldDef fieldDef;

    protected LinkLabel(boolean fixed, boolean hidden, String text, FieldDef fieldDef) {
        this.fixed = fixed;
        this.hidden = hidden;
        this.text = text;
        this.fieldDef = fieldDef;
    }

    protected LinkLabel(LinkProxy linkProxy) {
        hidden = linkProxy.getLinkDef().isHideLabels();
        AttributeDef adef = linkProxy.getLabelAttributeDef();
        if (adef != null) { //defaults
            FieldDef fdef = adef.getFieldDef();
            if (fdef != null) {
                this.fixed = fdef.getFieldType().equals(FieldType.STATIC);
                this.text = fdef.getStaticText();
                this.fieldDef = fdef;
            }
        } else { //default values
            hidden = true;
            fixed = true;
            text = "";
            fieldDef = new FieldDef();
        }
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public void persist(LinkProxy linkProxy) {
        AttributeDef labelAttributeDef = linkProxy.getLabelAttributeDef();
        if (labelAttributeDef == null) {
            labelAttributeDef = new AttributeDef();
            labelAttributeDef.setName(ObjectAttributes.CSI_INTERNAL_LABEL);
            linkProxy.getAttributeDefs().add(labelAttributeDef);
        }
        if (isFixed()) {
            fieldDef = new FieldDef();
            fieldDef.setFieldType(FieldType.STATIC);
            fieldDef.setStaticText(getText());
            fieldDef.setFieldName(null);
        }
        labelAttributeDef.setFieldDef(fieldDef);
        linkProxy.getLinkDef().setHideLabels(hidden);
    }
}
