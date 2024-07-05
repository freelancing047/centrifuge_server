package csi.client.gwt.viz.map.settings;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapViewDef;

public class MapGeneralTab extends MapSettingsComposite {
	private static MapGeneralTabUiBinder uiBinder = GWT.create(MapGeneralTabUiBinder.class);

	interface MapGeneralTabUiBinder extends UiBinder<Widget, MapGeneralTab> {
	}

	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	protected static int POINTMAP_INDEX = 0;
	protected static int HEATMAP_INDEX = 1;
	protected static int BUNDLEMAP_INDEX = 2;
	protected static int TRACKMAP_INDEX = 3;

	@UiField
	TextBox mapName;
	@UiField
	TextBox minSizeTextBox;
	@UiField
	TextBox maxSizeTextBox;
	@UiField
	RadioButton pointsModeRB;
	@UiField
	RadioButton heatmapModeRB;
	@UiField
	RadioButton bundlesModeRB;
	@UiField
	RadioButton breadcrumbModeRB;

	private MapSettingsPresenter presenter;

	public MapGeneralTab() {
		initWidget(uiBinder.createAndBindUi(this));
		pointsModeRB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (pointsModeRB.getValue()) {
					presenter.showAssociationsTab(true);
					presenter.showBundleTab(false);
					presenter.showTrackTab(false);
					presenter.showHeatmapTab(false);
					MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
					MapSettings settings = mapViewDef.getMapSettings();
					settings.setUseBundle(false);
					settings.setUseHeatMap(false);
					settings.setUseTrack(false);
				}

			}
		});
		bundlesModeRB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (bundlesModeRB.getValue()) {
					presenter.showAssociationsTab(false);
					presenter.showBundleTab(true);
					presenter.showTrackTab(false);
					presenter.showHeatmapTab(false);
					MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
					MapSettings settings = mapViewDef.getMapSettings();
					settings.setUseBundle(true);
					settings.setUseHeatMap(false);
					settings.setUseTrack(false);
				}

			}
		});
		breadcrumbModeRB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (breadcrumbModeRB.getValue()) {
					presenter.showAssociationsTab(false);
					presenter.showBundleTab(false);
					presenter.showTrackTab(true);
					presenter.showHeatmapTab(false);
					MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
					MapSettings settings = mapViewDef.getMapSettings();
					settings.setUseBundle(false);
					settings.setUseHeatMap(false);
					settings.setUseTrack(true);
				}

			}
		});
		heatmapModeRB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (heatmapModeRB.getValue()) {
					presenter.showAssociationsTab(false);
					presenter.showBundleTab(false);
					presenter.showTrackTab(false);
					presenter.showHeatmapTab(true);
					MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
					MapSettings settings = mapViewDef.getMapSettings();
					settings.setUseBundle(false);
					settings.setUseHeatMap(true);
					settings.setUseTrack(false);
				}

			}
		});
	}

	public void setPresenter(MapSettingsPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void updateViewFromModel() {
		MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
		MapSettings settings = mapViewDef.getMapSettings();
		mapName.setValue(mapViewDef.getName());
		presenter.showAssociationsTab(false);
		presenter.showBundleTab(false);
		presenter.showTrackTab(false);
		presenter.showHeatmapTab(false);
		if (settings.isUseHeatMap()) {
			presenter.showHeatmapTab(true);
			pointsModeRB.setValue(false);
			heatmapModeRB.setValue(true);
			bundlesModeRB.setValue(false);
			breadcrumbModeRB.setValue(false);
		} else if (settings.isUseBundle()) {
			presenter.showBundleTab(true);
			pointsModeRB.setValue(false);
			heatmapModeRB.setValue(false);
			bundlesModeRB.setValue(true);
			breadcrumbModeRB.setValue(false);
		} else if (settings.isUseTrack()) {
			presenter.showTrackTab(true);
			pointsModeRB.setValue(false);
			heatmapModeRB.setValue(false);
			bundlesModeRB.setValue(false);
			breadcrumbModeRB.setValue(true);
		} else {
			presenter.showAssociationsTab(true);
			pointsModeRB.setValue(true);
			heatmapModeRB.setValue(false);
			bundlesModeRB.setValue(false);
			breadcrumbModeRB.setValue(false);
		}
		minSizeTextBox.setValue(Integer.toString(settings.getMinPlaceSize()));
		maxSizeTextBox.setValue(Integer.toString(settings.getMaxPlaceSize()));
	}

	@Override
	public void updateModelWithView() {
		MapViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
		viewDef.setName(mapName.getValue().trim());
		MapSettings settings = viewDef.getMapSettings();
		settings.setUseHeatMap(false);
		settings.setUseBundle(false);
		settings.setUseTrack(false);
		settings.setUseHeatMap(heatmapModeRB.getValue());
		settings.setUseBundle(bundlesModeRB.getValue());
		settings.setUseTrack(breadcrumbModeRB.getValue());
		int minSizeValue = Integer.parseInt(minSizeTextBox.getValue());
		settings.setMinPlaceSize(minSizeValue);
		int maxSizeValue = Integer.parseInt(maxSizeTextBox.getValue());
		settings.setMaxPlaceSize(maxSizeValue);
	}

}