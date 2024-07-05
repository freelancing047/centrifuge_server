package csi.client.gwt.viz.map.settings;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
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
import csi.client.gwt.widget.combo_boxes.ResourceBasicsComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapAssociation;
import csi.server.common.service.api.ThemeActionsServiceProtocol;

public class MapAssociationsTab extends MapSettingsComposite {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	private static MapAssociationTabUiBinder uiBinder = GWT.create(MapAssociationTabUiBinder.class);

	interface MapAssociationTabUiBinder extends UiBinder<Widget, MapAssociationsTab> {
	}

	interface MapAssociationModelProperties extends PropertyAccess<MapAssociation> {
		ModelKeyProvider<MapAssociation> uuid();

		ValueProvider<MapAssociation, String> name();

		ValueProvider<MapAssociation, String> source();

		ValueProvider<MapAssociation, String> destination();

		ValueProvider<MapAssociation, Void> voidFn();
	}

	MapAssociationModelProperties associationProp = GWT.create(MapAssociationModelProperties.class);

	@UiField
	GridContainer associationGridContainer;

	private ResizeableGrid<MapAssociation> associationGrid;

	private MapSettingsPresenter presenter;

	public MapAssociationsTab() {
		super();

		initWidget(uiBinder.createAndBindUi(this));

		initGrid();
	}

	private void initGrid() {
		initAssociationGrid();
	}

	private void initAssociationGrid() {
		ColumnConfig<MapAssociation, Void> dragCol = new ColumnConfig<MapAssociation, Void>(associationProp.voidFn());
		{
			dragCol.setHeader(""); //$NON-NLS-1$
			dragCol.setWidth(20);
			dragCol.setCell(DragCell.<Void>create());
			dragCol.setResizable(false);
		}
		ColumnConfig<MapAssociation, String> nameColumn = new ColumnConfig<MapAssociation, String>(associationProp.name());
		{
			nameColumn.setHeader(i18n.mapSettingsView_associationsTab_AssociationName()); // $NON-NLS-1$
			nameColumn.setWidth(150);
			nameColumn.setCell(new TextCell());
		}
		ColumnConfig<MapAssociation, String> place1Column = new ColumnConfig<MapAssociation, String>(associationProp.source());
		{
			place1Column.setHeader(i18n.mapSettingsView_associationsTab_AssociationSource()); // $NON-NLS-1$
			place1Column.setWidth(150);
			place1Column.setCell(new TextCell());
		}
		ColumnConfig<MapAssociation, String> place2Column = new ColumnConfig<MapAssociation, String>(associationProp.destination());
		{
			place2Column.setHeader(i18n.mapSettingsView_associationsTab_AssociationDestination()); // $NON-NLS-1$
			place2Column.setWidth(150);
			place2Column.setCell(new TextCell());
		}
		final ColumnConfig<MapAssociation, Void> editColumn = new ColumnConfig<MapAssociation, Void>(associationProp.voidFn());
		{
			IconCell iconCell = new IconCell(IconType.PENCIL);
			iconCell.setTooltip(i18n.mapSettingsView_associationsTab_editCellTooltip()); // $NON-NLS-1$
			editColumn.setCell(iconCell);
			editColumn.setWidth(20);

		}
		final ColumnConfig<MapAssociation, Void> deleteColumn = new ColumnConfig<MapAssociation, Void>(associationProp.voidFn());
		{
			IconCell iconCell = new IconCell(IconType.REMOVE);
			iconCell.setTooltip(i18n.mapSettingsView_associationsTab_deleteTooltip()); // $NON-NLS-1$
			deleteColumn.setCell(iconCell);
			deleteColumn.setWidth(20);

		}

		List<ColumnConfig<MapAssociation, ?>> columnConfigs = Lists.newArrayList();
		columnConfigs.add(dragCol);
		columnConfigs.add(nameColumn);
		columnConfigs.add(place1Column);
		columnConfigs.add(place2Column);
		columnConfigs.add(editColumn);
		columnConfigs.add(deleteColumn);

		ColumnModel<MapAssociation> columnModel = new ColumnModel<MapAssociation>(columnConfigs);

		ListStore<MapAssociation> store = new ListStore<MapAssociation>(associationProp.uuid());

		associationGrid = new ResizeableGrid<MapAssociation>(store, columnModel);
		GridView<MapAssociation> view = associationGrid.getView();
		view.setShowDirtyCells(false);
		view.setSortingEnabled(false);
		view.setAdjustForHScroll(true);
		associationGrid.setColumnReordering(false);
		associationGrid.setColumnResize(false);
		associationGrid.setAllowTextSelection(false);
		GridHelper.setDraggableRowsDefaults(associationGrid);
		associationGridContainer.setGrid(associationGrid);

		associationGrid.addCellClickHandler(event -> {
			int rowIndex = event.getRowIndex();
			int cellIndex = event.getCellIndex();
			ListStore<MapAssociation> gridstore = associationGrid.getStore();
			MapAssociation mapAssociation = gridstore.get(rowIndex);
			if (cellIndex == associationGrid.getColumnModel().indexOf(editColumn)) {
				presenter.editAssociation(mapAssociation);
			} else if (cellIndex == associationGrid.getColumnModel().indexOf(deleteColumn)) {
				gridstore.remove(rowIndex);
				presenter.removeAssociation(mapAssociation);
			}
		});
	}

	@Override
	public void updateViewFromModel() {
		PopulateThemeIdToResourceCallback callback = new PopulateThemeIdToResourceCallback() {
			@Override
			public void actionComplete() {
				updateAssociationGrid();
			}
		};
		if (presenter.getThemeIdToResource() == null) {
			presenter.populateThemeIdToResource(callback);
		} else {
			callback.actionComplete();
		}
	}

	private void updateAssociationGrid() {
		List<MapAssociation> associations = getMapSettings().getMapAssociations();
		if (associations == null) {
			associations = new ArrayList<MapAssociation>();
		}

		getMapSettings().setMapAssociations(associations);
		associationGrid.getStore().clear();
		associationGrid.getStore().addAll(associations);
	}

	@Override
	public void updateModelWithView() {
		associationGrid.getStore().commitChanges();
		getMapSettings().getMapAssociations().clear();
		int i = 0;
		for (MapAssociation mapAssociation : associationGrid.getStore().getAll()) {
			mapAssociation.setListPosition(i++);
			getMapSettings().getMapAssociations().add(mapAssociation);
		}
	}

	@UiHandler("newAssociationButton")
	void onNewAssociationButtonClick(ClickEvent event) {
		presenter.newAssociation();
	}

	public MapSettingsPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(MapSettingsPresenter presenter) {
		this.presenter = presenter;
	}

}
