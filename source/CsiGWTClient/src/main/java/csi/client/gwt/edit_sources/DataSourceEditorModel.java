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
package csi.client.gwt.edit_sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.client.gwt.dataview.DataSourceUtilities;
import csi.client.gwt.edit_sources.dialogs.common.ParameterPresenter;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.edit_sources.right_panel.ColumnMappingSet;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.CsiMap;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.interfaces.DataDefinition;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.FunctionType;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.OrphanColumn;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.functions.ScriptFunction;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.ValuePair;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataSourceEditorModel {

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private DataSourceEditorPresenter _dataSourcePresenter;
    
    // This is used to store the field-defs that existed when the dataview was opened. When modifying columnDefs,
    // we check if the resulting field-defs being affected (on columndef de-selection) are those that already existed
    // on load. If yes, we need to do a comprehensive check on the server-side for references to those field-defs by
    // calling the server.
    private DataDefinition _meta;
    private List<DataSourceDef> _dataSources;
    private List<QueryParameterDef> _dataSetParameters;
    private ParameterPresenter _parameterPresenter = null;

    private Map<String, SqlTableDef> _customResetMap;       // Map of original table configurations by display object id
    private Map<String, FieldDef> _initialFields;           // Initial column-referencing fields by field uuid
    private List<FieldDef> _baseFieldList;                  // Non-column-referencing fields

    private Map<String, FieldDef> _requiredFields;          // Required column-referencing fields by column local id
    private Map<String, FieldDef> _discardedFields;         // Unmapped column-referencing fields by column local id

    private Map<String, OrphanColumn> _brokenFieldData;     // Column template for unmapped fields by column local id
    private Map<String, List<String>> _brokenColumns;       // Column templates for unmapped fields by column name

    private Map<String, FieldDef> _fieldNames;              // All fields (including discarded) by field name
    private Map<String, FieldDef> _fieldMap;                // All fields (including discarded) by field local id

    private Map<String, FieldDef> _activeFields;            // Column-referencing fields by column local id
    private Map<String, ColumnDef> _activeColumns;          // All columns by column local id
    private Map<String, DataSetOp> _activeTables;           // All tables by table local id
    private Map<String, List<String>> _mappedColumns;       // All columns mapped in unions with counterparts (local id)

    private List<DataSetOp> _dataList;                      // List of potential data trees -- only 1 can exist at exit
    private Map<String, Boolean> _queryNameMap;
    private Map<String, FieldDef> _serverRequiredFields;
    private List<FieldDef> _derivedFields;
    private int _nextOrdinal = 0;
    private String _metaUuid = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //Handle response to request for required fields list
    //
    private VortexEventHandler<List<String>> handleRequiredFieldsResponse
    = new AbstractVortexEventHandler<List<String>>() {

        @Override
        public boolean onError(Throwable myException) {
        
            Display.error("DataSourceEditorModel", 1, myException);
            
                return false;
        }
        @Override
        public void onSuccess(List<String> listIn) {

            updateRequiredFields(buildRequiredList(buildRequiredFieldsMap(listIn)));
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataSourceEditorModel(DataContainer resourceIn, DataSourceEditorPresenter dataSourcePresenterIn) {

        _dataSourcePresenter = dataSourcePresenterIn;
        initializeModel(identifyResource(resourceIn));
        _customResetMap = new TreeMap<String, SqlTableDef>();
    }

    public DataSourceEditorModel close() {

        _dataSourcePresenter = null;
        return null;
    }

    public void adjustButtons() {

        _dataSourcePresenter.adjustButtons(_dataSources, _dataList, missingRequiredFields(),
                                            (0 == _activeFields.size()), unrecognizedDataTypes());
    }

    public void recordCustomReset(SqlTableDef tableIn) {

        String myId = tableIn.getDsoName().substring(0,2);

        _customResetMap.put(myId, tableIn);
    }

    public SqlTableDef retrieveCustomReset(SqlTableDef tableIn) {

        String myId = tableIn.getDsoName().substring(0,2);

        return _customResetMap.get(myId);
    }

    public void clearCustomReset(SqlTableDef tableIn) {

        String myId = tableIn.getDsoName().substring(0,2);

        _customResetMap.remove(myId);
    }

    public Map<String, Boolean> getQueryNameMap() {
        
        return _queryNameMap;
    }

    public ParameterPresenter createParameterPresenter() {

        if (null != _parameterPresenter) {

            replaceQueryParameters();
        }
        _parameterPresenter = new ParameterPresenter(_dataSetParameters, _dataList,
                                                        _meta.getFieldListAccess().getOrderedCaculatedFieldDefs());
        return _parameterPresenter;
    }

    public void replaceQueryParameters() {

        if (null != _parameterPresenter) {

            _dataSetParameters = _parameterPresenter.getParameters();
            _parameterPresenter = null;
        }
    }

    public void replaceQueryParameters(List<QueryParameterDef> listIn) {

        _dataSetParameters = listIn;
        _parameterPresenter = null;
    }

    public List<QueryParameterDef> getParameters() { return _dataSetParameters; }

    public List<DataSetOp> getDataList() {
        return _dataList;
    }
    
    public List<FieldDef> getWorkingFieldDefs() {

        List<FieldDef> myFieldList = new ArrayList<FieldDef>();
        Map<Integer, FieldDef> myMap = new TreeMap<Integer, FieldDef>();

        for (FieldDef myField : _baseFieldList) {

            myMap.put(myField.getOrdinal(), myField);
        }

        for (FieldDef myField : _activeFields.values()) {

            myMap.put(myField.getOrdinal(), myField);
        }

        for (FieldDef myField : _requiredFields.values()) {

            myMap.put(myField.getOrdinal(), myField);
        }

        for (FieldDef myField : myMap.values()) {

            myFieldList.add(myField);
        }

        return myFieldList;
    }
/*
    public void setDataTree(DataSetOp treeIn) {

        if ((null != _meta) && (null != treeIn)) {

            _meta.setDataTree(treeIn);
            treeIn.initialize();
            _dataList.add(treeIn);
        }
    }

    public DataSetOp getDataTree() {

        return (null != _meta) ? _meta.getDataTree() : null;
    }
*/
    public List<DataSourceDef> getDataSources() {
        return _dataSources;
    }

    public SqlTableDef getTableDefByLocalId(String tableLocalIdIn) {

        SqlTableDef myTable = null;

        for (DataSetOp myTree : _dataList) {

            myTable = DataSourceUtilities.locateSqlTable(myTree, tableLocalIdIn);

            if (null != myTable) {

                break;
            }
        }
        return myTable;
    }

    public Map<String, FieldDef> getRequiredFields() {

        return _requiredFields;
    }

    public List<ValuePair<FieldDef, ColumnDef>> getRequiredFieldList() {

        List<ValuePair<FieldDef, ColumnDef>> myFieldList = new ArrayList<ValuePair<FieldDef, ColumnDef>>();
        Map<String, FieldDef> myRequiredMap = getRequiredFields();

        if ((null != myRequiredMap) && (0 < myRequiredMap.size())) {

            for (FieldDef myField : myRequiredMap.values()) {

                String myKey = myField.getColumnKey();
                ColumnDef myColumn = (null != myKey) ? getColumnByColumnKey(myKey) : null;

                myFieldList.add(new ValuePair<FieldDef, ColumnDef>(myField, myColumn));
            }
        }
        return myFieldList;
    }

    public List<ValuePair<FieldDef, ColumnDef>> getUnrequiredFieldList() {

        List<ValuePair<FieldDef, ColumnDef>> myFieldList = new ArrayList<ValuePair<FieldDef, ColumnDef>>();
        Map<String, FieldDef> myRequiredMap = getRequiredFields();

        for (FieldDef myField : _fieldNames.values()) {

            String myKey = myField.getColumnKey();

            if (null == myKey) {

                myFieldList.add(new ValuePair<FieldDef, ColumnDef>(myField, null));

            } else if ((null == myRequiredMap) || (!getRequiredFields().containsKey(myKey))) {

                myFieldList.add(new ValuePair<FieldDef, ColumnDef>(myField, getColumnByColumnKey(myKey)));
            }
        }
        return myFieldList;
    }

    public List<FieldDef> getActiveFields() {

        List<FieldDef> myList = new ArrayList<FieldDef>();

        for (FieldDef myField : _fieldNames.values()) {

            if (_activeFields.containsKey(myField.getColumnLocalId())) {

                myList.add(myField);
            }
        }
        return myList;
    }

    public Map<String, FieldDef> getDiscardedFields() {

        return _discardedFields;
    }

    public Map<String, OrphanColumn> getBrokenFields() {

        return _brokenFieldData;
    }

    public Map<String, ColumnDef> getAllColumns() {

        return _activeColumns;
    }

    public void removeField(String columnIdIn, boolean retainColumnIn) {

        if (_activeColumns.containsKey(columnIdIn)) {
            
            removeField(_activeColumns.get(columnIdIn), retainColumnIn);
        }
    }

    public void removeField(ColumnDef columnIn, boolean retainColumnIn) {

        String myColumnId = columnIn.getLocalId();

        if (_activeFields.containsKey(myColumnId)) {

            FieldDef myField = _activeFields.get(myColumnId);

            recordBrokenField(myField, columnIn);
            setChanged();
        }
        columnIn.setSelected(retainColumnIn);
        setChanged();
    }

    /**
     * @param columnIn Add field corresponding to the given column.
     */
    public FieldDef addField(ColumnDef columnIn, boolean doMatchingIn) {

        String myColumnId = columnIn.getLocalId();
        FieldDef myField = null;

        if (null != myColumnId) {

            myField = _activeFields.get(myColumnId);

            // Prevent duplicate addition.
            if (null == myField) {

                String myColumnName = columnIn.getColumnName();
                List<String> myPossibilities = doMatchingIn ? _brokenColumns.get(myColumnName) : null;

                // Satisfy field requirements
                if (null != myPossibilities) {

                    int myCount = 0;
                    int mySize = myPossibilities.size();

                    for (myCount = 0; mySize > myCount; myCount++) {

                        OrphanColumn myPossibleMatch = _brokenFieldData.get(myPossibilities.get(myCount));

                        if ((null != myPossibleMatch) && myPossibleMatch.getDataype().equals(columnIn.getCsiType())) {

                            myField = activateField(myPossibleMatch, columnIn);
                            break;
                        }
                    }
                }
                if (null == myField) {

                    myField = createField(columnIn);
                }
                setChanged();
            }
        }
        return myField;
    }

    public FieldDef createField(ColumnDef columnIn) {

        FieldDef myFieldDef = new FieldDef(guaranteeUniqueFieldname(columnIn.getColumnName()),
                                            FieldType.COLUMN_REF, columnIn.getCsiType());
        String myFieldId = myFieldDef.getLocalId();
        String myColumnId = columnIn.getLocalId();

        columnIn.setSelected(true);

        myFieldDef.setFunctionType(FunctionType.NONE);
        myFieldDef.setFunctions(new ArrayList<ScriptFunction>());
        myFieldDef.setClientProperties(new CsiMap<String, String>());
        myFieldDef.setColumnLocalId(myColumnId);
        myFieldDef.setTableLocalId(columnIn.getTableDef().getLocalId());
        myFieldDef.setDsLocalId(columnIn.getTableDef().getSource().getLocalId());
        myFieldDef.setOrdinal(_nextOrdinal++);

        _fieldNames.put(myFieldDef.getFieldName(), myFieldDef);
        _fieldMap.put(myFieldId, myFieldDef);
        _activeFields.put(myColumnId, myFieldDef);
        _activeColumns.put(myColumnId, columnIn);
        setChanged();

        return myFieldDef;
    }

    public FieldDef activateField(OrphanColumn orphanIn, ColumnDef columnIn) {

        String myOrphanKey = orphanIn.getColumnKey();
        FieldDef myField = _requiredFields.get(myOrphanKey);

        if (null == myField) {

            myField = _discardedFields.get(myOrphanKey);
        }
        return activateField(myField, columnIn);
    }

    public FieldDef activateField(FieldDef fieldIn, ColumnDef columnIn) {

        if (null != fieldIn) {

            String myColumnKey = columnIn.getColumnKey();
            String myOrphanKey = fieldIn.getColumnKey();
            String myColumnLocalId = columnIn.getLocalId();

            removeBrokenColumn(myOrphanKey);
            if (_requiredFields.containsKey(myOrphanKey)) {

                _requiredFields.remove(myOrphanKey);
                _requiredFields.put(myColumnKey, fieldIn);
            }
            if (_discardedFields.containsKey(myOrphanKey)) {

                _discardedFields.remove(myOrphanKey);
            }
            if (_brokenFieldData.containsKey(myOrphanKey)) {

                _brokenFieldData.remove(myOrphanKey);
            }
            _activeFields.put(myColumnKey, fieldIn);
            _activeColumns.remove(myOrphanKey);
            _activeColumns.put(myColumnKey, columnIn);
            _fieldNames.put(fieldIn.getFieldName(), fieldIn);

            fieldIn.setTableLocalId(columnIn.getTableDef().getLocalId());
            fieldIn.setDsLocalId(columnIn.getTableDef().getSource().getLocalId());
            fieldIn.setColumnLocalId(myColumnLocalId);

            columnIn.setLocalId(myColumnLocalId);
            columnIn.setSelected(true);

            setChanged();
        }
        return fieldIn;
    }

    public void mapField(final FieldDef fieldIn, final ColumnDef columnIn) {

        if ((null != fieldIn) && (null != columnIn)) {

            OrphanColumn myOrphan = _brokenFieldData.get(fieldIn.getColumnKey());

            if (null != myOrphan) {

                activateField(myOrphan, columnIn);

            } else {

                activateField(fieldIn, columnIn);
            }
        }
        setChanged();
    }

    public void unmapField(FieldDef fieldIn, ColumnDef columnIn) {

        recordBrokenField(fieldIn, columnIn);
        setChanged();
    }

    public FieldDef addFieldAsRequired(String tableLocalIdIn, String columnLocalIdIn) {

        FieldDef myField = findFieldSubstituteForCreate(tableLocalIdIn, columnLocalIdIn);

        if (null == myField) {

            ColumnDef myColumn = _activeColumns.get(columnLocalIdIn + tableLocalIdIn);

            if (null != myColumn) {

                myField = createField(myColumn);
            }

            setChanged();
        }
        return myField;
    }

    public void setChanged() {

        _dataSourcePresenter.adjustButtons(_dataSources, _dataList, missingRequiredFields(),
                                            (0 == _activeFields.size()), unrecognizedDataTypes());
    }

    public void handleDsoAdd(DataSetOp dsoIn) {

        SqlTableDef myTable = dsoIn.getTableDef();

        if (null != myTable) {

            DataSourceDef mySource = myTable.getSource();
            String myQueryName = myTable.getQueryName();

            myTable.setDsoName(dsoIn.getName());
            if (null != myQueryName) {

                _queryNameMap.put(myQueryName.toLowerCase(), true);
            }

            if (null != mySource) {

                mySource.incrementChildCount();
            }

            for (ColumnDef myColumn : myTable.getColumns()) {

                if (myColumn.isSelected()) {

                    addField(myColumn, true);

                }
            }
            _activeTables.put(myTable.getLocalId(), dsoIn);
            setChanged();
        }
    }
    public void handleDsoDelete(DataSetOp discardIn) {

        DataSetOp myParent = discardIn.getParent();

        for (DataSetOp myDso = discardIn.getFirstOp(); discardIn != myDso; myDso = myDso.getNextOp()) {

            removeTable(myParent, myDso.getTableDef());
        }
        removeTable(myParent, discardIn.getTableDef());
        setChanged();
    }

    private void removeTable(DataSetOp parentIn, SqlTableDef tableIn) {

        if (null != tableIn) {

            DataSourceDef mySource = tableIn.getSource();

            String myQueryName = tableIn.getQueryName();

            if (null != myQueryName) {

                _queryNameMap.remove(myQueryName.toLowerCase());
            }

            if (null != mySource) {

                mySource.decrementChildCount();
            }
            deleteColumns(parentIn, tableIn.getColumns());
            removeFromMappings(parentIn, tableIn.getLocalId());
            _activeTables.remove(tableIn.getLocalId());
        }
    }

    public void handleDsoReplace(DataSetOp dsoIn) {

        if (null != dsoIn.getTableDef()) {


        } else {

            // TODO:

            // TODO:

            // TODO:

            // TODO:

            // TODO:

            // TODO:

            DataSetOp myParent = dsoIn.getParent();
            DataSetOp myTop = (null != myParent) ? myParent : dsoIn;
        }
    }

    public void handleDsoUpdate(DataSetOp dsoIn, SqlTableDef newTableIn, int prefixIn) {

        DataSetOp myParent = dsoIn.getParent();
        DataSetOp myTop = (null != myParent) ? myParent :dsoIn;
        SqlTableDef myOldTable = dsoIn.getTableDef();
        List<ColumnDef> myOldColumnList = myOldTable.getColumns();
        List<ColumnDef> myNewColumnList = newTableIn.getColumns();
        List<ColumnDef> myDiscardList = new ArrayList<ColumnDef>();
        Map<String, ColumnDef> myNewColumnMap = new TreeMap<String, ColumnDef>();
        String myOldLocalId = myOldTable.getLocalId();
        String myNewLocalId = newTableIn.getLocalId();
        Map<String, Integer> myColumnMap = new HashMap<String, Integer>();
        String mySourceType = (null != myOldTable)
                ? myOldTable.getIsCustom()
                ? _constants.dataSourceEditor_Query()
                : _constants.dataSourceEditor_Table()
                : null;
        boolean _showWarning = false;

        newTableIn.setLocalId(myOldLocalId);
        for (ColumnDef myNewColumn : myNewColumnList) {

            myNewColumnMap.put(myNewColumn.getColumnName(), myNewColumn);
        }
        for (ColumnDef myOldColumn : myOldColumnList) {

            String myColumnName = myOldColumn.getColumnName();
            ColumnDef myNewColumn = myNewColumnMap.get(myColumnName);

            if (null != myNewColumn) {

                myNewColumn.setLocalId(myOldColumn.getLocalId());
                myNewColumnMap.remove(myColumnName);

                if (!myOldColumn.getCsiType().equals(myNewColumn.getCsiType())) {

                    _showWarning = true;
                }

            } else {

                myDiscardList.add(myOldColumn);
            }
        }
        //
        // HERE IS WHERE USER WOULD BE GIVEN THE OPPORTUNITY TO COMPLETE OLD_COLUMN-NEW_COLUMN MAPPING
        //
        deleteColumns(myTop, myDiscardList);
        for (Map.Entry<String, ColumnDef> myEntry : myNewColumnMap.entrySet()) {

            ColumnDef myColumn = myEntry.getValue();

            if (myColumn.isSelected()) {

                addField(myColumn, true);

            } else {

                _activeColumns.put(myColumn.getColumnKey(), myColumn);
            }
        }
        if (0 < prefixIn) {

            dsoIn.createName(prefixIn);
        }
        newTableIn.setDsoName(dsoIn.getName());
        dsoIn.setTableDef(newTableIn);

        for (ColumnDef myNewColumn : newTableIn.getColumns()) {

            myColumnMap.put(myNewColumn.getColumnKey(), 0);
        }

        if (fixUpParent(myParent, myColumnMap, myOldLocalId, myNewLocalId) || _showWarning) {

            final String myTitle = (0 < prefixIn)
                    ? _constants.dataSourceEditor_FinalizeReplace_Title(mySourceType)
                    : _constants.dataSourceEditor_FinalizeQueryUpdate_Title();
        }
        setChanged();
    }

    public void finalizeChanges(Integer rowLimitIn) {

        List<OrphanColumn> myBrokenFields = new ArrayList<OrphanColumn>();
        DataSetOp myDataTree = _dataList.get(0);
        Map<String, CsiDataType> myCastingMap = (null != myDataTree)
                                                    ? myDataTree.buildUnionCastingMap()
                                                    : new HashMap<String, CsiDataType>();

        for (String myKey : _brokenFieldData.keySet()) {

            if (_requiredFields.containsKey(myKey)) {

                myBrokenFields.add(_brokenFieldData.get(myKey));
            }
        }
        replaceQueryParameters();

        if (null != _dataSources) {
            int i = 0;
            for (DataSourceDef mySource : _dataSources) {
                mySource.setOrdinal(i++);
            }
        }
        if (null != _dataSetParameters) {
            int i = 0;
            for (QueryParameterDef myParameter : _dataSetParameters) {
                myParameter.setOrdinal(i++);
            }
        }
        _meta.resetTransients();
        _meta.setRowLimit(rowLimitIn);
        _meta.setDataTree(myDataTree);
        _meta.setDataSources(_dataSources);
        _meta.getParameterListAccess().setDataSetParameters(_dataSetParameters);
        _meta.getFieldListAccess().setFieldDefList(getWorkingFieldDefs());
        _meta.setOrphanColumns(myBrokenFields);
        for (FieldDef myField : _meta.getFieldListAccess().getFieldDefList()) {

            if (null == myField.getStorageType()) {

                ColumnDef myColumn = _meta.getColumnByKey(myField.getColumnKey());

                if (null != myColumn) {

                    CsiDataType myDataType = myCastingMap.get(myColumn.getColumnKey());

                    myField.forceStorageType((null != myDataType) ? myDataType : myColumn.getCsiType());
                }
            } else {

                myField.promoteStorageType();
            }
        }
        _meta.setStorageTypesFlag();
    }

    public List<ColumnDef> getAvailableColumns() {

        List<ColumnDef> myList = new ArrayList<ColumnDef>();

        for (ColumnDef myColumn : _activeColumns.values()) {

            if ((!myColumn.isSelected())
                    || (!_activeFields.keySet().contains(myColumn.getColumnKey()))) {

                myList.add(myColumn);
            }
        }
        return myList;
    }

    public ColumnDef getColumnByColumnKey(String columnKeyIn) {
        return _activeColumns.get(columnKeyIn);
    }

    public List<FieldDef> getActiveFields(DataSetOp dsoIn) {

        List<FieldDef> myActiveFields = new ArrayList<FieldDef>();
        SqlTableDef myTable = dsoIn.getTableDef();

        for (ColumnDef myColumn : myTable.getColumns()) {

            String myColumnKey = myColumn.getColumnKey();

            if (_requiredFields.containsKey(myColumnKey)) {

                FieldDef myField = _requiredFields.get(myColumnKey);

                if (null != myField) {

                    myActiveFields.add(myField);
                }
            }
        }
        return myActiveFields;
    }

    public void displayActiveTableNames() {

        StringBuilder myBuffer = new StringBuilder();

        for (DataSetOp myDso : _activeTables.values()) {

            SqlTableDef myTable = myDso.getTableDef();

            myBuffer.append(myTable.getDsoName() + "\n");
        }
        Dialog.showScrollingDialog("Table Names", myBuffer.toString());
    }

    public void displayActiveColumnNames() {

        StringBuilder myBuffer = new StringBuilder();

        for (ColumnDef myColumn : _activeColumns.values()) {

            myBuffer.append(myColumn.getColumnName() + "\n");
        }
        Dialog.showScrollingDialog("Column Names", myBuffer.toString());
    }

    public void displayActiveFieldNames() {

        StringBuilder myBuffer = new StringBuilder();

        for (FieldDef myField : _activeFields.values()) {

            myBuffer.append(myField.getFieldName() + "\n");
        }
        Dialog.showScrollingDialog("Column Names", myBuffer.toString());
    }

    public void displayDebug() {

        StringBuilder myBuffer = new StringBuilder();

        myBuffer.append("--- TABLE NAMES ---\n");
        for (DataSetOp myDso : _activeTables.values()) {

            SqlTableDef myTable = myDso.getTableDef();

            if (null != myTable) {

                myBuffer.append(myTable.getDsoName() + "\n");
            }
        }

        myBuffer.append("--- COLUMN NAMES ---\n");
        for (ColumnDef myColumn : _activeColumns.values()) {

            myBuffer.append(myColumn.getColumnName() + "\n");
        }

        myBuffer.append("--- FIELD NAMES ---\n");
        for (FieldDef myField : _activeFields.values()) {

            myBuffer.append(myField.getFieldName() + "\n");
        }
        Dialog.showScrollingDialog("Active Tables, Columns, and Fields", myBuffer.toString());
    }

    public void mapColumnPair(DataSetOp dsoIn, ColumnMappingSet selectionIn) {

        OpMapItem myItem = ((ColumnMappingSet)selectionIn).getResult();

        if (null != myItem) {

            dsoIn.addMappingItem(myItem);
            if (OpMapType.APPEND.equals(dsoIn.getMapType())) {

                removeField(myItem.getRightColumnLocalId(), true);
            }
        }
        setChanged();
    }

    public void updateColumnPair(ColumnMappingSet selectionIn) {

        OpMapItem myItem = ((ColumnMappingSet)selectionIn).getResult();

        if (null != myItem) {

            DataSetOp myParent = myItem.getParent();
            CsiDataType myCastToType = selectionIn.getCastToType();

            if (null != myCastToType) {
/*
                if ((null != myParent) && (OpMapType.APPEND.equals(myParent.getMapType()))) {

                    String myLeftColumnId = myItem.getLeftColumnKey();
                    ColumnDef myLeftColumn = (null != myLeftColumnId) ? _meta.getColumnByKey(myLeftColumnId) : null;
                    String myRightColumnId = myItem.getRightColumnKey();
                    ColumnDef myRightColumn = (null != myRightColumnId) ? _meta.getColumnByKey(myRightColumnId) : null;

                    if (null != myLeftColumn) {

                        myLeftColumn.setCastToType(myCastToType);
                    }
                    if (null != myRightColumn) {

                        myRightColumn.setCastToType(myCastToType);
                    }
                }
*/
                myItem.setCastToType(myCastToType);
                myItem.setComparingToken(selectionIn.getComparingToken());
            }
        }
        setChanged();
    }

    public void unmapColumnPair(DataSetOp dsoIn, ColumnMappingSet selectionIn) {

        OpMapItem myItem = ((ColumnMappingSet)selectionIn).getResult();

        if (null != myItem) {

            dsoIn.removeMappingItem(myItem);
            if (OpMapType.APPEND.equals(dsoIn.getMapType())) {

                addFieldAsRequired(myItem.getLeftTableLocalId(), myItem.getLeftColumnLocalId());
                addFieldAsRequired(myItem.getRightTableLocalId(), myItem.getRightColumnLocalId());
            }
            setChanged();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void deleteColumns(DataSetOp topIn, List<ColumnDef> columnListIn) {

        for (ColumnDef myColumn : columnListIn) {

            String myColumnKey = myColumn.getColumnKey();
            String myColumnId = myColumn.getLocalId();

            if (_activeFields.containsKey(myColumnKey)) {

                FieldDef myField = _activeFields.get(myColumnKey);
                String mySubstituteId = findColumnSubstitute(topIn, myColumnKey);
                ColumnDef mySubstitute = _activeColumns.get(mySubstituteId);

                if (null != mySubstitute) {

                    mySubstitute.setSelected(true);
                    mySubstitute.setLocalId(myColumnId);
                    myField.setTableLocalId(mySubstitute.getTableDef().getLocalId());
                    myField.setDsLocalId(mySubstitute.getTableDef().getSource().getLocalId());
                    _activeColumns.remove(mySubstituteId);
                    _activeColumns.put(myColumnKey, mySubstitute);

                } else {

                    removeField(myColumn, false);
                }
            } else {

                _activeColumns.remove(myColumnKey);
            }
        }
    }

    private void initializeModel(DataDefinition metaIn) {

        _meta = metaIn;

        if (null != metaIn) {

            Map<String, DataSourceDef> mySourcerMap = initDataSources();

            _dataSetParameters = new ArrayList<>();
            for (QueryParameterDef myParameter : _meta.getParameterListAccess().getOrderedFullParameterList()) {

                _dataSetParameters.add(myParameter.fullClone());
            }
            _dataList = new ArrayList<DataSetOp>();
            if (null != _meta.getDataTree()) {

                DataSetOp myDataTree = _meta.getDataTree().fullClone(mySourcerMap, new TreeMap<String, SqlTableDef>());
                myDataTree.initialize();
                _dataList.add(myDataTree);
            }
            initColums();
            initFieldDefs();
            createParameterPresenter();
        }
    }

    private String findColumnSubstitute(DataSetOp dsoIn, String columnKeyIn) {

        String mySubstitute = null;

        for (DataSetOp myDso = dsoIn; null != myDso; myDso = myDso.getParent()) {

            if (OpMapType.APPEND.equals(myDso.getMapType())) {

                mySubstitute = myDso.getMapResult(columnKeyIn);

                if (null != mySubstitute) {

                    break;
                }
            }
        }
        return mySubstitute;
    }

    private FieldDef findFieldSubstituteForDelete(DataSetOp dsoIn, String columnKeyIn) {

        FieldDef myField = null;

        for (DataSetOp myDso = dsoIn; null != myDso; myDso = myDso.getParent()) {

            if (OpMapType.APPEND.equals(myDso.getMapType())) {

                String mySubstitute = myDso.getMapResult(columnKeyIn);

                if (null != mySubstitute) {

                    myField = _activeFields.get(mySubstitute);

                    if (null != myField) {

                        break;
                    }
                }
            }
        }
        return myField;
    }

    private FieldDef findFieldSubstituteForCreate(String tableLocalIdIn, String columnIdIn) {

        String myKey = columnIdIn + tableLocalIdIn;
        FieldDef myField = _activeFields.get(myKey);

        if (null == myField) {

            for (DataSetOp myDso = _activeTables.get(tableLocalIdIn); null != myDso; myDso = myDso.getParent()) {

                if (OpMapType.APPEND.equals(myDso.getMapType())) {

                    String mySubstitute = myDso.getMapResult(myKey);

                    if (null != mySubstitute) {

                        myField = _activeFields.get(mySubstitute);

                        if (null != myField) {

                            break;
                        }
                    }
                }
            }
        }
        return myField;
    }

    private void removeFromMappings(DataSetOp dsoIn, String tableIdIn) {

        for (DataSetOp myDso = dsoIn; null != myDso; myDso = myDso.getParent()) {

            myDso.removeTableMapEntry(tableIdIn);
        }
    }

    private void prepare(DataSetOp branchIn) {

        if (null != branchIn) {

            SqlTableDef myTable = branchIn.getTableDef();

            if (null != myTable) {

                for (ColumnDef myColumn : myTable.getColumns()) {

                    myColumn.setTableDef(myTable);
                }
            }
            prepare(branchIn.getLeftChild());
            prepare(branchIn.getRightChild());
        }
    }

    private Map<String, DataSourceDef> initDataSources() {

        Map<String, DataSourceDef> mySourceMap = new TreeMap<>();
        _dataSources = new ArrayList<DataSourceDef>();

        for (DataSourceDef mySource : _meta.getDataSources()) {

            DataSourceDef myCopy = mySource.fullClone();
            myCopy.zeroChildCount();
            _dataSources.add(myCopy);
            mySourceMap.put(myCopy.getUuid(), myCopy);
        }
        return mySourceMap;
    }

    private void initColums() {

        List<DataSetOp> myList = DataSourceUtilities.getTableOps(_dataList);

        _activeTables = new HashMap<String, DataSetOp>();
        _queryNameMap = new HashMap<String, Boolean>();
        _requiredFields = new HashMap<String, FieldDef>();
        _activeColumns = new HashMap<String, ColumnDef>();

        for (DataSetOp myDso : myList) {

            SqlTableDef myTable = myDso.getTableDef();
            String myQueryName = myTable.getQueryName();

            _activeTables.put(myTable.getLocalId(), myDso);

            if (null != myQueryName) {

                _queryNameMap.put(myQueryName.toLowerCase(), true);
            }

            for (ColumnDef myColumn : myTable.getColumns()) {

                _activeColumns.put(myColumn.getColumnKey(), myColumn);
            }
        }
    }

    /**
     * Computes existing field-defs and sets up transient relations to ease column, table and data source handling.
     */
    private void initFieldDefs() {

        List<String> myCurrentFieldIds = new ArrayList<String>();
        List<OrphanColumn> myBrokenFields = _meta.getOrphanColumns();
        List<FieldDef> myFieldDefs = _meta.getFieldListAccess().getFieldDefList();

        _initialFields = new HashMap<String, FieldDef>();
        _activeFields = new HashMap<String, FieldDef>();
        _fieldNames = new HashMap<String, FieldDef>();
        _brokenFieldData = new HashMap<String, OrphanColumn>();
        _brokenColumns = new HashMap<String, List<String>>();
        _fieldMap = new HashMap<String, FieldDef>();
        _derivedFields = new ArrayList<FieldDef>();
        _discardedFields = new HashMap<String, FieldDef>();
        _baseFieldList = new ArrayList<FieldDef>();

        if (null == myFieldDefs) {

            myFieldDefs = new ArrayList<FieldDef>();
            _meta.getFieldListAccess().setFieldDefList(myFieldDefs);
        }

        for (FieldDef myField : myFieldDefs) {

            String myFieldId = myField.getUuid();
            String myLocalId = myField.getLocalId();
            String myColumnKey = myField.getColumnKey();

            _nextOrdinal = Math.max(_nextOrdinal, (myField.getOrdinal() + 1));

            _initialFields.put(myFieldId, myField);
            _fieldNames.put(myField.getFieldName(), myField);
            _fieldMap.put(myColumnKey, myField);

            if (FieldType.COLUMN_REF.equals(myField.getFieldType())) {

                myCurrentFieldIds.add(myFieldId);

                if (null != myField.getTableLocalId()) {

                    _activeFields.put(myColumnKey, myField);

                } else {

                    _discardedFields.put(myColumnKey, myField);
                }

            } else {

                _baseFieldList.add(myField);
            }
            if (null != myField.getSqlExpression()) {

                _derivedFields.add(myField);
            }
        }
        if (null != myBrokenFields) {

            for (OrphanColumn myColumn : myBrokenFields) {

                String myColumnKey = myColumn.getColumnKey();
                String myColumnName = myColumn.getColumnName();
                List<String> myList = _brokenColumns.get(myColumnName);

                if (null == myList) {

                    myList = new ArrayList<String>();
                }
                myList.add(myColumnKey);
                _brokenFieldData.put(myColumnKey, myColumn);
                _brokenColumns.put(myColumnName, myList);
            }
        }
        if ((null != _metaUuid) && (null != myFieldDefs) && (0 < myFieldDefs.size())) {

            _dataSourcePresenter.retrieveRequiredCoreFieldList(handleRequiredFieldsResponse);
        }
        if ((null != _dataList) && (0 < _dataList.size()) && (null != _dataList.get(0))) {

            prepare(_dataList.get(0));
        }
    }

    private String guaranteeUniqueFieldname(String nameIn) {

        String myName = nameIn;

        if (_fieldNames.containsKey(nameIn)) {

            FieldDef myField = _fieldNames.get(nameIn);
            String myKey = myField.getColumnKey();

            if (_discardedFields.containsKey(myKey) && (!_requiredFields.containsKey(myKey))) {

                discardBrokenField(myField);

            } else {

                for (Integer i = 2; ; i++) {

                    myName = nameIn + "_" + Integer.toString(i / 10) + Integer.toString(i % 10); //$NON-NLS-1$ //$NON-NLS-2$

                    if (!_fieldNames.containsKey(myName)) {

                        break;
                    }
                }
            }
        }
        return myName;
    }

    private Map<String, FieldDef> buildRequiredFieldsMap(List<String> listIn) {

        _serverRequiredFields = new HashMap<String, FieldDef>();

        for (String myFieldId : listIn) {

            FieldDef myField = _initialFields.get(myFieldId);

            if (null != myField) {

                _serverRequiredFields.put(myFieldId, myField);
            }
        }
        return _serverRequiredFields;
    }

    private Map<String, FieldDef> buildDerivedFieldsRequiredFieldsMap() {

        Map<String, FieldDef> myMap = null;

        for (FieldDef myField : _derivedFields) {

            myMap = myField.getSqlExpression().mapRequiredFields(myMap, _meta.getFieldListAccess());
        }
        return myMap;
    }

    private Map<String, QueryParameterDef> buildDerivedFieldsRequiredParametersMap() {

        Map<String, QueryParameterDef> myMap = null;

        for (FieldDef myField : _derivedFields) {

            myMap = myField.getSqlExpression().mapRequiredParameters(myMap, _meta.getParameterListAccess());
        }
        return myMap;
    }

    private List<FieldDef> extractRequiredList(Map<String, FieldDef> mapIn) {

        List<FieldDef> myList = null;
        Map<String, FieldDef> myOtherRequiredFields = buildDerivedFieldsRequiredFieldsMap();

        if ((null != mapIn) && (0 < mapIn.size())) {

            myList = new ArrayList<FieldDef>();

            for (FieldDef myField : mapIn.values()) {

                myList.add(myField);
            }

            if ((null != myOtherRequiredFields) && (0 < myOtherRequiredFields.size())) {

                for (FieldDef myField : myOtherRequiredFields.values()) {

                    if (!mapIn.containsKey(myField.getUuid())) {

                        myList.add(myField);
                    }
                }
            }

        } else if ((null != myOtherRequiredFields) && (0 < myOtherRequiredFields.size())) {

            myList = new ArrayList<FieldDef>();

            for (FieldDef myField : myOtherRequiredFields.values()) {

                myList.add(myField);
            }
        }

        return myList;
    }

    private List<FieldDef> buildRequiredList(Map<String, FieldDef> mapIn) {

        return extractRequiredList(mapIn);
    }

    private void updateRequiredFields(List<FieldDef> requiredFieldsIn) {

        if (null != requiredFieldsIn) {

            _requiredFields = new HashMap<String, FieldDef>();

            for (FieldDef myField : requiredFieldsIn) {

                if (null != myField.getColumnLocalId()) {

                    _requiredFields.put(myField.getColumnKey(), myField);
                }
            }
        }
    }

    private void recordBrokenField(FieldDef fieldIn, ColumnDef columnIn) {

        String myColumnKey = columnIn.getColumnKey();

        if (_requiredFields.containsKey(myColumnKey)) {

            addBrokenColumn(new OrphanColumn(fieldIn, columnIn));
            _discardedFields.put(myColumnKey, fieldIn);
            if (_activeFields.containsKey(myColumnKey)) {

                _activeFields.remove(myColumnKey);
            }

            fieldIn.setTableLocalId(null);
            fieldIn.setDsLocalId(null);

        } else {

            discardBrokenField(fieldIn);
        }
    }

    private void discardBrokenField(FieldDef fieldIn) {

        String myColumnKey = fieldIn.getColumnKey();
        String myFieldId = fieldIn.getLocalId();
        String myFieldName = fieldIn.getFieldName();

        removeBrokenColumn(myColumnKey);

        if (_activeFields.containsKey(myColumnKey)) {

            _activeFields.remove(myColumnKey);
        }
        if (_discardedFields.containsKey(myColumnKey)) {

            _discardedFields.remove(myColumnKey);
        }
        if (_fieldNames.containsKey(myFieldName)) {

            _fieldNames.remove(myFieldName);
        }
        if (_fieldMap.containsKey(myFieldId)) {

            _fieldMap.remove(myFieldId);
        }
    }

    private boolean missingRequiredFields() {

        for (String myKey : _requiredFields.keySet()) {

            if (_brokenFieldData.containsKey(myKey) || _discardedFields.containsKey(myKey)) {

                return true;
            }
        }
        return false;
    }

    private boolean unrecognizedDataTypes() {

        for (FieldDef myField : _activeFields.values()) {

            CsiDataType myDataType = myField.getValueType();

            if ((null == myDataType) || CsiDataType.Unsupported.equals(myDataType)) {

                return true;
            }
        }
        return false;
    }

    private void addBrokenColumn(OrphanColumn columnIn) {

        if (null != columnIn) {

            String myOrphanKey = columnIn.getColumnKey();
            String myOrphanName = columnIn.getColumnName();
            List<String> myList = _brokenColumns.get(myOrphanName);

            if (null == myList) {

                myList = new ArrayList<String>();
            }
            myList.add(myOrphanKey);
            _brokenColumns.put(myOrphanName, myList);
            _brokenFieldData.put(myOrphanKey, columnIn);
        }
    }

    private void removeBrokenColumn(String orphanIdIn) {

        if (null != orphanIdIn) {

            OrphanColumn myOrphanColumn = _brokenFieldData.get(orphanIdIn);

            if (null != myOrphanColumn) {

                String myOrphanName = myOrphanColumn.getColumnName();
                List<String> myList = _brokenColumns.get(myOrphanName);

                if (null != myList) {

                    int myCount = 0;
                    int mySize = myList.size();

                    for (myCount = 0; mySize > myCount; myCount++) {

                        if (myList.get(myCount) == orphanIdIn) {

                            break;
                        }
                    }
                    if (mySize > myCount) {

                        myList.remove(myCount);
                    }
                    if (0 == myList.size()) {

                        _brokenColumns.remove(myOrphanName);
                    }
                }
                _brokenFieldData.remove(orphanIdIn);
            }
        }
    }

    private boolean fixUpParent(DataSetOp dsoIn, Map<String, Integer> columnMapIn,
                                String oldLocalIdIn, String newLocalIdIn) {

        boolean myChangeFlag = false;

        if (null != dsoIn) {

            DataSetOp myParent = dsoIn.getParent();
            List<OpMapItem> myMapItems = dsoIn.getMapItems();
            List<Integer> myDiscardList = new ArrayList<Integer>();

            if (null != myParent) {

                myChangeFlag = fixUpParent(myParent, columnMapIn, oldLocalIdIn, newLocalIdIn);
            }
            for (int i = myMapItems.size() - 1; 0 <= i; i--) {

                OpMapItem myItem = myMapItems.get(i);

                if (oldLocalIdIn.equals(myItem.getLeftTableLocalId())) {

                    if (columnMapIn.containsKey(myItem.getLeftColumnLocalId())) {

                        myItem.setLeftTableLocalId(newLocalIdIn);

                    } else {

                        myMapItems.remove(i);
                        myChangeFlag = true;
                    }

                } else if (oldLocalIdIn.equals(myItem.getRightTableLocalId())) {

                    if (columnMapIn.containsKey(myItem.getRightColumnLocalId())) {

                        myItem.setRightTableLocalId(newLocalIdIn);

                    } else {

                        myMapItems.remove(i);
                        myChangeFlag = true;
                    }
                }
            }
        }
        return myChangeFlag;
    }

    private DataDefinition identifyResource(DataContainer resourceIn) {

        if (resourceIn instanceof DataView) {

            _metaUuid = ((DataView)resourceIn).getMeta().getUuid();

        } else if (resourceIn instanceof DataViewDef) {

            _metaUuid = ((DataViewDef)resourceIn).getUuid();
        }
        return resourceIn.getDataDefinition();
    }
}
