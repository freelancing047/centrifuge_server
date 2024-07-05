package csi.client.gwt.viz.viewer.lens.graph.node;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.lens.shared.ExpandableItem;
import csi.client.gwt.viz.viewer.lens.shared.ListWithMore;
import csi.client.gwt.viz.viewer.lens.shared.MiniViewer;
import csi.client.gwt.viz.viewer.lens.shared.SingleItem;
import csi.server.common.model.visualization.viewer.NodeObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.NodeLensImage;

import java.util.Collections;
import java.util.List;

public class NodeLensImageViewer extends Composite implements LensImageViewer {
    private NodeLensImage lensImage;
    private final MiniViewer miniViewer;
    private NodeObjective objective;
    private String lensDef;

    public NodeLensImageViewer(LensImage lensImage, String lensDef, Objective objective) {

        this.lensDef = lensDef;
        if (lensImage instanceof NodeLensImage) {
            this.lensImage = (NodeLensImage) lensImage;
        }

        miniViewer = new MiniViewer("Node Details");
        initWidget(miniViewer);
        setObjective(objective);

    }

    @Override
    public void setObjective(Objective objective) {
        if (objective instanceof NodeObjective) {
            this.objective = (NodeObjective) objective;
            build();
        }
    }

    public void build() {


        //TODO:Visual Item

        buildLabel();
        buildType();
        buildOccurrence();
        buildSNA();
        buildNeighbors();


    }

    private void buildNeighbors() {
        ExpandableItem neighbors = new ExpandableItem("Neighbors");
        miniViewer.add(neighbors);


        List<String> neighborLabels = Lists.newArrayList(lensImage.getNeighborLabels());
        Collections.sort(neighborLabels);
        ExpandableItem neighborLabelsEI = new ExpandableItem("Labels");
        neighborLabelsEI.add(new ListWithMore(neighborLabels, objective, lensDef, "Type", 50, null, null, 3, null));
        neighbors.add(neighborLabelsEI);

        ExpandableItem neighborTypes = new ExpandableItem("Types");
        neighborTypes.add(new ListWithMore(lensImage.getNeighborTypes(), objective, lensDef, "Type", 50, null, null, 3, null));
        neighbors.add(neighborTypes);

    }

    private void buildSNA() {
        ExpandableItem sna = new ExpandableItem("SNA");
        miniViewer.add(sna);
        SingleItem numNeighbors = new SingleItem("Degree " + lensImage.getNumberOfNeighbors());
        sna.add(numNeighbors);
        SingleItem betweenness = new SingleItem("Betweenness: " + lensImage.getBetweenness());
        sna.add(betweenness);
        SingleItem eigenvector = new SingleItem("Eigenvector " + lensImage.getEigenvector());
        sna.add(eigenvector);
        SingleItem closeness = new SingleItem("Closeness " + lensImage.getCloseness());
        sna.add(closeness);
    }

    private void buildOccurrence() {
        miniViewer.add(new SingleItem("Occurrences: " + lensImage.getOccurrences()));


    }

    private void buildType() {
        ExpandableItem type = new ExpandableItem("Type");
        type.add(new ListWithMore(lensImage.getTypes(), objective, lensDef, "Type", 50, null, null, 3, null));
        miniViewer.add(type);
    }

    private void buildLabel() {
        ExpandableItem label = new ExpandableItem("Label");
        label.add(new ListWithMore(lensImage.getLabels(),objective, lensDef, "Type", 50, null, null, 3, null));
        miniViewer.add(label);

    }

    @Override
    public boolean handles(Objective objective) {
        return objective instanceof NodeObjective;
    }

}
