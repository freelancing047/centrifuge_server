package csi.client.gwt.edit_sources.right_panel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.mapper.data_model.FieldDisplay;
import csi.client.gwt.mapper.grids.FieldSelectionGrid;
import csi.client.gwt.mapper.grids.SelectionGrid;
import csi.client.gwt.mapper.menus.GridMenu;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 4/19/2016.
 */
public class FieldGrid extends FieldSelectionGrid {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DataSourceEditorModel _model;
    private List<DataSetOp> _dataSetList;
    private DataSetOp _dso;
    private Map<String, CsiDataType> _unionCastingMap = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldGrid(String idIn, String gridHeaderIn, GridMenu<SelectionGrid<FieldDisplay>> gridMenuIn,
                              CsiDropEventHandler handlerIn, DataSourceEditorModel modelIn) {

        super(idIn, gridHeaderIn, gridMenuIn, handlerIn);

        _model = modelIn;
        _dataSetList = _model.getDataList();
        _dso = _dataSetList.get(0);
        _unionCastingMap = (null != _dso) ? _dso.buildUnionCastingMap() : new HashMap<>();
    }

    public String getSelectionLabel() {

        FieldDisplay myItem = getSelectionModel().getSelectedItem();
        String myField = (null != myItem) ? myItem.getItemDisplayName() : null;

        return (null != myField) ? myField : "";
    }

    @Override
    public Map<String, Map<String, FieldDisplay>> createDisplayMap() {

        Map<String, Map<String, FieldDisplay>> myDisplayList = new TreeMap<String, Map<String, FieldDisplay>>();
        Map<String, FieldDisplay> myMap = new TreeMap<String, FieldDisplay>();
        List<ValuePair<FieldDef, ColumnDef>> myRequiredFields = _model.getRequiredFieldList();
        List<ValuePair<FieldDef, ColumnDef>> myUnrequiredFields = _model.getUnrequiredFieldList();

        if ((null != myRequiredFields) && (0 < myRequiredFields.size())) {

            for (ValuePair<FieldDef, ColumnDef> myPair : myRequiredFields) {

                FieldDef myField = myPair.getValue1();
                ColumnDef myColumn = myPair.getValue2();
                CsiDataType myOverride = (null != myColumn) ? _unionCastingMap.get(myColumn.getColumnKey()) : null;

                myMap.put(myField.getLocalId(), new FieldDisplay(getGridId(), myField, myColumn, myOverride, true));
            }
        }
        if (0 < myMap.size()) {

            myDisplayList.put(_txtGroupInUse, myMap);
            myMap = new TreeMap<String, FieldDisplay>();
        }
        if ((null != myUnrequiredFields) && (0 < myUnrequiredFields.size())) {

            for (ValuePair<FieldDef, ColumnDef> myPair : myUnrequiredFields) {

                FieldDef myField = myPair.getValue1();
                ColumnDef myColumn = myPair.getValue2();
                CsiDataType myOverride = (null != myColumn) ? _unionCastingMap.get(myColumn.getColumnKey()) : null;

                myMap.put(myField.getLocalId(), new FieldDisplay(getGridId(), myField, myColumn, myOverride, false));
            }
        }
        if (0 < myMap.size()) {

            myDisplayList.put(_txtGroupNotInUse, myMap);
        }
        return myDisplayList;
    }
}
