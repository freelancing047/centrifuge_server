package csi.client.gwt.maplayer.editor;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.button.ButtonBar;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.misc.EmptyValueProvider;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.model.map.Basemap;

public class MapLayerEditorPanel extends Composite {

	private static BasemapEditorPanelUiBinder uiBinder = GWT.create(BasemapEditorPanelUiBinder.class);

	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	BasemapModelProperties props = GWT.create(BasemapModelProperties.class);
	private MapLayerEditorPresenter presenter;

    @UiField
    GridContainer gridContainer;

    @UiField
    ButtonBar toolBar;

    ResizeableGrid<ResourceBasics> grid;

	interface BasemapEditorPanelUiBinder extends UiBinder<Widget, MapLayerEditorPanel> {
	}

	public MapLayerEditorPanel() {
		initWidget(uiBinder.createAndBindUi(this));

		List<ColumnConfig<ResourceBasics, ?>> columnConfigs = Lists.newArrayList();

		ColumnConfig<ResourceBasics, String> nameColumn = new ColumnConfig<ResourceBasics, String>(props.name());
		nameColumn.setHeader(i18n.name()); // $NON-NLS-1$
		nameColumn.setWidth(280);
		columnConfigs.add(nameColumn);

		ColumnConfig<ResourceBasics, String> ownerColumn = new ColumnConfig<ResourceBasics, String>(props.owner());
		ownerColumn.setHeader(i18n.sharingDialogs_SharingColumn_5()); // $NON-NLS-1$
		ownerColumn.setWidth(150);
		columnConfigs.add(ownerColumn);

		final ColumnConfig<ResourceBasics, Void> editColumn = new ColumnConfig<ResourceBasics, Void>(
				new EmptyValueProvider<ResourceBasics>());
		IconCell editCell = new IconCell(IconType.PENCIL);
		editCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); // $NON-NLS-1$
		editColumn.setCell(editCell);
		editColumn.setWidth(20);
		columnConfigs.add(editColumn);

		final ColumnConfig<ResourceBasics, Void> deleteColumn = new ColumnConfig<ResourceBasics, Void>(
				new EmptyValueProvider<ResourceBasics>());
		IconCell deleteCell = new IconCell(IconType.REMOVE);
		deleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); // $NON-NLS-1$
		deleteColumn.setCell(deleteCell);
		deleteColumn.setWidth(20);
		columnConfigs.add(deleteColumn);

		ListStore<ResourceBasics> store = new ListStore<ResourceBasics>(props.uuid());
		ColumnModel<ResourceBasics> columnModel = new ColumnModel<ResourceBasics>(columnConfigs);

		grid = new ResizeableGrid<ResourceBasics>(store, columnModel);
		gridContainer.setGrid(grid);
		grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {
			@Override
			public void onCellClick(CellClickEvent event) {
				final int rowIndex = event.getRowIndex();
				int cellIndex = event.getCellIndex();
				final ListStore<ResourceBasics> store = grid.getStore();
				final ResourceBasics basemap = store.get(rowIndex);
				int delColIndex = grid.getColumnModel().indexOf(deleteColumn);
				if (cellIndex == delColIndex) {
					WarningDialog dialog = new WarningDialog(i18n.themeDeleteTitle(), // $NON-NLS-1$
							i18n.themeDeleteWarning()); // $NON-NLS-1$
					dialog.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							store.remove(rowIndex);
							presenter.deleteBasemap(basemap.getUuid());
						}
					});
					dialog.show();
				}
			}
		});
		grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {
			@Override
			public void onCellClick(CellClickEvent event) {
				int rowIndex = event.getRowIndex();
				int cellIndex = event.getCellIndex();
				ListStore<ResourceBasics> store = grid.getStore();
				ResourceBasics basemap = store.get(rowIndex);
				int editColIndex = grid.getColumnModel().indexOf(editColumn);
				if (cellIndex == editColIndex) {
					presenter.editBasemap(basemap.getUuid(), null);
				}
			}
		});

		Button createButton = new Button(i18n.themeCreateTitle()); // $NON-NLS-1$
		createButton.setIcon(IconType.PLUS);
		createButton.setType(ButtonType.PRIMARY);
		toolBar.add(createButton);

		createButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.editBasemap(null, new Basemap());
			}
		});
	}

	public MapLayerEditorPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(MapLayerEditorPresenter presenter) {
		this.presenter = presenter;
	}

	interface BasemapModelProperties extends PropertyAccess<ResourceBasics> {
		ValueProvider<ResourceBasics, String> name();

		ValueProvider<ResourceBasics, String> owner();

		ModelKeyProvider<ResourceBasics> uuid();

		ValueProvider<ResourceBasics, Void> voidFn();
	}

	public void updateGrid(List<ResourceBasics> result) {
		grid.getStore().replaceAll(result);
	}
}
