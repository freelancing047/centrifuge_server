package csi.client.gwt.viz.viewer.lens.graph.node;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.lens.shared.ExpandableItem;
import csi.client.gwt.viz.viewer.lens.shared.ListWithMore;
import csi.client.gwt.viz.viewer.lens.shared.MiniViewer;
import csi.client.gwt.viz.viewer.lens.shared.SingleItem;
import csi.server.common.model.visualization.viewer.LinkObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.LinkLensImage;

public class LinkLensImageViewer extends Composite implements LensImageViewer {
    private LinkLensImage lensImage;
    private final MiniViewer miniViewer;
    private LinkObjective objective;
    private String lensDef;

    public LinkLensImageViewer(LensImage lensImage, String lensDef, Objective objective) {

        this.lensDef = lensDef;
        if (lensImage instanceof LinkLensImage) {
            this.lensImage = (LinkLensImage) lensImage;
        }

        miniViewer = new MiniViewer("Link Details");
        initWidget(miniViewer);
        setObjective(objective);

    }

    @Override
    public void setObjective(Objective objective) {
        if (objective instanceof LinkObjective) {
            this.objective = (LinkObjective) objective;
            build();
        }
    }

    @Override
    public boolean handles(Objective objective) {
        return objective instanceof LinkObjective;
    }

    public void build() {


        //TODO:Visual Item

        buildLabel();
        buildType();
        buildOccurrence();


    }

    private void buildOccurrence() {
        miniViewer.add(new SingleItem("Occurrence: " + lensImage.getCount()));
    }

    private void buildType() {
        ExpandableItem types= new ExpandableItem("Types");
        types.add(new ListWithMore(lensImage.getTypes(), objective, lensDef, LinkLensImage.LinkLensTokens.TYPE.toString(), 50, null, null, 3, null));
        miniViewer.add(types);
    }

    private void buildLabel() {
        if (Strings.isNullOrEmpty(lensImage.getLabel())) {
            miniViewer.add(new SingleItem("Label: " + lensImage.getLabel()));
        }
    }
}
