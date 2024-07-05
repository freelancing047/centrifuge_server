package csi.client.gwt.viz.graph.node.settings;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.node.settings.bundle.NodeBundle;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;

public class BundleProxy {
    private final List<NodeBundle> bundles = Lists.newArrayList();
    private NodeProxy nodeProxy;


    public BundleProxy(BundleDef bundleDef, NodeProxy nodeProxy) {
        this.nodeProxy = nodeProxy;
        List<BundleOp> operations = bundleDef.getOperations();
        for (BundleOp bundleOp : operations) {
            if (bundleOp.getNodeDef() != null) {
                if (bundleOp.getNodeDef().getUuid().equals(nodeProxy.getUuid())) {
                    bundles.add(new NodeBundle(bundleOp));
                }
            }

        }
    }

    public List<NodeBundle> getBundles() {
        return bundles;
    }

    public void persist(BundleDef bundleDefs) {
        List<BundleOp> operations = bundleDefs.getOperations();
        ArrayList<BundleOp> copy = Lists.newArrayList(operations);
        for (BundleOp bundleOp : copy) {
            if (bundleOp.getNodeDef().getUuid().equals(nodeProxy.getUuid())) {
                operations.remove(bundleOp);
            }
        }
        int priority = bundles.size();

        for (NodeBundle bundle : bundles) {
            BundleOp bundleOp = bundle.getBundleOp();
            bundleOp.setNodeDef(nodeProxy.getNodeDef());
            bundleOp.setPriority(priority--);
            operations.add(bundleOp);
        }
    }
}
