package csi.client.gwt.viz.graph.link.settings;

import static com.google.common.base.Preconditions.checkNotNull;
import csi.client.gwt.viz.graph.node.settings.tooltip.AbstractGraphTooltip;
import csi.client.gwt.viz.graph.node.settings.tooltip.AnchorLinkType;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipType;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.attribute.AttributeDef;

public class LinkTooltip extends AbstractGraphTooltip {

    public LinkTooltip(AttributeDef attributeDef) {
        checkNotNull(attributeDef);
        key = attributeDef.getUuid();
        displayName = attributeDef.getName();
        function = TooltipFunction.getByAggregateFunction(attributeDef.getAggregateFunction());
        field = attributeDef.getFieldDef();
        if (attributeDef.getFieldDef() != null) {
            if (attributeDef.getFieldDef().isAnonymous()) {
                value = field.getStaticText();
                setType(TooltipType.FIXED);
            } else if (function != null) {
                setType(TooltipType.COMPUTED);
            } else {
                setType(TooltipType.DYNAMIC);
            }
        }
        else{
            setType(TooltipType.FIXED);
        }

        hideEmpty = attributeDef.isHideEmptyInTooltip();
        linkType = attributeDef.getTooltipLinkType();
        link = (linkType != null && !AnchorLinkType.NONE.equals(linkType));
        switch (linkType) {
            case DYNAMIC:
                linkFeildDef = attributeDef.getTooltipLinkFeildDef();
                break;
            case FIXED:
                linkText = attributeDef.getTooltipLinkText();
                break;
            default:
                break;
        }
        // TODO: might only need this for save...
        order = attributeDef.getTooltipOrdinal();
    }

    public LinkTooltip() {
        key = CsiUUID.randomUUID();
    }

}
