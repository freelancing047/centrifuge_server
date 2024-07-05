package csi.client.gwt.viz.graph.node.settings.bundle;

import java.util.List;

import csi.client.gwt.viz.graph.node.settings.NodeSettings;

public class BundleSpecificationPresenterImpl implements BundleSpecificationPresenter {
    private NodeSettings nodeSettings;

    public BundleSpecificationPresenterImpl(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
    }

    @Override
    public void remove(NodeBundle nodeBundle) {
        nodeSettings.getModel().getBundles().remove(nodeBundle);
        nodeSettings.getView().setBundles(nodeSettings.getModel().getBundles());
    }

    @Override
    public void fieldChange(NodeBundle nodeBundle) {
        List<NodeBundle> bundles = nodeSettings.getModel().getBundles();
        bundles.remove(nodeBundle);
        bundles.add(nodeBundle);
        nodeSettings.getView().setBundles(bundles);
    }
}
