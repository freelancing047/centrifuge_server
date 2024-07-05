package csi.client.gwt.viz.viewer.lens;

import com.google.gwt.user.client.ui.IsWidget;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

public interface LensImageViewer extends IsWidget{
    void setObjective(Objective objective);

    boolean handles(Objective objective);


    void setVisible(boolean visible);

}
