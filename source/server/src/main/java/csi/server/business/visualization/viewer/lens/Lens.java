package csi.server.business.visualization.viewer.lens;

import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

import java.util.List;

public interface Lens {
    LensImage focus(LensDef lensDef, Objective objective);

    List<List<?>> focus(LensDef lensDef, Objective objective, String token);

    ViewerGridConfig getGridConfig();
}
