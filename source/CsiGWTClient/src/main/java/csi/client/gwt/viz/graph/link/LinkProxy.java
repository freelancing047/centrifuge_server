package csi.client.gwt.viz.graph.link;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.regexp.shared.RegExp;

import csi.client.gwt.viz.graph.link.settings.LinkTooltip;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.shared.core.color.ClientColorHelper.Color;

public class LinkProxy {

    private static RegExp uuidPatternRegExp;

    private static boolean matchUuidPattern(String name) {
        if (uuidPatternRegExp == null) {
            String hex = "[0-9a-f]";
            String hex4 = hex + "{4}";
            String hex8 = hex + "{8}";
            String hex12 = hex + "{12}";
            String dash = "-";
            String uuidPattern = hex8 + dash + hex4 + dash + hex4 + dash + hex4 + dash + hex12;
            uuidPatternRegExp = RegExp.compile(uuidPattern);
        }
        return uuidPatternRegExp.test(name);
    }

    private LinkDef linkDef;

    // private static Map<String, LinkProxy> linkProxyMap = Maps.newTreeMap();

    private LinkProxy() {
        linkDef = new LinkDef();
    }

    public LinkProxy(LinkDef linkDef) {
        this.linkDef = linkDef;
    }

    public NodeDef getNode1() {
        return linkDef.getNodeDef1();
    }

    public NodeDef getNode2() {
        return linkDef.getNodeDef2();
    }

    // public static Optional<LinkProxy> get(String UUID) {
    // return Optional.fromNullable(linkProxyMap.get(UUID));
    // }

    public static LinkProxy get(LinkDef linkDef) {
        // LinkProxy linkProxy = linkProxyMap.get(linkDef.getUuid());
        // if (linkProxy == null) {
        LinkProxy linkProxy = new LinkProxy(linkDef);
        // linkProxyMap.put(linkDef.getUuid(), linkProxy);
        // }
        return linkProxy;
    }

    public static LinkProxy create(NodeProxy node1, NodeProxy node2) {
        LinkProxy linkProxy = new LinkProxy();
        linkProxy.setNode1(node1);
        linkProxy.setNode2(node2);
        // linkProxyMap.put(linkProxy.getUUID(), linkProxy);
        return linkProxy;
    }

    private String getUUID() {
        return linkDef.getUuid();
    }

    public void setNode1(NodeProxy nodeProxy) {
        linkDef.setNodeDef1(nodeProxy.getNodeDef());
    }

    public void setNode2(NodeProxy nodeProxy) {
        linkDef.setNodeDef2(nodeProxy.getNodeDef());
    }

    public LinkDef getLinkDef() {
        return linkDef;
    }

    public String getName() {
        if (linkDef.getName().equals(linkDef.getUuid()) || matchUuidPattern(linkDef.getName())) {
            return "";
        }
        return linkDef.getName();
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

    public Set<AttributeDef> getAttributeDefs() {
        return linkDef.getAttributeDefs();
    }

    public AttributeDef getSizeAttributeDef() {
        return linkDef.getAttributeDef(SIZE);
    }

    private static final String SIZE = ObjectAttributes.CSI_INTERNAL_SIZE;
    private static final String LABEL = "csi.internal.Label";

    public AttributeDef getLabelAttributeDef() {
        return linkDef.getAttributeDef(LABEL);
    }

    public DirectionDef getDirectionDef() {
        return linkDef.getDirectionDef();
    }

    private static final String TYPE = "csi.internal.Type";
    private static final String COLOR = "csi.internal.Color";
    private static final String OVERRIDE_DEF_TEXT = "override";

    public AttributeDef getTypeAttributeDef() {
        return linkDef.getAttributeDef(TYPE);
    }

    public AttributeDef getColorAttributeDef() {
        return linkDef.getAttributeDef(COLOR);
    }

    public void setName(String name) {
        linkDef.setName(name);

    }

    public void removeTooltips() {
        Set<AttributeDef> attributeDefs = getAttributeDefs();
        List<AttributeDef> tooltipAttributeDefs = getTooltipAttributeDefs();
        attributeDefs.removeAll(tooltipAttributeDefs);

    }

    public void addTooltip(LinkTooltip linkTooltip) {
        Set<AttributeDef> tooltipFields = getAttributeDefs();
        // This function marshall the client side tooltip concept to the server
        // tooltip concept
        // does the new tooltip field have an AttributeDef already
        AttributeDef newTooltipDef = linkTooltip.asAttributeDef();
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

    public void setColor(Color color) {
        checkNotNull(color);
        removeAttribute(COLOR);
        FieldDef fieldDef = new FieldDef(FieldType.STATIC);
        AttributeDef attributeDef = new AttributeDef(COLOR, fieldDef);
        attributeDef.getFieldDef().setStaticText(color.getIntColor() + "");
        // FIXME: Magic String
        attributeDef.getFieldDef().setValueType(CsiDataType.Number);
        linkDef.addAttributeDef(attributeDef);
    }

    private void removeAttribute(String name) {
        Set<AttributeDef> attributeDefs = getAttributeDefs();
        List<AttributeDef> itemsToRemove = Lists.newArrayList();
        for (AttributeDef def : attributeDefs) {
            if (def.getName().equalsIgnoreCase(name)) {
                itemsToRemove.add(def);
            }
        }
        for (AttributeDef attributeDef : itemsToRemove) {
            getAttributeDefs().remove(attributeDef);
        }
    }

    public void setDirectionDef(DirectionDef directionDef) {
        linkDef.setDirectionDef(directionDef);
    }

    public AttributeDef getTransparencyAttributeDef() {
        return linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY);
    }

    public AttributeDef getTransparencyFunctionAttributeDef() {
        return linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TRANSPARENCY_FUNCTION);
    }

