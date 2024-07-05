package csi.server.common.service.api;

import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.dto.CustomPagingResultBean;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;
import csi.shared.gwt.vortex.VortexService;

import java.util.List;
import java.util.Map;

public interface ViewerActionServiceProtocol extends VortexService {
    LensImage getLensImage(LensDef lensDef, Objective objective);
    List<LensImage> getLensImage(String dvuuid, Objective objective);

    ViewerGridConfig getGridConfig(Objective objective, String lensDef, String dvuuid);

    CustomPagingResultBean<List<?>> getGridData(Objective s, String lensDef, String token, PagingLoadConfig loadConfig, String dvuuid);

    Map<String,List<LensDefSettings>> getAvailableLenses();

    List<LensDefSettings> getLensConfiguration(String dvuuid);

    void updateSettings(List<LensDefSettings> lensSettingsControls, String dvuuid);

    String exportMoreGrid(Objective objective, String lensDef, PagingLoadConfig loadConfig, String dvuuid);

    String exportMoreGrid(List<NodeListDTO> selItems);
}
