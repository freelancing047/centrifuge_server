package csi.client.gwt.viz.graph.node;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.util.HasXY;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.node.settings.tooltip.NodeTooltip;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.client.gwt.viz.graph.settings.fielddef.FieldProxy;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.shared.core.color.ClientColorHelper;

public class NodeProxy implements HasXY {

    // Override text is almost completely unimportant, but can't be empty or
    // null
    private static final String OVERRIDE_DEF_TEXT = "override";
    private static final String HIDE_ALL_EMPTY_LABELS = "hideAllEmptyLabels";

    private NodeProxy(FieldDef fieldDef) {
        nodeDef = new NodeDef();
        setRandomPositon();
        setFieldDef(fieldDef);
        nodeDef.setHideLabels(false);
        setHideEmptyTooltips(true);
        setRandomShape();
        setColor(getRandomColor());
    }

    NodeProxy(FieldProxy object) {
        this(object.getFieldDef());

    }

    private static String getRandomColor() {
        float satuartion = (float) ((Random.nextDouble() / 5F) + .7F);
        float value = (float) ((Random.nextDouble() / 4F) + .75F);
        return ClientColorHelper.get().randomHueWheel(satuartion, value).getIntColor() + "";
    }

    private NodeDef nodeDef;

    NodeProxy() {
        this.nodeDef = new NodeDef();
    }

    NodeProxy(NodeDef nodeDef) {
        this.nodeDef = nodeDef;
    }

