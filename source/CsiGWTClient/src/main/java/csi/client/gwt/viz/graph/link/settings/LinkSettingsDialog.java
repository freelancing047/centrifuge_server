package csi.client.gwt.viz.graph.link.settings;

import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.SizeProvidingModal;

class LinkSettingsDialog implements LinkSettingsView {

    interface MyUiBinder extends UiBinder<Widget, LinkSettingsDialog> {
    }

    @UiField
    SizeProvidingModal settingsModal;
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    private LinkSettings linkSettings;
    private LinkSettingsPresenter presenter;
    private LinkAppearanceTab appearanceTab;
    private LinkDirectionTab directionTab;
    @UiField
    CsiTabPanel tabPanel;
    private TooltipTab tooltipTab;

    public LinkSettingsDialog(LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
        uiBinder.createAndBindUi(this);
        appearanceTab = new LinkAppearanceTab(linkSettings);
        tooltipTab = new TooltipTab(linkSettings);
        tabPanel.add(appearanceTab.getTab());
        directionTab = new LinkDirectionTab(linkSettings);
        tabPanel.add(directionTab.getTab());
        tabPanel.add(tooltipTab.getTab());
    }

    public void bind(LinkSettingsPresenter presenter) {
        this.presenter = presenter;
        appearanceTab.bind(presenter);
        directionTab.bind(presenter);
    }

    @Override
    public void close() {
        settingsModal.hide();
    }

    @Override
    public TooltipTab getTooltipTab() {
        return tooltipTab;
    }

    @UiHandler("buttonCancel")
    public void onCancelClicked(ClickEvent clickEvent) {
        presenter.cancel();
    }

    @UiHandler("buttonDelete")
    public void onDeleteClicked(ClickEvent clickEvent) {
        presenter.delete();
    }

    @UiHandler("buttonSave")
    public void onSaveClicked(ClickEvent clickEvent) {
        presenter.save();
    }

    @Override
    public void setColor(LinkColor color) {
        appearanceTab.setColor(color);
    }

    @Override
    public void setDirection(LinkDirectionDef linkDirection) {
        directionTab.setDirection(linkDirection);
    }

    @Override
    public void setLabel(LinkLabel linkLabel) {
        appearanceTab.setLabel(linkLabel);

    }

    @Override
    public void setName(String name) {
        appearanceTab.setName(name);

    }

    @Override
    public void setSize(LinkSize linkSize) {
        appearanceTab.setSize(linkSize);
    }

    @Override
    public void setTooltips(List<LinkTooltip> linkTooltips) {
        for (LinkTooltip linkTooltip : linkTooltips) {
            tooltipTab.add(linkTooltip);
        }
    }

    @Override
    public void setType(LinkType type) {
        appearanceTab.setType(type);
    }

    @Override
    public void show() {
        RootPanel.get().add(settingsModal);
        settingsModal.show();
    }

    @Override
    public LinkDirectionTab getDirectionTab() {
        return directionTab;
    }

    @Override
    public void setTransparency(LinkTransparency transparency) {
        appearanceTab.setTransparency(transparency);
    }
    
    @Override
    public LinkAppearanceTab getAppearanceTab() {
        return appearanceTab;
    }

    

}
