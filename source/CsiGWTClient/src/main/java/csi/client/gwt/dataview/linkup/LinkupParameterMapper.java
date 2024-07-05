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

import java.util.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.gxt.grid.CsiCheckboxSingleSelectionModel;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ParamMapEntry;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;


public class LinkupParameterMapper extends LinkupGridMapper<FieldToLabelMapStore> {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ParameterMapProperties extends PropertyAccess<FieldToLabelMapStore> {
        @Path("key")
        ModelKeyProvider<FieldToLabelMapStore> key();
        @Path("mapField")
        ValueProvider<FieldToLabelMapStore, Boolean> mapField();
        @Path("label")
        ValueProvider<FieldToLabelMapStore, String> label();
        @Path("mappingField")
        ValueProvider<FieldToLabelMapStore, FieldDef> mappingField();
    }
    
    public class CustomProvider implements ValueProvider<FieldToLabelMapStore, Boolean> {

        
        @Override
        public String getPath() {
            // TODO Auto-generated method stub
            return "mapField";
        }

        @Override
        public Boolean getValue(FieldToLabelMapStore object) {
            
            return object.getMapField();
        }

        @Override
        public void setValue(FieldToLabelMapStore object, Boolean value) {
            object.setMapField(value);
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private final String _txtIncludeCheckBoxHeader = _constants.linkupGridMapper_IncludeCheckBoxHeader();
    private final String _txtTemplateParameterHeader = _constants.linkupParameterMapper_TemplateParameterHeader();
    private final String _txtFieldHeader = _constants.linkupParameterMapper_FieldHeader();

    private FieldDefNameCell _dataFieldCell = null;

    private Map<String, QueryParameterDef> _paramMap = null;

    private CsiCheckboxSingleSelectionModel selectionModel = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    public LinkupParameterMapper(SelectionChangeResponder parentIn, AbstractDataViewPresenter _dvPresenter,
                                 int widthIn, int heightIn, String helpFileNameIn) {
        super(parentIn, _dvPresenter, widthIn, heightIn, helpFileNameIn);
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

        List<QueryParameterDef> myList = (null != templateIn) ? templateIn.getDataSetParameters() : null;

        resetGrid(templateIn);
        refreshDataFieldComboBox(_dataFieldCell.getComboBox());

        _paramMap = new HashMap<String, QueryParameterDef>();

        if ((null != myList) && (0 < myList.size())) {

            List<String> myParameterMissList = new ArrayList<String>();

            for (QueryParameterDef myItem : myList)  {

                String myKey = myItem.getLocalId();
                String myPrompt = myItem.getPrompt();

                _paramMap.put(myKey, myItem);

                if ((null == myPrompt) || (0 == myPrompt.length())) {

                    String myName = myItem.getName();

                    if ((null != myName) && (0 < myName.length())) {

                        myPrompt = myName;

                    } else {

                        myPrompt = "<unnamed parameter>"; //$NON-NLS-1$
                    }
                }
                ParamMapEntry myEntry = (null != parmListIn) ? findInList(parmListIn, myKey) : null;

                if (null != myEntry) {

                    FieldDef myDataField = _dvPresenter.getFieldByLocalId(myEntry.getFieldLocalId());

                    if (null != myDataField) {

                        dataGrid.getStore().add(new FieldToLabelMapStore(this, myKey, myPrompt, myDataField));
                    } else {

                        dataGrid.getStore().add(new FieldToLabelMapStore(this, myKey, myPrompt));
                    }
                } else {

                    dataGrid.getStore().add(new FieldToLabelMapStore(this, myKey, myPrompt));
                }
            }
            for (ParamMapEntry myEntry : parmListIn) {

                if (!_paramMap.containsKey(myEntry.getParamId())) {

                    myParameterMissList.add(myEntry.getParamName());
                }
            }
            if (0 < myParameterMissList.size()) {

                StringBuilder myBuffer = new StringBuilder();

                myBuffer.append(_constants.linkupParameterMapperDialogTitle());
                for (String myParameter : myParameterMissList) {

                    myBuffer.append('\n');
                    myBuffer.append(Format.value(myParameter));
                }
                Display.error(_constants.linkupDialog_TemplateParameterFailure(), myBuffer.toString());
            }
        }
    }

    //
    //
    //
    @Override
    public void initGridFields(DataViewDef templateIn) {

        List<QueryParameterDef> myList = (null != templateIn) ? templateIn.getDataSetParameters() : null;

        resetGrid(templateIn);
        refreshDataFieldComboBox(_dataFieldCell.getComboBox());

        _paramMap = new HashMap<String, QueryParameterDef>();

        if ((null != myList) && (0 < myList.size())) {

            for (QueryParameterDef myItem : myList)  {

                String myKey = myItem.getLocalId();
                String myPrompt = myItem.getPrompt();

                _paramMap.put(myKey, myItem);

                if ((null == myPrompt) || (0 == myPrompt.length())) {

                    String myName = myItem.getName();

                    if ((null != myName) && (0 < myName.length())) {

                        myPrompt = myName;

                    } else {

                        myPrompt = "<unnamed parameter>"; //$NON-NLS-1$
                    }
                }
                dataGrid.getStore().add(new FieldToLabelMapStore(this, myKey, myPrompt));
            }
        }
    }

    //
    //
    //
    @Override
    public boolean extractGridData(LinkupMapDef linkupMapIn) {

        try {

            linkupMapIn.linkupParms = extractAndTestGridData(linkupMapIn.linkupParms);

        } catch (Exception myException) {

            Display.error(_txtErrorTitle, myException);
            return false;
        }

        return true;
    }

    //
    //
    //
    @Override
    public List<ParamMapEntry> extractGridData(List<ParamMapEntry> listIn) {

        List<ParamMapEntry> myList = listIn;

        try {

            myList = extractAndTestGridData(myList);

        } catch (Exception myException) {

            // Ignore error.
        }

        return myList;
    }

    //
    //
    //
    @Override
    public boolean extractFinalGridData(List<ParamMapEntry> listIn) {

        try {

            listIn.clear();

            extractAndTestGridData(listIn);

        } catch (Exception myException) {

            Display.error(_txtErrorTitle, myException);
            return false;
        }

        return true;
    }

    //
    //
    //
    public void forceUpdate(FieldDef valueIn, Integer rowIn, Integer columnIn){

        if ((null != dataGrid) && (null != columnIn)) {

            FieldToLabelMapStore myRowData = getRowData(rowIn);

            if ((null != myRowData) && (2 == columnIn)) {

                myRowData.setMappingField(valueIn);
            }
        }
    }

    //
    //
    //
    public String getLabel(Integer rowIn, Integer columnIn){

        if ((null != dataGrid) && (null != columnIn)) {

            FieldToLabelMapStore myRowData = getRowData(rowIn);

            if ((null != myRowData) && (2 == columnIn)) {

                return myRowData.getLabel();
            }
        }

        return ""; //$NON-NLS-1$
     }

    //
    // Check the grid for missing elements or conflicts
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    @Override
    public String checkIntegrity() {

        return checkIntegrity(false);
    }

    //
    // Check the grid for missing elements or conflicts
    //  -- return the proper error message if there is a problem
    //     after changing focus to the offending grid cell if requested
    //  -- return <null> if everything is OK
    //
    @Override
    public String checkIntegrity(boolean changeFocusIn) {

        ListStore<FieldToLabelMapStore> myStore = dataGrid.getStore();

        applyComboBoxSelection(_dataFieldCell);

        for (int myCounter = 0; myStore.size() > myCounter; myCounter++) {

            FieldToLabelMapStore myItem = myStore.get(myCounter);

            if (myItem.getMapField()) {

                FieldDef myMappingField = myItem.getMappingField();

                if ((null == myMappingField) || (null == myMappingField.getLocalId())) {

                    QueryParameterDef myParameter = _paramMap.get(myItem.getParameterKey());
                    List<String> myBadParameters = new ArrayList<String>();

                    myBadParameters.add(myParameter.getName());

                    return buildErrorString(myBadParameters);
                }
            }
        }

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    @SuppressWarnings("unchecked")
    protected GridComponentManager<FieldToLabelMapStore> defineGridColumns(int widthIn) {

        int mySharedWidth = (widthIn - 22) / 2;
        ParameterMapProperties myProperties = GWT.create(ParameterMapProperties.class);

        final GridComponentManager<FieldToLabelMapStore> myManager
                = (GridComponentManager<FieldToLabelMapStore>)WebMain.injector.getGridFactory().create(myProperties.key());

        createSelectionModel();
        myManager.getColumnConfigList().add(selectionModel.getColumn());
        
        ColumnConfig<FieldToLabelMapStore, String> myParameterName = myManager.create(myProperties.label(),
                mySharedWidth, _txtTemplateParameterHeader, false, true);
        ColumnConfig<FieldToLabelMapStore, FieldDef> myParameterData = myManager.create(myProperties.mappingField(),
                mySharedWidth, _txtFieldHeader, false, true);

        myParameterName.setWidth(mySharedWidth);
        myParameterData.setWidth(mySharedWidth);

        // Create the grid so that it will be available for linking with individual cells
        createGrid(myManager);

        _dataFieldCell = addFieldDefComboBoxCell(myParameterData, getDataFields());

        myParameterName.setCell(new CsiTitleCell());
        myParameterData.setCell(_dataFieldCell);

        getGrid().setSelectionModel(selectionModel);
        return myManager;
    }

    //
    //
    //
    @Override
    protected Collection<FieldDef> refreshFieldList(DataViewDef metaDataIn) {
        return metaDataIn.getModelDef().getFieldListAccess().getAlphaOrderedFieldSet();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public List<ParamMapEntry> extractAndTestGridData(List<ParamMapEntry> listIn) throws CentrifugeException {

        ListStore<FieldToLabelMapStore> myStore = dataGrid.getStore();
        List<ParamMapEntry> myList = listIn;
        List<String> myBadParameters = new ArrayList<String>();

        if (null == myList) {

            myList = new ArrayList<ParamMapEntry>();
        }
        for (int myCounter = 0; myStore.size() > myCounter; myCounter++) {

            FieldToLabelMapStore myItem = myStore.get(myCounter);
            String myKey = myItem.getParameterKey();

            if (myItem.getMapField()) {

                FieldDef myMappingField = myItem.getMappingField();
                QueryParameterDef myParameter = _paramMap.get(myKey);

                if ((null != myMappingField) && (null != myMappingField.getLocalId())) {

                    ParamMapEntry myEntry = new ParamMapEntry();

                    myEntry.setParamId(myKey);
                    myEntry.setParamName(myParameter.getName());
                    myEntry.setFieldLocalId(myMappingField.getLocalId());
                    myList.add(myEntry);

                } else {

                    myBadParameters.add(myParameter.getName());
                }
            }
        }

        if (0 < myBadParameters.size()) {

            throw new CentrifugeException(buildErrorString(myBadParameters));
        }

        return myList;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createSelectionModel() {
        selectionModel = new CsiCheckboxSingleSelectionModel(new CustomProvider());
        selectionModel.addSelectionChangedHandler(new SelectionChangedHandler<FieldToLabelMapStore>(){

            @Override
            public void onSelectionChanged(SelectionChangedEvent<FieldToLabelMapStore> event) {
                List<FieldToLabelMapStore> list = event.getSelection();
                
                for(FieldToLabelMapStore store: list){
                    if(!store.getMapField()){
                        store.setMapField(true);
                    }
                }
                
                for(FieldToLabelMapStore store: getGrid().getStore().getAll()){
                    if(store.getMapField() && !list.contains(store)){
                        store.setMapField(false);
                    }
                }
                getGrid().getView().refresh(false);
            }
        });
        //selectionModel = new CsiCheckboxSelectionModel(new MapValueProvider<FieldToFieldMapStore>());
        selectionModel.setShowSelectAll(true);
        selectionModel.setSelectionMode(SelectionMode.MULTI);
    }

    private List<String> locateBadEntries() {

        ListStore<FieldToLabelMapStore> myStore = dataGrid.getStore();
        List<String> myBadParameters = new ArrayList<String>();

        for (int myCounter = 0; myStore.size() > myCounter; myCounter++) {

            FieldToLabelMapStore myItem = myStore.get(myCounter);

            if (myItem.getMapField()) {

                FieldDef myMappingField = myItem.getMappingField();

                if ((null == myMappingField) || (null == myMappingField.getLocalId())) {

                    QueryParameterDef myParameter = _paramMap.get(myItem.getParameterKey());

                    myBadParameters.add(myParameter.getName());
                }
            }
        }

        return myBadParameters;
    }

    private String buildErrorString(List<String> badParametersIn) {

        String myErrorString = null;

        if ((null != badParametersIn) && (0 < badParametersIn.size())) {

            StringBuilder myBuffer = new StringBuilder(i18n.linkupParameterMapperInstructions()); //$NON-NLS-1$

            myBuffer.append(" "); //$NON-NLS-1$
            myBuffer.append(badParametersIn.get(0));

            for (int i = 1; badParametersIn.size() > i; i++) {

                myBuffer.append(", "); //$NON-NLS-1$
                myBuffer.append(badParametersIn.get(i));
            }
            myBuffer.append("."); //$NON-NLS-1$

            myErrorString = myBuffer.toString();
        }
        return myErrorString;
    }

    //
    //
    //
    private ParamMapEntry findInList(List<ParamMapEntry> listIn, String keyIn) {

        ParamMapEntry myEntry = null;

        if ((null != listIn) && (0 < listIn.size())) {

            int myIndex;

            for (myIndex = 0; listIn.size() > myIndex; myIndex++) {

                ParamMapEntry myItem = listIn.get(myIndex);

                if (keyIn.equals(myItem.getParamId())) {

                    myEntry = myItem;
                }
            }


        }
        return myEntry;
    }
}
