package csi.client.gwt.widget.gxt.grid;
/*
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.client.gwt.widget.cells.readonly.ColumnNameCell;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.InstalledColumn;

import java.util.*;
*/


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Header;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.dataview.linkup.SelectionChangeResponder;
import csi.client.gwt.WebMain;
import csi.client.gwt.widget.cells.readonly.ColumnNameCell;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.FieldDef;
import csi.server.common.util.ValuePair;


/**
 * Created by centrifuge on 9/24/2018.
 */
public class InstalledColumnMapper extends InstalledColumnMappingGrid {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public class CustomProvider implements ValueProvider<ColumnMappingDataItem, Boolean> {


        @Override
        public String getPath() {
            // TODO Auto-generated method stub
            return "mapField";
        }

        @Override
        public Boolean getValue(ColumnMappingDataItem object) {

            return object.getMapField();
        }

        @Override
        public void setValue(ColumnMappingDataItem object, Boolean value) {
            object.setMapField(value);
        }
    }

    interface ParameterMapProperties extends PropertyAccess<ColumnMappingDataItem> {
        ModelKeyProvider<ColumnMappingDataItem> key();

        @Editor.Path("mappedColumn")
        ValueProvider<ColumnMappingDataItem, InstalledColumn> mappedColumn();
        @Editor.Path("mappingField")
        ValueProvider<ColumnMappingDataItem, FieldDef> mappingField();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private FieldDefNameCell _fieldCell = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

//    private CsiCheckboxSingleSelectionModel _selectionModel = null;
    private GridComponentManager<ColumnMappingDataItem> _manager = null;
    private CsiCheckboxSingleSelectionModel _selectionModel = null;
    private List<InstalledColumn> _columnList;
    private List<FieldDef> _fieldList;
    private ListStore<ColumnMappingDataItem> _dataStore;
    private RepeatingCommand styleEnforcer;
    private boolean _validSelection = true;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstalledColumnMapper(SelectionChangeResponder parentIn, int widthIn, int heightIn, String helpFileNameIn,
                                 List<InstalledColumn> columnListIn, List<FieldDef> fieldListIn) {

        super(parentIn, widthIn, heightIn, helpFileNameIn);
        initializeAll(columnListIn, fieldListIn, widthIn);
    }

    public InstalledColumnMapper(SelectionChangeResponder parentIn, int widthIn, int heightIn,
                                 String helpFileNameIn, List<InstalledColumn> columnListIn,
                                 List<FieldDef> fieldListIn, String columnHeaderIn, String FieldHeaderIn) {

        super(parentIn, widthIn, heightIn, helpFileNameIn, columnHeaderIn, FieldHeaderIn);
        initializeAll(columnListIn, fieldListIn, widthIn);
    }

    private void initializeAll(List<InstalledColumn> columnListIn, List<FieldDef> fieldListIn, int widthIn) {

        _columnList = columnListIn;
        _fieldList = fieldListIn;
        _manager =  createDataGrid(widthIn);
        _dataStore = getListStore();

        for (InstalledColumn myColumn : _columnList) {

            _dataStore.add(new ColumnMappingDataItem(this, myColumn.getLocalId(), myColumn));
        }
        DeferredCommand.add(new Command() {
            public void execute() {
                finalizeHeader();
            }
        });
    }

    public boolean haveValidSelection() {

        return _validSelection;
    }

    public int getSelectionCount() {

        ListStore<ColumnMappingDataItem> myList = dataGrid.getStore();
        int myCount = 0;

        applyComboBoxSelection(_fieldCell);

        _validSelection = true;

        for (int i = 0; myList.size() > i; i++) {

            ColumnMappingDataItem myTestRow = myList.get(i);

            if (myTestRow.getMapField()) {

                FieldDef myMappingField = myTestRow.getMappingField();

                myCount++;

                if ((null == myMappingField) || (null == myMappingField.getLocalId())) {

                    _validSelection = false;
                }
            }
        }
        return myCount;
    }

    public FixedSizeGrid<ColumnMappingDataItem> getGrid() {

        return dataGrid;
    }

