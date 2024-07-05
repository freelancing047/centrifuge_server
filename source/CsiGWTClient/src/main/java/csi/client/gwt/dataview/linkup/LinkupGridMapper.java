/** 
 *  Copyright (c) 2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.dataview.linkup;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.event.*;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.ApplicationResources;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.cells.GridCellAssist;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.grid.DataStoreColumnAccess;
import csi.client.gwt.widget.gxt.grid.FixedSizeGrid;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;


public abstract class LinkupGridMapper<T extends DataStoreColumnAccess> implements GridCellAssist<FieldDef> {
	

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public class MyValueProvider<S> implements ValueProvider<S, String> {

        /**
         * Returns the property value of the given object.
         * 
         * @param object the target object
         * @return the property value
         */
        public String getValue(S object) {
            
            return ""; //$NON-NLS-1$
        }

        /**
         * Sets the value of the given object
         * 
         * @param object
         * @param value
         */
        public void setValue(S object, String value) {
            
        }

        /**
         * Returns the path that this ValueProvider makes available, from the object,
         * to the value.
         * 
         * @return the path from the object to the value
         */
        public String getPath() {
            
            return ""; //$NON-NLS-1$
        }
      }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    
    protected ApplicationResources resources = GWT.create(ApplicationResources.class);
    
    protected final int _helpIconWidth = 22;

    protected final String _txtIncludeCheckBoxHeader = _constants.linkupGridMapper_IncludeCheckBoxHeader();
    protected final String _txtTemplateFieldHeader = _constants.linkupGridMapper_TemplateFieldHeader();
    protected final String _txtTemplateFieldDataHeader = _constants.linkupGridMapper_TemplateFieldDataHeader();
    protected final String _txtSelectedDataViewFieldHeader = _constants.linkupGridMapper_SelectedDataViewFieldHeader();
    protected final String _txtDataViewFieldHeader = _constants.linkupGridMapper_DataViewFieldHeader();
    protected final String _txtErrorTitle = _constants.linkupGridMapper_ErrorTitle();
    protected final String _txtEmptyParameterError = _constants.linkupGridMapper_EmptyParameterError();
    protected final String _txtBadFieldError = _constants.linkupGridMapper_BadFieldError();

    protected AbstractDataViewPresenter _dvPresenter = null;
    protected SelectionChangeResponder _parent = null;

    protected FixedSizeGrid<T> dataGrid;
    protected T _listStore = null;

    private DataViewDef _template = null;
    private FieldListAccess _templateModel = null;
    private Collection<FieldDef> _allDataViewFields;
    private Map<String, FieldDef> _baseFieldNameMap = new HashMap<String, FieldDef>();
    private Map<String, FieldDef> _fieldNameMap = new HashMap<String, FieldDef>();
    private Map<String, FieldDef> _fieldIdMap = new HashMap<String, FieldDef>();
    private GridInlineEditing<T> _gridEditor = null;
    HandlerRegistration _listViewClickRemoval = null;
    HandlerRegistration _listViewHideRemoval = null;
    private FieldDef _selectedField = null;
    private int _clickCount = 0;
    private int _activeRow = -1;
    private boolean _isBlocked = false;

//    private FieldDefComboBox[] _dropDown;


    private String _helpKey = null;

    private int _width;
    private int _height;
    private int _infoColumn = -1;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected abstract GridComponentManager<T> defineGridColumns(int widthIn);

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle help button button click
    //
//    private HeaderClickHandler handleHeaderClick
//    = new HeaderClickHandler() {
//        @Override
//        public void onHeaderClick(HeaderClickEvent eventIn) {
//            
//            int myColumn = eventIn.getColumnIndex();
//            
//            // If this is the information button process the request
//            if (_infoColumn == myColumn) {
//                
//                HelpWindow.display(_helpKey);
//                
//            } else {
//                
//                //
//                // Pass event on to specialized handler
//                //
//                handleHeaderClick(eventIn);
//            }
//        }
//    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    public LinkupGridMapper(SelectionChangeResponder parentIn, AbstractDataViewPresenter dvPresenterIn, int widthIn, int heightIn, String helpFileNameIn) {
        _parent = parentIn;
        _dvPresenter = dvPresenterIn;
        _width = widthIn;
        _height = heightIn;
        _helpKey = helpFileNameIn;

