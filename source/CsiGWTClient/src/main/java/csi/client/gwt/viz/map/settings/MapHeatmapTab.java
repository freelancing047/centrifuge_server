package csi.client.gwt.viz.map.settings;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.ui.color.ColorPicker;
import csi.client.gwt.widget.ui.color.ColorPicker.ColorPickerCallback;
import csi.client.gwt.widget.ui.color.ColorPicker.ColorType;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.service.api.ColorActionsServiceProtocol;
import csi.server.common.service.api.ColorActionsServiceProtocol.RangeDirection;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;

public class MapHeatmapTab extends MapSettingsComposite {
	private static MapHeatmapTabUiBinder uiBinder = GWT.create(MapHeatmapTabUiBinder.class);

	interface MapHeatmapTabUiBinder extends UiBinder<Widget, MapHeatmapTab> {
	}

	@UiField
	Button colorModelButton;
	@UiField
	Image colorDisplayImage;
	@UiField
	FieldDefComboBox weightField;

	private ColorModel colorModel = new ContinuousColorModel();
	private ColorPicker colorPicker = new ColorPicker();

	public MapHeatmapTab() {
		initWidget(uiBinder.createAndBindUi(this));
		colorPicker.addColorType(ColorType.CONTINUOUS);
		colorPicker.bind(colorModelButton);
		colorPicker.setColorPickerCallback(new ColorPickerCallback() {
			@Override
			public void beforeShow() {
				colorPicker.setColorModel(colorModel);
				updateDisplayImage();
			}

			@Override
			public void onSelection(ColorModel model) {
				MapHeatmapTab.this.colorModel = model;
				MapViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
				MapSettings settings = viewDef.getMapSettings();
				settings.setColorModel(colorModel);
				updateDisplayImage();
			}
		});
	}

	protected void updateDisplayImage() {
		WebMain.injector.getVortex().execute((String result) -> {
			colorDisplayImage.setUrl(result);
		}, ColorActionsServiceProtocol.class).getColorRangeSample(300, 30, colorModel, RangeDirection.HORIZONTAL);
	}

	@Override
	public void updateViewFromModel() {
		MapViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
		MapSettings settings = viewDef.getMapSettings();
		colorModel = settings.getColorModel();
		updateDisplayImage();

		List<FieldDef> fieldDefs = getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs();
		for (FieldDef fieldDef : fieldDefs) {
			CsiDataType type = fieldDef.getValueType();
			if (type == CsiDataType.Number || type == CsiDataType.Integer)
				weightField.getStore().add(fieldDef);
		}

		if (settings != null) {
			FieldDef weightSetting = settings.getWeightField();
			if (weightSetting != null)
				weightField.setValue(weightSetting);
		}
	}

	@Override
	public void updateModelWithView() {
		MapViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
		MapSettings settings = viewDef.getMapSettings();
		settings.setWeightField(weightField.getCurrentValue());
	}

	@UiHandler("clearWeight")
	public void clearWeightField(ClickEvent e) {
		weightField.setValue(null);
	}
}
