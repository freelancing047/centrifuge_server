package csi.server.business.cachedb.dataset;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.helper.ModelHelper;
import csi.server.business.helper.QueryHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;

public class DataSetUtil {
   private static final Logger LOG = LogManager.getLogger(DataSetUtil.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

    public static Set<DataSourceDef> getDistinctSources(DataSetOp op) throws CentrifugeException {
        Set<DataSourceDef> dsSet = new HashSet<DataSourceDef>();
        SqlTableDef myTable = op.getTableDef();
        if (null != myTable) {
            DataSourceDef myDataSource = myTable.getSource();

            if (myTable.isSingleTable()) {
                myDataSource = ModelHelper.cloneObject(myDataSource);
            }
            dsSet.add(myDataSource);
        } else {
            DataSetOp myLeftChild = op.getLeftChild();
            DataSetOp myRightChild = op.getRightChild();

            if ((null == myLeftChild) || (null == myRightChild)) {
                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }

            Set<DataSourceDef> dsSet1 = getDistinctSources(myLeftChild);
            Set<DataSourceDef> dsSet2 = getDistinctSources(myRightChild);

            dsSet.addAll(dsSet1);
            dsSet.addAll(dsSet2);
        }

        return dsSet;
    }

    public static DataSourceDef getFirstSource(DataSetOp opIn) throws CentrifugeException {

        for (DataSetOp myOp = opIn; null != myOp; myOp = myOp.getLeftChild()) {

            SqlTableDef myTable = myOp.getTableDef();

            if (null != myTable) {

                return myTable.getSource();
            }
        }
        throw new CentrifugeException("No data source found!");
    }

    public static boolean isHomogeneous(DataSetOp op) throws CentrifugeException {

        return !op.getForceLocal() && (getDistinctSources(op).size() == 1);
    }

    public static List<ColumnDef> getResultColumns(DataSetOp op, List<String> joinedColumns)
            throws CentrifugeException, GeneralSecurityException {

        List<ColumnDef> columns = new ArrayList<ColumnDef>();
        SqlTableDef myTable = op.getTableDef();
        DataSetOp myLeftChild = op.getLeftChild();
        DataSetOp myRightChild = op.getRightChild();

        if ((null != myLeftChild) && (null != myRightChild)) {

            List<ColumnDef> cols1 = getResultColumns(myLeftChild, joinedColumns);
            List<ColumnDef> cols2 = getResultColumns(myRightChild, joinedColumns);

            columns.addAll(cols1);
            columns.addAll(cols2);

            if (op.getMapType() == OpMapType.APPEND) {

                List<ColumnDef> mappedCols = new ArrayList<ColumnDef>();
                List<OpMapItem> mapItems = op.getMapItems();

                if (mapItems != null) {

                    for (ColumnDef col : columns) {

                        for (OpMapItem item : mapItems) {

                            if (item.getRightColumnLocalId().equalsIgnoreCase(col.getLocalId())) {

                                mappedCols.add(col);
                                break;
                            }
                        }
                    }
                }

                for (ColumnDef col : mappedCols) {

                    columns.remove(col);
                }
            }

        } else if (null != myTable) {

            List<ColumnDef> myColumns = getTableColumns(myTable);

            if (null != myColumns) {

                for (ColumnDef col : myColumns) {

                    if (col.isSelected()) {

                        columns.add(col);

                    } else if ((joinedColumns != null) && joinedColumns.contains(col.getLocalId())) {

                        columns.add(col);
                    }
                }
            }

        } else {

            throw new CentrifugeException( "Encountered invalid data set operation. Two children required.");
        }
        return columns;
    }

    public static List<ColumnDef> getTableColumns(SqlTableDef tableIn) throws CentrifugeException, GeneralSecurityException {

        List<ColumnDef> myColumns = tableIn.getColumns();

        if (((myColumns == null) || myColumns.isEmpty()) && (tableIn.getReferenceId() != null)) {

            DataSourceDef mySource = tableIn.getSource();
            if (null != mySource) {

                ConnectionDef myConnectionDef = mySource.getConnection();

                if (null != myConnectionDef) {

                    ConnectionFactory myFactory = ConnectionFactoryManager.getInstance().getConnectionFactory(myConnectionDef);
                    myColumns = myFactory.listColumnDefs(myConnectionDef, tableIn);
                }
            }
        }
        return myColumns;
    }

    public static List<ColumnDef> getAllColumns(DataSetOp op) throws CentrifugeException {
        List<ColumnDef> columns = new ArrayList<ColumnDef>();
        SqlTableDef tableDef = op.getTableDef();
        if (tableDef != null) {
            for (ColumnDef col : tableDef.getColumns()) {
                columns.add(col);
            }
        } else {
            DataSetOp myLeftChild = op.getLeftChild();
            DataSetOp myRightChild = op.getRightChild();

            if ((null == myLeftChild) || (null == myRightChild)) {
                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }

            List<ColumnDef> cols1 = getAllColumns(myLeftChild);
            List<ColumnDef> cols2 = getAllColumns(myRightChild);

            columns.addAll(cols1);
            columns.addAll(cols2);
        }
        return columns;
    }

    public static Set<QueryParameterDef> listRequiredParameters(DataSetOp op, List<QueryParameterDef> allParameters) throws CentrifugeException {
        // normalize the param names to lowercase to make it easier
        // for us to look up parameters by name
        Map<String, QueryParameterDef> paramNameMap = new HashMap<String, QueryParameterDef>();
        Map<String, QueryParameterDef> paramLocalIdMap = new HashMap<String, QueryParameterDef>();

        for (QueryParameterDef p : allParameters) {
            paramNameMap.put(p.getName().toLowerCase(), p);
            paramLocalIdMap.put(p.getLocalId(), p);

            if (_doDebug) {
               LOG.debug(" ----- " + Format.value(p.getName().toLowerCase()) + "  ::  " + Format.value(p.getLocalId() + "  ::  " + Format.value(p.getValue())));
            }
        }

        Set<QueryParameterDef> params = new HashSet<QueryParameterDef>();
        List<SqlTableDef> tables = listTableDefs(op);
        for (SqlTableDef tableDef : tables) {
            DataSourceDef source = tableDef.getSource();
            if (source != null) {
                ConnectionDef conndef = source.getConnection();
                if (conndef != null) {
                    Set<String> names = new HashSet<String>();
                    String preSql = conndef.getPreSql();
                    names.addAll(QueryHelper.listParameterNames(preSql));

                    String postSql = conndef.getPostSql();
                    names.addAll(QueryHelper.listParameterNames(postSql));

                    for (String name : names) {
                        QueryParameterDef p = paramNameMap.get(name.toLowerCase());
                        if (p == null) {
                            throw new CentrifugeException("Missing required parameter definition: " + name);
                        }
                        params.add(p);
                    }
                }
            }

            if (tableDef.getIsCustom()) {
                List<String> names = QueryHelper.listParameterNames(tableDef.getCustomQuery().getQueryText());
                for (String name : names) {
                    QueryParameterDef p = paramNameMap.get(name.toLowerCase());
                    if (p == null) {
                        throw new CentrifugeException("Missing required parameter definition: " + name);
                    }
                    params.add(p);
                }
            }

            for (ColumnDef col : tableDef.getColumns()) {
                List<ColumnFilter> filters = col.getColumnFilters();
                if (filters != null) {
                    for (ColumnFilter filter : filters) {
                        if (FilterOperandType.PARAMETER == filter.operandType) {
                            String paramLocalId = filter.getParamLocalId();
                            QueryParameterDef p = paramLocalIdMap.get(paramLocalId);
                            if (p == null) {
                                throw new CentrifugeException("Missing required parameter definition: " + paramLocalId);
                            }
                            params.add(p);
                        }
                    }
                }
            }
        }

        return params;
    }

    public static Set<QueryParameterDef> listRequiredParameters(DataSetOp op, List<QueryParameterDef> allParameters,
                                                                String vizUuid, String dataViewName)
          throws CentrifugeException {
       return listRequiredParameters(op, allParameters);
    }

    public static List<SqlTableDef> listTableDefs(DataSetOp op) throws CentrifugeException {
        List<SqlTableDef> tables = new ArrayList<SqlTableDef>();
        SqlTableDef tableDef = op.getTableDef();
        if (tableDef != null) {
            tables.add(tableDef);
        } else {
            DataSetOp myLeftChild = op.getLeftChild();
            DataSetOp myRightChild = op.getRightChild();

            if ((null == myLeftChild) || (null == myRightChild)) {
                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }

            List<SqlTableDef> myLeftChildparams = listTableDefs(myLeftChild);
            List<SqlTableDef> myRightChildparams = listTableDefs(myRightChild);

            tables.addAll(myLeftChildparams);
            tables.addAll(myRightChildparams);
        }
        return tables;
    }

    public static List<String> listJoinedColumns(DataSetOp op) throws CentrifugeException {
        List<String> list = new ArrayList<String>();
        List<OpMapItem> mapItems = op.getMapItems();

        if (op.getMapType() == OpMapType.JOIN) {
            for (OpMapItem item : mapItems) {
                String fromId = item.getLeftColumnLocalId();
                if (fromId != null) {
                    list.add(fromId);
                }

                String toId = item.getRightColumnLocalId();
                if (toId != null) {
                    list.add(toId);
                }
            }
        }

        DataSetOp myLeftChild = op.getLeftChild();
        DataSetOp myRightChild = op.getRightChild();

        if ((null != myLeftChild) && (null != myRightChild)) {

            list.addAll(listJoinedColumns(myLeftChild));
            list.addAll(listJoinedColumns(myRightChild));
        }

        return list;
    }

    public static boolean hasAppendsWithNothingSelected(DataSetOp rootOp) throws CentrifugeException {
        if ((null != rootOp) && (null != rootOp.getTableDef())
                && (null != rootOp.getLeftChild()) && (null != rootOp.getRightChild())) {
            DataSetOp myLeftChild = rootOp.getLeftChild();
            DataSetOp myRightChild = rootOp.getRightChild();

            if (rootOp.getMapType() == OpMapType.APPEND) {
                boolean myLeftChildIsEmpty = true;

                for (SqlTableDef table : listTableDefs(myLeftChild)) {
                    List<ColumnDef> myLeftChildCols = table.getColumns();
                    for (ColumnDef col : myLeftChildCols) {
                        if (col.isSelected()) {
                            myLeftChildIsEmpty = false;
                            break;
                        }
                    }
                    if (!myLeftChildIsEmpty) {
                        break;
                    }
                }

                if (myLeftChildIsEmpty) {
                    return true;
                } else {
                    boolean myRightChildIsEmpty = true;
                    for (SqlTableDef tableDef : listTableDefs(myRightChild)) {
                        List<ColumnDef> myRightChildCols = tableDef.getColumns();
                        for (ColumnDef col : myRightChildCols) {
                            if (col.isSelected()) {
                                myRightChildIsEmpty = false;
                                break;
                            }
                        }
                        if (!myRightChildIsEmpty) {
                            break;
                        }
                    }

                    if (myRightChildIsEmpty) {
                        return true;
                    }
                }
            }

            if (hasAppendsWithNothingSelected(myLeftChild)) {
                return true;
            }

            if (hasAppendsWithNothingSelected(myRightChild)) {
                return true;
            }
        }

        return false;
    }
}
