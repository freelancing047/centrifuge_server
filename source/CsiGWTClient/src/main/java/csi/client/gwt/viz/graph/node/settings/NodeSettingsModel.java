package csi.client.gwt.viz.graph.node.settings;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.viz.graph.GraphImpl;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeIcon;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeLabel;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeShape;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeSize;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeTransparency;
import csi.client.gwt.viz.graph.node.settings.bundle.NodeBundle;
import csi.client.gwt.viz.graph.node.settings.tooltip.NodeTooltip;
import csi.client.gwt.viz.graph.node.settings.type.NodeType;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.shared.core.color.ClientColorHelper;

public class NodeSettingsModel {

    private final BundleProxy bundleProxy;
    private boolean hideEmptyTooltipValues;
    private boolean hideLabels;
    private NodeIcon icon;
    private String Name;
    private NodeProxy nodeProxy;
    private NodeSettings nodeSettings;
    private NodeLabel label;
    private boolean themeMatch;
    private NodeSize scale;
    private NodeType type;
    private NodeShape shape;
    private List<NodeTooltip> nodeTooltips = Lists.newArrayList();
    private NodeTransparency transparency;
    private NodeIdentity identity;

    public NodeSettingsModel(NodeSettings nodeSettings, NodeProxy nodeProxy) {
        this.nodeSettings = nodeSettings;
        this.nodeProxy = nodeProxy;
        // begin deep copy
        // Name
        setName(nodeProxy.getName());
        // Size
        setScale(new NodeSize(nodeProxy));
        // Transparency
        setTransparency(new NodeTransparency(nodeProxy));
        // Shape
        setShape(new NodeShape(nodeProxy));
        // Label
        setLabel(new NodeLabel(nodeProxy));
        // Icon
        setIcon(new NodeIcon(nodeProxy, nodeSettings));
        // Bundling
        List<BundleDef> bundleDefs = nodeSettings.getGraphSettings().getBundleDefs();
        if (bundleDefs.isEmpty()) {
            bundleDefs.add(new BundleDef());
        }
        BundleDef bundleDef = bundleDefs.get(0);
        bundleProxy = new BundleProxy(bundleDef, nodeProxy);

        // Tooltip

        List<AttributeDef> tooltipAttributeDefs = nodeProxy.getTooltipAttributeDefs();
        for (AttributeDef tooltipAttributeDef : tooltipAttributeDefs) {
            nodeTooltips.add(new NodeTooltip(tooltipAttributeDef));
        }

        setHideEmptyTooltipValues(nodeProxy.hideEmptyTooltips());
        // Other
        setHideLabels(nodeProxy.getNodeDef().getHideLabels());
        setType(new NodeType(nodeProxy));
        setIdentity(new NodeIdentity(nodeProxy));
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public NodeLabel getLabel() {
        return label;
    }

    public void setLabel(NodeLabel label) {
        this.label = label;
    }

    public void setScale(NodeSize size) {
        this.scale = size;
    }

    public NodeIcon getIcon() {
        return icon;
    }

    public Image getImage() {
        return nodeProxy.getRenderedIcon(nodeSettings.getTheme());
    }

    public String getName() {
        return Name;
    }

    public NodeSize getSize() {
        return scale;
    }

    public NodeShape getShape() {
        return shape;
    }

    public boolean isHideEmptyTooltipValues() {
        return hideEmptyTooltipValues;
    }

    public boolean isHideLabels() {
        return hideLabels;
    }

    public boolean isThemeMatch() {
        return themeMatch;
    }

    public void setHideEmptyTooltipValues(boolean hideEmptyTooltipValues) {
        this.hideEmptyTooltipValues = hideEmptyTooltipValues;
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels = hideLabels;
    }

    public void setIcon(NodeIcon icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setShape(NodeShape shape) {
        this.shape = shape;
    }

    public void setThemeMatch(boolean themeMatch) {
        this.themeMatch = themeMatch;
    }

    public Image getRenderedNode() {
        String iconURI = "";
        ShapeType shape = ShapeType.NONE;
        int color = 0;
        int size = 40;// FIXME
        Double iconScale = null;
        NodeStyle style = null;
        if(getShape() != null){
            shape = getShape().getType();
            color = getShape().getColor().getIntColor();
        }

        GraphTheme theme = nodeSettings.getTheme();
        if(theme != null && getType() != null && getType().getText() != null){
            style = theme.findNodeStyle(getType().getText());
        }
        
        if(getShape() == null){
            setShape(new NodeShape(nodeProxy));
        }
        if(style != null){
            if(!getShape().isEnabled()){
                getShape().setType(style.getShape());
            }
            
            if(getShape().isColorEnabled()){
                color = getShape().getColor().getIntColor();
            } else {
                getShape().setColor(ClientColorHelper.get().make(style.getColor()));
                color = style.getColor();
            }

            shape = getShape().getType();
            iconScale = style.getIconScale();
        }
        
        if(getIcon().isEnabled()){
            iconURI = getIcon().getName();
        } else {
            if(style != null){
                iconURI = style.getIconId();
                iconScale = style.getIconScale();
            }
        }

        if(iconScale ==  null){
            iconScale = 1.0;
        }
        //iconScale = nodeSettings.getTheme().getIconScale();
        return GraphImpl.getRenderedIcon(iconURI, shape, color, size, iconScale);
    }

    public boolean hasLabel() {
        return !getLabel().isHidden();
    }

    public NodeType getType() {
        return type;
    }

    public void save() {// Should this move to presenter?
        // Name
        nodeProxy.setName(getName());
        // Size
        scale.persist(nodeProxy);
        // Transparency
        transparency.persist(nodeProxy);
        // Label
        label.persist(nodeProxy);
        //Type
        type.persist(nodeProxy);
        //Identity
        identity.persist(nodeProxy);
        nodeProxy.setHideLabels(hideLabels);
        // Shape
        if (getShape().isEnabled()) {
            nodeProxy.setShapeOverride(true);
            nodeProxy.setShape(getShape().getType());
        } else {
            nodeProxy.setShapeOverride(false);
        }
        
        if(getShape().isColorEnabled()){
            nodeProxy.setColorOverride(true);
            nodeProxy.setColor(getShape().getColor().getIntColor() + "");
        } else {
            nodeProxy.setColorOverride(false);
        }
        // Icon
        icon.persist(nodeProxy);
        // Bundle
        BundleDef bundleDef = nodeSettings.getGraphSettings().getBundleDefs().get(0);
        bundleProxy.persist(bundleDef);

        // Tooltips

        // We first need to remove all of them, because it is easier than tracking the deletes.
        // We don't delete synchronously to avoid shifting the problem to roll back logic on cancel
        nodeProxy.removeTooltips();
        // FIXME: I would rather not get info from the view...
        List<NodeTooltip> tooltips = nodeSettings.getView().getTooltipTab().getTooltips();
        int i = 0;
        for (NodeTooltip nodeTooltip : tooltips) {
            nodeTooltip.setOrder(i++);
            nodeProxy.addTooltip(nodeTooltip);
        }
    }

    public List<NodeTooltip> getTooltips() {
        return nodeTooltips;
    }

    public List<NodeBundle> getBundles() {
        return bundleProxy.getBundles();
    }

    public NodeProxy getNodeProxy() {
        return nodeProxy;
    }

    public void setTransparency(NodeTransparency transparency) {
        this.transparency = transparency;
    }

    public NodeTransparency getTransparency() {
        return transparency;
    }

    public NodeIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(NodeIdentity identity) {
        this.identity = identity;
    }
}
