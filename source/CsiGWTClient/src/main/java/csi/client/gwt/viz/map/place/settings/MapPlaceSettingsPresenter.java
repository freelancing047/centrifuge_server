package csi.client.gwt.viz.map.place.settings;

import java.util.ArrayList;
import java.util.List;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.map.settings.MapSettingsPresenter;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;

public class MapPlaceSettingsPresenter {
	private MapSettingsPresenter mapSettingsPresenter;
	private MapPlace mapPlace;
	private List<CurrentNameListener> currentNameListeners = new ArrayList<CurrentNameListener>();

	public void setMapSettingsPresenter(MapSettingsPresenter mapSettingsPresenter) {
		this.mapSettingsPresenter = mapSettingsPresenter;
	}
	
	public MapSettings getMapSettings() {
		return mapSettingsPresenter.getVisualizationDef().getMapSettings();
	}
	
	public DataModelDef getDataModel() {
		return mapSettingsPresenter.getDataViewDef().getModelDef();
	}
	
	public List<FieldDef> getFieldDefs() {
		List<FieldDef> fieldDefs = FieldDefUtils.getAllSortedFields(getDataModel(), FieldDefUtils.SortOrder.ALPHABETIC);
		return fieldDefs;
	}

	public void setMapPlace(MapPlace mapPlace) {
		this.mapPlace = mapPlace;
	}
	
	public MapPlace getMapPlace() {
		return mapPlace;
	}
	
	public void removePlace() {
		mapSettingsPresenter.removePlace(mapPlace);
		mapPlace = null;
	}
	
	public void savePlace() {
		mapSettingsPresenter.saveMapPlace(mapPlace);
		mapPlace = null;
	}
	
	public void cancelMapPlaceDefinition() {
		mapSettingsPresenter.cancelMapPlaceDefinition();
		mapPlace = null;
	}

	public void registerCurrentNameListener(CurrentNameListener listener) {
		currentNameListeners.add(listener);
	}
	
	public void setCurrentPlaceName(String currentPlaceName) {
		for (CurrentNameListener listener : currentNameListeners) {
			listener.notify(currentPlaceName);
		}
	}
	
	public PlaceStyle getPlaceStyle() {
		String typeName = mapPlace.getTypeName();
        if (typeName == null || typeName.trim().length() == 0) {
        	typeName = mapPlace.getName();
        }
		return getPlaceStyle(typeName);
	}
	
	public ShapeType getNextNodeShape() {
		MapTheme mapTheme = getTheme();
		if (mapTheme != null && mapTheme.getDefaultShape() != null) {
			return mapTheme.getDefaultShape();
		} else {
			ShapeType shapeType = ShapeType.getNextNodeShape();
			while (shapeType == ShapeType.NONE) {
				shapeType = ShapeType.getNextNodeShape();
			}
			return shapeType;
		}
	}
	
	private MapTheme getTheme() {
		return mapSettingsPresenter.getTheme();
	}
	
	public ShapeType getDefaultShape() {
		return mapSettingsPresenter.getDefaultShape();
	}
	
	public PlaceStyle getPlaceStyle(String placeName) {
		return mapSettingsPresenter.getPlaceStyle(placeName);
	}
}
