package csi.client.gwt.dataview.export.kml;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.kml.KmlMapping;

/**
 * Created by Patrick on 10/20/2014.
 */
public class KmlExportDialog implements KmlExport.View {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    Dialog dialog;
    @UiField
    Button newButton;
    @UiField
    GridContainer gridContainer;
    @UiField
    StringComboBox filterListBox;
    @UiField
    StringComboBox visualizationListBox;
    

	@UiField(provided = true)
	String heading = i18n.kmlExportDialogTitle(); //$NON-NLS-1$
	@UiField(provided = true)
	String newButtonText = i18n.kmlExportDialogNewLinkText(); //$NON-NLS-1$
	@UiField(provided = true)
	String filterLabel = i18n.kmlExportDialogFilterLabel(); //$NON-NLS-1$
	@UiField(provided = true)
	String listboxMessage = i18n.kmlExportDialogIncludeLabel(); //$NON-NLS-1$

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private KmlExport kmlExport;
    private KmlExport.Presenter presenter;
    private KmlMappingModelProperties kmlMappingModelProps = GWT.create(KmlMappingModelProperties.class);
    private BiMap<Integer, Filter> positionToFilterMap = HashBiMap.create();
    private BiMap<Integer, Visualization> positionToVisualizationMap = HashBiMap.create();
    private ResizeableGrid<KmlMapping> grid;

    public KmlExportDialog(KmlExport kmlExport) {
        this.kmlExport = kmlExport;
        uiBinder.createAndBindUi(this);
        initGrid();
        initFilters();
        initDialog();
        initVisualizations();
    }

    @UiHandler("visualizationListBox")
    public void onVisualizationListBox(ChangeEvent event) {
        presenter.setVisualization(positionToVisualizationMap.get(visualizationListBox.getSelectedIndex()));
    }

    private void initVisualizations() {
        List<Visualization> visualizations = kmlExport.getVisualizations();
        visualizationListBox.getStore().add(i18n.bundleFunctionNone()); //$NON-NLS-1$
        int position = 1;
        for (Visualization visualization : visualizations) {
            positionToVisualizationMap.put(position++, visualization);
            visualizationListBox.getStore().add(visualization.getName());
        }
    }

