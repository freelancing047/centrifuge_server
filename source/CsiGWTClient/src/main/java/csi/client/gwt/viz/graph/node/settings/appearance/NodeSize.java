package csi.client.gwt.viz.graph.node.settings.appearance;

import java.util.Set;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.settings.SizingAttribute;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;

public class NodeSize extends AbstractGraphObjectScale {

    public NodeSize(NodeProxy nodeProxy) {
        AttributeDef attributeDef = nodeProxy.getSizeAttributeDef();
        if (attributeDef != null) {
            // implicitly SIZE
            if (attributeDef.getKind().equals(AttributeKind.REFERENCE)) {
                AttributeDef funcDef = nodeProxy.getSizeFunctionAttributeDef();
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
                if (getValue() == 1) {
                    setEnabled(false);
                }
            }
        }
        //else??
    }

    public void persist(NodeProxy nodeProxy) {
        AttributeDef sizeAttributeDef = nodeProxy.getSizeAttributeDef();
        if (sizeAttributeDef == null) {
            sizeAttributeDef = new AttributeDef();
            sizeAttributeDef.setName(SCALE);
            sizeAttributeDef.setHideEmptyInTooltip(false);
            sizeAttributeDef.setIncludeInTooltip(true);
            nodeProxy.getAttributeDefs().add(sizeAttributeDef);
        }
        if (!isEnabled()) {
            setMode(ScaleMode.FIXED);
            setValue(1);
            setMeasure(null);
        }
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
                Set<AttributeDef> attributeDefs = nodeProxy.getAttributeDefs();
                AttributeDef attributeDef = nodeProxy.getSizeFunctionAttributeDef();
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