    public List<ValuePair<InstalledColumn, FieldDef>> getResults() {

        List<ValuePair<InstalledColumn, FieldDef>> myResults = new ArrayList<ValuePair<InstalledColumn, FieldDef>>();
        ListStore<ColumnMappingDataItem> myList = dataGrid.getStore();

        for (int i = 0; myList.size() > i; i++) {

            ColumnMappingDataItem myTestRow = myList.get(i);

            if (myTestRow.getMapField()) {

                myResults.add(new ValuePair<InstalledColumn, FieldDef>(myTestRow.getMappedField(),
                        myTestRow.getMappingField()));

            } else {

                myResults.add(new ValuePair<InstalledColumn, FieldDef>(myTestRow.getMappedField(), null));
            }
        }
        return myResults;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    protected GridComponentManager<ColumnMappingDataItem> createDataGrid(int widthIn) {

        int mySharedWidth = (widthIn - 22) / 2;
        ParameterMapProperties myProperties = GWT.create(ParameterMapProperties.class);
        SafeHtmlBuilder myBuffer = new SafeHtmlBuilder();

        final GridComponentManager<ColumnMappingDataItem> myManager
                = (GridComponentManager<ColumnMappingDataItem>) WebMain.injector.getGridFactory().create(myProperties.key());

        // Create check box column
        createSelectionModel();
        myManager.getColumnConfigList().add(_selectionModel.getColumn());

        // Create Installed Table Column column
        ColumnConfig<ColumnMappingDataItem, InstalledColumn> myColumn = myManager.create(myProperties.mappedColumn(),
                                                                        mySharedWidth, _txtColumnHeader, false, true);
        myColumn.setHideable(false);
        myColumn.setWidth(mySharedWidth);
        myColumn.setCell(new ColumnNameCell());

        // Create DataView Field column
        ColumnConfig<ColumnMappingDataItem, FieldDef> myField = myManager.create(myProperties.mappingField(),
                                                                        mySharedWidth, _txtFieldHeader, false, false);
        myBuffer.appendHtmlConstant(_txtFieldHeader + "<span style=\"float:right;\"><i class=\"icon-caret-down\"></i></span>");
        myField.setHeader(myBuffer.toSafeHtml());
        myField.setWidth(mySharedWidth);
        _fieldCell = createFieldDefComboBoxCell(myManager.getStore(), _fieldList);
        myField.setCell(_fieldCell);

        CustomGridView<ColumnMappingDataItem> gridView = createGridView();

        // Create the grid so that it will be available for linking with individual cells
        createGrid(myManager, gridView);
        dataGrid.setSelectionModel(_selectionModel);

        addFieldEditor(myField);
        gridView.initializeMenu(2);

        return myManager;
    }

    @SuppressWarnings("rawtypes")
    private CustomGridView<ColumnMappingDataItem> createGridView() {

        CustomGridView<ColumnMappingDataItem> gridView = new CustomGridView<ColumnMappingDataItem>();

        gridView.addByNameHandler(new SelectionHandler() {
            @Override
            public void onSelection(SelectionEvent event) {
                List<ColumnMappingDataItem> items = dataGrid.getSelectionModel().getSelectedItems();

                for (ColumnMappingDataItem item : items) {
                    if ((null != item.getMappedField()) && (null != item.getMappingField())) {
                        String myName = item.getMappedField().getFieldName();
                        if (myName != null) {
                            for (FieldDef mappingField : _fieldList) {
                                if (myName.equals(mappingField.getFieldName())) {
                                    item.setMappingField(mappingField);
                                    item.setMapField(true);
                                    item.signalChange();
                                    break;
                                }
                            }
                        }
                    }
                }
                refreshGridKeepState();
            }
        });

        gridView.addExactMatchHandler(new SelectionHandler() {
            @Override
            public void onSelection(SelectionEvent event) {
                List<ColumnMappingDataItem> items = dataGrid.getSelectionModel().getSelectedItems();

                for (ColumnMappingDataItem item : items) {
                    if (item.getMappedField() != null) {
                        String name = item.getMappedField().getFieldName();
                        if (name != null) {
                            for (FieldDef mappingField : _fieldList) {
                                if (mappingField != null && mappingField.getFieldName() != null && name.equals(mappingField.getFieldName())) {
                                    if (item.getMappedField().getType() != null && mappingField.getValueType() != null && item.getMappedField().getType() == mappingField.getValueType()) {
                                        item.setMappingField(mappingField);
                                        item.setMapField(true);
                                        item.signalChange();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                refreshGridKeepState();
            }
        });

        gridView.addByPositionHandler(new SelectionHandler() {
            @Override
            public void onSelection(SelectionEvent event) {
                List<ColumnMappingDataItem> items = dataGrid.getSelectionModel().getSelectedItems();

                for (int ii = 0; ii < dataGrid.getStore().size(); ii++) {
                    ColumnMappingDataItem item = dataGrid.getStore().get(ii);

                    if (dataGrid.getSelectionModel().isSelected(item)) {

                        if (item.getMappedField() != null) {
                            int ordinal = item.getMappedField().getOrdinal();
                            if (_fieldList.size() > ordinal) {
                                item.setMappingField(_fieldList.get(ordinal));
                                item.setMapField(true);
                                item.signalChange();
                            }
                        }
                    }
                }

                refreshGridKeepState();
            }
        });

        gridView.addByTypeHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<ColumnMappingDataItem> items = dataGrid.getSelectionModel().getSelectedItems();

                Collections.sort(items, new Comparator<ColumnMappingDataItem>() {

                    @Override
                    public int compare(ColumnMappingDataItem o1, ColumnMappingDataItem o2) {
                        if (o1 == null) {
                            return 1;
                        } else if (o2 == null) {
                            return -1;
                        } else if (o1.getMappedField() == null) {
                            return 1;
                        } else if (o2.getMappedField() == null) {
                            return -1;
                        }

                        return o1.getMappedField().getOrdinal() - o2.getMappedField().getOrdinal();
                    }
                });
                List<String> used = new ArrayList<String>();

                for(ColumnMappingDataItem item: items){
                    if(item.getMappedField() != null){
                        int ordinal = item.getMappedField().getOrdinal();
                        CsiDataType type = item.getMappedField().getType();
                        for(int ii=ordinal; ii<_fieldList.size(); ii++){
                            if(_fieldList.get(ii) != null && _fieldList.get(ii).getValueType() == type && !used.contains(_fieldList.get(ii).getUuid())){
                                used.add(_fieldList.get(ii).getUuid());
                                item.setMappingField(_fieldList.get(ii));
                                item.setMapField(true);
                                item.signalChange();
                                break;
                            }
                        }
                    }
                }

                refreshGridKeepState();
            }});

        gridView.addByRelativePositionHandler(new SelectionHandler(){
            @Override
            public void onSelection(SelectionEvent event) {
                List<ColumnMappingDataItem> items = dataGrid.getSelectionModel().getSelectedItems();

                Collections.sort(items, new Comparator<ColumnMappingDataItem>(){

                    @Override
                    public int compare(ColumnMappingDataItem o1, ColumnMappingDataItem o2) {
                        if(o1 == null){
                            return 1;
                        } else if(o2 == null){
                            return -1;
                        } else if(o1.getMappedField() == null){
                            return 1;
                        } else if(o2.getMappedField() == null){
                            return -1;
                        }

                        return o1.getMappedField().getOrdinal() - o2.getMappedField().getOrdinal();
                    }});
                List<String> used = new ArrayList<String>();

                for(ColumnMappingDataItem item: items){
                    if(item.getMappedField() != null){

                        for(int ii=0; ii<_fieldList.size(); ii++){
                            if(_fieldList.get(ii) != null && !used.contains(_fieldList.get(ii).getUuid())){
                                used.add(_fieldList.get(ii).getUuid());
                                item.setMappingField(_fieldList.get(ii));
                                item.setMapField(true);
                                item.signalChange();
                                break;
                            }
                        }
                    }
                }

                refreshGridKeepState();
            }});

        return gridView;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createSelectionModel() {

        _selectionModel = new CsiCheckboxSingleSelectionModel(new CustomProvider());

        _selectionModel.addSelectionChangedHandler(new SelectionChangedHandler<ColumnMappingDataItem>(){

            @Override
            public void onSelectionChanged(SelectionChangedEvent<ColumnMappingDataItem> event) {

                List<ColumnMappingDataItem> list = event.getSelection();
                Scroll scroll = dataGrid.getView().getScroller().getScroll();

                for(ColumnMappingDataItem store: list){

                    if(!store.getMapField()){

                        store.setMapField(true);
                    }
                }

                for(ColumnMappingDataItem store: dataGrid.getStore().getAll()){
                    if(store.getMapField() && !list.contains(store)){
                        store.setMapField(false);
                    }
                }
                dataGrid.getView().refresh(false);
                dataGrid.getView().getScroller().setScrollTop(scroll.getScrollTop());
            }
        });
        _selectionModel.setShowSelectAll(true);
        _selectionModel.setSelectionMode(SelectionMode.MULTI);
    }

    private void refreshGridKeepState() {

        if (null != dataGrid) {

            Scroll scroll = dataGrid.getView().getScroller().getScroll();
            dataGrid.getView().refresh(false);
            dataGrid.getView().getScroller().setScrollTop(scroll.getScrollTop());
        }
    }

    private void finalizeHeader() {

        try {

            ColumnHeader<ColumnMappingDataItem> myHeader = getGrid().getView().getHeader();

            if((null != myHeader) && (null != myHeader.getHead(2))) {

                myHeader.refresh();
                myHeader.getHead(2).addStyleName("linkupCustomHeader");
                myHeader.getHead(2).getElement().addClassName("linkupCustomHeader");
                myHeader.getHead(2).getElement().getFirstChildElement().getFirstChildElement().addClassName("linkupCustomHeader");
                myHeader.getHead(2).getElement().getFirstChildElement().getFirstChildElement().getStyle().setDisplay(Style.Display.BLOCK);
            }

        } catch (Exception IGNORE_) {}
    }
}
