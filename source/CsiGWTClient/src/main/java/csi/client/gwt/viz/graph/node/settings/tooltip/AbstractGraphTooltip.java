package csi.client.gwt.viz.graph.node.settings.tooltip;

import csi.client.gwt.util.Display;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTipItem;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.attribute.AttributeKind;

public class AbstractGraphTooltip {

    protected String displayName;
    protected FieldDef field;
    protected String key;
    protected boolean link;
    protected String value;
    private TooltipType type = TooltipType.DYNAMIC;
    protected AnchorLinkType linkType = AnchorLinkType.NONE;
    protected boolean hideEmpty = true;
    protected FieldDef linkFeildDef;
    protected String linkText;
    protected TooltipFunction function;
    private ToolTipItem graphAttribute;
    protected int order = 0;

    public AbstractGraphTooltip() {
        super();
    }

    public AttributeDef asAttributeDef() {
        AttributeDef def = new AttributeDef();
        def.setUuid(key);
        def.setName(displayName);
        // Since this is a tooltip we always want to include it in tooltips...
        def.setIncludeInTooltip(true);
        def.setHideEmptyInTooltip(hideEmpty());
        def.setTooltipOrdinal(order);
        def.setTooltipLinkType(linkType);
        switch (type) {
            case COMPUTED:
                def.setAggregateFunction(function.getAggregateType());
                def.setKind(AttributeKind.COMPUTED);
                def.setFieldDef(field);
                break;
            case DYNAMIC:
                def.setFieldDef(field);
                break;
            case FIXED:
                FieldDef fieldDef = new FieldDef();
                fieldDef.setFieldName(null);
                fieldDef.setStaticText(value);
                fieldDef.setFieldType(FieldType.STATIC);
                def.setFieldDef(fieldDef);
                break;
            case GRAPH_ATTRIBUTE:
                // TODO:
                break;
            default:
                // should error here
                break;
        }
        if (link) {
            switch (linkType) {
                case DYNAMIC:
                    def.setTooltipLinkFeildDef(linkFeildDef);
                    break;
                case SIMPLE:
                    def.setTooltipLinkFeildDef(def.getFieldDef());
                    break;
                case FIXED:
                    def.setTooltipLinkText(linkText);
                    break;
                case NONE:
                    break;
                default:
                    break;
            }
        }
        return def;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FieldDef getFieldDef() {
        return field;
    }

    public TooltipFunction getFunction() {
        return function;
    }

    public String getKey() {
        return key;
    }

    public FieldDef getLinkFeildDef() {
        return linkFeildDef;
    }

    public String getLinkText() {
        return linkText;
    }

    public AnchorLinkType getLinkType() {
        return linkType;
    }

    public TooltipType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean hideEmpty() {
        return hideEmpty;
    }

    public boolean isLink() {
        return link;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public void setField(FieldDef field) {
        this.field = field;
    }

    public void setFieldDef(FieldDef field) {
        this.field = field;
    }

    public void setFunction(TooltipFunction function) {
        this.function = function;
    }

    public void setHideEmpty(boolean hideEmpty) {
        this.hideEmpty = hideEmpty;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public void setLinkFeildDef(FieldDef linkFeildDef) {
        this.linkFeildDef = linkFeildDef;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public void setLinkType(AnchorLinkType simple) {
        this.linkType = simple;
    
    }

    public void setType(TooltipType type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ToolTipItem getGraphAttribute() {
        return graphAttribute;
    }

    public void setGraphAttribute(ToolTipItem graphAttribute) {
        this.graphAttribute = graphAttribute;
    }

    public void setOrder(int order) {
        this.order = order;
    
    }

    public int getOrder() {
        return order;
    }

}