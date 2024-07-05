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

public class LinkSize extends AbstractGraphObjectScale {

    protected LinkSize(LinkProxy linkProxy) {
        AttributeDef attributeDef = linkProxy.getSizeAttributeDef();
        if (attributeDef != null) {
            // implicitly SIZE
            if (attributeDef.getKind().equals(AttributeKind.REFERENCE)) {
                AttributeDef funcDef = linkProxy.getSizeFunctionAttributeDef();
                if (funcDef != null && ObjectAttributes.CSI_INTERNAL_SIZE_FUNCTION.equals(attributeDef.getReferenceName())) {
                    setMode(ScaleMode.COMPUTED);
                    setFunction(TooltipFunction.getByAggregateFunction(funcDef.getAggregateFunction()));
                    setField(funcDef.getFieldDef());
                }
                else{
                    setMode(ScaleMode.DYNAMIC);
                    setMeasure(SizingAttribute.valueOfIgnoreCase(attributeDef.getReferenceName()));
                }
            }// implicitly FIXED
            if (getMode().equals(ScaleMode.FIXED)) {
                try {
                    setValue(Double.parseDouble(attributeDef.getFieldDef().getStaticText()));
                } catch (NumberFormatException e) {
                    setValue(1);
                }
            }
        }
        
        setEnabled(linkProxy.isWidthOverride());
    }

    protected void persist(LinkProxy linkProxy) {
        AttributeDef sizeAttributeDef = linkProxy.getSizeAttributeDef();
        if (sizeAttributeDef == null) {
            sizeAttributeDef = new AttributeDef();
            sizeAttributeDef.setName(SCALE);
            sizeAttributeDef.setHideEmptyInTooltip(false);
            sizeAttributeDef.setIncludeInTooltip(true);
            linkProxy.getAttributeDefs().add(sizeAttributeDef);
        }
        if (!isEnabled()) {
            setMode(ScaleMode.FIXED);
            setValue(1);
            setMeasure(null);
        }
        
        linkProxy.setWidthOverride(isEnabled());
        
        // Dimension

        sizeAttributeDef.setBySize(true);
        sizeAttributeDef.setByStatic(getMode() == ScaleMode.FIXED);

        // MODE
        switch (getMode()) {
            case DYNAMIC:
                sizeAttributeDef.setKind(AttributeKind.REFERENCE);
                sizeAttributeDef.setReferenceName(getMeasure().toString());
                sizeAttributeDef.setFieldDef(null);
                break;
            case FIXED:
                sizeAttributeDef.setKind(AttributeKind.STATIC);
                sizeAttributeDef.setReferenceName(null);
                FieldDef fieldDef = sizeAttributeDef.getFieldDef();
                if (fieldDef == null) {
                    fieldDef = new FieldDef();
                    fieldDef.setFieldType(FieldType.STATIC);
                    sizeAttributeDef.setFieldDef(fieldDef);
                }
                fieldDef.setFieldType(FieldType.STATIC);
                fieldDef.setStaticText("" + getValue());
                break;
            case COMPUTED:
                Set<AttributeDef> attributeDefs = linkProxy.getAttributeDefs();
                AttributeDef attributeDef = linkProxy.getSizeFunctionAttributeDef();
                if (attributeDef == null) {
                    attributeDef = new AttributeDef();
                    attributeDef.setName(ObjectAttributes.CSI_INTERNAL_SIZE_FUNCTION);
                    attributeDefs.add(attributeDef);
                }

                attributeDef.setFieldDef(getField());
                attributeDef.setAggregateFunction(getFunction().getAggregateType());
                attributeDef.setBySize(false);
                attributeDef.setByStatic(false);
                attributeDef.setIncludeInTooltip(false);
                attributeDef.setKind(AttributeKind.COMPUTED);


                sizeAttributeDef.setIncludeInTooltip(false);
                sizeAttributeDef.setReferenceName(ObjectAttributes.CSI_INTERNAL_SIZE_FUNCTION);
                sizeAttributeDef.setKind(AttributeKind.REFERENCE);
                sizeAttributeDef.setByStatic(false);
                sizeAttributeDef.setFieldDef(null);
                break;
        }
    }
}
