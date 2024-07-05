package csi.client.gwt.dataview;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.server.common.model.column.ColumnDef;


public class DataSourceUtilities {

    
    public static SqlTableDef finalizeSqlTable(DataSourceDef dataSourceIn, SqlTableDef tableIn) {
        
        if (null != tableIn) {
            
            tableIn.setSource(dataSourceIn);
            tableIn.setLocalId(UUID.randomUUID());
            
            for (ColumnDef myColumn : tableIn.getColumns()) {
                
                myColumn.setTableDef(tableIn);
                myColumn.setTableName(tableIn.getTableName());
                myColumn.setMapped(false);
            }
        }
        
        return tableIn;
    }

    public static SqlTableDef locateSqlTable(DataSetOp branchIn, String tableLocalIdIn) {
        
        SqlTableDef myTable = null;
        
        if ((null != branchIn) && (null != tableLocalIdIn)) {
            
            if (branchIn.hasChildren()) {
                
                myTable = locateSqlTable(branchIn.getLeftChild(), tableLocalIdIn);
                
                if (null == myTable) {
                    
                    myTable = locateSqlTable(branchIn.getRightChild(), tableLocalIdIn);
                }
                
            } else {
                
                SqlTableDef myTestTable = branchIn.getTableDef();
                
                if ((null != myTestTable) && tableLocalIdIn.equals(myTestTable.getLocalId())) {
                    
                    myTable = myTestTable;
                }
            }
        }
        return myTable;
    }

    public static ColumnDef locateColumn(DataSetOp branchIn, String columnLocalId) {
        
        ColumnDef myColumn = null;
        
        if ((null != branchIn) && (null != columnLocalId)) {
            
            if (branchIn.hasChildren()) {
                
                myColumn = locateColumn(branchIn.getLeftChild(), columnLocalId);
                
                if (null == myColumn) {
                    
                    myColumn = locateColumn(branchIn.getRightChild(), columnLocalId);
                }
                
            } else {
                
                SqlTableDef myTestTable = branchIn.getTableDef();
                
                if (null != myTestTable) {
                    
                    for (ColumnDef myTestColumn : myTestTable.getColumns()) {
                        
                        if (columnLocalId.equals(myTestColumn.getLocalId())) {
                            
                            myColumn = myTestColumn;
                            break;
                        }
                    }
                }
            }
        }
        return myColumn;
    }

    public static List<DataSetOp> getTableOps(DataSetOp dataSetOpIn) {

        List<DataSetOp> myList = new ArrayList<DataSetOp>();

        getTableOps(dataSetOpIn, myList);

        return myList;
    }

    public static List<DataSetOp> getTableOps(List<DataSetOp> dsoListIn) {

        List<DataSetOp> myList = new ArrayList<DataSetOp>();

        for (DataSetOp myDsp : dsoListIn) {

            getTableOps(myDsp, myList);
        }

        return myList;
    }

    public static List<DataSetOp> getTableOps(DataSetOp[] dsoArrayIn) {

        List<DataSetOp> myList = new ArrayList<DataSetOp>();

        for (int i = 0; dsoArrayIn.length > i; i++) {

            getTableOps(dsoArrayIn[i], myList);
        }

        return myList;
    }

    public static void getTableOps(DataSetOp dataSetOpIn, List<DataSetOp> tablesIn) {
        
        if ((null != dataSetOpIn) && (null != tablesIn)) {
            
            SqlTableDef mySqlTable = dataSetOpIn.getTableDef();
            
            if (null != mySqlTable) {
                
                tablesIn.add(dataSetOpIn);
                
            } else {

                getTableOps(dataSetOpIn.getLeftChild(), tablesIn);
                getTableOps(dataSetOpIn.getRightChild(), tablesIn);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ModelObject> List<T> duplicateList(List<T> listIn) {

        List<T> myList = new ArrayList<T>();
        
        for (T myItem : listIn) {
            
            myList.add((T)myItem.fullClone());
        }
        
        return myList;
    }
}
