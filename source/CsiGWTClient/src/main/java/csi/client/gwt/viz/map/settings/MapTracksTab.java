package csi.client.gwt.viz.map.settings;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
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

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.model.visualization.map.MapTrack;

public class MapTracksTab extends MapSettingsComposite {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	private static MapTracksTabUiBinder uiBinder = GWT.create(MapTracksTabUiBinder.class);

	interface MapTracksTabUiBinder extends UiBinder<Widget, MapTracksTab> {
	}

	interface MapTrackModelProperties extends PropertyAccess<MapTrack> {
		ModelKeyProvider<MapTrack> uuid();

		ValueProvider<MapTrack, String> name();

		ValueProvider<MapTrack, Void> voidFn();
	}

	MapTrackModelProperties trackProp = GWT.create(MapTrackModelProperties.class);

	@UiField
	GridContainer trackGridContainer;

	private ResizeableGrid<MapTrack> tracksGrid;

	private MapSettingsPresenter presenter;

	public MapTracksTab() {
		super();

		initWidget(uiBinder.createAndBindUi(this));

		initGrid();
	}

	private void initGrid() {
		initTrackGrid();
	}

	private void initTrackGrid() {
		ColumnConfig<MapTrack, Void> dragCol = new ColumnConfig<MapTrack, Void>(trackProp.voidFn());
		{
			dragCol.setHeader(""); //$NON-NLS-1$
			dragCol.setWidth(20);
			dragCol.setCell(DragCell.<Void>create());
			dragCol.setResizable(false);
		}
		ColumnConfig<MapTrack, String> nameColumn = new ColumnConfig<MapTrack, String>(
				trackProp.name());
		{
			nameColumn.setHeader(i18n.mapSettingsView_tracksTab_TrackName()); // $NON-NLS-1$
			nameColumn.setWidth(150);
			nameColumn.setCell(new TextCell());
		}
		final ColumnConfig<MapTrack, Void> editColumn = new ColumnConfig<MapTrack, Void>(
				trackProp.voidFn());
		{
			IconCell iconCell = new IconCell(IconType.PENCIL);
			iconCell.setTooltip(i18n.mapSettingsView_tracksTab_editCellTooltip()); // $NON-NLS-1$
			editColumn.setCell(iconCell);
			editColumn.setWidth(20);

		}
		final ColumnConfig<MapTrack, Void> deleteColumn = new ColumnConfig<MapTrack, Void>(
				trackProp.voidFn());
		{
			IconCell iconCell = new IconCell(IconType.REMOVE);
			iconCell.setTooltip(i18n.mapSettingsView_tracksTab_deleteTooltip()); // $NON-NLS-1$
			deleteColumn.setCell(iconCell);
			deleteColumn.setWidth(20);

		}
		List<ColumnConfig<MapTrack, ?>> columnConfigs = Lists.newArrayList();
		columnConfigs.add(dragCol);
		columnConfigs.add(nameColumn);
		columnConfigs.add(editColumn);
		columnConfigs.add(deleteColumn);

		ColumnModel<MapTrack> columnModel = new ColumnModel<MapTrack>(columnConfigs);

		ListStore<MapTrack> store = new ListStore<MapTrack>(trackProp.uuid());

		tracksGrid = new ResizeableGrid<MapTrack>(store, columnModel);
		GridView<MapTrack> view = tracksGrid.getView();
		view.setShowDirtyCells(false);
		view.setSortingEnabled(false);
		view.setAdjustForHScroll(true);
		tracksGrid.setColumnReordering(false);
		tracksGrid.setColumnResize(false);
		tracksGrid.setAllowTextSelection(false);
		GridHelper.setDraggableRowsDefaults(tracksGrid);
		trackGridContainer.setGrid(tracksGrid);

		tracksGrid.addCellClickHandler(event -> {
			int rowIndex = event.getRowIndex();
			int cellIndex = event.getCellIndex();
			ListStore<MapTrack> gridstore = tracksGrid.getStore();
			MapTrack mapTrack = gridstore.get(rowIndex);
			if (cellIndex == tracksGrid.getColumnModel().indexOf(editColumn)) {
				presenter.editTrack(mapTrack);
			} else if (cellIndex == tracksGrid.getColumnModel().indexOf(deleteColumn)) {
				gridstore.remove(rowIndex);
				presenter.removeTrack(mapTrack);
			}
		});
	}

	@Override
	public void updateViewFromModel() {
		PopulateThemeIdToResourceCallback callback = new PopulateThemeIdToResourceCallback() {
			@Override
			public void actionComplete() {
				updateTrackGrid();
			}
		};
		if (presenter.getThemeIdToResource() == null) {
			presenter.populateThemeIdToResource(callback);
		} else {
			callback.actionComplete();
		}
	}

	private void updateTrackGrid() {
		List<MapTrack> tracks = getMapSettings().getMapTracks();
		if (tracks == null) {
			tracks = new ArrayList<MapTrack>();
			getMapSettings().setMapTracks(tracks);
		}
		tracksGrid.getStore().clear();
		tracksGrid.getStore().addAll(tracks);
	}

	@Override
	public void updateModelWithView() {
		tracksGrid.getStore().commitChanges();
		getMapSettings().getMapTracks().clear();
		int i = 0;
		for (MapTrack mapTrack : tracksGrid.getStore().getAll()) {
			mapTrack.setListPosition(i++);
			getMapSettings().getMapTracks().add(mapTrack);
		}
	}

	@UiHandler("newTrackButton")
	void onNewAssociationButtonClick(ClickEvent event) {
		presenter.newTrack();
	}

	public MapSettingsPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(MapSettingsPresenter presenter) {
		this.presenter = presenter;
	}

}
