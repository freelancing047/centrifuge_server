package csi.client.gwt.mapper.grids;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.mapper.cells.SelectionMapperCell;
import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.grid_model.BuilderInfo;
import csi.client.gwt.mapper.grid_model.GroupProvider;
import csi.client.gwt.mapper.grid_model.ModelBuilder;
import csi.client.gwt.mapper.grid_model.ObjectProvider;
import csi.client.gwt.mapper.menus.GridMenu;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;

/**
 * Created by centrifuge on 3/27/2016.
 */
public class ColumnSelectionGrid extends SelectionGrid<ColumnDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected DataSetOp _dso;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ColumnSelectionGrid(String gridHeaderIn, GridMenu<SelectionGrid<ColumnDisplay>> gridMenuIn,
                               CsiDropEventHandler handlerIn, DataSetOp dsoIn) {

        super(new ModelBuilder<ColumnDisplay>()
                .addColumn(new BuilderInfo<ColumnDisplay, String>(new GroupProvider<ColumnDisplay>(),
                        150, "", false, false, new CsiTitleCell()))
                .addColumn(new BuilderInfo<ColumnDisplay, ColumnDisplay>(new ObjectProvider<ColumnDisplay>(),
                        150, gridHeaderIn, false, false, new SelectionMapperCell<ColumnDef>())), gridMenuIn, handlerIn);

        _dso = dsoIn;
    }

    public ColumnSelectionGrid(String idIn, String gridHeaderIn, GridMenu<SelectionGrid<ColumnDisplay>> gridMenuIn,
                               CsiDropEventHandler handlerIn, DataSetOp dsoIn) {

        this(gridHeaderIn, gridMenuIn, handlerIn, dsoIn);

        setGridId(idIn);
    }

    public String getSelectionLabel() {

        ColumnDisplay myItem = getSelectionModel().getSelectedItem();
        String myGroup = (null != myItem) ? myItem.getGroupDisplayName() : null;
        String myField = (null != myItem) ? myItem.getItemDisplayName() : null;

        return ((null != myGroup) ? myGroup + "." : "") + ((null != myField) ? myField : "");
    }

    @Override
    public Map<String, Map<String, ColumnDisplay>> createDisplayMap() {

        Map<String, Map<String, ColumnDisplay>> myMap = new TreeMap<String, Map<String, ColumnDisplay>>();

        if (null != _dso) {

            fillDisplayMap(myMap, _dso);
        }
        return myMap;
    }

    public static OpMapItem validateMapping(Map<String, Map<String, ColumnDisplay>> leftMapIn,
                                            Map<String, Map<String, ColumnDisplay>> rightMapIn, OpMapItem itemIn) {


        if (null != itemIn) {

            String myLeftTableId = itemIn.getLeftTableLocalId();
            String myLeftColumnId = itemIn.getLeftColumnLocalId();
            String myRightTableId = itemIn.getRightTableLocalId();
            String myRightColumnId = itemIn.getRightColumnLocalId();

            if ((null!= myLeftTableId) && (null!= myLeftColumnId)
                    && (null!= myRightTableId) && (null!= myRightColumnId)) {

                Map<String, ColumnDisplay> myLeftMap = leftMapIn.get(myLeftTableId);
                Map<String, ColumnDisplay> myRightMap = rightMapIn.get(myRightTableId);

                if ((null != myLeftMap) && myLeftMap.containsKey(myLeftColumnId)
                        && (null != myRightMap) && myRightMap.containsKey(myRightColumnId)) {

                   return itemIn;
                }
            }
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void fillDisplayMap(Map<String, Map<String, ColumnDisplay>> mapIn, DataSetOp dsoIn) {

        SqlTableDef myTable = dsoIn.getTableDef();

        if (null != myTable) {

            // Add the column map for the table
            Map<String, ColumnDisplay> myMap = new TreeMap<String, ColumnDisplay>();

            for (ColumnDef myColumn : myTable.getColumns()) {

                if (myColumn.isSelected()) {

                    myMap.put(myColumn.getLocalId(), new ColumnDisplay(getGridId(), myColumn));
                }
            }
            mapIn.put(myTable.getLocalId(), myMap);

        } else {

            DataSetOp myLeftDso = dsoIn.getLeftChild();
            DataSetOp myRightDso = dsoIn.getRightChild();
            List<OpMapItem> myList = dsoIn.getMapItems();
            boolean myUnionFlag = OpMapType.APPEND.equals(dsoIn.getMapType());

            if (null != myLeftDso) {

                fillDisplayMap(mapIn, myLeftDso);
            }

            if (null != myRightDso) {

                fillDisplayMap(mapIn, myRightDso);
            }

            if ((null != myList) && (0 < myList.size())) {

                List<OpMapItem> myNewList = new ArrayList<OpMapItem>(myList.size());

                for (int i = 0; myList.size() > i; i++) {

                    OpMapItem myItem = validateMapping(mapIn, mapIn, myList.get(i));

                    if (null != myItem) {

                        if (myUnionFlag) {

                            String myRightTableId = myItem.getRightTableLocalId();
                            String myRightColumnId = myItem.getRightColumnLocalId();
                            Map<String, ColumnDisplay> myRightMap = mapIn.get(myRightTableId);

                            myRightMap.remove(myRightColumnId);
                        }
                        myNewList.add(myItem);
                    }
                }
                _dso.setMapItems(myNewList);
            }
        }
    }
}