    private void initDialog() {
        csi.client.gwt.widget.buttons.Button actionButton = dialog.getActionButton();
        actionButton.setText(i18n.kmlExportDialogcreateButton()); //$NON-NLS-1$
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //dialog.hide();
                presenter.createKML();
            }
        });
        csi.client.gwt.widget.buttons.Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.hide();
                kmlExport.save();
            }
        });
    }

    private void initFilters() {
        filterListBox.getStore().clear();
        List<Filter> filters = kmlExport.getFilters();
        int position = 1;
        filterListBox.getStore().add(i18n.bundleFunctionNone()); //$NON-NLS-1$
        for (Filter filter : filters) {
            positionToFilterMap.put(position++, filter);
            filterListBox.getStore().add(filter.getName());
        }
        filterListBox.getStore().add(i18n.kmlExportDialognewFilterDropBox()); //$NON-NLS-1$
    }

    public void onFilterListBox(ChangeEvent event) {
        if (filterListBox.getItemCount() == filterListBox.getSelectedIndex() + 1) {
            presenter.createFilter();
        } else if (filterListBox.getSelectedIndex() == 0) {
            setFilter(null);
        } else {
            presenter.setFilter(positionToFilterMap.get(filterListBox.getSelectedIndex()));
        }
    }


    private void initGrid() {
        ColumnConfig<KmlMapping, String> nameColumn = new ColumnConfig<KmlMapping, String>(kmlMappingModelProps.name());
        {
            nameColumn.setHeader(i18n.kmlExportDialogNameHeader()); //$NON-NLS-1$
            nameColumn.setWidth(235);
        }
        final ColumnConfig<KmlMapping, Boolean> includeColumn = new ColumnConfig<KmlMapping, Boolean>(kmlMappingModelProps.isSelected());
        {
            CheckboxCell cell = new CheckboxCell();
            includeColumn.setCell(cell);
            includeColumn.setWidth(20);
        }
        final ColumnConfig<KmlMapping, Void> deleteColumn = new ColumnConfig<KmlMapping, Void>(kmlMappingModelProps.voidFn());
        {
            IconCell cell = new IconCell(IconType.REMOVE);
            cell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
            deleteColumn.setCell(cell);
            deleteColumn.setWidth(20);

        } final ColumnConfig<KmlMapping, Void> editColumn = new ColumnConfig<KmlMapping, Void>(kmlMappingModelProps.voidFn());
        {
            IconCell cell = new IconCell(IconType.PENCIL);
            cell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
            editColumn.setCell(cell);
            editColumn.setWidth(20);

        }
        List<ColumnConfig<KmlMapping, ?>> columnConfigs = Lists.newArrayList();
//        columnConfigs.add(includeColumn);
        columnConfigs.add(nameColumn);
        columnConfigs.add(editColumn);
        columnConfigs.add(deleteColumn);

        ColumnModel<KmlMapping> columnModel = new ColumnModel<KmlMapping>(columnConfigs);

        final ListStore<KmlMapping> KmlMappingModelStore = new ListStore<>(kmlMappingModelProps.uuid());
        grid = new ResizeableGrid<KmlMapping>(KmlMappingModelStore, columnModel);
        {
            grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

                @Override
                public void onCellClick(CellClickEvent event) {
                    int rowIndex = event.getRowIndex();
                    int cellIndex = event.getCellIndex();
                    ListStore<KmlMapping> store = grid.getStore();
                    int delColIndex = grid.getColumnModel().indexOf(deleteColumn);
                    if (cellIndex == delColIndex) {
                        store.remove(rowIndex);
                    }
                }
            });
        }
        {
            grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

                @Override
                public void onCellClick(CellClickEvent event) {
                    int rowIndex = event.getRowIndex();
                    int cellIndex = event.getCellIndex();
                    ListStore<KmlMapping> store = grid.getStore();
                    KmlMapping kmlMapping = store.get(rowIndex);
                    int delColIndex = grid.getColumnModel().indexOf(editColumn);
                    if (cellIndex == delColIndex) {
                        presenter.editFilter(kmlMapping);

                    }
                }
            });
        }
        {
            GridView<KmlMapping> view = grid.getView();
            view.setShowDirtyCells(false);
            view.setSortingEnabled(false);
            view.setAdjustForHScroll(true);
            grid.setColumnReordering(false);
            grid.setColumnResize(false);
            grid.setAllowTextSelection(false);
            grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
            grid.getView().setTrackMouseOver(false);
        }
        gridContainer.setGrid(grid);

        grid.getStore().addStoreRemoveHandler(new StoreRemoveEvent.StoreRemoveHandler<KmlMapping>() {
            @Override
            public void onRemove(StoreRemoveEvent<KmlMapping> event) {
                presenter.removeMapping(event.getItem());
            }
        });
    }

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void setPresenter(KmlExport.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setFilter(Filter filter) {
        Integer index = positionToFilterMap.inverse().get(filter);
        if (index != null) {
            filterListBox.setSelectedIndex(index);
        } else {
            filterListBox.setSelectedIndex(0);
        }
    }

    @Override
    public void updateFilters() {
        initFilters();
    }

    @Override
    public void addMapping(KmlMapping kmlMapping) {
        grid.getStore().add(kmlMapping);
    }

    @Override
    public void setVisualization(Visualization visualization) {
        Integer integer = positionToVisualizationMap.inverse().get(visualization);
        if (integer != null) {
            visualizationListBox.setSelectedIndex(integer);
        } else {
            visualizationListBox.setSelectedIndex(0);
        }
    }

    @Override
    public void setMappings(List<KmlMapping> kmlMappings) {
        ListStore<KmlMapping> store = grid.getStore();
        store.clear();
        store.addAll(kmlMappings);
    }

    @Override
    public void removeMapping(KmlMapping kmlMapping) {
        grid.getStore().remove(kmlMapping);
    }

    @UiHandler("newButton")
    void onNewButtonClick(ClickEvent event) {
        presenter.newMapping();
    }
    
    @UiHandler("filterListBox")
    void onSelection(SelectionEvent<String> event) {
        onFilterListBox(null);
    }

    interface MyUiBinder extends UiBinder<Widget, KmlExportDialog> {
    }

    interface KmlMappingModelProperties extends PropertyAccess<KmlMapping> {
        ValueProvider<KmlMapping, String> name();

        ModelKeyProvider<KmlMapping> uuid();

        ValueProvider<KmlMapping,Boolean> isSelected();

        ValueProvider<KmlMapping,Void> voidFn();
    }
}
