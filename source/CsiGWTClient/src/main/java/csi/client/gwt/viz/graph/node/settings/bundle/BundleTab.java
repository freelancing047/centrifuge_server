package csi.client.gwt.viz.graph.node.settings.bundle;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import csi.client.gwt.viz.graph.node.settings.NodeSettings;
import csi.client.gwt.viz.graph.node.settings.ShowNodeSettings;
import csi.server.common.model.FieldDef;

public class BundleTab {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    @UiField
    FluidRow row;
    @UiField
    FluidContainer container;
    @UiField
    FluidContainer bundleRules;
    @UiField
    Button reorder;
    private ShowNodeSettings presenter;
    private Tab tab;
    private NodeSettings nodeSettings;

    public BundleTab(final NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
        tab = uiBinder.createAndBindUi(this);
    }

    public ShowNodeSettings getPresenter() {
        return presenter;
    }

    public void setPresenter(ShowNodeSettings presenter) {
        this.presenter = presenter;
    }

    public Tab getTab() {
        return tab;
    }

    public void add(NodeBundle bundleDef, FieldDef parentFieldDef) {

        BundleSpecificationView view = new BundleSpecificationView(bundleDef, nodeSettings, parentFieldDef);
        view.setPresenter(presenter.getBundleSpecificationPresenter());
        bundleRules.add(view.asWidget());
    }

    @UiHandler("reorder")
    void reorderClickHandler(ClickEvent event) {
        throw new RuntimeException("Not implemented");
    }

    public void addAll(List<NodeBundle> bundles) {
        NodeBundle previousBundle = null;
        for (NodeBundle bundle : bundles) {
            if (previousBundle == null) {
                addFirstBundle(bundle);
            } else {
                add(bundle, previousBundle.getField());
            }

            previousBundle = bundle;
        }
        addPlaceholderRule(bundles);
    }

    private void addPlaceholderRule(List<NodeBundle> bundles) {
        if (bundles.size() == 0) {
            addInitialPlaceholderRule();
        } else {
            addFinalPlaceholderRule(bundles.get(bundles.size()- 1).getField());
        }
    }

    private void addInitialPlaceholderRule() {
        NodeBundle bundleDef = new NodeBundle();
        add(bundleDef, nodeSettings.getModel().getLabel().getField());
    }

    private void addFinalPlaceholderRule(FieldDef field) {
        NodeBundle bundleDef = new NodeBundle();
        add(bundleDef, field);

    }

    private void addFirstBundle(NodeBundle nodeBundle) {
        add(nodeBundle, nodeSettings.getModel().getLabel().getField());
    }

    public void clearAll() {
        bundleRules.clear();
    }


    interface SpecificUiBinder extends UiBinder<Tab, BundleTab> {
    }
}
