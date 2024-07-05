package csi.client.gwt.viz.viewer.lens;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.FieldLensImage;
import csi.shared.gwt.viz.viewer.LensImage.ImageLensImage;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

public class ImageLensImageViewer implements LensImageViewer {

    private final FluidContainer fc;

    public ImageLensImageViewer(LensImage lensImage) {
        fc = new FluidContainer();
        if (lensImage instanceof ImageLensImage) {
            ImageLensImage lensImage1 = (ImageLensImage) lensImage;
            Image i = new Image();
            i.setUrl(lensImage1.getValue());
            fc.add(i);
        }
    }

    @Override
    public void setObjective(Objective objective) {
    }

    @Override
    public boolean handles(Objective objective) {
        return true;
    }

    @Override
    public void setVisible(boolean visible) {

    }

    @Override
    public Widget asWidget() {
        return fc;
    }
}
