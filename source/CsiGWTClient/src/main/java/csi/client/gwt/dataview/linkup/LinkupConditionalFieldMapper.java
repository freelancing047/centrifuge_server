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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.HeaderClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.widget.boot.BooleanButtonCell;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.cells.ComboBoxEditCell;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;


public class LinkupConditionalFieldMapper extends LinkupGridMapper<LinkupConditionalFieldMapStore> {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ConditionalFieldMapProperties extends PropertyAccess<LinkupConditionalFieldMapStore> {
        ModelKeyProvider<LinkupConditionalFieldMapStore> key();
        LabelProvider<LinkupConditionalFieldMapStore> templateFieldName();
        ValueProvider<LinkupConditionalFieldMapStore, FieldDef> templateField();
        ValueProvider<LinkupConditionalFieldMapStore, FieldDef> mappedField();
        ValueProvider<LinkupConditionalFieldMapStore, Boolean> active();
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private final String _txtRequiredTemplateFieldLabel = _constants.linkupConditionalFieldMapper_RequiredTemplateFieldLabel();
    private final String _txtRequiredMappedFieldLabel = _constants.linkupConditionalFieldMapper_RequiredMappedFieldLabel();
    private final String _txtButtonColumnHeader = _constants.linkupConditionalFieldMapper_ButtonColumnHeader();

