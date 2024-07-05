/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.edit_sources.right_panel;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.edit_sources.cells.FilterAwareColumnNameCell;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoTable;
import csi.client.gwt.edit_sources.dialogs.column.ColumnDefFilterCell;
import csi.client.gwt.edit_sources.dialogs.column.ColumnSelectionCheckboxCell;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.AuthorizationSource;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.CsiHeading;
import csi.client.gwt.widget.buttons.MiniBlueButton;
import csi.client.gwt.widget.buttons.MiniRedButton;
import csi.client.gwt.widget.cells.context_menu.DataTypeCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.util.ConnectorSupport;
import csi.server.connector.AbstractConnectionFactory;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableDetailEditor extends ResizeComposite implements AuthorizationSource {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      Interfaces                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ColumnDefPropertyAccess extends PropertyAccess<ColumnDef> {

        @Path("localId")
        public ModelKeyProvider<ColumnDef> key();

        public ValueProvider<ColumnDef, Boolean> selected();

        @Path("csiType")
        public ValueProvider<ColumnDef, CsiDataType> csiTypeDisplay();

//        @Path("castToType")
//        public ValueProvider<ColumnDef, CsiDataType> castToTypeDisplay();
    }

    interface SpecificUiBinder extends UiBinder<Widget, TableDetailEditor> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    CsiHeading heading;
    @UiField
    FullSizeLayoutPanel topLevelPanel;
    @UiField
    FullSizeLayoutPanel columnPanel;
    @UiField
    FlowPanel tableSummary;
    @UiField
    GridContainer gridContainer;
    @UiField
    MiniRedButton buttonResetQuery;
    @UiField
    MiniBlueButton buttonEditQuery;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	private static final String _source_Name = _constants.tableDetailEditor_Source_Name();
	private static final String _source_Type = _constants.tableDetailEditor_Source_Type();
	private static final String _filename = _constants.tableDetailEditor_Filename();
	private static final String _host_Port = _constants.tableDetailEditor_Host_Port();
	private static final String _schema = _constants.tableDetailEditor_Schema();
	private static final String _type = _constants.tableDetailEditor_Type();

    private ResizeableGrid<ColumnDef> grid;
    private DataSourceEditorModel _model;
    private WienzoTable _parent;
    private DataSetOp _dso;
    private SqlTableDef _tableDef;
    private DataSourceDef _dataSource;

    private static SpecificUiBinder uiBinder = GWT
            .create(SpecificUiBinder.class);
    private ColumnDefPropertyAccess propertyAccess = GWT
            .create(ColumnDefPropertyAccess.class);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiHandler("buttonResetQuery")
    public void handleResetQuery(ClickEvent event) {

        if (null != _tableDef) {

            SqlTableDef myTable = _model.retrieveCustomReset(_tableDef);

            if (null != myTable) {

                _tableDef = myTable;

            } else {

                _tableDef.setCustomQuery(null);
                _tableDef.setIsCustom(false);
            }
            tableSummary.clear();
            initHeader();
            initGrid();
        }
        grid.refreshKeepState();
    }

    @UiHandler("buttonEditQuery")
    public void handleEditQuery(ClickEvent event) {
        launchQueryEditor();
    }

    @UiHandler("buttonSelectAll")
    public void handleSelectAll(ClickEvent event) {
        for (ColumnDef column : grid.getStore().getAll()) {
            if (!column.isSelected()) {
                column.setSelected(true);
                _model.addField(column, true);
            }
        }
        grid.refreshKeepState();
    }

    @UiHandler("buttonDeselectAll")
    public void handleDeselectAll(ClickEvent event) {
        // Check for references.
        // ColumnDefReferenceUtil.checkDeselectAll(_tableDef, _model, new
        // GenericCallback<Void, Void>() {

        // @Override
        // public Void onCallback(Void parameter) {
        _handleDeselectAll();
        // return null;
        // }
        // });
    }

    public boolean isOk(Object objectIn) {

        if ((null != _tableDef) && (null != _tableDef.getCustomQuery())) {

            String myMethod = buttonResetQuery.isVisible()
                                    ? _constants.tableDetailEditor_RejectFilter_UseReset()
                                    : _constants.tableDetailEditor_RejectFilter_ReplaceQuery();

            Display.error(_constants.tableDetailEditor_RejectFilter_Title(),
                    _constants.tableDetailEditor_RejectFilter_Message(myMethod));

            return false;
        }
        return true;
    }
/*
    private GridClickEventHandler castingClickHandler = new GridClickEventHandler() {
        @Override
        public void onGridClick(GridClickEvent eventIn) {

            ColumnDef myColumn = grid.getStore().get(eventIn.getRow());
            CsiDataType myCurrentCast = myColumn.getCastToType();

            myColumn.setCastToType(myCurrentCast.getNextValue());
            grid.getView().refresh(false);
        }
    };

    private DataTypeCallback castingCallback = new DataTypeCallback() {
        @Override
        public void onTypeSelection(CsiDataType dataTypeIn, int rowIn) {

            ColumnDef myColumn = grid.getStore().get(rowIn);

            myColumn.setCastToType(dataTypeIn);
            grid.getView().refresh(false);
        }
    };
*/

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TableDetailEditor(WienzoTable parentIn, DataSourceEditorModel _model) {
        super();
        _parent = parentIn;
        _dso = _parent.getDso();
        _tableDef = _dso.getTableDef();
        _dataSource = _tableDef.getSource();
        this._model = _model;
        initWidget(uiBinder.createAndBindUi(this));
        buttonEditQuery.setVisible(ConnectorSupport.getInstance().canExecuteQuery(_dataSource)
                                    && ConnectorSupport.getInstance().canEditQuery(_dataSource));
        initHeader();
        initGrid();
    }

    public void launchQueryEditor() {

        if (ConnectorSupport.getInstance().canExecuteQuery(_dataSource) && ConnectorSupport.getInstance().canEditQuery(_dataSource)) {

            if (null == _tableDef.getCustomQuery()) {

                _model.recordCustomReset(_tableDef);
            }
            _parent.getConfigurationPresenter().editCustomQuery(_dso, _dataSource, _tableDef);

        } else {

            Display.error(_constants.dataSourceEditor_CustomQuery(), _constants.blockingCustomQuery());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createQueryForEdit(SqlTableDef tableIn) {

    }

    private void initHeader() {
        {
            FluidRow row = new FluidRow();
            Column title = new Column(4);
            title.addStyleName("rightAlignedChild");
            Label titleLabel = new Label(_source_Name);
            title.add(titleLabel);
            Column value = new Column(8);
            com.google.gwt.user.client.ui.Label valueLabel = new com.google.gwt.user.client.ui.Label(
                    _tableDef.getSource().getName());
            value.add(valueLabel);
            row.add(title);
            row.add(value);
            // Gets the layer above the summary and sets it to scroll if
            // necessary
            tableSummary.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.AUTO);
            tableSummary.add(row);
        }
        {
            FluidRow row = new FluidRow();
            Column title = new Column(4);
            title.addStyleName("rightAlignedChild");
            Label titleLabel = new Label(_source_Type);
            title.add(titleLabel);
            Column value = new Column(8);
            com.google.gwt.user.client.ui.Label valueLabel = new com.google.gwt.user.client.ui.Label(
                    DataSourceClientUtil.getConnectionTypeName(_tableDef
                            .getSource().getConnection()));
            value.add(valueLabel);
            row.add(title);
            row.add(value);
            tableSummary.add(row);
        }

        String filename = _tableDef.getSource().getConnection().getProperties()
                .getPropertiesMap().get(AbstractConnectionFactory.CSI_REMOTEFILEPATH);
        if (!Strings.isNullOrEmpty(filename)) {
            FluidRow row = new FluidRow();
            Column title = new Column(4);
            title.addStyleName("rightAlignedChild");
            Label titleLabel = new Label(_filename);
            title.add(titleLabel);
            Column value = new Column(8);
            com.google.gwt.user.client.ui.Label valueLabel = new com.google.gwt.user.client.ui.Label(
                    filename);
            value.add(valueLabel);
            row.add(title);
            row.add(value);
            tableSummary.add(row);
        }

        String hostname = _tableDef.getSource().getConnection().getProperties()
                .getPropertiesMap().get(AbstractConnectionFactory.CSI_HOSTNAME);
        if (!Strings.isNullOrEmpty(hostname)) {
            FluidRow row = new FluidRow();
            Column title = new Column(4);
            title.addStyleName("rightAlignedChild");
            Label titleLabel = new Label(_host_Port);
            title.add(titleLabel);
            Column value = new Column(8);
            String port = _tableDef.getSource().getConnection().getProperties()
                    .getPropertiesMap().get(AbstractConnectionFactory.CSI_PORT);
            if (!Strings.isNullOrEmpty(port)) {
                hostname = hostname + ":" + port;
            }
            com.google.gwt.user.client.ui.Label valueLabel = new com.google.gwt.user.client.ui.Label(
                    hostname);
            value.add(valueLabel);
            row.add(title);
            row.add(value);
            tableSummary.add(row);
        }

        if (!Strings.isNullOrEmpty(_tableDef.getSchemaName())) {
            FluidRow row = new FluidRow();
            Column title = new Column(4);
            title.addStyleName("rightAlignedChild");
            Label titleLabel = new Label(_schema);
            title.add(titleLabel);
            Column value = new Column(8);
            com.google.gwt.user.client.ui.Label valueLabel = new com.google.gwt.user.client.ui.Label(
                    _tableDef.getSchemaName());
            value.add(valueLabel);
            row.add(title);
            row.add(value);
            tableSummary.add(row);
        }
        {

            FluidRow row = new FluidRow();
            Column title = new Column(4);
            title.addStyleName("rightAlignedChild");
            Label titleLabel = new Label(_type);
            title.add(titleLabel);
            Column value = new Column(8);
            com.google.gwt.user.client.ui.Label valueLabel = new com.google.gwt.user.client.ui.Label(
                    (null != _tableDef.getCustomQuery()) ? _constants.dataSourceEditor_CustomQuery()
                            : _tableDef.getTableType());
            value.add(valueLabel);
            row.add(title);
            row.add(value);
            tableSummary.add(row);
        }
        heading.setText(_tableDef.getDsoName());
        buttonResetQuery.setVisible(buttonEditQuery.isVisible() && (null != _tableDef.getCustomQuery())
                                    && (null != _model.retrieveCustomReset(_tableDef)));
    } // end initHeader()

    @SuppressWarnings("unchecked")
    private void initGrid() {
        final GridComponentManager<ColumnDef> manager = WebMain.injector
                .getGridFactory().create(propertyAccess.key());
        ListStore<ColumnDef> gridStore = manager.getStore();
        ColumnSelectionCheckboxCell myCheckBox = new ColumnSelectionCheckboxCell(gridStore, _model);

        ColumnConfig<ColumnDef, Boolean> selectCol = manager.create(
                propertyAccess.selected(), 75, _constants.dataSourceEditor_Selected());
        selectCol.setCell(myCheckBox);
        selectCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
/*
        ColumnConfig<ColumnDef, CsiDataType> castCol = manager.create(
                propertyAccess.castToTypeDisplay(), 60, _constants.type(), true, true);
        castCol.setCell(new DataTypeCell(true, false, castingCallback));
        castCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
*/

        ColumnConfig<ColumnDef, CsiDataType> typeCol = manager.create(
                propertyAccess.csiTypeDisplay(), 60, _constants.source(), true, true);
        typeCol.setCell(new DataTypeCell(true, false));
        typeCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        ColumnConfig<ColumnDef, ColumnDef> nameCol = manager
                .create(new IdentityValueProvider<ColumnDef>(), 200, _constants.dataSourceEditor_Name(),
                        true, true);
        nameCol.setCell(new FilterAwareColumnNameCell());
/*
        ColumnConfig<ColumnDef, CsiDataType> typeCol = manager.create(
                propertyAccess.csiTypeDisplay(), 60, _constants.source(), true, true);
        typeCol.setCell(new DataTypeCell(true, false));
        typeCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
*/
//        if (!_tableDef.getIsCustom()) {

            ColumnConfig<ColumnDef, ColumnDef> filterCol = manager.create(
                    new IdentityValueProvider<ColumnDef>(), 70, _constants.dataSourceEditor_Filter(), false,
                    true);
            ClickHandler updateFiltersHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    boolean viewReady = grid.isViewReady();
                    grid.getView().refresh(true);
                    grid.getView().layout();
                }
            };
            filterCol.setCell(new ColumnDefFilterCell(_model, _tableDef,
                    updateFiltersHandler, this));
            filterCol.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
//        }

        List<ColumnConfig<ColumnDef, ?>> columns = manager
                .getColumnConfigList();
        ColumnModel<ColumnDef> cm = new ColumnModel<ColumnDef>(columns);

        gridStore.setAutoCommit(true);

        grid = new ResizeableGrid<ColumnDef>(gridStore, cm);
        grid.getView().setAutoExpandColumn(nameCol);
//        GridHelper.setDraggableRowsDefaults(grid);

        gridContainer.setGrid(grid);
        grid.getStore().addAll(_tableDef.getColumns());
    }

    protected void _handleDeselectAll() {
        for (ColumnDef column : grid.getStore().getAll()) {
            if (column.isSelected()) {
                column.setSelected(false);
                _model.removeField(column, false);
            }
        }
        grid.refreshKeepState();
    }
}
