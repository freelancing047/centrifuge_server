package csi.client.gwt.viz.viewer.lens;

import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.viz.viewer.lens.field.FieldLensImageViewer;
import csi.client.gwt.viz.viewer.lens.graph.node.LinkLensImageViewer;
import csi.client.gwt.viz.viewer.lens.graph.node.NodeLensImageViewer;
import csi.client.gwt.viz.viewer.lens.graph.node.NodeNeighborLabelImageViewer;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.ImageLensImage;
import csi.shared.gwt.viz.viewer.LensImage.FieldLensImage;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.LensImage.LinkLensImage;
import csi.shared.gwt.viz.viewer.LensImage.NodeLensImage;
import csi.shared.gwt.viz.viewer.LensImage.NodeNeighborLabelLensImage;

public class LensImageViewerLocator {
    public static LensImageViewer getLensImageViewer(LensImage lensImage, Objective objective, Viewer viewer) {
        String lensDef = lensImage.getLensDef();
        if(lensImage instanceof FieldLensImage) {
            return new FieldLensImageViewer((FieldLensImage) lensImage, lensDef, objective,viewer);
        }
        if(lensImage instanceof NodeLensImage){
            return new NodeLensImageViewer(lensImage, lensDef, objective);

        }
        if(lensImage instanceof NodeNeighborLabelLensImage){
            return new NodeNeighborLabelImageViewer(lensImage, lensDef, objective);

        }
        if(lensImage instanceof LinkLensImage){
            return new LinkLensImageViewer(lensImage, lensDef, objective);

        }
        if(lensImage instanceof ImageLensImage){
            return new ImageLensImageViewer(lensImage);

        }
        return null;
    }
}