//        _dropDown = new FieldDefComboBox[columnCountIn];

        resetFieldList();
    }
    
    //
    //
    //
    public LinkupGridMapper(AbstractDataViewPresenter dvPresenterIn, int widthIn, int heightIn, String helpFileNameIn) {
        _dvPresenter = dvPresenterIn;
        _width = widthIn;
        _height = heightIn;
        _helpKey = helpFileNameIn;

//        _dropDown = new FieldDefComboBox[columnCountIn];

        resetFieldList();
    }

    //
    //
    //
    public void registerTemplate(DataViewDef templateIn) {

        if (null != templateIn) {

            _template = templateIn;
            _templateModel = templateIn.getModelDef().getFieldListAccess();
        }
    }

    //
    //
    //
    public Grid<T> getGrid() {

        if (null == dataGrid) {

            final GridComponentManager<T> myManager = defineGridColumns(_width - _helpIconWidth);

            if ((null == dataGrid) && (null != myManager)) {
                
               _infoColumn = myManager.getColumnConfigList().size();

                dataGrid = new FixedSizeGrid<T>(myManager.getStore(), new ColumnModel<T>(myManager.getColumnConfigList()));
                dataGrid.setWidth(_width);
                dataGrid.setHeight(_height);
                dataGrid.getStore().setAutoCommit(true);
                dataGrid.getView().setColumnLines(true);
                dataGrid.getView().setStripeRows(true);
                dataGrid.setBorders(true);
            }
        }
        return dataGrid;
    }

    public ListStore<T> getListStore() {

        return getGrid().getStore();
    }

    //
    //
    //
    public void resetGrid(DataViewDef templateIn) {

        registerTemplate(templateIn);

        if ((null != dataGrid) && (null != dataGrid.getStore())) {

            dataGrid.getStore().clear();
        }
    }

    //
    //
    //
    public void resetGrid() {

        registerTemplate(null);

        if ((null != dataGrid) && (null != dataGrid.getStore())) {

            dataGrid.getStore().clear();
        }
    }

    //
    //
    //
    public void initGridFields(LinkupMapDef linkupIn, DataViewDef templateIn, DataViewDef dvDefIn) {

        resetGrid(templateIn);
    }

    //
    //
    //
    public void initGridFields(LinkupMapDef linkupIn, DataViewDef templateIn) {

        resetGrid(templateIn);
    }

    //
    //
    //
    public void initGridFields(DataViewDef templateIn, List<ParamMapEntry> parmListIn) {

        resetGrid(templateIn);
    }

    //
    //
    //
    public void initGridFields(DataViewDef templateIn, DataViewDef dvDefIn) {

        resetGrid(templateIn);
    }

    //
    //
    //
    public void initGridFields(DataViewDef templateIn) {

        resetGrid(templateIn);
    }

    //
    //
    //
    public boolean extractGridData(LinkupMapDef linkupMapIn) {
        
        return false;
    }
    
    //
    //
    //
    public List<ParamMapEntry> extractGridData(List<ParamMapEntry> listIn) {
        
        return null;
    }
    
    //
    //
    //
    public boolean extractFinalGridData(List<ParamMapEntry> listIn) {
        
        return extractFinalGridData(listIn, 0);
    }
    
    //
    //
    //
    public boolean extractFinalGridData(List<ParamMapEntry> listIn, int baseCountIn) {
        
        return false;
    }

    //
    //
    //
    public void forceRedraw(Integer rowIn, Integer columnIn) {
        T myRowData = getRowData(rowIn);
        
        if (null != myRowData){
            getListStore().update(myRowData);
        }
    }

    //
    //
    //
    public void forceRedraw(Integer rowIn) {
        T myRowData = getRowData(rowIn);
        
        if (null != myRowData){
            getListStore().update(myRowData);
        }
    }

    //
    //
    //
    public String getStyle(Integer rowIn, Integer columnIn) {
        return null;
    }

    //
    //
    //
    public void forceUpdate(FieldDef valueIn, Integer rowIn, Integer columnIn){
        T myRowData = getRowData(rowIn);
        
        if (null != myRowData){
            myRowData.setColumnData(columnIn, valueIn);
        }
    }

    //
    //
    //
    public void reportTextChange(String valueIn, Integer rowIn, Integer columnIn) {
        if ((null == valueIn) || (0 == valueIn.length())) {
            forceUpdate(null, rowIn, columnIn);
        }
    }

    //
    //
    //
    public void resetFieldList() {

        // Set up structures for accessing valid dataview field definitions
        _allDataViewFields = refreshFieldList(_dvPresenter.getDataView().getMeta());
        _baseFieldNameMap.clear();
        _fieldNameMap.clear();
        if(null != _allDataViewFields) {

            for (FieldDef myField : _allDataViewFields) {
                _baseFieldNameMap.put(myField.getFieldName(), myField);
                _fieldNameMap.put(myField.getFieldName(), myField);
                _fieldIdMap.put(myField.getLocalId(), myField);
            }
        }
    }

    //
    //
    //
    public void addField(FieldDef fieldIn) {

        if (!_fieldNameMap.containsKey(fieldIn.getFieldName())) {

            _allDataViewFields.add(fieldIn);
            _fieldNameMap.put(fieldIn.getFieldName(), fieldIn);
            _fieldIdMap.put(fieldIn.getLocalId(), fieldIn);
        }
    }

    //
    //
    //
    public void removeField(FieldDef fieldIn) {

        if (!_baseFieldNameMap.containsKey(fieldIn.getFieldName())) {

            _allDataViewFields.remove(fieldIn);
            _fieldNameMap.remove(fieldIn.getFieldName());
            _fieldIdMap.remove(fieldIn.getLocalId());
        }
    }

    //
    // Check the grid for missing elements or conflicts focusing on a specific row
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    public String checkIntegrity(Object rowDataIn, boolean changeFocusIn) {
        return null;
    }

    //
    // Check the grid for missing elements or conflicts focusing on a specific row
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    public String checkIntegrity(Object rowDataIn) {
        return null;
    }

    //
    // Check the grid for missing elements or conflicts
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    public String checkIntegrity(boolean changeFocusIn) {
        return null;
    }

    //
    // Check the grid for missing elements or conflicts
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    public String checkIntegrity() {
        return null;
    }

    public boolean isBlocked() {

        return _isBlocked;
    }

    public void selectionChange(Object dataRowIn) {
        
        if (null != _parent) {
            _parent.selectionChange(dataRowIn);
        }
    }
    
    public void rowComplete(Object dataRowIn) {
        
        if (null != _parent) {
            _parent.rowComplete(dataRowIn);
        }
    }
    
    public int rowCount() {
        
        return getListStore().size();
    }

    public Map<String, FieldDef>  getFieldNameMap() {

        return _fieldNameMap;
    }

    public List<FieldDef> getTemplateFields() {

        return (null != _templateModel) ? _templateModel.getOrderedColumnFieldDefs() : null;
    }

    public Collection<FieldDef> getDataFields() {

        return _allDataViewFields;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void refreshFieldDefComboBox(FieldDefComboBox fieldDefComboBoxIn, Collection<FieldDef> fieldListIn) {
        fieldDefComboBoxIn.getStore().clear();
        if ((null != fieldListIn) && (0 < fieldListIn.size())) {
            fieldDefComboBoxIn.getStore().addAll(fieldListIn);
        }
    }

    //
    //
    //
    protected void refreshDataFieldComboBox(FieldDefComboBox fieldDefComboBoxIn) {
        refreshFieldDefComboBox(fieldDefComboBoxIn, _allDataViewFields);
    }

    //
    //
    //
    protected void refreshTemplateFieldComboBox(FieldDefComboBox fieldDefComboBoxIn, DataViewDef templateIn) {
        registerTemplate(templateIn);
        refreshTemplateFieldComboBox(fieldDefComboBoxIn);
    }

    //
    //
    //
    protected void refreshTemplateFieldComboBox(FieldDefComboBox fieldDefComboBoxIn) {
        fieldDefComboBoxIn.getStore().clear();
        if (null != _templateModel) {
            refreshFieldDefComboBox(fieldDefComboBoxIn, _templateModel.getAlphaOrderedColumnFieldDefs());
        } else {
            fieldDefComboBoxIn.getStore().clear();
        }
    }

    //
    //
    //
    protected Collection<FieldDef> refreshFieldList(DataViewDef metaDataIn) {
        return metaDataIn.getModelDef().getFieldListAccess().getAlphaOrderedReferenceFieldDefs();
    }

    //
    //
    //
    protected FieldDef findDataViewFieldDef(String localIdIn, String nameIn) {

        FieldDef myResult = _fieldIdMap.get(localIdIn);

        if (null == myResult) {

            myResult = _fieldNameMap.get(nameIn);
        }
        return myResult;
    }

    //
    //
    //
    protected FieldDef findTemplateFieldDef(String localIdIn, String nameIn) {

        FieldDef myResult = _templateModel.getFieldDefByLocalId(localIdIn);

        if (null == myResult) {

            myResult = _templateModel.getFieldDefByName(nameIn);
        }
        return myResult;
    }

    //
    //
    //
    protected T getRowData(Integer rowIn) {
        
        int myStoreSize = getGrid().getStore().size();
        
        if ((null != rowIn) && (0 <= rowIn) && (myStoreSize > rowIn)) {
            
            return getListStore().get(rowIn);
        }
        return null;
    }

    //
    //
    //
    protected void handleHeaderClick(HeaderClickEvent eventIn) {
        
    }

    protected FieldDefNameCell addFieldDefComboBoxCell(ColumnConfig columnIn, Collection<FieldDef> fieldListIn) {

        FieldDefComboBox myFieldComboBox = new FieldDefComboBox();
        refreshFieldDefComboBox(myFieldComboBox, fieldListIn);

        return addFieldEditor(columnIn, myFieldComboBox);
    }

    //
    //
    //
    protected void createGrid(GridComponentManager<T> managerIn) {

        if ((null == dataGrid) && (null != managerIn)) {

            _infoColumn = managerIn.getColumnConfigList().size();

            dataGrid = new FixedSizeGrid<T>(managerIn.getStore(), new ColumnModel<T>(managerIn.getColumnConfigList()));
            dataGrid.setWidth(_width);
            dataGrid.setHeight(_height);
            getListStore().setAutoCommit(true);
            dataGrid.getView().setColumnLines(true);
            dataGrid.getView().setStripeRows(true);
            dataGrid.setBorders(true);
        }
    }
    
    protected void createGrid(GridComponentManager<T> managerIn, GridView<T> gridView) {

        if ((null == dataGrid) && (null != managerIn)) {

            _infoColumn = managerIn.getColumnConfigList().size();

            dataGrid = new FixedSizeGrid<T>(managerIn.getStore(), new ColumnModel<T>(managerIn.getColumnConfigList()), gridView);
            dataGrid.setWidth(_width);
            dataGrid.setHeight(_height);
            getListStore().setAutoCommit(true);
            dataGrid.getView().setColumnLines(true);
            dataGrid.getView().setStripeRows(true);
            dataGrid.setBorders(true);
        }
    }

    //
    //
    //
    protected void applyComboBoxSelection(FieldDefNameCell fieldDefCellIn) {

        if ((null != fieldDefCellIn) && (0 <= _activeRow)) {

            FieldDefComboBox myComboBox = fieldDefCellIn.getComboBox();

            if (1 == _clickCount) {

                if (myComboBox.isExpanded()) {

                    _clickCount = 2;
                }
            }

            if (2 == _clickCount) {

                if (!myComboBox.isExpanded()) {

                    ListView<FieldDef, ?> myListView = myComboBox.getListView();

                    if (null != myListView) {

                        try {

                            FieldDef mySelection = myListView.getSelectionModel().getSelectedItem();

                            if (null == mySelection) {

                                myComboBox.setValue(mySelection);
                            }

                        } catch (Exception myException) {

                            Display.error(myException.getMessage());
                        }
                    }
                    _clickCount = 0;
                    _gridEditor.completeEditing();
                }
            }
        }
    }

    private GridInlineEditing<T> createEditor() {
        GridInlineEditing<T> myEditor = new GridInlineEditing<T>(getGrid());

        // Add handler to identify row in edit mode
        myEditor.addStartEditHandler(new StartEditEvent.StartEditHandler<T>() {
            @Override
            public void onStartEdit(StartEditEvent<T> eventIn) {

                _activeRow = _gridEditor.getActiveCell().getRow();
                _clickCount = 0;
                _isBlocked = true;
                _selectedField = null;
            }
        });
        myEditor.addCancelEditHandler(new CancelEditEvent.CancelEditHandler<T>() {
            @Override
            public void onCancelEdit(CancelEditEvent<T> tCancelEditEvent) {

                _activeRow = -1;
                _clickCount = 0;
                Scroll scroll = getGrid().getView().getScroller().getScroll();
                getGrid().getView().refresh(false);
                getGrid().getView().getScroller().setScrollTop(scroll.getScrollTop());
                _isBlocked = false;
            }
        });
        // Add handler to remove row from edit mode
        myEditor.addCompleteEditHandler(new CompleteEditEvent.CompleteEditHandler<T>() {
            @Override
            public void onCompleteEdit(CompleteEditEvent<T> eventIn) {

                _activeRow = -1;
                _clickCount = 0;
                Scroll scroll = getGrid().getView().getScroller().getScroll();
                getGrid().getView().refresh(false);
                getGrid().getView().getScroller().setScrollTop(scroll.getScrollTop());
                _isBlocked = false;
            }
        });
        // Add handler to block edit when row has not been selected.
        myEditor.addBeforeStartEditHandler(new BeforeStartEditEvent.BeforeStartEditHandler<T>() {
            @Override
            public void onBeforeStartEdit(BeforeStartEditEvent<T> eventIn) {

                ListStore<T> myListStore = getListStore();

                if (null != myListStore) {

                    Grid.GridCell myCell = eventIn.getEditCell();

                    if (null != myCell) {

                        int myRow = myCell.getRow();

                        if ((0 <= myRow) && (myListStore.size() > myRow)) {

                            T myRowData = getListStore().get(myRow);

                            if (!myRowData.isSelected()) {

                                eventIn.setCancelled(true);
                            }
                        }
                    }
                }
                _clickCount = 0;
                _selectedField = null;
            }
        });
        return myEditor;
    }

    private FieldDefNameCell addFieldEditor(ColumnConfig columnIn, FieldDefComboBox comboBoxIn) {

        if (null != comboBoxIn) {

            comboBoxIn.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    _clickCount = 1;
                }
            }, ClickEvent.getType() );

            if (null == _gridEditor) {

                _gridEditor = createEditor();
            }
            _gridEditor.addEditor(columnIn, comboBoxIn);
            return new FieldDefNameCell<T>(getListStore(), comboBoxIn, i18n.linkupGridMapperFieldInstructions()); //$NON-NLS-1$
        }
        return new FieldDefNameCell();
    }
}
