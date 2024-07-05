package csi.client.gwt.viz.map.settings;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.ResourceBasicsComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.model.FieldDef;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.service.api.ThemeActionsServiceProtocol;

public class MapPlacesTab extends MapSettingsComposite {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	private static MapPlaceTabUiBinder uiBinder = GWT.create(MapPlaceTabUiBinder.class);

	interface MapPlaceTabUiBinder extends UiBinder<Widget, MapPlacesTab> {
	}

	interface MapPlaceModelProperties extends PropertyAccess<MapPlace> {
		ModelKeyProvider<MapPlace> uuid();

		ValueProvider<MapPlace, String> name();

		ValueProvider<MapPlace, FieldDef> latField();

		ValueProvider<MapPlace, FieldDef> longField();

		ValueProvider<MapPlace, Void> voidFn();
	}

	MapPlaceModelProperties placeProp = GWT.create(MapPlaceModelProperties.class);

	@UiField
	ResourceBasicsComboBox themeListBox;
	@UiField
	GridContainer placeGridContainer;

	private ResizeableGrid<MapPlace> placeGrid;

	private MapSettingsPresenter presenter;

	public MapPlacesTab() {
		super();

		initWidget(uiBinder.createAndBindUi(this));

		addThemeChangeHandler();
		initGrid();
	}

	public void addTheme(ResourceBasics mapTheme) {// OK
		themeListBox.getStore().add(mapTheme);
	}

	private void updateTheme() {
		String themeUuid = getMapSettings().getThemeUuid();
		if (themeListBox.getStore().size() > 0) {
			ResourceBasics selectedMapTheme = null;
			String themeName = "";
			if (themeUuid != null && !themeUuid.isEmpty()) {
				ResourceBasics theme = presenter.getThemeIdToResource().get(themeUuid);
				if (theme != null)
					themeName = presenter.getThemeIdToResource().get(themeUuid).getDisplayName();
			}
			List<ResourceBasics> themes = themeListBox.getStore().getAll();
			for (ResourceBasics theme : themes) {
				if (themeName.equals(theme.getName())) {
					selectedMapTheme = theme;
				}
			}
			resolveTheme(selectedMapTheme);
		}
	}

	private void resolveTheme(ResourceBasics selectedMapTheme) {
		themeListBox.select(selectedMapTheme);
		themeListBox.setValue(selectedMapTheme, false);
		if (selectedMapTheme == null) {
			themeListBox.setText("");
		} else {
			themeListBox.setText(selectedMapTheme.getName());
		}
		setTheme(selectedMapTheme);
	}

	private void addThemeChangeHandler() {
		themeListBox.addSelectionHandler(event -> {
			ResourceBasics mapTheme = event.getSelectedItem();
			setTheme(mapTheme);
		});

		themeListBox.addKeyUpHandler(event -> {
			String text = themeListBox.getText();
			ResourceBasics theme = null;
			boolean doUpdate = false;
			if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
				if (text == null || text.equals("")) {
					theme = null;
				} else {
					boolean found = false;
					List<ResourceBasics> themes = themeListBox.getStore().getAll();
					for (ResourceBasics resourceBasics : themes) {
						if (text.equals(resourceBasics.getName())) {
							theme = resourceBasics;
							found = true;
							break;
						}
					}
					if (!found) {
						theme = null;
					}
				}
				doUpdate = true;
			} else {
				List<ResourceBasics> themes = themeListBox.getStore().getAll();
				for (ResourceBasics resourceBasics : themes) {
					if (text.equals(resourceBasics.getName())) {
						theme = resourceBasics;
						doUpdate = true;
						break;
					}
				}
			}

			if (doUpdate) {
				resolveTheme(theme);
			}
		});

