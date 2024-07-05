package csi.client.gwt.viz.graph.link.settings;

import java.util.Set;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.settings.SizingAttribute;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;

public class LinkTransparency extends AbstractGraphObjectScale {

    public LinkTransparency(LinkProxy linkProxy) {
        setValue(100);
        AttributeDef attributeDef = linkProxy.getTransparencyAttributeDef();
        if (attributeDef != null) {
            // implicitly SIZE
            if (attributeDef.getKind().equals(AttributeKind.REFERENCE)) {
                AttributeDef funcDef = linkProxy.getTransparencyFunctionAttributeDef();
                if (funcDef != null && ObjectAttributes.CSI_INTERNAL_TRANSPARENCY_FUNCTION.equals(attributeDef.getReferenceName())) {
                    setMode(ScaleMode.COMPUTED);
                    setFunction(TooltipFunction.getByAggregateFunction(funcDef.getAggregateFunction()));
                    setField(funcDef.getFieldDef());
                } else {
                    setMode(ScaleMode.DYNAMIC);
                    setMeasure(SizingAttribute.valueOfIgnoreCase(attributeDef.getReferenceName()));
                }
            }// implicitly FIXED
            if (getMode().equals(ScaleMode.FIXED)) {
                try {
                    setValue(Double.parseDouble(attributeDef.getFieldDef().getStaticText()));
                } catch (NumberFormatException e) {
                }
                if (getValue() == 100) {
                    setEnabled(false);
                }
            }
        }
    }

    public void persist(LinkProxy linkProxy) {
        AttributeDef def = linkProxy.getTransparencyAttributeDef();
        if (def == null) {
            def = new AttributeDef();
            def.setName(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);
            def.setHideEmptyInTooltip(false);
            def.setIncludeInTooltip(true);
            linkProxy.getAttributeDefs().add(def);

            Set<AttributeDef> attributeDefs = linkProxy.getAttributeDefs();
            AttributeDef attributeDef = linkProxy.getTransparencyFunctionAttributeDef();
            if (attributeDef != null) {
                attributeDefs.remove(attributeDef);
            }
        }
        if (!isEnabled()) {
            setMode(ScaleMode.FIXED);
            setValue(100);
            setMeasure(null);
        }

        // MODE
        switch (getMode()) {
            case DYNAMIC:
                def.setKind(AttributeKind.REFERENCE);
                def.setReferenceName(getMeasure().toString());
                def.setFieldDef(null);
                break;
            case FIXED:
                def.setByStatic(true);
                def.setKind(AttributeKind.STATIC);
                def.setReferenceName(null);
                FieldDef fieldDef = def.getFieldDef();
                if (fieldDef == null) {
                    fieldDef = new FieldDef();
                    fieldDef.setFieldType(FieldType.STATIC);
                    def.setFieldDef(fieldDef);
                }
                fieldDef.setFieldType(FieldType.STATIC);
                fieldDef.setStaticText("" + getValue());
                break;
            case COMPUTED:
                Set<AttributeDef> attributeDefs = linkProxy.getAttributeDefs();
                AttributeDef attributeDef = linkProxy.getTransparencyFunctionAttributeDef();
                if (attributeDef == null) {
                    attributeDef = new AttributeDef();
                    attributeDef.setName(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY_FUNCTION);
                    attributeDefs.add(attributeDef);
                }
                String referencedAttrDef = ObjectAttributes.CSI_INTERNAL_TRANSPARENCY_FUNCTION;
                attributeDef.setFieldDef(getField());
                attributeDef.setAggregateFunction(getFunction().getAggregateType());
                attributeDef.setBySize(false);
                attributeDef.setByStatic(false);
                attributeDef.setIncludeInTooltip(false);
                attributeDef.setKind(AttributeKind.COMPUTED);
                attributeDef.setName(referencedAttrDef);


                def.setIncludeInTooltip(false);
                def.setReferenceName(referencedAttrDef);
                def.setKind(AttributeKind.REFERENCE);
                def.setByStatic(false);
                def.setAggregateFunction(getFunction().getAggregateType());
                def.setFieldDef(null);
                break;
        }
    }

    @Override
    public void setValue(double value) {
        if (value > 100) {
            this.value = 100;
        } else if (value < 0) {
            this.value = 0;
        } else {
            this.value = value;
        }
    }
}
