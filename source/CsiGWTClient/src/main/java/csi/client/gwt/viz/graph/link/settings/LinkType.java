package csi.client.gwt.viz.graph.link.settings;

import com.google.common.base.Strings;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;

public class LinkType {

    private boolean fixed;
    private boolean hidden = false;
    private String text;
    private FieldDef fieldDef;

    public LinkType(LinkProxy linkProxy) {
        AttributeDef adef = linkProxy.getTypeAttributeDef();
        if (adef == null) {
            hidden = false;
            fixed = true;
            text = CentrifugeConstantsLocator.get().linkSetting_typePlaceholder();
            fieldDef = new FieldDef();
            return;
        }
        FieldDef fdef = adef.getFieldDef();
        if (fdef != null) {
            this.fixed = fdef.getFieldType() == FieldType.STATIC;
            this.hidden = false;
            this.text = fdef.getStaticText();
            this.fieldDef = fdef;
        } else {
            hidden = false;
            fixed = true;
            text = CentrifugeConstantsLocator.get().linkSetting_typePlaceholder();
            fieldDef = new FieldDef();
        }
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef field) {
        fieldDef = field;

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
        AttributeDef typeAttributeDef = linkProxy.getTypeAttributeDef();
        if (typeAttributeDef == null) {
            typeAttributeDef = new AttributeDef();
            typeAttributeDef.setName(ObjectAttributes.CSI_INTERNAL_TYPE);
            linkProxy.getAttributeDefs().add(typeAttributeDef);
        }
        if (fixed) {
            fieldDef = new FieldDef();
            fieldDef.setFieldType(FieldType.STATIC);
            if (Strings.isNullOrEmpty(text)) {
                text = CentrifugeConstantsLocator.get().linkSetting_typePlaceholder();
            }
            fieldDef.setStaticText(text);
            fieldDef.setFieldName(null);
        }
        typeAttributeDef.setFieldDef(fieldDef);
    }

}