    public int getColor() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR);
        if (attributeDef == null) {
            return 0;
        }
        FieldDef fieldDef = attributeDef.getFieldDef();
        if (fieldDef == null) {
            return 0;
        }
        String colorString = fieldDef.getStaticText();
        return Integer.parseInt(colorString);
    }

    public String getFieldName() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE).getFieldDef();
        return fieldDef.getFieldName();
    }

    public FieldType getFieldType() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE).getFieldDef();
        return fieldDef.getFieldType();
    }

    public String getIconURI() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON);
        if (attributeDef == null) {
            return null;
        }
        FieldDef fieldDef = attributeDef.getFieldDef();
        if(attributeDef.getFieldDef() == null){
            return null;
        }
        return fieldDef.getStaticText();
    }

    public String getID() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID).getFieldDef();
        return fieldDef.getStaticText();
    }

    public String getIDfieldName() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID).getFieldDef();
        return fieldDef.getFieldName();
    }

    public String getLabel() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL).getFieldDef();
        return fieldDef.getStaticText();
    }

    public String getLabelFieldName() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL).getFieldDef();
        return fieldDef.getFieldName();
    }

    public String getName() {
        return nodeDef.getName();
    }

    public NodeDef getNodeDef() {
        return nodeDef;
    }

    public ShapeType getShape() {
        ShapeType shape = ShapeType.getNextNodeShape();
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE);
        if (attributeDef != null) {

            FieldDef fieldDef = attributeDef.getFieldDef();
            shape = ShapeType.getShape(fieldDef.getStaticText());
        }
        return shape;
    }

    public String getType() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE).getFieldDef();
        if (FieldType.STATIC.equals(fieldDef.getFieldType())) {
            return fieldDef.getStaticText();
        }
        return "";
    }

    public String getUuid() {
        return nodeDef.getUuid();
    }

    public double getX() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_Y_POS).getFieldDef();
        String xPosString = fieldDef.getStaticText();
        return Double.parseDouble(xPosString);
    }

    public double getY() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_X_POS).getFieldDef();
        String xPosString = fieldDef.getStaticText();
        return Double.parseDouble(xPosString);
    }

    public boolean hasID() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
        return attributeDef != null;
    }

    public boolean hasLabel() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
        return attributeDef != null;
    }

    public boolean hasName() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_NAME);
        return attributeDef != null;
    }

    public boolean hasType() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        return attributeDef != null;
    }

    public boolean isIDAnonymous() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID).getFieldDef();
        return fieldDef.isAnonymous();
    }

    public boolean isLabelAnonymous() {
        FieldDef fieldDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL).getFieldDef();
        return fieldDef.isAnonymous();
    }

    public void setAddPrefixId(boolean value) {
        nodeDef.setAddPrefixId(value);
    }

    public void setColor(String color) {
        checkNotNull(color);
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR);
        if ((attributeDef != null) && attributeDef.getFieldDef().isAnonymous()
                && (attributeDef.getFieldDef().getFieldType() == FieldType.STATIC)) {
        } else {
            FieldDef fieldDef = new FieldDef(FieldType.STATIC);
            attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR, fieldDef);
        }
        attributeDef.getFieldDef().setStaticText(color);
        // FIXME: Magic String
        attributeDef.getFieldDef().setValueType(CsiDataType.Number);
        nodeDef.addAttributeDef(attributeDef);
    }

    private void setFieldDef(FieldDef fieldDef) {
        checkNotNull(fieldDef);
        setLabelField(fieldDef);
        setName(fieldDef.getFieldName());
        setIDField(fieldDef);
        FieldDef fieldDef2 = new FieldDef();
        fieldDef2.setFieldType(FieldType.STATIC);
        fieldDef2.setStaticText(fieldDef.getFieldName());
        fieldDef2.setFieldName(null);
        setType(fieldDef2);
        // not yet implemented
        // this.addPrefixId =
        // ModelLocator.getInstance().getSystemPreferenceBoolean(ModelLocator.QUALIFY_BY_NODE_TYPE_DEFAULT);
    }

    private void setIDField(FieldDef fieldDef) {
        checkNotNull(fieldDef);
        AttributeDef attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_ID, fieldDef);
        nodeDef.addAttributeDef(attributeDef);
    }

    private void setLabelField(FieldDef fieldDef) {
        checkNotNull(fieldDef);
        AttributeDef attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL, fieldDef);
        nodeDef.addAttributeDef(attributeDef);
    }

    public void setName(String name) {
        checkNotNull(name);
        nodeDef.setName(name);
    }

    // private void setRandomShape() {
    // setShape(ShapeType.getNextNodeShape());
    // }

    private void setRandomShape() {
        setShape(ShapeType.getNextNodeShape());
    }

    private void setRandomPositon() {// we can do better
        // Places the node randomly in the middle 80%
        double x = (Random.nextDouble() * .8) + .1;
        double y = (Random.nextDouble() * .8) + .1;
        setPosition(x, y);
    }

    public boolean isShapeOverride() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE);
        return attributeDef != null;
    }

    public void setShapeOverride(boolean override) {

        if (override) {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE);
            if ((attributeDef == null)) {
                FieldDef fieldDef = new FieldDef(FieldType.STATIC);
                fieldDef.setStaticText(OVERRIDE_DEF_TEXT);
                attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE, fieldDef);
                nodeDef.addAttributeDef(attributeDef);
            }
        } else {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE_OVERRIDE);
            if (attributeDef != null) {
                nodeDef.removeAttributeDef(attributeDef);
            }
        }
    }

    public boolean isIconOverride() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE);
        return attributeDef != null;
    }

    public void setIconOverride(boolean override) {

        if (override) {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE);
            if ((attributeDef == null)) {
                FieldDef fieldDef = new FieldDef(FieldType.STATIC);
                fieldDef.setStaticText(OVERRIDE_DEF_TEXT);
                attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE, fieldDef);
                nodeDef.addAttributeDef(attributeDef);
            }
        } else {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON_OVERRIDE);
            if (attributeDef != null) {
                nodeDef.removeAttributeDef(attributeDef);
            }
        }
    }

    public boolean isColorOverride() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
        return attributeDef != null;
    }

    public void setColorOverride(boolean override) {

        if (override) {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
            if ((attributeDef == null)) {
                FieldDef fieldDef = new FieldDef(FieldType.STATIC);
                fieldDef.setStaticText(OVERRIDE_DEF_TEXT);
                attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE, fieldDef);
                nodeDef.addAttributeDef(attributeDef);
            }
        } else {
            AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
            if (attributeDef != null) {
                nodeDef.removeAttributeDef(attributeDef);
            }
        }
    }

    public void setShape(ShapeType shape) {
        checkNotNull(shape);
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE);
        if ((attributeDef == null)) {
            FieldDef fieldDef = new FieldDef(FieldType.STATIC);
            attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_SHAPE, fieldDef);
        }
        // FIXME: Magic String
        attributeDef.getFieldDef().setValueType(CsiDataType.String);
        attributeDef.getFieldDef().setStaticText(shape.toString());
        nodeDef.addAttributeDef(attributeDef);
    }

    private void setType(FieldDef fieldDef) {
        checkNotNull(fieldDef);
        AttributeDef attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE, fieldDef);
        nodeDef.addAttributeDef(attributeDef);
    }

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    public void setX(double d) {
        checkArgument((d >= 0) && (d <= 1), "All nodeDefs should be visible in the graph configuration");
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_Y_POS);
        if (attributeDef == null) {
            // if I don't have one, make one
            FieldDef fieldDef = new FieldDef(FieldType.STATIC);
            attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_Y_POS, fieldDef);
            nodeDef.addAttributeDef(attributeDef);
        }
        FieldDef fieldDef = attributeDef.getFieldDef();
        fieldDef.setStaticText("" + d);
    }

    public void setY(double d) {
        checkArgument((d >= 0) && (d <= 1), "All nodeDefs should be visible in the graph configuration");
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_X_POS);
        if (attributeDef == null) {
            // if I don't have one, make one
            FieldDef fieldDef = new FieldDef(FieldType.STATIC);
            attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_X_POS, fieldDef);
            nodeDef.addAttributeDef(attributeDef);
        }
        FieldDef fieldDef = attributeDef.getFieldDef();
        fieldDef.setStaticText("" + d);
    }

    public void setScale(double iconScale) {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SCALE);
        if (attributeDef == null) {
            // if I don't have one, make one
            FieldDef fieldDef = new FieldDef(FieldType.STATIC);
            attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_SCALE, fieldDef);
            nodeDef.addAttributeDef(attributeDef);
        }
        FieldDef fieldDef = attributeDef.getFieldDef();
        fieldDef.setValueType(CsiDataType.Number);
        fieldDef.setStaticText("" + iconScale);
    }

    public void setIconURI(String iconURI) {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON);
        if (attributeDef == null) {
            // if I don't have one, make one
            FieldDef fieldDef = new FieldDef(FieldType.STATIC);
            attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_ICON, fieldDef);
            nodeDef.addAttributeDef(attributeDef);
        }
        FieldDef fieldDef = attributeDef.getFieldDef();
        fieldDef.setValueType(CsiDataType.String);
        fieldDef.setStaticText(iconURI);
    }

    public Image getRenderedIcon(GraphTheme graphTheme) {
        final int size = 40;// FIXME
        final int color = getColor();
        final ShapeType shape = getShape();
        final String iconURI = getIconURI();
        final Image image = new Image();
        NodeStyle style = null;
        Double scale = null;
        if(graphTheme != null){
            style = graphTheme.findNodeStyle(getType()); 
            if(style != null){
                scale = style.getIconScale();
            }
        }
        if(scale == null){
            GraphImpl.getRenderedIcon(iconURI, shape, color, size, 1.0, image);
        } else {
            GraphImpl.getRenderedIcon(iconURI, shape, color, size, scale, image);
        }
        return image;
    }

    public boolean hideEmptyTooltips() {
        String string = nodeDef.getClientProperties().get(HIDE_ALL_EMPTY_LABELS);
        return Boolean.parseBoolean(string);
    }

    public void setHideEmptyTooltips(boolean hide) {
        nodeDef.getClientProperties().put(HIDE_ALL_EMPTY_LABELS, hide + "");
    }

    public String getIconName() {
        String iconURI = getIconURI();
        return iconURI;
    }

    public String getIconCategory() {
        String iconURI = getIconURI();
        if (Strings.isNullOrEmpty(iconURI)) {
            return iconURI;
        }
        String[] splitIconURI = iconURI.split("/");
        if (splitIconURI.length < 2) {
            return iconURI;
        }
        String iconCategory = splitIconURI[splitIconURI.length - 2];
        return iconCategory;
    }

    public double getSize() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE);
        if (attributeDef.getKind().equals(AttributeKind.STATIC)) {
            FieldDef fieldDef = attributeDef.getFieldDef();
            String xPosString = fieldDef.getStaticText();
            return Double.parseDouble(xPosString);
        }
        return 1;
    }

    public boolean isSizeByField() {
        AttributeDef attributeDef = nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE);
        return attributeDef.getKind().equals(AttributeKind.REFERENCE);
    }

    public String getSizeByFieldName() {
        String fieldName = "";
        AttributeDef attributeDef = getSizeAttributeDef();
        if (attributeDef != null) {
            fieldName = attributeDef.getReferenceName();
        }
        return fieldName;
    }

    public AttributeDef getSizeAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE);
    }

    public AttributeDef getSizeFunctionAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE_FUNCTION);
    }

    public AttributeDef getLabelAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_LABEL);
    }

    public AttributeDef getTypeAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
    }

    public AttributeDef getIconAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ICON);
    }

    public void removeIcon() {
        nodeDef.removeAttributeDef(getIconAttributeDef());
    }

    public void setHideLabels(boolean hideLabels) {
        nodeDef.setHideLabels(hideLabels);
    }

    public Set<AttributeDef> getAttributeDefs() {
        return nodeDef.getAttributeDefs();
    }

    public void addTooltip(NodeTooltip tooltip) {
        Set<AttributeDef> tooltipFields = getAttributeDefs();
        // This function marshall the client side tooltip concept to the server
        // tooltip concept
        // does the new tooltip field have an AttributeDef already
        AttributeDef newTooltipDef = tooltip.asAttributeDef();
        if (newTooltipDef == null) {
            // TODO: Error
            return;
        }
        HashSet<AttributeDef> temp = Sets.newHashSet(tooltipFields);
        for (AttributeDef tooltipField : temp) {
            if (tooltipField.getName().equals(newTooltipDef.getName())) {
                tooltipFields.remove(tooltipField);
            }
        }
        tooltipFields.add(newTooltipDef);
    }

    public void removeTooltips() {
        Set<AttributeDef> attributeDefs = getAttributeDefs();
        List<AttributeDef> tooltipAttributeDefs = getTooltipAttributeDefs();
        attributeDefs.removeAll(tooltipAttributeDefs);
    }

    public List<AttributeDef> getTooltipAttributeDefs() {
        List<AttributeDef> out = Lists.newArrayList();
        Set<AttributeDef> attributeDefs = getAttributeDefs();
        for (AttributeDef attributeDef : attributeDefs) {
            if (attributeDef.getName().indexOf("csi.internal.") != 0) {
                out.add(attributeDef);
            }
        }
        return out;
    }

    public void setIcon(FieldDef fieldDef) {
        checkNotNull(fieldDef);
        AttributeDef attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_ICON, fieldDef);
        nodeDef.addAttributeDef(attributeDef);

    }

    public AttributeDef getTransparencyAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);
    }

    public AttributeDef getTransparencyFunctionAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY_FUNCTION);
    }

    public AttributeDef getIdentityAttributeDef() {
        return nodeDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_ID);
    }

    public void apply(GraphSettings graphSettings) {
        String nodeType = getType();
        if (nodeType == null) {
            return;
        }

        if (graphSettings.getCurrentTheme() == null) {
            if(!isIconOverride()){
                setIconURI(null);
                if (!isShapeOverride() && (getShape() == null || getShape() == ShapeType.NONE)) {
                    setShape(ShapeType.getNextNodeShape());
                }
            }
            
        } else {
            NodeStyle nodeStyle = graphSettings.getNodeStyle(nodeType);
            if (nodeStyle != null) {
                if (!isShapeOverride()) {
                    ShapeType shape = nodeStyle.getShape();
                    if (shape != null) {
                        setShape(shape);
                    }
                }

                if (!isColorOverride()) {

                    Integer color = nodeStyle.getColor();
                    if (color != null) {
                        setColor(color + "");
                    }
                }

                if (!isIconOverride()) {
                    String iconId = nodeStyle.getIconId();
                    if (iconId != null && !iconId.isEmpty()) {
                        setIconURI(iconId);
                    } else {
                        setIconURI(null);
                    }
                }

                if (nodeStyle.getIconScale() != null)
                    setScale(nodeStyle.getIconScale());

                // if (!Strings.isNullOrEmpty(nodeStyle.getIconId())) {
                // setIconURI(nodeStyle.getIconId());//setIconURI("/Centrifuge/resources/icons/"
                // + iconRoot + "/" + nodeTheme.icon);//NON-NLS
                // }
            } else {
                if(graphSettings.getCurrentTheme().getDefaultShape() != null && !isShapeOverride()){
                    setShape(graphSettings.getCurrentTheme().getDefaultShape());
                }
                if(!isIconOverride())
                    setIconURI(null);
            }
        }

        if (getShape() == null) {
            setShape(ShapeType.getNextNodeShape());
        }
    }

}