		themeListBox.addBlurHandler(event -> {
			String text = themeListBox.getText();
			boolean doUpdate = false;
			ResourceBasics theme = themeListBox.getCurrentValue();
			if (text == null || text.isEmpty()) {
				doUpdate = true;
				theme = null;
			} else {
				List<ResourceBasics> themes = themeListBox.getStore().getAll();
				doUpdate = true;
				for (ResourceBasics resourceBasics : themes) {
					if (text.equals(resourceBasics.getName())) {
						doUpdate = false;
						break;

					}
				}
			}

			if (doUpdate) {
				resolveTheme(theme);
			}
		});
	}

	private void setTheme(ResourceBasics mapThemeIn) {
		String themeUuid = null;
		if (mapThemeIn == null) {
			presenter.applyTheme(null);
		} else {
			themeUuid = mapThemeIn.getUuid();
		}
		getMapSettings().setThemeUuid(themeUuid);
		WebMain.injector.getVortex().execute((MapTheme mapTheme) -> {
			presenter.applyTheme(mapTheme);
		}, ThemeActionsServiceProtocol.class).findMapTheme(themeUuid);
	}

	private void initGrid() {
		initPlaceGrid();
	}

	private void initPlaceGrid() {
		Cell<FieldDef> cell = new FieldDefNameCell();

		ColumnConfig<MapPlace, Void> dragCol = new ColumnConfig<MapPlace, Void>(placeProp.voidFn());
		{
			dragCol.setHeader(""); //$NON-NLS-1$
			dragCol.setWidth(20);
			dragCol.setCell(DragCell.<Void>create());
			dragCol.setResizable(false);
		}

		ColumnConfig<MapPlace, String> nameColumn = new ColumnConfig<MapPlace, String>(placeProp.name());
		{
			nameColumn.setHeader(i18n.mapSettingsView_placesTab_PlaceName()); // $NON-NLS-1$
			nameColumn.setWidth(150);
			nameColumn.setCell(new TextCell());
		}
		ColumnConfig<MapPlace, FieldDef> latColumn = new ColumnConfig<MapPlace, FieldDef>(placeProp.latField());
		{
			latColumn.setHeader(i18n.mapSettingsView_placesTab_Latitude()); // $NON-NLS-1$
			latColumn.setWidth(150);
			latColumn.setCell(cell);
		}
		ColumnConfig<MapPlace, FieldDef> longColumn = new ColumnConfig<MapPlace, FieldDef>(placeProp.longField());
		{
			longColumn.setHeader(i18n.mapSettingsView_placesTab_Longitude()); // $NON-NLS-1$
			longColumn.setWidth(150);
			longColumn.setCell(cell);
		}
		final ColumnConfig<MapPlace, Void> editColumn = new ColumnConfig<MapPlace, Void>(placeProp.voidFn());
		{
			IconCell iconCell = new IconCell(IconType.PENCIL);
			iconCell.setTooltip(i18n.mapSettingsView_placesTab_editCellTooltip()); // $NON-NLS-1$
			editColumn.setCell(iconCell);
			editColumn.setWidth(20);

		}
		final ColumnConfig<MapPlace, Void> deleteColumn = new ColumnConfig<MapPlace, Void>(placeProp.voidFn());
		{
			IconCell iconCell = new IconCell(IconType.REMOVE);
			iconCell.setTooltip(i18n.mapSettingsView_placesTab_deleteTooltip()); // $NON-NLS-1$
			deleteColumn.setCell(iconCell);
			deleteColumn.setWidth(20);

		}
		List<ColumnConfig<MapPlace, ?>> columnConfigs = Lists.newArrayList();
		columnConfigs.add(dragCol);
		columnConfigs.add(nameColumn);
		columnConfigs.add(latColumn);
		columnConfigs.add(longColumn);
		columnConfigs.add(editColumn);
		columnConfigs.add(deleteColumn);

		ColumnModel<MapPlace> columnModel = new ColumnModel<MapPlace>(columnConfigs);

		ListStore<MapPlace> store = new ListStore<MapPlace>(placeProp.uuid());

		placeGrid = new ResizeableGrid<MapPlace>(store, columnModel);
		GridView<MapPlace> view = placeGrid.getView();
		view.setShowDirtyCells(false);
		view.setSortingEnabled(false);
		view.setAdjustForHScroll(true);
		placeGrid.setColumnReordering(false);
		placeGrid.setColumnResize(false);
		placeGrid.setAllowTextSelection(false);
		GridHelper.setDraggableRowsDefaults(placeGrid);
		placeGridContainer.setGrid(placeGrid);

		placeGrid.addCellClickHandler(event -> {
			int rowIndex = event.getRowIndex();
			int cellIndex = event.getCellIndex();
			ListStore<MapPlace> gridstore = placeGrid.getStore();
			MapPlace mapPlace = gridstore.get(rowIndex);
			if (cellIndex == placeGrid.getColumnModel().indexOf(editColumn)) {
				presenter.editPlace(mapPlace);
			} else if (cellIndex == placeGrid.getColumnModel().indexOf(deleteColumn)) {
				presenter.removePlace(mapPlace);
			}
		});
	}

	@Override
	public void updateViewFromModel() {
		PopulateThemeIdToResourceCallback callback = new PopulateThemeIdToResourceCallback() {
			@Override
			public void actionComplete() {
				updateTheme();
				updatePlaceGrid();
			}
		};
		if (presenter.getThemeIdToResource() == null) {
			presenter.populateThemeIdToResource(callback);
		} else {
			callback.actionComplete();
		}
	}

	private void updatePlaceGrid() {
		List<MapPlace> places = presenter.getMapPlaces();
		if (places == null) {
			places = new ArrayList<MapPlace>();
		}

		List<MapPlace> badEvents = new ArrayList<MapPlace>();
		for (MapPlace mapPlace : places) {
			FieldDef FieldDef = mapPlace.getLatField();
			FieldDef longField = mapPlace.getLongField();

			if (FieldDef == null) {
				badEvents.add(mapPlace);
				continue;
			}

			if (longField == null) {
				badEvents.add(mapPlace);
			}
		}

		places.removeAll(badEvents);
		presenter.setMapPlaces(places);
		placeGrid.getStore().clear();
		placeGrid.getStore().addAll(places);
	}

	@Override
	public void updateModelWithView() {
		placeGrid.getStore().commitChanges();
		getMapSettings().setMapPlaces(presenter.getMapPlaces());
	}

	@UiHandler("newPlaceButton")
	void onNewPlaceButtonClick(ClickEvent event) {
		presenter.newPlace();
	}

	public MapSettingsPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(MapSettingsPresenter presenter) {
		this.presenter = presenter;
	}

	public void populateThemes() {
		themeListBox.getStore().clear();
		for (String uuid : presenter.getThemeIdToResource().keySet()) {
			ResourceBasics resourceBasics = presenter.getThemeIdToResource().get(uuid);
			addTheme(resourceBasics);
		}
	}
}
