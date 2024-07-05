package csi.client.gwt.viz.map.settings;

import csi.client.gwt.WebMain;
import csi.server.common.service.api.MapActionsServiceProtocol;
import csi.shared.core.visualization.map.MapConfigDTO;

import java.util.List;

public class MapConfigProxy {
    private static MapConfigProxy instance;
    private MapConfigDTO mapConfigDTO;

    public MapConfigProxy(MapConfigDTO mapConfigDTO) {
        this.mapConfigDTO = mapConfigDTO;
    }

    public static void initialize() {
        WebMain.injector.getVortex().execute((MapConfigDTO result) -> instance = new MapConfigProxy(result), MapActionsServiceProtocol.class).getMapConfig();
    }

    public static MapConfigProxy instance() {
        return instance;
    }

    public List<String> getBoundaryLayerIds() {
        return mapConfigDTO.getBoundaryLayerIds();
    }

    public String getDefaultBasemapOwner() {
        return mapConfigDTO.getDefaultBasemapOwner();
    }

    public String getDefaultBasemapId() {
        return mapConfigDTO.getDefaultBasemapId();
    }

    public int getMinPlaceSize() {
        return mapConfigDTO.getMinPlaceSize();
    }

    public int getMaxPlaceSize() {
        return mapConfigDTO.getMaxPlaceSize();
    }

    public String getDefaultThemeName() {
        return mapConfigDTO.getDefaultThemeName();
    }

    public int getPointLimit() {
        return mapConfigDTO.getPointLimit();
    }

    public int getTypeLimit() {
        return mapConfigDTO.getTypeLimit();
    }

    public int getLinkLimit() {
        return mapConfigDTO.getLinkLimit();
    }

    public String getLocatorUrl() {
        return mapConfigDTO.getLocatorUrl();
    }
}
