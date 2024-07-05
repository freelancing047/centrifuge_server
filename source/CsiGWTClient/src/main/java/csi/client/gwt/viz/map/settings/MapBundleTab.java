package csi.client.gwt.viz.map.settings;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.form.ColorPickerCell;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.client.gwt.widget.ui.form.ShapePickerCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.map.MapBundleDefinition;

public class MapBundleTab extends MapSettingsComposite {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	private Grid<MapBundleDefinition> grid;

	@UiField(provided = true)
	FieldDefComboBox fieldList;
	@UiField
	GridContainer gridContainer;
	@UiField
	Button buttonAdd;

	interface MapBundlePropertyAccess extends PropertyAccess<MapBundleDefinition> {
		ModelKeyProvider<MapBundleDefinition> uuid();

		ValueProvider<MapBundleDefinition, FieldDef> fieldDef();

		ValueProvider<MapBundleDefinition, String> shapeString();

		ValueProvider<MapBundleDefinition, String> color();

		ValueProvider<MapBundleDefinition, Boolean> showLabel();

		ValueProvider<MapBundleDefinition, Boolean> allowNulls();
	}

	private static MapBundleTabUiBinder uiBinder = GWT.create(MapBundleTabUiBinder.class);
	private static MapBundlePropertyAccess propertyAccess = GWT.create(MapBundlePropertyAccess.class);

	interface MapBundleTabUiBinder extends UiBinder<Widget, MapBundleTab> {
	}

	public MapBundleTab() {
		fieldList = new FieldDefComboBox();
		initWidget(uiBinder.createAndBindUi(this));
		initGrid();
		initAddButton();
	}

	private void initAddButton() {
		buttonAdd.setIcon(IconType.CIRCLE_ARROW_DOWN);
		buttonAdd.setType(ButtonType.LINK);
		buttonAdd.setSize(ButtonSize.LARGE);
		Style buttonStyle = buttonAdd.getElement().getStyle();
		buttonStyle.setFontSize(23.0D, Style.Unit.PX);
		buttonStyle.setTextDecoration(Style.TextDecoration.NONE);
		buttonStyle.setPaddingLeft(0, Style.Unit.PX);
		buttonStyle.setPaddingTop(0, Style.Unit.PX);
		buttonStyle.setMarginBottom(0, Style.Unit.PX);
		buttonStyle.setMarginTop(0, Style.Unit.PX);

		fieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {
			@Override
			public void onSelection(SelectionEvent<FieldDef> event) {
				FieldDef newCategory = event.getSelectedItem();
				if (newCategory != null) {
					addBundle(newCategory);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void initGrid() {
		final GridComponentManager<MapBundleDefinition> manager = WebMain.injector.getGridFactory().create(propertyAccess.uuid());

		ColumnConfig<MapBundleDefinition, FieldDef> dragCol = manager.create(propertyAccess.fieldDef(), 20, i18n.mapSettingsView_bundleTab_dragCol(), false, true);
		dragCol.setCell(DragCell.<FieldDef>create());
		dragCol.setResizable(false);

		ColumnConfig<MapBundleDefinition, FieldDef> nameCol = manager.create(propertyAccess.fieldDef(), 150, i18n.mapSettingsView_bundleTab_fieldCol(), false, true);
		nameCol.setCell(new FieldDefNameCell());

		ColumnConfig<MapBundleDefinition, String> shapeCol = manager.create(propertyAccess.shapeString(), 100, i18n.mapSettingsView_bundleTab_shapeCol(), false, true);
		shapeCol.setCell(new ShapePickerCell());

		ColumnConfig<MapBundleDefinition, String> colorCol = manager.create(propertyAccess.color(), 40, i18n.mapSettingsView_bundleTab_colorCol(), false, true);
		colorCol.setCell(new ColorPickerCell());

		ColumnConfig<MapBundleDefinition, Boolean> labelCol = manager.create(propertyAccess.showLabel(), 40, i18n.mapSettingsView_bundleTab_showLabelCol(), false, true);
		labelCol.setCell(new CheckBoxCell());

		ColumnConfig<MapBundleDefinition, Boolean> allowNulls = manager.create(propertyAccess.allowNulls(), 40, i18n.mapSettingsView_bundleTab_allowNullsCol(), false, true);
		allowNulls.setColumnStyle(SafeStylesUtils.forTextAlign(TextAlign.CENTER));
		allowNulls.setCell(new CheckboxCell());

		List<ColumnConfig<MapBundleDefinition, ?>> columns = manager.getColumnConfigList();
		ColumnModel<MapBundleDefinition> cm = new ColumnModel<MapBundleDefinition>(columns);
		ListStore<MapBundleDefinition> gridStore = manager.getStore();
		grid = new ResizeableGrid<MapBundleDefinition>(gridStore, cm);
		grid.getStore().setAutoCommit(true);
		GridHelper.setDraggableRowsDefaults(grid);
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		gridContainer.setGrid(grid);
	}

	@Override
	public void updateViewFromModel() {
		fieldList.getStore().addAll(Lists.newArrayList(getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs()));
		fieldList.setSelectedIndex(0);

		for (MapBundleDefinition definition : getMapSettings().getMapBundleDefinitions())
			grid.getStore().add(definition);
	}

	@Override
	public void updateModelWithView() {
		grid.getStore().commitChanges();

		getMapSettings().getMapBundleDefinitions().clear();
		int i = 0;
		for (MapBundleDefinition definition : grid.getStore().getAll()) {
			definition.setListPosition(i++);
			getMapSettings().getMapBundleDefinitions().add(definition);
		}
	}

	@UiHandler("buttonAdd")
	public void handleAdd(ClickEvent event) {
		FieldDef currentValue = fieldList.getCurrentValue();
		if (currentValue != null)
			addBundle(currentValue);
	}

	private void addBundle(FieldDef newFieldDef) {
		MapBundleDefinition definition = new MapBundleDefinition();

		definition.setFieldDef(newFieldDef);
		definition.setColor("#124356");
		definition.setShapeString("Square");
		definition.setShowLabel(true);
		definition.setAllowNulls(true);

		grid.getStore().add(definition);

		// Increment to next item.
		fieldList.incrementSelected();
	}

	@UiHandler("buttonDelete")
	public void handleDelete(ClickEvent event) {
		List<MapBundleDefinition> selected = grid.getSelectionModel().getSelection();
		for (MapBundleDefinition definition : selected)
			grid.getStore().remove(definition);
	}
}