    private FieldDefComboBox dataviewFieldComboBox;
    private FieldDefComboBox templateFieldComboBox;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    // Handle row delete button click
    //
    private SelectHandler handleRowDeleteRequest
    = new SelectHandler() {
        @Override
        public void onSelect(SelectEvent eventIn) {
            
            int myIndex = eventIn.getContext().getIndex();
            LinkupConditionalFieldMapStore myRowData = dataGrid.getStore().get(myIndex);
            myRowData.setActive(false);
            dataGrid.getStore().remove(myIndex);
        }
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    public LinkupConditionalFieldMapper(SelectionChangeResponder parentIn, AbstractDataViewPresenter _dvPresenter, int widthIn, int heightIn, String helpFileNameIn) {
        super(parentIn, _dvPresenter, widthIn, heightIn, helpFileNameIn);
    }
    
    //
    //
    //
    public LinkupConditionalFieldMapper(AbstractDataViewPresenter _dvPresenter, int widthIn, int heightIn, String helpFileNameIn) {
        super(_dvPresenter, widthIn, heightIn, helpFileNameIn);
    }
    
    //
    //
    //
    @Override
    public void initGridFields(LinkupMapDef linkupIn, DataViewDef templateIn) {
    
        initGridFields(templateIn, linkupIn.getLinkupParms());
    }
    
    //
    //
    //
    @Override
    public void initGridFields(DataViewDef templateIn, List<ParamMapEntry> parmListIn) {

        FieldListAccess myModel = templateIn.getModelDef().getFieldListAccess();
        resetGrid(templateIn);
            
        refreshTemplateFieldComboBox(templateFieldComboBox, templateIn);
       
        // Initialize grid with existing conditional mappings
        if ((null != parmListIn) && (0 < parmListIn.size())) {
            
            for (ParamMapEntry myEntry : parmListIn) {
                
                FieldDef myTemplateField = myModel.getFieldDefByLocalId(myEntry.getTargetFieldLocalId());
                FieldDef myMappedField = _dvPresenter.getFieldByLocalId(myEntry.getFieldLocalId());
                String myParameterName = myEntry.getParamName();
                
                // Add a row if either field is defined and this is not a parameter reference
                if (((null != myTemplateField) || (null != myMappedField))
                        && ((null == myParameterName) || (0 == myParameterName.length()))) {
                    
                    dataGrid.getStore().add(new LinkupConditionalFieldMapStore(this, myTemplateField, myMappedField));
                }
            }
        }
    }
        
    //
    //
    //
    @Override
    public void initGridFields(DataViewDef templateIn) {

        resetGrid(templateIn);
            
        refreshTemplateFieldComboBox(templateFieldComboBox, templateIn);
    }
    
    //
    //
    //
    @Override
    public boolean extractGridData(LinkupMapDef linkupMapIn) {
        
        boolean mySuccess = false;
        
        try {
            linkupMapIn.linkupParms = extractGridData(linkupMapIn.linkupParms);
            mySuccess = true;
        } catch (Exception myException) {
            
            Dialog.showException(_txtErrorTitle, myException);
        }
        
        return mySuccess;
    }
    
    //
    //
    //
    @Override
    public List<ParamMapEntry> extractGridData(List<ParamMapEntry> listIn) {
        
        ListStore<LinkupConditionalFieldMapStore> myStore = dataGrid.getStore();
        List<ParamMapEntry> myList = listIn;

        if (null == myList) {
            
            myList = new ArrayList<ParamMapEntry>();
        }
        for (int i = 0; myStore.size() > i; i++) {
            
            LinkupConditionalFieldMapStore myItem = myStore.get(i);
            FieldDef myTemplateField = myItem.getTemplateField();
            FieldDef myMappedField = myItem.getMappedField();
            
            if ((null != myTemplateField) || (null != myMappedField)){
                
                ParamMapEntry myEntry = new ParamMapEntry();

                if (null != myTemplateField) {

                    myEntry.setTargetFieldLocalId(myTemplateField.getLocalId());
                }
                if (null != myMappedField) {

                    myEntry.setFieldLocalId(myMappedField.getLocalId());
                }
                myList.add(myEntry);
            }
        }
        
        return myList;
    }
    
    //
    //
    //
    @Override
    public boolean extractFinalGridData(List<ParamMapEntry> listIn, int baseCountIn) {
        
        boolean mySuccess = true;
        
        try {
            ListStore<LinkupConditionalFieldMapStore> myStore = dataGrid.getStore();
            int myCounter = 0;
            
            listIn.clear();

            for (myCounter = 0; myStore.size() > myCounter; myCounter++) {
                
                LinkupConditionalFieldMapStore myItem = myStore.get(myCounter);
                FieldDef myTemplateField = myItem.getTemplateField();
                FieldDef myMappedField = myItem.getMappedField();
                
                if ((null == myTemplateField) || (null == myMappedField)) {
                    
                    break;
                }                    
                ParamMapEntry myEntry = new ParamMapEntry();
                
                myEntry.setFieldLocalId(myMappedField.getLocalId());
                myEntry.setTargetFieldLocalId(myTemplateField.getLocalId());
                myEntry.setParamName(myTemplateField.getFieldName());
                myEntry.setParamOrdinal(myCounter + baseCountIn);
                listIn.add(myEntry);
            }
            if (myStore.size() == myCounter) {
                
                mySuccess = true;
                
            } else {
                
                (new ErrorDialog(_txtErrorTitle, _txtBadFieldError)).show();
            }
        } catch (Exception myException) {
            
            Dialog.showException(_txtErrorTitle, myException);
        }
        
        return mySuccess;
    }
    
    //
    //
    //
    @Override
    public void forceUpdate(FieldDef objectIn, Integer rowIn, Integer columnIn) {
        
        LinkupConditionalFieldMapStore myRowData = getRowData(rowIn);
        
        if (null != myRowData) {
            
            myRowData.setColumn(columnIn, objectIn);
            dataGrid.getStore().update(myRowData);
        }
    }
    
    //
    // Check the grid for missing elements or conflicts focusing on a specific row
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    @Override
    public String checkIntegrity(Object rowDataIn, boolean changeFocusIn) {
        
        String myError = null;
        
        if (rowDataIn instanceof LinkupConditionalFieldMapStore) {
            
            myError = checkRowIntegrity((LinkupConditionalFieldMapStore)rowDataIn, changeFocusIn);
        }
        
        return myError;
    }
    
    //
    // Check the grid for missing elements or conflicts
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    @Override
    public String checkIntegrity(boolean changeFocusIn) {
        
        String myError = null;
        ListStore<LinkupConditionalFieldMapStore> myDataStore = dataGrid.getStore();
        
        for (int i = 0; myDataStore.size() > i; i++) {
            
            LinkupConditionalFieldMapStore myRowData = myDataStore.get(i);
            
            myError = checkRowIntegrity(myRowData, changeFocusIn);
            
            if (null != myError) {
                
                break;
            }
        }
        
        return myError;
    }

    //
    //
    //
    @Override
    public void reportTextChange(String valueIn, Integer rowIn, Integer columnIn) {
        
        if (null != dataGrid) {
            
            ListStore<LinkupConditionalFieldMapStore> myDataStore = dataGrid.getStore();
            
            if ((null != myDataStore) && (myDataStore.size() > rowIn)) {
                myDataStore.get(rowIn).exitEditMode(columnIn);
                forceRedraw(rowIn, columnIn);
            }
        }
    }
    
    //
    //
    //
    @Override
    public int rowCount() {
        
        int myCount = 0;
        ListStore<LinkupConditionalFieldMapStore> myListStore = dataGrid.getStore();
        
        for (int i = 0; myListStore.size() > i; i++) {
            
            LinkupConditionalFieldMapStore myDataRow = myListStore.get(i);
            
            if (myDataRow.getActive()) {
                
                myCount++;
            }
        }
        return myCount;
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected GridComponentManager<LinkupConditionalFieldMapStore> defineGridColumns(int widthIn) {

        int mySharedWidth = (widthIn - 60) / 2;
        int mySingleWidth = widthIn - (mySharedWidth * 2);
        ConditionalFieldMapProperties myProperties = GWT.create(ConditionalFieldMapProperties.class);
        
        final GridComponentManager<LinkupConditionalFieldMapStore> myManager = (GridComponentManager<LinkupConditionalFieldMapStore>)WebMain.injector.getGridFactory().create(myProperties.key());

        ColumnConfig<LinkupConditionalFieldMapStore, FieldDef> myTemplateField = myManager.create(myProperties.templateField(),
                mySharedWidth, _txtTemplateFieldHeader, false, true);
        ColumnConfig<LinkupConditionalFieldMapStore, FieldDef> myMappedField = myManager.create(myProperties.mappedField(),
                mySharedWidth, _txtSelectedDataViewFieldHeader, false, true);
        ColumnConfig<LinkupConditionalFieldMapStore, Boolean> myDeleteButton = myManager.create(myProperties.active(),
                mySingleWidth, _txtButtonColumnHeader, false, true);
        
        BooleanButtonCell myButtonCell = new BooleanButtonCell(resources.deleteIcon(), resources.deleteIcon()); // resources.invisiblePixel());
        myButtonCell.addSelectHandler(handleRowDeleteRequest);
        
        myTemplateField.setWidth(mySharedWidth);
        myMappedField.setWidth(mySharedWidth);
        myDeleteButton.setWidth(mySingleWidth);

        templateFieldComboBox = new FieldDefComboBox();
        refreshTemplateFieldComboBox(templateFieldComboBox);
        ComboBoxEditCell<FieldDef> myTemplateFieldCell = (ComboBoxEditCell<FieldDef>)templateFieldComboBox.getCell();
        //ComboBoxCell<FieldDef> myTemplateFieldCell = (ComboBoxCell<FieldDef>)templateFieldComboBox.getCell();
        myTemplateField.setCell(myTemplateFieldCell);
        
        dataviewFieldComboBox = new FieldDefComboBox();
        refreshDataFieldComboBox(dataviewFieldComboBox);
        ComboBoxEditCell<FieldDef> myDataFieldCell = (ComboBoxEditCell<FieldDef>)dataviewFieldComboBox.getCell();
        //ComboBoxCell<FieldDef> myDataFieldCell = (ComboBoxCell<FieldDef>)dataviewFieldComboBox.getCell();
        myMappedField.setCell(myDataFieldCell);
        
        myDeleteButton.setCell(myButtonCell);
       
        return myManager;
    }
    
    //
    // Handle row delete button click
    //
    @Override
    protected void handleHeaderClick(HeaderClickEvent eventIn) {
        
        int myColumn = eventIn.getColumnIndex();
        
        if (2 == myColumn) {
            
            addRow();
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    public void addRow() {
    
        LinkupConditionalFieldMapStore myRowData = new LinkupConditionalFieldMapStore(this);
        dataGrid.getStore().add(myRowData);
        myRowData.setActive(true);
    }

    private String checkRowIntegrity(LinkupConditionalFieldMapStore rowDataIn, boolean changeFocusIn) {
        
        String myError = null;
        
        if (rowDataIn.getActive()) {
            
            FieldDef myTemplateField = rowDataIn.getTemplateField();
            
            if (null == myTemplateField) {
                
                myError = _txtRequiredTemplateFieldLabel;
            }
            
            if (null == rowDataIn.getMappedField()) {
                myError = _txtRequiredMappedFieldLabel;
            }
        }
        
        return myError;
    }
}
