package csi.client.gwt.viz.viewer.lens.graph.node;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.lens.shared.ExpandableItem;
import csi.client.gwt.viz.viewer.lens.shared.ListWithMore;
import csi.client.gwt.viz.viewer.lens.shared.MiniViewer;
import csi.server.common.model.visualization.viewer.NodeObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.NodeNeighborLabelLensImage;

import java.util.List;

public class NodeNeighborLabelImageViewer extends Composite implements LensImageViewer {
    private NodeNeighborLabelLensImage lensImage;
    private NodeObjective objective;
    private MiniViewer miniViewer;

    public NodeNeighborLabelImageViewer(LensImage lensImage, String lensDef, Objective objective) {
        if (lensImage instanceof NodeNeighborLabelLensImage) {
            this.lensImage = (NodeNeighborLabelLensImage) lensImage;
        }
        if (objective instanceof NodeObjective) {
            this.objective = (NodeObjective) objective;
        }

        miniViewer = new MiniViewer("Testing");
        initWidget(miniViewer);
        setObjective(objective);
    }

    @Override
    public void setObjective(Objective objective) {
            build();
    }

    private void build() {
        String neighbor_labels = lensImage.getLabel();
        ExpandableItem neighbors = new ExpandableItem(neighbor_labels);
        miniViewer.add(neighbors);

        List<String> nl = lensImage.getNeighborLabels();
        List<String> neighborLabels = Lists.newArrayList();
        neighborLabels.addAll(nl);
        neighbors.add(new ListWithMore(neighborLabels, objective, lensImage.getLensDef(), "Label", 10, lensImage.getNeighborLabelsCounts(), lensImage.getNeighborLabelsOccurrances(), lensImage.getDistinctCount(), neighbor_labels));
    }

    @Override
    public boolean handles(Objective objective) {
        return objective instanceof NodeObjective;
    }

    @Override
    public void setVisible(boolean visible) {

    }

}
