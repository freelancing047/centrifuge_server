package csi.client.gwt.viz.graph.node.settings.appearance;

import com.google.common.base.Strings;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.settings.NodeSettings;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;


public class NodeIcon {

    private final NodeSettings nodeSettings;
    private boolean enabled;
    private String name;
    private boolean fixed = true;
    private FieldDef fieldDef = null;

    public NodeIcon(NodeProxy nodeProxy, NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
        this.name = nodeProxy.getIconName();
        AttributeDef iconAttributeDef = nodeProxy.getIconAttributeDef();
        enabled = nodeProxy.isIconOverride();
        if (enabled) {
            FieldDef field = iconAttributeDef.getFieldDef();
            if (field != null) {
                this.fixed = Strings.isNullOrEmpty(field.getFieldName());
                this.fieldDef = field;
            }
        }
    }

    public String getName() {
        if (name == null || !fixed) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public void persist(NodeProxy nodeProxy) {
        if (isEnabled()) {
            AttributeDef def = nodeProxy.getIconAttributeDef();

            nodeProxy.setIconOverride(true);
            if (def == null) {
                def = new AttributeDef();
                def.setName(ObjectAttributes.CSI_INTERNAL_ICON);
                def.setHideEmptyInTooltip(true);
                def.setIncludeInTooltip(false);
                nodeProxy.getAttributeDefs().add(def);
            }
            if (fixed) {
                FieldDef fieldDef = new FieldDef(FieldType.STATIC);
                fieldDef.setValueType(CsiDataType.String);
                fieldDef.setStaticText(getName());
                def.setFieldDef(fieldDef);
            } else {
                def.setFieldDef(fieldDef);
            }
        } else {
            nodeProxy.setIconOverride(false);
            nodeProxy.removeIcon();
        }
    }

//    public String getIconURI() {
//        return "/Centrifuge/resources/icons/" + nodeSettings.getIconRoot() + "/" + getCategory() + "/"
//                + getName() + ".png";
//    }
}
