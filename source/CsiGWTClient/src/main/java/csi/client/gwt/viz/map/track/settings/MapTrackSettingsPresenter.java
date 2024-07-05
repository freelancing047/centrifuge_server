package csi.client.gwt.viz.map.track.settings;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTrack;

import java.util.List;

public class MapTrackSettingsPresenter {
    private MapSettingsPresenter mapSettingsPresenter;
    private MapTrack mapTrack;

    void setMapSettingsPresenter(MapSettingsPresenter mapSettingsPresenter) {
        this.mapSettingsPresenter = mapSettingsPresenter;
    }

    public MapSettings getMapSettings() {
        return mapSettingsPresenter.getVisualizationDef().getMapSettings();
    }

    List<MapTrack> getMapTracks() {
        return getMapSettings().getMapTracks();
    }

    MapTrack getMapTrack() {
        return mapTrack;
    }

    void setMapTrack(MapTrack mapTrack) {
        this.mapTrack = mapTrack;
    }

    void removeMapTrack() {
        mapSettingsPresenter.removeTrack(mapTrack);
        mapTrack = null;
    }

    void saveMapTrack() {
        mapSettingsPresenter.saveMapTrack(mapTrack);
        mapTrack = null;
    }

    void cancelMapTrack() {
        mapSettingsPresenter.cancelMapTrackDefinition();
        mapTrack = null;
    }

    private DataModelDef getDataModel() {
        return mapSettingsPresenter.getDataViewDef().getModelDef();
    }

    public List<FieldDef> getFieldDefs() {
        return FieldDefUtils.getAllSortedFields(getDataModel(), FieldDefUtils.SortOrder.ALPHABETIC);
    }

}
