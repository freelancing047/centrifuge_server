package csi.client.gwt.viz.graph.link.settings;

import java.util.List;

import com.google.common.collect.Lists;

import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.server.common.model.attribute.AttributeDef;

public class LinkSettingsModel {

    private String name;
    private LinkSettings linkSettings;
    // TODO:Make private
    LinkProxy linkProxy;
    private LinkSize linkSize;
    private LinkLabel linkLabel;
    private LinkType linkType;
    private List<LinkTooltip> linkTooltips = Lists.newArrayList();
    private LinkDirectionDef linkDirection;
    private LinkColor linkColor;
    private LinkTransparency transparency;

    public LinkSettingsModel(LinkSettings linkSettings, LinkProxy linkProxy) {
        this.linkSettings = linkSettings;
        this.linkProxy = linkProxy;
        // deep defensive copy
        // name
        setName(linkProxy.getName());
        // scale(contains override)
        setLinkSize(new LinkSize(linkProxy));
        setTransparency(new LinkTransparency(linkProxy));
        // Color(contains override)
        setLinkColor(new LinkColor(linkProxy));
        // label
        setLinkLabel(new LinkLabel(linkProxy));
        // type
        setLinkType(new LinkType(linkProxy));
        // Direction
        setLinkDirection(new LinkDirectionDef(linkProxy));
        

        // tooltips
        List<AttributeDef> tooltipAttributeDefs = linkProxy.getTooltipAttributeDefs();
        for (AttributeDef tooltipAttributeDef : tooltipAttributeDefs) {
            linkTooltips.add(new LinkTooltip(tooltipAttributeDef));
        }
    }

    public LinkColor getLinkColor() {
        return linkColor;
    }

    public LinkDirectionDef getLinkDirection() {
        return linkDirection;
    }

    public LinkLabel getLinkLabel() {
        return linkLabel;
    }

    public LinkSize getLinkSize() {
        return linkSize;
    }

    public LinkSettings getLinkSettings() {
        return linkSettings;
    }

    public List<LinkTooltip> getLinkTooltips() {
        return linkTooltips;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public String getName() {
        return name;
    }

    public LinkType getType() {
        return linkType;
    }

    public void save() {
        linkProxy.setName(getName());

        // Label
        linkLabel.persist(linkProxy);
        linkColor.persist(linkProxy);
        linkDirection.persist(linkProxy);
        linkSize.persist(linkProxy);
        transparency.persist(linkProxy);
        linkType.persist(linkProxy);
        // Tooltips

        // We first need to remove all of them, because it is easier than tracking the deletes.
        // We don't delete synchronously to avoid shifting the problem to roll back logic on cancel
        linkProxy.removeTooltips();
        // FIXME: I would rather not get info from the view...
        List<LinkTooltip> tooltips = linkSettings.getView().getTooltipTab().getTooltips();
        int i = 0;
        for (LinkTooltip linkTooltip : tooltips) {
            linkTooltip.setOrder(i++);
            linkProxy.addTooltip(linkTooltip);
        }

    }

    private void setLinkColor(LinkColor linkColor) {
        this.linkColor = linkColor;
    }

    public void setLinkDirection(LinkDirectionDef linkDirection) {
        this.linkDirection = linkDirection;
    }

    public void setLinkLabel(LinkLabel linkLabel) {
        this.linkLabel = linkLabel;
    }

    public void setLinkSize(LinkSize linkSize) {
        this.linkSize = linkSize;
    }

    public void setLinkSettings(LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
    }

    public void setLinkTooltips(List<LinkTooltip> linkTooltips) {
        this.linkTooltips = linkTooltips;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkTransparency getTransparency() {
        return transparency;
    }

    public void setTransparency(LinkTransparency transparency) {
        this.transparency = transparency;
    }

}