    public AttributeDef getSizeFunctionAttributeDef() {
        return linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_SIZE_FUNCTION);
    }

    public String getType() {
        AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_TYPE);
        if(attributeDef != null){
            FieldDef fieldDef = attributeDef.getFieldDef();
            if (FieldType.STATIC.equals(fieldDef.getFieldType())) {
                return fieldDef.getStaticText();
            }
        }
        return "";
    }

    public void apply(GraphSettings graphSettings) {
        
        String linkType = getType();
        if (linkType == null || linkType.isEmpty()) {
            return;
        }
        
        if(graphSettings.getCurrentTheme() == null){
            
        } else {
            LinkStyle linkStyle = graphSettings.getLinkStyle(linkType);
            if (linkStyle != null) {

                if (!isColorOverride()) {
                    setColor(new Color(linkStyle.getColor()));
                }

                if (!isWidthOverride()) {
                    setWidth(linkStyle.getWidth());
                }

            }
            
        }
        
        if(getWidthText() == null){
            setWidth(1.0);
        }
        
    }

    private void setWidth(Double width) {
        checkNotNull(width);
        removeAttribute(ObjectAttributes.CSI_INTERNAL_WIDTH);
        FieldDef fieldDef = new FieldDef(FieldType.STATIC);
        AttributeDef attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_WIDTH, fieldDef);
        attributeDef.getFieldDef().setStaticText(width + "");
        // FIXME: Magic String
        attributeDef.getFieldDef().setValueType(CsiDataType.Number);
        linkDef.addAttributeDef(attributeDef);
    }
    
    private String getWidthText() {
        AttributeDef attributeDef = getSizeAttributeDef();
        if(attributeDef != null && attributeDef.getFieldDef() != null){
            return attributeDef.getFieldDef().getStaticText();
        }
        
        return null;
    }

    public boolean isWidthOverride() {
        AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_WIDTH_OVERRIDE);
        return attributeDef != null;
    }

    public boolean isColorOverride() {
        AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
        return attributeDef != null;
    }
    
    public void setColorOverride(boolean override) {

        if(override){
            AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
            if ((attributeDef == null)) {
                FieldDef fieldDef = new FieldDef(FieldType.STATIC);
                fieldDef.setStaticText(OVERRIDE_DEF_TEXT);
                attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE, fieldDef);
                linkDef.addAttributeDef(attributeDef);
            }
        } else {
            AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_COLOR_OVERRIDE);
            if(attributeDef != null){
                linkDef.removeAttributeDef(attributeDef);
            }
        }
    }
    
    public void setWidthOverride(boolean override) {

        if(override){
            AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_WIDTH_OVERRIDE);
            if ((attributeDef == null)) {
                FieldDef fieldDef = new FieldDef(FieldType.STATIC);
                fieldDef.setStaticText(OVERRIDE_DEF_TEXT);
                attributeDef = new AttributeDef(ObjectAttributes.CSI_INTERNAL_WIDTH_OVERRIDE, fieldDef);
                linkDef.addAttributeDef(attributeDef);
            }
        } else {
            AttributeDef attributeDef = linkDef.getAttributeDef(ObjectAttributes.CSI_INTERNAL_WIDTH_OVERRIDE);
            if(attributeDef != null){
                linkDef.removeAttributeDef(attributeDef);
            }
        }
    }
    
}
