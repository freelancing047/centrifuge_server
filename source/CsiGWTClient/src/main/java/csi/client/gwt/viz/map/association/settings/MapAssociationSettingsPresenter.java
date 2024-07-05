package csi.client.gwt.viz.map.association.settings;

import csi.client.gwt.viz.map.place.settings.CurrentNameListener;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapAssociation;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;

import java.util.ArrayList;
import java.util.List;

public class MapAssociationSettingsPresenter {
    private MapSettingsPresenter mapSettingsPresenter;
    private MapAssociation mapAssociation;
    private String mapAssociationUuid;
    private List<CurrentNameListener> currentNameListeners = new ArrayList<>();

    void setMapSettingsPresenter(MapSettingsPresenter mapSettingsPresenter) {
        this.mapSettingsPresenter = mapSettingsPresenter;
    }

    public MapSettings getMapSettings() {
        return mapSettingsPresenter.getVisualizationDef().getMapSettings();
    }

    List<MapPlace> getMapPlaces() {
        return mapSettingsPresenter.getMapPlaces();
    }

    List<MapAssociation> getMapAssociations() {
        return getMapSettings().getMapAssociations();
    }

    MapAssociation getMapAssociation() {
        return mapAssociation;
    }

    void setMapAssociation(MapAssociation mapAssociation) {
        this.mapAssociation = mapAssociation;
        mapAssociationUuid = mapAssociation.getUuid();
    }

    String getMapAssociationUuid() {
        return mapAssociationUuid;
    }

    void removeMapAssociation() {
        mapSettingsPresenter.removeAssociation(mapAssociation);
        mapAssociation = null;
    }

    void saveMapAssociation() {
        mapSettingsPresenter.saveMapAssociation(mapAssociation);
        mapAssociation = null;
    }

    void cancelMapAssociation() {
        mapSettingsPresenter.cancelMapAssociationDefinition();
        mapAssociation = null;
    }

    AssociationStyle getAssociationStyle() {
        return getAssociationStyle(mapAssociation.getName());
    }

    AssociationStyle getAssociationStyle(String associationName) {
        MapTheme mapTheme = mapSettingsPresenter.getTheme();
        if (mapTheme != null) {
            return mapTheme.getAssociationStyleMap().get(associationName);
        } else {
            return null;
        }
    }

    void registerCurrentNameListener(CurrentNameListener listener) {
        currentNameListeners.add(listener);
    }

    void setCurrentAssociationName(String currentAssociationName) {
        for (CurrentNameListener listener : currentNameListeners) {
            listener.notify(currentAssociationName);
        }
    }
}
