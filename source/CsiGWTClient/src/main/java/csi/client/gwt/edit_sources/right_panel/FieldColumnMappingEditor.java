package csi.client.gwt.edit_sources.right_panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.mapper.VerticalMappingEditorLayout;
import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.FieldDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.client.gwt.mapper.grids.ColumnColumnResultGrid;
import csi.client.gwt.mapper.grids.ColumnSelectionGrid;
import csi.client.gwt.mapper.grids.FieldColumnResultGrid;
import csi.client.gwt.mapper.grids.SelectionGrid;
import csi.client.gwt.mapper.menus.AutoMapMenu;
import csi.client.gwt.mapper.menus.MappingSupport;
import csi.client.gwt.mapper.menus.SortFilterMenu;
import csi.client.gwt.widget.gxt.drag_n_drop.DragLabelProvider;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.ColumnDef;


/**
 * Created by centrifuge on 4/3/2016.
 */
public class FieldColumnMappingEditor extends VerticalMappingEditorLayout<FieldDisplay, ColumnDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String[] _resultGridHeaders = {
            _constants.mapper_GridHeader_Field(),
            _constants.mapper_GridHeader_TableQuery(),
            _constants.mapper_GridHeader_Column()};

    private DataSourceEditorModel _model;
    private List<DataSetOp> _dataSetList;
    private DataSetOp _dso;
    private Map<String, CsiDataType> _unionCastingMap = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected SelectionHandler genNewFieldsHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            AutoMapMenu myMenu = (AutoMapMenu)getMappingMenu();
            MappingSupport<FieldDisplay, ColumnDisplay> mySupport = myMenu.getSupport();
            ListStore<ColumnDisplay> myColumnStore = _rightGrid.getStore();
            ListStore<FieldDisplay> myFieldStore = new ListStore<FieldDisplay>(_leftGrid.getStore().getKeyProvider());

            if (null != myColumnStore) {

                for (int i = 0; myColumnStore.size() > i; i++) {

                    ColumnDisplay myDisplay = myColumnStore.get(i);
                    ColumnDef myColumn = (null != myDisplay) ? myDisplay.getData() : null;

                    if (null != myColumn) {

                        FieldDef myField = _model.addField(myColumn, false);
                        CsiDataType myOverride = _unionCastingMap.get(myColumn.getColumnKey());

                        myFieldStore.add(new FieldDisplay(_leftId, myField, myColumn, myOverride, false));
                    }
                }
//                myColumnStore.clear();
                menuHandler.onMenuSelectionProcessed(mySupport.mapByPosition(myFieldStore, myColumnStore));
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldColumnMappingEditor(DataSourceEditorModel modelIn) {

        super();

        selectFieldColumn();
        _model = modelIn;
        _dataSetList = _model.getDataList();
        _dso = _dataSetList.get(0);
        _unionCastingMap = (null != _dso) ? _dso.buildUnionCastingMap() : new HashMap<>();

        initAll();
        addHandlers();
        finalizeMenus();

        getPanelTitle().setText(_constants.mapper_DSE_MapperTitle());
    }

    public DataSourceEditorModel getModel() {

        return _model;
    }

    public DataSetOp getDso() {

        return _dso;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onAddMapping(SelectionPair<FieldDisplay, ColumnDisplay> selectionIn) {

        FieldDef myField = ((null != selectionIn) && (null != selectionIn.getLeftData()))
                                ? selectionIn.getLeftData().getData() : null;
        ColumnDef myColumn = ((null != selectionIn) && (null != selectionIn.getRightData()))
                                ? selectionIn.getRightData().getData() : null;

        if ((null != myField) && (null != myColumn)) {

            _model.mapField(myField, myColumn);
            selectionIn.setMapped(true);
        }
        _model.adjustButtons();
    }

    @Override
    protected void onDeleteMapping(SelectionPair<FieldDisplay, ColumnDisplay> selectionIn) {

        FieldDef myField = ((null != selectionIn) && (null != selectionIn.getLeftData()))
                ? selectionIn.getLeftData().getData() : null;
        ColumnDef myColumn = ((null != selectionIn) && (null != selectionIn.getRightData()))
                ? selectionIn.getRightData().getData() : null;

        if ((null != myField) && (null != myColumn)) {

            selectionIn.setMapped(false);
            _model.unmapField(myField, myColumn);
        }
        _model.adjustButtons();
    }

    @Override
    protected List<SelectionPair<FieldDisplay, ColumnDisplay>> createPreSelectionList() {

        List<SelectionPair<FieldDisplay, ColumnDisplay>> myList = new ArrayList<SelectionPair<FieldDisplay, ColumnDisplay>>();
        Map<String, FieldDef> myRequiredMap = _model.getRequiredFields();
        Map<String, ColumnDef> myColumnMap = _model.getAllColumns();

        for (FieldDef myField : _model.getActiveFields()) {

            String myKey = myField.getColumnLocalId();
            ColumnDef myColumn = myColumnMap.get(myKey);

            if (null != myColumn) {

                CsiDataType myOverride = _unionCastingMap.get(myColumn.getColumnKey());

                myList.add(new SelectionPair<FieldDisplay, ColumnDisplay>(_mappingId,
                            new FieldDisplay(_leftId, myField, myColumn, myOverride, myRequiredMap.containsKey(myKey)),
                            new ColumnDisplay(_rightId, myColumn)));
            }
        }
        _model.adjustButtons();
        return myList;
    }

    protected void initAll() {

        leftLabelProvider = new DragLabelProvider() {
            @Override
            public String getLabel() {

                return ((FieldGrid)_leftGrid).getSelectionLabel();
            }
        };

        rightLabelProvider = new DragLabelProvider() {
            @Override
            public String getLabel() {

                return ((ColumnSelectionGrid)_rightGrid).getSelectionLabel();
            }
        };

        initializeGrids(new FieldGrid(_leftId, _txtHeader_Field_SortFilter, new SortFilterMenu<SelectionGrid<FieldDisplay>>(), dropHandler, _model),
                new ColumnSelectionGrid(_rightId, _txtHeader_Column_SortFilter, new SortFilterMenu<SelectionGrid<ColumnDisplay>>(), dropHandler, _dso),
                new FieldColumnResultGrid(_mappingId, _resultGridHeaders));
    }

    @Override
    protected void addHandlers() {

        super.addHandlers();

        _rightGrid.addCellDoubleClickHandler(new CellDoubleClickEvent.CellDoubleClickHandler() {
            @Override
            public void onCellClick(CellDoubleClickEvent eventIn) {

                ColumnDisplay mySource = _rightGrid.getSelectionModel().getSelectedItem();
                ColumnDef myColumn = (null != mySource) ? mySource.getData() : null;

                if (null != myColumn) {

                    CsiDataType myOverride = _unionCastingMap.get(myColumn.getColumnKey());

                    FieldDef myField = _model.addField(myColumn, false);
                    FieldDisplay myTarget = new FieldDisplay(_leftId, myField, myColumn, myOverride, false);
                    mapSelectedPair(myTarget, mySource);
                }
            }
        });
    }

    @Override
    protected SelectionPair<FieldDisplay, ColumnDisplay> createMappingItem(String idIn, FieldDisplay leftSelectionIn,
                                                                           ColumnDisplay rightSelectionIn,
                                                                           CsiDataType castToTypeIn,
                                                                           ComparingToken comparingTokenIn) {

        return new SelectionPair(idIn, leftSelectionIn, rightSelectionIn, castToTypeIn, comparingTokenIn);
    }

    protected void finalizeMenus() {

        AutoMapMenu myMenu = (AutoMapMenu)getMappingMenu();
        MenuItem genNewFieldsItem = new MenuItem();

        genNewFieldsItem.setText(_constants.mapperMenu_FieldMap_genNewFieldsItem()); //$NON-NLS-1$
        genNewFieldsItem.addSelectionHandler(genNewFieldsHandler);
        myMenu.add(genNewFieldsItem);
    }
}

