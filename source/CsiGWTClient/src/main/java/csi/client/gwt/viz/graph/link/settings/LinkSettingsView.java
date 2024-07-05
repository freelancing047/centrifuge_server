package csi.client.gwt.viz.graph.link.settings;

import java.util.List;

public interface LinkSettingsView {

    void bind(LinkSettingsPresenter presenter);

    void show();

    void setName(String name);

    void close();

    void setSize(LinkSize linkSize);

    void setLabel(LinkLabel linkLabel);

    void setDirection(LinkDirectionDef linkDirection);

    void setTooltips(List<LinkTooltip> linkTooltips);

    void setType(LinkType type);

    void setColor(LinkColor color);

    TooltipTab getTooltipTab();

    LinkDirectionTab getDirectionTab();

    void setTransparency(LinkTransparency transparency);

    LinkAppearanceTab getAppearanceTab();
}
