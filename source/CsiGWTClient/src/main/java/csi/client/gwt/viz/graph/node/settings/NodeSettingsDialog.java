package csi.client.gwt.viz.graph.node.settings;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.graph.node.settings.appearance.AppearanceTab;
import csi.client.gwt.viz.graph.node.settings.bundle.BundleTab;
import csi.client.gwt.viz.graph.node.settings.bundle.NodeBundle;
import csi.client.gwt.viz.graph.node.settings.tooltip.NodeTooltip;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipTab;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.SizeProvidingModal;

public class NodeSettingsDialog {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    SizeProvidingModal settingsModal;
    @UiField
    CsiTabPanel tabPanel;
    private AppearanceTab appearanceTab;
    private ShowNodeSettings presenter;
    private BundleTab bundleTab;
    private TooltipTab tooltipTab;

    public NodeSettingsDialog(NodeSettings nodeSettings) {
        uiBinder.createAndBindUi(this);
        appearanceTab = new AppearanceTab(nodeSettings);
        bundleTab = new BundleTab(nodeSettings);
        tooltipTab = new TooltipTab(nodeSettings);
        tabPanel.add(appearanceTab.getTab());
        tabPanel.add(bundleTab.getTab());
        tabPanel.add(tooltipTab.getTab());
    }

    public void setBundles(List<NodeBundle> bundles) {
        bundleTab.clearAll();
        bundleTab.addAll(bundles);
    }

    public void addBundle(NodeBundle bundle) {
        bundleTab.add(bundle, null);
    }

    public void addTooltip(NodeTooltip tooltip) {
        tooltipTab.add(tooltip);
    }

    public void close() {
        settingsModal.hide();
    }

    public AppearanceTab getAppearanceTab() {
        return appearanceTab;
    }

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

    public void setPresenter(ShowNodeSettings presenter) {
        this.presenter = presenter;
        appearanceTab.setPresenter(presenter);
        bundleTab.setPresenter(presenter);
    }

    public void show() {
        RootPanel.get().add(settingsModal);
        settingsModal.show();
    }

    interface MyUiBinder extends UiBinder<Widget, NodeSettingsDialog> {
    }
}