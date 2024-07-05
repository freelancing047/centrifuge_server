package csi.client.gwt.viz.graph.node.settings.bundle;

import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.graph.BundleOp;

public class NodeBundle {

    private BundleOp bundleOp;

    public NodeBundle() {
        bundleOp = new BundleOp();
    }

    public NodeBundle(BundleOp bundleOp) {
        this.bundleOp = bundleOp;

    }

    public String getFieldName() {
        if (bundleOp != null) {
            if (bundleOp.getField() != null) {
                return bundleOp.getField().getFieldName();
            }
        }
        return "";
    }

    public String getKey() {
        return bundleOp.getUuid();
    }

    public FieldDef getField() {
        return bundleOp.getField();
    }

    public void setField(FieldDef field){
        bundleOp.setField(field);
    }

    public BundleOp getBundleOp() {
        return bundleOp;
    }
}
