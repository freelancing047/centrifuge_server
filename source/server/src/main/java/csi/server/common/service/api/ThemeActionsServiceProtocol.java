package csi.server.common.service.api;

import java.util.List;

import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.VisualizationType;
import csi.shared.gwt.vortex.VortexService;

public interface ThemeActionsServiceProtocol extends VortexService {
        
    public List<List<ResourceBasics>> getThemeOverWriteControlLists() throws CentrifugeException;

    public void deleteTheme(String uuid) throws CentrifugeException;

    public void saveTheme(Theme graphTheme) throws CentrifugeException;

    public List<ResourceBasics> listThemes() throws CentrifugeException;

    public Theme findTheme(String uuid) throws CentrifugeException;

    public List<ResourceBasics> listThemesByType(VisualizationType type) throws CentrifugeException;

    public MapTheme findMapTheme(String uuid);

    public GraphTheme findGraphTheme(String uuid);

    public void deleteThemes(List<String> myItemList) throws CentrifugeException;

}
