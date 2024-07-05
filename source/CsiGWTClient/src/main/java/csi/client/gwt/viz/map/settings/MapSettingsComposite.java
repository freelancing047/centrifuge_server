package csi.client.gwt.viz.map.settings;

import java.util.List;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.shared.settings.AbstractSettingsComposite;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;

public abstract class MapSettingsComposite extends AbstractSettingsComposite<MapViewDef> {
	public MapSettings getMapSettings() {
		MapViewDef def = getVisualizationSettings().getVisualizationDefinition();
		return def.getMapSettings();
	}

	public List<FieldDef> getAllColumns() {
		VisualizationSettings vs = getVisualizationSettings();

		DataModelDef dataModel = vs.getDataViewDefinition().getModelDef();
		return FieldDefUtils.getAllSortedFields(dataModel, FieldDefUtils.SortOrder.ALPHABETIC);
	}

	protected DataModelDef getDataModelDef() {
		return getVisualizationSettings().getDataViewDefinition().getModelDef();
	}
}
