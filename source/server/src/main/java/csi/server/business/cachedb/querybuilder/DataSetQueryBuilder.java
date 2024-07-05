package csi.server.business.cachedb.querybuilder;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.cachedb.dataset.DataSetUtil;
import csi.server.business.helper.QueryHelper;
import csi.server.business.helper.linkup.ParameterSetFactory;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.column.ColumnFilter;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.FilterOperatorType;
import csi.server.common.model.operator.OpJoinType;
import csi.server.common.model.operator.OpMapItem;
import csi.server.common.model.operator.OpMapType;
import csi.server.common.model.query.QueryDef;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.sql.CacheCommands;
import csi.server.util.sql.CacheTokens;
import csi.server.util.sql.SqlTokens;

public class DataSetQueryBuilder {
   private static final Logger LOG = LogManager.getLogger(DataSetQueryBuilder.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

    private static final Map<FilterOperatorType, String> operationMap = new HashMap<FilterOperatorType, String>();

    static {
        operationMap.put(FilterOperatorType.BEGINS_WITH, "%1$s LIKE '%2$s%%'");
        operationMap.put(FilterOperatorType.CONTAINS, "%1$s LIKE '%%%2$s%%'");
        operationMap.put(FilterOperatorType.EMPTY, "%1$s = ''");
        operationMap.put(FilterOperatorType.ENDS_WITH, "%1$s LIKE '%%%2$s'");
        operationMap.put(FilterOperatorType.EQUALS, "%1$s = %2$s");
        operationMap.put(FilterOperatorType.GEQ, "%1$s >= %2$s");
        operationMap.put(FilterOperatorType.GT, "%1$s > %2$s");
        operationMap.put(FilterOperatorType.IN_LIST, "%1$s IN (%2$s)");
        operationMap.put(FilterOperatorType.ISNULL, "%1$s IS NULL");
        operationMap.put(FilterOperatorType.LEQ, "%1$s <= %2$s");
        operationMap.put(FilterOperatorType.LT, "%1$s < %2$s");
        operationMap.put(FilterOperatorType.NULL_OR_EMPTY, "%1$s IS NULL OR %1$s = ''");
    }

    private ConnectionFactory connectionFactory;
    private ParameterSetFactory parameterFactory;
    private Map<String, String> aliasMap;

    private List<QueryParameterDef> parameters;
    private List<String> joinColumns;
    private int wrapperId = 1;

    public DataSetQueryBuilder(ConnectionFactory connectionFactory, FieldListAccess fieldAccessIn,
                               Map<String, String> aliasMap, List<String> joinColumns,
                               ParameterSetFactory parameterFactoryIn) {
        this.connectionFactory = connectionFactory;
        this.aliasMap = aliasMap;
        this.joinColumns = joinColumns;
        this.parameterFactory = parameterFactoryIn;
    }

    public DataSetQueryBuilder(ConnectionFactory connectionFactory, ParameterSetFactory parameterFactoryIn) {
        this.connectionFactory = connectionFactory;
        this.parameterFactory = parameterFactoryIn;
    }

    public DataSetQueryBuilder(ConnectionFactory connectionFactory, List<QueryParameterDef> parametersIn) {
        this.connectionFactory = connectionFactory;
        this.parameters = parametersIn;
    }

    public StringBuilder createCompleteQuery(DataSetOp op, DataSetOp parent, boolean returnAllIn, boolean isLeft)
            throws CentrifugeException, GeneralSecurityException {

        StringBuilder myBuffer = new StringBuilder();
        String myUnion = returnAllIn ? SqlTokens.UNION_ALL_TOKEN : SqlTokens.UNION_TOKEN;
        Integer myParameterSetId = 0;

        if (1 < parameterFactory.count()) {

            for (parameters = parameterFactory.getFirstList();
                 null != parameters; parameters = parameterFactory.getNextList()) {

                if (0 < myBuffer.length()) {

                    myBuffer.append(myUnion);
                }
                myBuffer.append(createHomogeneousQuery(op, parent, returnAllIn, isLeft, myParameterSetId++));
            }

        } else {

            parameters = parameterFactory.getFirstList();
            myBuffer.append(createHomogeneousQuery(op, parent, returnAllIn, isLeft, null));
        }
        return myBuffer;
    }

    public String createInstalledViewQuery(String tableNameIn, SqlTableDef tableDefIn, InstalledTable installedTableIn,
                                           Map<String, FieldDef> fieldMapIn)
            throws CentrifugeException, GeneralSecurityException {

        parameters = parameterFactory.getFirstList();
        return createViewQuery(tableNameIn, installedTableIn, tableDefIn, fieldMapIn).toString();
    }

    public StringBuilder createLocalQuery(DataSetOp opIn, boolean returnAllIn)
            throws CentrifugeException, GeneralSecurityException {

        StringBuilder myBuffer = new StringBuilder();

        if (DataSetUtil.isHomogeneous(opIn)) {

            List<ColumnDef> myColumnList = DataSetUtil.getResultColumns(opIn, joinColumns);
            String mySelect = returnAllIn ? SqlTokens.SELECT_TOKEN : SqlTokens.SELECT_DISTINCT_TOKEN;
            boolean first = true;

            myBuffer.append(mySelect);

            for (ColumnDef myColumn : myColumnList) {
                String myKey = myColumn.getLocalId();
                String myExpression = (null != aliasMap) ? getQuotedAlias(myKey) : CacheUtil.toQuotedDbUuid(myKey);

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(SqlTokens.COMMA_SPACE);
                }
                myBuffer.append(myExpression);
            }
            myBuffer.append(SqlTokens.FROM_TOKEN);
            myBuffer.append(CacheUtil.toQuotedDbUuid(opIn.getUuid()));

        } else {

            String myUnion = returnAllIn ? SqlTokens.UNION_ALL_TOKEN : SqlTokens.UNION_TOKEN;
            Integer myParameterSetId = 0;

            if (1 < parameterFactory.count()) {

                for (parameters = parameterFactory.getFirstList();
                     null != parameters; parameters = parameterFactory.getNextList()) {

                    if (0 < myBuffer.length()) {

                        myBuffer.append(myUnion);
                    }
                    myBuffer.append(createLocalQuery(opIn, null, returnAllIn, myParameterSetId++));
                }

            } else {

                parameters = parameterFactory.getFirstList();
                myBuffer.append(createLocalQuery(opIn, null, returnAllIn, null));
            }
        }
        return myBuffer;
    }

    private StringBuilder createLocalQuery(DataSetOp opIn, DataSetOp parentIn, boolean returnAllIn, Integer parameterIdIn)
            throws CentrifugeException, GeneralSecurityException {

        StringBuilder myBuffer = new StringBuilder();
        if (DataSetUtil.isHomogeneous(opIn)) {
            // op is homogeneous
            List<ColumnDef> myColumnList = DataSetUtil.getResultColumns(opIn, joinColumns);
            String mySelect = returnAllIn ? SqlTokens.SELECT_TOKEN : SqlTokens.SELECT_DISTINCT_TOKEN;
            boolean first = true;

//            myBuffer.append(SELECT_ALL_FROM);
//            myBuffer.append(CacheUtil.toQuotedDbUuid(opIn.getUuid()));
            myBuffer.append(mySelect);

            for (ColumnDef myColumn : myColumnList) {
                String myKey = myColumn.getLocalId();
                String myExpression = (null != aliasMap) ? getQuotedAlias(myKey) : CacheUtil.toQuotedDbUuid(myKey);

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(SqlTokens.COMMA_SPACE);
                }
                myBuffer.append(myExpression);
            }
            myBuffer.append(SqlTokens.FROM_TOKEN);
            myBuffer.append(CacheUtil.toQuotedDbUuid(opIn.getUuid()));
            if (null != parameterIdIn) {

                myBuffer.append(SqlTokens.WHERE_TOKEN);
                myBuffer.append(SqlTokens.PARAMETER_ID);
                myBuffer.append(SqlTokens.EQUAL_TOKEN);
                myBuffer.append(parameterIdIn.toString());
            }

        } else {

            DataSetOp myLeftChild = opIn.getLeftChild();
            DataSetOp myRightChild = opIn.getRightChild();

            if ((null != myLeftChild) && (null != myRightChild)) {

// TODO:

// TODO:

// TODO:

// TODO:

                StringBuilder myLeftQuery = createLocalQuery(myLeftChild, opIn, returnAllIn, parameterIdIn);
                StringBuilder myRightQuery = createLocalQuery(myRightChild, opIn, returnAllIn, parameterIdIn);

                return mergeQueries(opIn, myLeftChild, myRightChild, myLeftQuery, myRightQuery, returnAllIn, null);

            } else {

                throw new CentrifugeException(
                        "Encountered invalid data set operation. Two children required.");
            }
        }
        return myBuffer;
    }

    public QueryDef createCustomQuery(SqlTableDef tableDefIn, List<String> requiredColumnsIn, boolean returnAllIn)
            throws CentrifugeException {

        QueryDef myQuery = new QueryDef();
        StringBuilder myBuffer = new StringBuilder();
        String mySelect = returnAllIn ? SqlTokens.SELECT_TOKEN : SqlTokens.SELECT_DISTINCT_TOKEN;
        Map<String, ColumnDef> myColumnMap = tableDefIn.getLocalIdMap();
        Map<String, ColumnDef> myFilteredMap = new TreeMap<String, ColumnDef>();
        Map<String, ColumnDef> mySelectedMap = new TreeMap<String, ColumnDef>();

        myBuffer.append(mySelect);

        if ((requiredColumnsIn != null) && !requiredColumnsIn.isEmpty()) {

            for (String myColumn : requiredColumnsIn) {

                mySelectedMap.put(myColumn, null);
            }
        }
        for (ColumnDef myColumnDef : tableDefIn.getColumns()) {

            String myKey = myColumnDef.getLocalId();

            if (null != myColumnDef) {

                List<ColumnFilter> columnFilters = myColumnDef.getColumnFilters();

                if ((myColumnDef.isSelected()) || ((null != joinColumns) && joinColumns.contains(myKey))) {

                    mySelectedMap.put(myKey, myColumnDef);
                }
                if ((null != columnFilters) && !columnFilters.isEmpty() && (null != columnFilters.get(0))) {

                    myFilteredMap.put(myKey, myColumnDef);
                }
            }
        }
        if (!mySelectedMap.isEmpty()) {
           boolean first = true;

            for (Map.Entry<String, ColumnDef> myEntry : mySelectedMap.entrySet()) {

                ColumnDef myColumnDef = myEntry.getValue();

                if (null != myColumnDef) {
                   if (first) {
                      first = false;
                   } else {
                      myBuffer.append(SqlTokens.COMMA_SPACE);
                   }
                    myBuffer.append(connectionFactory.getQuotedName(myColumnDef.getColumnName()));
                    if (null != aliasMap) {

                        myBuffer.append(SqlTokens.AS_TOKEN);
                        myBuffer.append(getQuotedAlias(myColumnDef.getLocalId()));
                    }
                }
            }
        }
        myBuffer.append(SqlTokens.FROM_TOKEN);
        myBuffer.append(connectionFactory.getQualifiedName(tableDefIn));

        // add where clause
        if (!myFilteredMap.isEmpty()) {
            myBuffer.append(SqlTokens.WHERE_TOKEN);
            myBuffer.append(buildFilterClause(myFilteredMap, myColumnMap, false));
        }
        myQuery.setSql(myBuffer.toString());

        return myQuery;
    }

    private StringBuilder createHomogeneousQuery(DataSetOp opIn, DataSetOp parent,
                                                boolean returnAllIn, boolean isLeftIn, Integer parameterIdIn)
            throws CentrifugeException, GeneralSecurityException {

        SqlTableDef myTableDef = opIn.getTableDef();
        if (null != myTableDef) {

//            return createTableOpQuery(myTableDef, parent, returnAllIn, isLeftIn);
            return createTableOpQuery(myTableDef, parent, returnAllIn, isLeftIn, parameterIdIn);

        } else {

            DataSetOp myLeftChild = opIn.getLeftChild();
            DataSetOp myRightChild = opIn.getRightChild();

            if ((null != myLeftChild) && (null != myRightChild)) {

                StringBuilder myLeftQuery = createHomogeneousQuery(myLeftChild, opIn, returnAllIn, true, null);
                StringBuilder myRightQuery = createHomogeneousQuery(myRightChild, opIn, returnAllIn, false, null);

                return mergeQueries(opIn, myLeftChild, myRightChild, myLeftQuery, myRightQuery, returnAllIn, parameterIdIn);

            } else {

                throw new CentrifugeException( "Encountered invalid data set operation. Two children required.");
            }
        }
    }

    private StringBuilder mergeQueries(DataSetOp parentIn, DataSetOp leftChildIn, DataSetOp rightChildIn,
                                      StringBuilder leftQueryIn, StringBuilder rightQueryIn,
                                      boolean returnAllIn, Integer parameterIdIn)
            throws CentrifugeException, GeneralSecurityException {

        StringBuilder myBuffer = new StringBuilder();
        myBuffer.append(SqlTokens.OPEN_WRAPPER);
        String mySelect = returnAllIn ? SqlTokens.SELECT_TOKEN : SqlTokens.SELECT_DISTINCT_TOKEN;
        String myUnion = (returnAllIn && parentIn.getAppendAll()) ? SqlTokens.UNION_ALL_TOKEN : SqlTokens.UNION_TOKEN;

        String myLeftChildAlias = (null != aliasMap) ? getQuotedAlias(leftChildIn.getUuid()) : null;
        String myRightChildAlias = (null != aliasMap) ? getQuotedAlias(rightChildIn.getUuid()) : null;

        List<OpMapItem> myMapItems = parentIn.getMapItems();

        Map<String, ColumnDef> myLeftColumnMap = new HashMap<String, ColumnDef>();
        Map<String, ColumnDef> myRightColumnMap = new HashMap<String, ColumnDef>();
        Map<String, OpMapItem> myItemMappingMap = new HashMap<String, OpMapItem>();

        List<ColumnDef> myLeftChildColumns = DataSetUtil.getResultColumns(leftChildIn, joinColumns);
        List<ColumnDef> myRightChildColumns = DataSetUtil.getResultColumns(rightChildIn, joinColumns);

        for (ColumnDef myColumn : myLeftChildColumns) {
            myLeftColumnMap.put(myColumn.getLocalId(), myColumn);
        }
        for (ColumnDef myColumn : myRightChildColumns) {
            myRightColumnMap.put(myColumn.getLocalId(), myColumn);
        }
        for (OpMapItem myMapItem : myMapItems) {

            String myLeftKey = myMapItem.getLeftColumnLocalId();
            String myRightKey = myMapItem.getRightColumnLocalId();
            myItemMappingMap.put(myLeftKey, myMapItem);
            myItemMappingMap.put(myRightKey, myMapItem);
        }
        myBuffer.append(mySelect);
        if (parentIn.getMapType() == OpMapType.JOIN) {
            buildJoinSelectList(myBuffer, myLeftChildAlias, myRightChildAlias,
                                myLeftChildColumns, myRightChildColumns, parameterIdIn);
            myBuffer.append(SqlTokens.FROM_TOKEN);

            myBuffer.append(SqlTokens.OPEN_PAREN);
            myBuffer.append(leftQueryIn);
            myBuffer.append(SqlTokens.CLOSE_PAREN_SPACE);
            // myBuffer.append( AS_TOKEN );
            myBuffer.append(myLeftChildAlias);

            if (parentIn.getJoinType() == OpJoinType.LEFT_OUTER) {
                myBuffer.append(SqlTokens.LEFT_JOIN_TOKEN);
            } else if (parentIn.getJoinType() == OpJoinType.RIGHT_OUTER) {
                myBuffer.append(SqlTokens.RIGHT_JOIN_TOKEN);
            } else {
                myBuffer.append(SqlTokens.INNER_JOIN_TOKEN);
            }

            myBuffer.append(SqlTokens.OPEN_PAREN);
            myBuffer.append(rightQueryIn);
            myBuffer.append(SqlTokens.CLOSE_PAREN_SPACE);
            myBuffer.append(myRightChildAlias);

            buildJoinCriteria(myBuffer, myLeftChildAlias, myRightChildAlias,
                                myMapItems, myLeftColumnMap, myRightColumnMap);

        } else {

            /*
            <all left child columns in order>
            followed by <nulls for unmapped right child columns>
             */
            buildUnionDirectList(myBuffer, myLeftChildAlias, myLeftChildColumns,
                                    myRightChildColumns, myItemMappingMap, parameterIdIn);

            myBuffer.append(SqlTokens.FROM_PAREN);
            myBuffer.append(leftQueryIn);
            myBuffer.append(SqlTokens.CLOSE_PAREN_SPACE);
            myBuffer.append(myLeftChildAlias);

            myBuffer.append(myUnion);
            myBuffer.append(mySelect);
            /*
            <all mapped right child columns in left column order with nulls for unmapped left child columns>
            followed by <unmapped right child columns in order>
             */
            buildUnionMappedList(myBuffer, myRightChildAlias, myRightChildColumns,
                                    myLeftChildColumns, myItemMappingMap, myRightColumnMap, parameterIdIn);

            myBuffer.append(SqlTokens.FROM_PAREN);
            myBuffer.append(rightQueryIn);
            myBuffer.append(SqlTokens.CLOSE_PAREN_SPACE);
            myBuffer.append(myRightChildAlias);
        }
        myBuffer.append(SqlTokens.CLOSE_WRAPPER);
        myBuffer.append(Integer.toString(wrapperId++));
        myBuffer.append(' ');
        return myBuffer;
    }

    private StringBuilder buildJoinSelectList(StringBuilder bufferIn, String myLeftChildAlias, String myRightChildAlias,
                                              List<ColumnDef> myLeftChildCols, List<ColumnDef> myRightChildCols,
                                              Integer parameterIdIn) {

        StringBuilder myBuffer = (null != bufferIn) ? bufferIn : new StringBuilder();

        if (null != parameterIdIn) {

            myBuffer.append(parameterIdIn.toString());
            myBuffer.append(SqlTokens.AS_PARAMETER_ID_COMMA);
        }
        if ((null != myLeftChildCols) && !myLeftChildCols.isEmpty()) {

            for (ColumnDef myColumn : myLeftChildCols) {

                String colAlias = (null != aliasMap) ? getQuotedAlias(myColumn.getLocalId()) : null;
                String myAlias = myLeftChildAlias;

                myBuffer.append(myAlias);
                myBuffer.append(SqlTokens.DOT);
                myBuffer.append(colAlias);
                myBuffer.append(SqlTokens.AS_TOKEN);
                myBuffer.append(colAlias);
                myBuffer.append(SqlTokens.COMMA_SPACE);
            }
        }
        if ((null != myRightChildCols) && !myRightChildCols.isEmpty()) {

            for (ColumnDef myColumn : myRightChildCols) {

                String colAlias = (null != aliasMap) ? getQuotedAlias(myColumn.getLocalId()) : null;
                String myAlias = myRightChildAlias;

                myBuffer.append(myAlias);
                myBuffer.append(SqlTokens.DOT);
                myBuffer.append(colAlias);
                myBuffer.append(SqlTokens.AS_TOKEN);
                myBuffer.append(colAlias);
                myBuffer.append(SqlTokens.COMMA_SPACE);
            }
            myBuffer.setLength(myBuffer.length() - SqlTokens.COMMA_SPACE.length());

        } else if ((null != myRightChildCols) && !myRightChildCols.isEmpty()) {

            myBuffer.setLength(myBuffer.length() - SqlTokens.COMMA_SPACE.length());

        } else {

            myBuffer.append(SqlTokens.STAR);
        }
        return myBuffer;
    }

    private StringBuilder buildJoinCriteria(StringBuilder bufferIn, String myLeftChildAlias, String myRightChildAlias,
                                           List<OpMapItem> mapItemsIn, Map<String, ColumnDef> leftColumnMapIn,
                                           Map<String, ColumnDef> rightColumnMapIn) {

        StringBuilder myBuffer = (null != bufferIn) ? bufferIn : new StringBuilder();

        myBuffer.append(SqlTokens.ON_TOKEN);
        if (!mapItemsIn.isEmpty()) {
           boolean first = true;

           for (OpMapItem mapItem : mapItemsIn) {
                CsiDataType myCast = getCast(leftColumnMapIn, mapItem);
                String myLeftKey = mapItem.getLeftColumnLocalId();
                String myRightKey = mapItem.getRightColumnLocalId();

                ColumnDef myLeftColumn = leftColumnMapIn.get(myLeftKey);
                ColumnDef myRightColumn = rightColumnMapIn.get(myRightKey);
                String myLeftColumnId = myLeftColumn.getLocalId();
                String myRightColumnId = myRightColumn.getLocalId();

                String myLeftAlias = (null != aliasMap) ? getQuotedAlias(myLeftColumnId) : null;
                String myRightAlias = (null != aliasMap) ? getQuotedAlias(myRightColumnId) : null;

                String leftExpr = myLeftChildAlias + SqlTokens.DOT + myLeftAlias;
                String rightExpr = myRightChildAlias + SqlTokens.DOT + myRightAlias;

                leftExpr = castExpression(leftExpr, (null != myLeftColumn) ? myLeftColumn.getCsiType() : null, myCast);
                rightExpr = castExpression(rightExpr, (null != myRightColumn) ? myRightColumn.getCsiType() : null, myCast);

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(SqlTokens.AND_TOKEN);
                }
                myBuffer.append(leftExpr);
                myBuffer.append(getComparingToken(mapItem));
                myBuffer.append(rightExpr);
            }
        } else {

            myBuffer.append(" 1 = 1 ");
        }
        return myBuffer;
    }

    /*
    <all left child columns in order>
    followed by <nulls for unmapped right child columns>
     */
    private StringBuilder buildUnionDirectList(StringBuilder bufferIn, String aliasIn, List<ColumnDef> displayedColumnsIn,
                                              List<ColumnDef> otherColumnsIn, Map<String, OpMapItem> itemMappingMapIn,
                                              Integer parameterIdIn) {

        StringBuilder myBuffer = (null != bufferIn) ? bufferIn : new StringBuilder();

        if (null != parameterIdIn) {

            myBuffer.append(parameterIdIn.toString());
            myBuffer.append(SqlTokens.AS_PARAMETER_ID_COMMA);
        }
        if ((null != displayedColumnsIn) && !displayedColumnsIn.isEmpty() && (null != itemMappingMapIn)) {
           boolean first = true;

           // <all left child columns in order>
            for (ColumnDef myColumn : displayedColumnsIn) {
                String myKey = myColumn.getLocalId();
                String myColumnAlias = (null != aliasMap) ? getQuotedAlias(myKey) : null;
                String myExpression = (null != aliasIn) ? (aliasIn + SqlTokens.DOT + myColumnAlias) : myColumnAlias;
                OpMapItem myMapItem = itemMappingMapIn.get(myKey);

                if (null != myMapItem) {

                    myExpression = castExpression(myExpression, myColumn.getCsiType(), myMapItem.getCastToType());
/*
                } else {

                    myExpression = castExpression(myExpression, myColumn.getDataType(), myColumn.getCastToType());
*/
                }
                if (first) {
                   first = false;
                } else {
                   myBuffer.append(SqlTokens.COMMA_SPACE);
                }
                myBuffer.append(myExpression);
                myBuffer.append(SqlTokens.AS_TOKEN);
                myBuffer.append(myColumnAlias);
            }
            if ((null != otherColumnsIn) && !otherColumnsIn.isEmpty()) {
               // <nulls for unmapped right child columns>
                for (ColumnDef myColumn : otherColumnsIn) {

                    String myKey = myColumn.getLocalId();

                    if (null == itemMappingMapIn.get(myKey)) {

                        String myColumnAlias = (null != aliasMap) ? getQuotedAlias(myKey) : null;

                        if (first) {
                           first = false;
                        } else {
                           myBuffer.append(SqlTokens.COMMA_SPACE);
                        }
                        myBuffer.append(connectionFactory.castNull(myColumn.getCsiType()));
                        myBuffer.append(SqlTokens.AS_TOKEN);
                        myBuffer.append(myColumnAlias);
                    }
                }
            }
        }
        return myBuffer;
    }

    /*
    <all mapped right child columns in left column order
     with nulls for unmapped left child columns>
    followed by <unmapped right child columns in order>
     */
    private StringBuilder buildUnionMappedList(StringBuilder bufferIn, String aliasIn, List<ColumnDef> displayedColumnsIn,
                                              List<ColumnDef> otherColumnsIn, Map<String, OpMapItem> itemMappingMapIn,
                                              Map<String, ColumnDef> columnMapIn, Integer parameterIdIn)
    {

        StringBuilder myBuffer = (null != bufferIn) ? bufferIn : new StringBuilder();

        if (null != parameterIdIn) {

            myBuffer.append(parameterIdIn.toString());
            myBuffer.append(SqlTokens.AS_PARAMETER_ID_COMMA);
        }
        if ((null != otherColumnsIn) && !otherColumnsIn.isEmpty() && (null != itemMappingMapIn)) {
           boolean first = true;
            // <all mapped right child columns in left column order
            //  with nulls for unmapped left child columns>
            for (ColumnDef myOtherColumn : otherColumnsIn) {
                String myKey = myOtherColumn.getLocalId();
                OpMapItem myMapItem = itemMappingMapIn.get(myKey);

                if (first) {
                   first = false;
                } else {
                   myBuffer.append(SqlTokens.COMMA_SPACE);
                }
                if (myMapItem == null) {
                   myBuffer.append(connectionFactory.castNull(myOtherColumn.getCsiType()));
                } else {
                    String myLocalId = myMapItem.getOtherColumnId(myKey);
                    ColumnDef myColumn = columnMapIn.get(myLocalId);
                    String myColumnAlias = (null != aliasMap) ? getQuotedAlias(myLocalId) : null;
                    String myExpression = (null != aliasIn) ? (aliasIn + SqlTokens.DOT + myColumnAlias) : myColumnAlias;
                    CsiDataType myCastToType = myMapItem.getCastToType();

                    myExpression = castExpression(myExpression, (null != myColumn) ? myColumn.getCsiType() : null,
                                                (null != myCastToType) ? myCastToType : myOtherColumn.getCsiType());
                    myBuffer.append(myExpression);
                    myBuffer.append(SqlTokens.AS_TOKEN);
                    myBuffer.append(myColumnAlias);
                }
            }
            // <unmapped right child columns in order>
            for (ColumnDef myColumn : displayedColumnsIn) {
                String myKey = myColumn.getLocalId();
                OpMapItem myMapItem = itemMappingMapIn.get(myKey);

                if (null == myMapItem) {
                    String myColumnAlias = (null != aliasMap) ? getQuotedAlias(myKey) : null;
                    String myExpression = (null != aliasIn) ? (aliasIn + SqlTokens.DOT + myColumnAlias) : myColumnAlias;

                    //  myExpression = castExpression(myExpression, myColumn.getDataType(), myColumn.getCastToType());
                    if (first) {
                       first = false;
                    } else {
                       myBuffer.append(SqlTokens.COMMA_SPACE);
                    }
                    myBuffer.append(myExpression);

                    if (null != myColumnAlias) {

                        myBuffer.append(SqlTokens.AS_TOKEN);
                        myBuffer.append(myColumnAlias);
                    }
                }
            }
        }
        return myBuffer;
    }

    // TODO:

    // TODO:


    // TODO: Handle installed tables for custom queries ???

    // TODO:

    // TODO:

    private StringBuilder createTableOpQuery(SqlTableDef tableDefIn, DataSetOp parentIn,
                                            boolean returnAllIn, boolean isLeftIn, Integer parameterIdIn)
            throws CentrifugeException {

        StringBuilder myBuffer = new StringBuilder();

        // handle case of custom query with SQL CALL or datasource is non-SQL
        if (tableDefIn.getIsCustom()) {
            if (connectionFactory.getBlockCustomQueries().booleanValue()) {

                throw new CentrifugeException("Custom query not allowed with this data source!");

            } else {

                QueryDef customQuery = tableDefIn.getCustomQuery();
                String myQuery = parameterFactory.isLinkupRequest()
                        ? (null != customQuery.getLinkupText())
                        ? customQuery.getLinkupText()
                        : QueryHelper.genLinkupQuery(customQuery.getQueryText())
                        : customQuery.getQueryText();
                boolean myTerminatorFlag = (SqlTokens.SEMI_COLON == myQuery.charAt(myQuery.length() - 1));
                if (!ConnectionFactoryManager.isAdvancedFactory(connectionFactory)
                        || CacheCommands.CALL_PATTERN.matcher(myQuery).find()) {

                    String expandedQuery = applySqlParameters(myQuery, this.parameters);
                    if (null != parameterIdIn) {

                        myBuffer.append(returnAllIn ? SqlTokens.SELECT_TOKEN : SqlTokens.SELECT_DISTINCT_TOKEN);
                        myBuffer.append(parameterIdIn.toString());
                        myBuffer.append(SqlTokens.AS_PARAMETER_ID_COMMA);
                        myBuffer.append(SqlTokens.STAR);
                        myBuffer.append(SqlTokens.FROM_PAREN);
                        if (myTerminatorFlag) {

                            myBuffer.append(expandedQuery.substring(0, expandedQuery.length() - 1));
                            myBuffer.append(SqlTokens.CLOSE_PAREN_TERMINATE);

                        } else {

                            myBuffer.append(expandedQuery);
                            myBuffer.append(SqlTokens.CLOSE_PAREN);
                        }

                    } else {

                        // TODO:

                        // TODO:

                        // TODO:

                        // TODO:

                        if (!returnAllIn) {

                            myBuffer.append(SqlTokens.SELECT_DISTINCT_FROM);
                            myBuffer.append(SqlTokens.OPEN_PAREN);
                            if (myTerminatorFlag) {

                                myBuffer.append(expandedQuery.substring(0, expandedQuery.length() - 1));
                                myBuffer.append(SqlTokens.CLOSE_PAREN_TERMINATE);

                            } else {

                                myBuffer.append(expandedQuery);
                                myBuffer.append(SqlTokens.CLOSE_PAREN);
                            }

                        } else {

                            myBuffer.append(expandedQuery);
                        }
                    }
                    return myBuffer;
                }
            }
        }
        // at this point we have sql based connection that
        // is not using SQL CALL
        List<OpMapItem> mapItems = (null != parentIn) ? parentIn.getMapItems() : null;
        Map<String, ColumnDef> myColumnMap = tableDefIn.getColumnIdMap();
        Map<String, ColumnDef> myFilteredMap = new TreeMap<String, ColumnDef>();
        Map<String, ColumnDef> mySelectedMap = new TreeMap<String, ColumnDef>();
        String myInstalledTableId = (null != tableDefIn) ? tableDefIn.getReferenceId() : null;
        InstalledTable myInstalledTable = (null != myInstalledTableId)
                ? CsiPersistenceManager.findObject(InstalledTable.class, myInstalledTableId, null)
                : null;

        for (Map.Entry<String, ColumnDef> myEntry : myColumnMap.entrySet()) {

            String myKey = myEntry.getKey();
            ColumnDef myColumnDef = myEntry.getValue();

            if (null != myColumnDef) {

                List<ColumnFilter> columnFilters = myColumnDef.getColumnFilters();

                if ((myColumnDef.isSelected()) || ((null != joinColumns) && joinColumns.contains(myKey))) {

                    mySelectedMap.put(myKey, myColumnDef);
                }
                if ((null != columnFilters) && !columnFilters.isEmpty() && (null != columnFilters.get(0))) {

                    myFilteredMap.put(myKey, myColumnDef);
                }
            }
        }
        if (null != mapItems) {

            for (OpMapItem myItem : mapItems) {

                String myKey = isLeftIn
                                ? myItem.getLeftColumnLocalId()
                                : myItem.getRightColumnLocalId();

                if (null != myKey) {

                    ColumnDef myColumnDef = myColumnMap.get(myKey);

                    if (null != myColumnDef) {

                        String myInstalledId = myColumnDef.getReferenceId();

                        if (_doDebug && (null != myInstalledTable)) {

                           LOG.debug("My Installed Column(" + Format.value(myInstalledId) + "): "
                                    + Format.value(myInstalledTable.getColumnByLocalId(myInstalledId)));
                        }
                        mySelectedMap.put(myKey, myColumnDef);
                    }
                }
            }
        }
        myBuffer.append(returnAllIn ? SqlTokens.SELECT_TOKEN : SqlTokens.SELECT_DISTINCT_TOKEN);
        if (null != parameterIdIn) {

            myBuffer.append(parameterIdIn.toString());
            myBuffer.append(SqlTokens.AS_PARAMETER_ID_COMMA);
        }
        if (mySelectedMap.isEmpty()) {
           myBuffer.append(SqlTokens.STAR);
        } else {
           boolean first = true;

           for (Map.Entry<String, ColumnDef> myEntry : mySelectedMap.entrySet()) {
              String myKey = myEntry.getKey();
              ColumnDef myColumnDef = myEntry.getValue();

              if (null != myColumnDef) {
                 String myColumnName = connectionFactory.getQuotedName(myColumnDef.getColumnName());

                 if (first) {
                    first = false;
                 } else {
                    myBuffer.append(SqlTokens.COMMA_SPACE);
                 }
                 myBuffer.append(castExpression(myColumnName, myColumnDef.getCsiType(),
                                                    myColumnDef.getCsiType()));

                 if (null != aliasMap) {
                    myBuffer.append(SqlTokens.AS_TOKEN);
                    myBuffer.append(getQuotedAlias(myKey));
                 }
              }
           }
        }
        myBuffer.append(SqlTokens.FROM_TOKEN);
        if (tableDefIn.getIsCustom()) {
            if (connectionFactory.getBlockCustomQueries().booleanValue()) {

                throw new CentrifugeException("Custom query not allowed with this data source!");

            } else {

                // handle custom query as a series of UNIONs of
                // replicated custom queries with different
                // set of parameters applied
                QueryDef customQuery = tableDefIn.getCustomQuery();
                String query = parameterFactory.isLinkupRequest()
                        ? (null != customQuery.getLinkupText())
                        ? customQuery.getLinkupText()
                        : QueryHelper.genLinkupQuery(customQuery.getQueryText())
                        : customQuery.getQueryText();
                String expandedQuery = applySqlParameters(query, this.parameters);

                myBuffer.append(SqlTokens.OPEN_PAREN);
                myBuffer.append(expandedQuery);
                myBuffer.append(SqlTokens.CLOSE_PAREN_SPACE);
                if (null != aliasMap) {
                    myBuffer.append(getQuotedAlias(tableDefIn.getLocalId()));
                }
            }

        } else {

            String myTableName = connectionFactory.getQualifiedName(tableDefIn);

            myBuffer.append(myTableName);
        }

        // add where clause
        if (!myFilteredMap.isEmpty()) {
            myBuffer.append(SqlTokens.WHERE_TOKEN);
            myBuffer.append(buildFilterClause(myFilteredMap, myColumnMap));
        }
        return myBuffer;
    }

    private StringBuilder createViewQuery(String tableNameIn, InstalledTable installedTableIn,
                                         SqlTableDef tableDefIn, Map<String, FieldDef> fieldMapIn)
            throws CentrifugeException {

        StringBuilder myBuffer = new StringBuilder();
        Map<String, ColumnDef> myColumnMap = tableDefIn.getColumnIdMap();
        Map<String, ColumnDef> myFilteredMap = new TreeMap<String, ColumnDef>();
        Map<String, ColumnDef> mySelectedMap = new TreeMap<String, ColumnDef>();

        for (Map.Entry<String, ColumnDef> myEntry : myColumnMap.entrySet()) {

            String myKey = myEntry.getKey();
            ColumnDef myColumnDef = myEntry.getValue();

            if (null != myColumnDef) {

                List<ColumnFilter> columnFilters = myColumnDef.getColumnFilters();

                if (myColumnDef.isSelected() || ((null != joinColumns) && joinColumns.contains(myKey))) {

                    mySelectedMap.put(myKey, myColumnDef);
                }
                if ((null != columnFilters) && !columnFilters.isEmpty() && (null != columnFilters.get(0))) {

                    myFilteredMap.put(myKey, myColumnDef);
                }
            }
        }
        if (!mySelectedMap.isEmpty()) {

            myBuffer.append(SqlTokens.SELECT_TOKEN);
            myBuffer.append(CacheTokens.CSI_ROW_ID);
            for (Map.Entry<String, ColumnDef> myEntry : mySelectedMap.entrySet()) {

                String myKey = myEntry.getKey();
                ColumnDef myColumnDef = myEntry.getValue();
                String myReference = myColumnDef.getReferenceId();
                InstalledColumn myColumn = (null != myReference)
                        ? installedTableIn.getColumnByLocalId(myReference)
                        : installedTableIn.getColumnByFieldName(myColumnDef.getName());

                if (null != myColumn) {

                    String myColumnName = myColumn.getColumnName();
                    FieldDef myField = fieldMapIn.get(myKey);
                    if (null != myField) {

                        String myAlias = CacheUtil.getColumnName(myField);

                        myBuffer.append(SqlTokens.COMMA_SPACE);
                        myBuffer.append(CacheUtil.quote(myColumnName));
                        myBuffer.append(SqlTokens.AS_TOKEN);
                        myBuffer.append(CacheUtil.quote(myAlias));

                    } else {

                        myBuffer.setLength(0);
                        break;
                    }
                }
            }
            if (myBuffer.length() > 0) {

                myBuffer.append(SqlTokens.FROM_TOKEN);
            }

        }
        if (myBuffer.length() == 0) {

            myBuffer.append(SqlTokens.SELECT_ALL_FROM);
        }
        myBuffer.append(CacheUtil.quote(tableNameIn));
        // add where clause
        if (!myFilteredMap.isEmpty()) {
            myBuffer.append(SqlTokens.WHERE_TOKEN);
            myBuffer.append(buildFilterClause(myFilteredMap, myColumnMap));
        }
        return myBuffer;
    }

    private StringBuilder buildFilterClause(Map<String, ColumnDef> filteredCols, Map<String, ColumnDef> tableColMap)
            throws CentrifugeException {

        return buildFilterClause(filteredCols, tableColMap, true);
    }

    private StringBuilder buildFilterClause(Map<String, ColumnDef> filteredColumnMapIn,
                                           Map<String, ColumnDef> tableColumnMapIn, boolean doSubstitutionIn)
            throws CentrifugeException {

        StringBuilder myBuffer = new StringBuilder();
        Map<String, QueryParameterDef> myParameterMap = new HashMap<String, QueryParameterDef>();

        if (null != parameters) {

            for (QueryParameterDef myParameter : parameters) {

                myParameterMap.put(myParameter.getLocalId(), myParameter);
            }
        }
        if ((null != filteredColumnMapIn) && !filteredColumnMapIn.isEmpty()) {
           boolean firstFilterColumn = true;

            for (Map.Entry<String, ColumnDef> myEntry : filteredColumnMapIn.entrySet()) {

                String myKey = myEntry.getKey();
                ColumnDef myColumn = tableColumnMapIn.get(myKey);
                List<ColumnFilter> filters = myColumn.getColumnFilters();

                if ((null != filters) && !filters.isEmpty()) {
                   boolean firstFilter = true;

                   for (ColumnFilter filter : filters) {
                        FilterOperandType operandType = filter.getOperandType();
                        FilterOperatorType operator = filter.getOperator();
                        String myRightChildlocalId = filter.getLocalColumnId();
                        String paramLocalId = filter.getParamLocalId();
                        List<String> staticValues = filter.getStaticValues();

                        if (firstFilter) {
                           firstFilter = false;
                            if (firstFilterColumn) {
                                firstFilterColumn = false;
                            } else {
                                myBuffer.append(SqlTokens.AND_TOKEN);
                            }
                        } else {
                           myBuffer.append(SqlTokens.AND_TOKEN);
                        }
                        if (filter.exclude) {
                            myBuffer.append(SqlTokens.NOT_PAREN);
                        } else {

                            myBuffer.append(SqlTokens.OPEN_PAREN);
                        }
                        if (operandType == null) {
                            myBuffer.append(buildValueFilterExpression(myColumn, operator, null, null));

                        } else {
                            if (FilterOperandType.STATIC == operandType) {
                                myBuffer.append(buildValueFilterExpression(myColumn, operator, staticValues, null));

                            } else if (FilterOperandType.COLUMN == operandType) {
                                ColumnDef myOtherColumn = tableColumnMapIn.get(myRightChildlocalId);
                                myBuffer.append(buildColToColFilterExpression(myColumn, operator, myOtherColumn));

                            } else if (FilterOperandType.PARAMETER == operandType) {
                                QueryParameterDef param = myParameterMap.get(paramLocalId);
                                if (param == null) {
                                    throw new CentrifugeException("Column filter is missing parameter info for column: "
                                            + myColumn.getColumnName());
                                }
                                if (doSubstitutionIn) {

                                    myBuffer.append(buildValueFilterExpression(myColumn, operator,
                                                                                param.getValues(), param.getType()));

                                } else {

                                    myBuffer.append(buildParameterFilterExpression(myColumn, operator,
                                                                                    param.getName(), param.getType()));
                                }
                            }
                        }
                        myBuffer.append(SqlTokens.CLOSE_PAREN);
                    }
                }
            }
        }
        return myBuffer;
    }

    private StringBuilder buildColToColFilterExpression(ColumnDef columnOneIn,
                                                       FilterOperatorType operatorIn, ColumnDef columnTwoIn) {
        StringBuilder myBuffer = new StringBuilder();
        String myTemplate = operationMap.get(operatorIn);

        String myOperandOne = connectionFactory.getQuotedName(columnOneIn.getColumnName());
        String myOperandTwo = connectionFactory.getQuotedName(columnTwoIn.getColumnName());

        myBuffer.append(String.format(myTemplate, myOperandOne, castExpression(myOperandTwo, columnTwoIn, columnOneIn)));
        return myBuffer;
    }

    private StringBuilder buildValueFilterExpression(ColumnDef columnIn, FilterOperatorType operatorIn,
                                                    List<String> valueListIn, CsiDataType parameterTypeIn) {

        String myTemplate = operationMap.get(operatorIn);
        String myOperand = connectionFactory.getQuotedName(columnIn.getColumnName());
        CsiDataType myCsiType = columnIn.getCsiType();
        boolean myQuoteFlag = requiresQuotes(myCsiType, operatorIn);
        StringBuilder myBuffer = new StringBuilder();

        if (operatorIn.hasOneOperand()) {

            myBuffer.append(String.format(myTemplate, myOperand));
            return myBuffer;
        }
        if (operatorIn == FilterOperatorType.IN_LIST) {

            myBuffer.append(buildInClause(myOperand, myTemplate, valueListIn, myQuoteFlag, columnIn, parameterTypeIn));

        } else {

            myBuffer.append(buildOrClause(myOperand, myTemplate, valueListIn, myQuoteFlag, columnIn, parameterTypeIn));
        }
        return myBuffer;
    }

    private StringBuilder buildParameterFilterExpression(ColumnDef columnIn, FilterOperatorType operatorIn,
                                                        String parameterNameIn, CsiDataType parameterTypeIn) {

        String myTemplate = operationMap.get(operatorIn);
        String myOperand = connectionFactory.getQuotedName(columnIn.getColumnName());
        StringBuilder myBuffer = new StringBuilder();

        if (operatorIn.hasOneOperand()) {

            myBuffer.append(String.format(myTemplate, myOperand));

        } else {
/*
            CsiDataType myDataType = columnIn.getCastToType();
            String myExpression = ((null != parameterTypeIn) && (!parameterTypeIn.equals(myDataType)))
                                        ? castExpression( "{:" + parameterNameIn + "}", null, columnIn)
                                        : "{:" + parameterNameIn + "}";

            myBuffer.append(String.format(myTemplate, myOperand, myExpression));
*/
            myBuffer.append(String.format(myTemplate, myOperand, "{:" + parameterNameIn + "}"));
        }
        return myBuffer;
    }

    private StringBuilder buildOrClause(String operandIn, String templateIn, List<String> valueListIn,
                                       boolean quoteFlagIn, ColumnDef targetIn, CsiDataType parameterTypeIn) {

        String equalsTemplate = operationMap.get(FilterOperatorType.EQUALS);
        String isNullTemplate = operationMap.get(FilterOperatorType.ISNULL);
        CsiDataType myDataType = targetIn.getCsiType();
        StringBuilder myBuffer = new StringBuilder();

        if ((valueListIn == null) || valueListIn.isEmpty()) {

            if (templateIn.equals(equalsTemplate)) {

                myBuffer.append(String.format(isNullTemplate, operandIn, SqlTokens.NULL_INDICATOR));

            } else {

                myBuffer.append(String.format(templateIn, operandIn, SqlTokens.NULL_INDICATOR));
            }

        } else {

            int cnt = 0;
            if (!valueListIn.isEmpty()) {

                myBuffer.append(SqlTokens.OPEN_PAREN);
            }
            for (String myValue : valueListIn) {

                if (cnt > 0) {

                    myBuffer.append(SqlTokens.OR_TOKEN);
                }
                cnt++;

                if (null == myValue) {

                    if (templateIn.equals(equalsTemplate)) {

                        myBuffer.append(String.format(isNullTemplate, operandIn, SqlTokens.NULL_INDICATOR));

                    } else {

                        myBuffer.append(String.format(templateIn, operandIn, SqlTokens.NULL_INDICATOR));
                    }

                } else {

                    if (requiresDateEscaping(myDataType, myValue)){

                        myValue = escapeDateValue(myValue);

                    } else if (requiresTimeEscaping(myDataType, myValue)){

                        myValue = escapeTimeValue(myValue);

                    } else if (requiresDateTimeEscaping(myDataType, myValue)){

                        myValue = escapeDateTimeValue(myValue);

                    } else if (quoteFlagIn || ((CsiDataType.String != myDataType) && notNumeric(myValue))) {

                        myValue = getQuotedTextValue(myValue);

                    } else if (requiresEscaping(myDataType, myValue)) {

                        myValue = escapeTextValue(myValue);
                    }
//                    myBuffer.append(String.format(templateIn, operandIn, castExpression(myValue, null, targetIn)));
                    myBuffer.append(String.format(templateIn, operandIn, myValue));
                }
            }
            if (!valueListIn.isEmpty()) {

                myBuffer.append(SqlTokens.CLOSE_PAREN);
            }
        }
        return myBuffer;
    }

    private StringBuilder buildInClause(String operandIn, String templateIn, List<String> valueListIn,
                                       boolean quoteFlagIn, ColumnDef targetIn, CsiDataType parameterTypeIn) {

        StringBuilder myOperandTwo = new StringBuilder();
        StringBuilder myBuffer = new StringBuilder();
        CsiDataType myDataType = targetIn.getCsiType();

        if ((valueListIn == null) || valueListIn.isEmpty()) {

            myBuffer.append(String.format(templateIn, operandIn, SqlTokens.NULL_INDICATOR));

        } else {
           boolean first = true;

            for (String myValue : valueListIn) {
               if (first) {
                  first = false;
               } else {
                  myOperandTwo.append(SqlTokens.COMMA_SPACE);
               }

                if ((null == myValue) || SqlTokens.NULL_STRING.equals(myValue) || SqlTokens.EMPTY_STRING.equals(myValue)) {

                    myOperandTwo.append(SqlTokens.NULL_INDICATOR);

                } else {

                    if (requiresDateEscaping(myDataType, myValue)){

                        myValue = escapeDateValue(myValue);

                    } else if (requiresTimeEscaping(myDataType, myValue)){

                        myValue = escapeTimeValue(myValue);

                    } else if (requiresDateTimeEscaping(myDataType, myValue)){

                        myValue = escapeDateTimeValue(myValue);

                    } else if (quoteFlagIn || ((CsiDataType.String != myDataType) && notNumeric(myValue))) {

                        myValue = getQuotedTextValue(myValue);

                    } else if (requiresEscaping(myDataType, myValue)) {

                        myValue = escapeTextValue(myValue);
                    }
//                    myOperandTwo.append(castExpression(myValue, null, targetIn));
                    myOperandTwo.append(myValue);
                }
            }
            myBuffer.append(String.format(templateIn, operandIn, myOperandTwo));
        }
        return myBuffer;
    }

    public final String applySqlParameters(final String sqlIn, final List<QueryParameterDef> parametersIn) {

        String myExpandedSql = (null != sqlIn) ? sqlIn.trim() : null;

        if ((null != sqlIn) && (0 < sqlIn.length()) && (parametersIn != null) && !parametersIn.isEmpty()) {

            for (final QueryParameterDef myParameter : parametersIn) {
                final StringBuilder myExpandedSqlBuffer = new StringBuilder(SqlTokens.EMPTY_STRING);
                List<String> myValues = myParameter.getValues().isEmpty() ? myParameter.getDefaultValues() : myParameter.getValues();
                String[] myChoices = null;

                for (final String myValue : myValues) {
                    if (myExpandedSqlBuffer.length() > 0) {
                        myExpandedSqlBuffer.append(SqlTokens.COMMA_SPACE);
                    }

                    if ((null == myValue) || SqlTokens.NULL_STRING.equals(myValue) || SqlTokens.EMPTY_STRING.equals(myValue)) {

                        if (!myValues.isEmpty() || (CsiDataType.String != myParameter.getType())) {

                            myExpandedSqlBuffer.append(SqlTokens.NULL_INDICATOR);

                        } else {

                            myChoices = new String[]{ SqlTokens.NULL_INDICATOR, SqlTokens.NULL_INDICATOR, SqlTokens.NULL_INDICATOR, SqlTokens.NULL_INDICATOR };
                        }

                    } else {

                        switch (myParameter.getType()) {

                            case String:
                                if (myValues.isEmpty()) {
                                   myChoices = new String[]{getQuotedTextValue(SqlTokens.SQL_WILD_CARD + myValue + SqlTokens.SQL_WILD_CARD),
                                         getQuotedTextValue(SqlTokens.SQL_WILD_CARD + myValue),
                                         getQuotedTextValue(myValue + SqlTokens.SQL_WILD_CARD),
                                         getQuotedTextValue(myValue)};
                                } else {
                                    myExpandedSqlBuffer.append(getQuotedTextValue(myValue));
                                }
                                break;

                            case Date:

                                myExpandedSqlBuffer.append(escapeDateValue(myValue));
                                break;

                            case Time:

                                myExpandedSqlBuffer.append(escapeTimeValue(myValue));
                                break;

                            case DateTime:

                                myExpandedSqlBuffer.append(escapeDateTimeValue(myValue));
                                break;

                            default:

                                myExpandedSqlBuffer.append(myValue);
                                break;
                        }
                    }
                }

                if (myValues.isEmpty()) {

                    if (CsiDataType.String == myParameter.getType()) {

                        myChoices = new String[]{ SqlTokens.NULL_INDICATOR, SqlTokens.NULL_INDICATOR, SqlTokens.NULL_INDICATOR, SqlTokens.NULL_INDICATOR };

                    } else {

                        myExpandedSqlBuffer.append(SqlTokens.NULL_INDICATOR);
                    }
                }

                if (null != myChoices) {

                    myExpandedSql = myExpandedSql.replaceAll("(?i)%\\{:" + myParameter.getName() + "\\}%",
                                                                Matcher.quoteReplacement(myChoices[0]));
                    myExpandedSql = myExpandedSql.replaceAll("(?i)%\\{:" + myParameter.getName() + "\\}",
                                                                Matcher.quoteReplacement( myChoices[1]));
                    myExpandedSql = myExpandedSql.replaceAll("(?i)\\{:" + myParameter.getName() + "\\}%",
                                                                Matcher.quoteReplacement(myChoices[2]));
                    myExpandedSql = myExpandedSql.replaceAll("(?i)\\{:" + myParameter.getName() + "\\}",
                                                                Matcher.quoteReplacement(myChoices[3]));
                } else {

                    myExpandedSql = myExpandedSql.replaceAll("(?i)\\{:" + myParameter.getName() + "\\}",
                                                            Matcher.quoteReplacement(myExpandedSqlBuffer.toString()));
                }
            }
        }
        return myExpandedSql;
    }

    private String getQuotedAlias(String idIn) {
        return connectionFactory.getQuotedName(aliasMap.get(idIn));
    }

    private String getQuotedTextValue(String value) {
        return (null != value) ? "'" + escapeTextValue(value) + "'" : null;
    }

    private String getNullSelectString() {
        return connectionFactory.getSelectNullString();
    }

    private String escapeTextValue(String value) {
        if ((value == null) || (value.length() == 0)) {
            return value;
        }
        return value.replace("'", "''");
    }

    private String getComparingToken(OpMapItem mapItemIn) {

        ComparingToken myToken = (null != mapItemIn) ? mapItemIn.getComparingToken() : null;

        return (null != myToken) ? myToken.getSqlSymbol() : ComparingToken.EQ.getSqlSymbol();
    }

    private String castExpression(String expressionIn, ColumnDef sourceIn, ColumnDef targetIn) {

        String myExpression = (null != connectionFactory)
                ? connectionFactory.castExpression(expressionIn, sourceIn, targetIn)
                : null;

        return (null != myExpression)
                ? myExpression
                : CacheUtil.castExpression(expressionIn, sourceIn.getCsiType().getLegacyType(),
                targetIn.getCsiType().getLegacyType());
    }

   private String castExpression(String expressionIn, CsiDataType currentTypeIn, CsiDataType castToTypeIn) {
      String expression = expressionIn;

      if ((castToTypeIn != null) && (castToTypeIn != currentTypeIn)) {
         CsiDataType myCurrentType = (currentTypeIn == null) ? CsiDataType.String : currentTypeIn;

         if (CsiDataType.Unsupported != castToTypeIn) {
            expression = connectionFactory.castExpression(expressionIn, castToTypeIn);
         }
         if (expression == null) {
            expression = CacheUtil.castExpression(expressionIn, myCurrentType.getLegacyType(),
                                                  castToTypeIn.getLegacyType());
         }
      }
      return expression;
   }

    private CsiDataType getCast(Map<String, ColumnDef> colMapIn, OpMapItem mapItemIn, ColumnDef columnIn) {

        CsiDataType myDataType = (null != mapItemIn) ? getCast(colMapIn, mapItemIn) : null;

        return (null != myDataType) ? myDataType : ((null != columnIn) ? columnIn.getCsiType() : null);
    }

    private CsiDataType getCast(Map<String, ColumnDef> colMapIn, OpMapItem mapItemIn) {

        CsiDataType myDesiredType = null;

        if (null != mapItemIn) {

            myDesiredType = mapItemIn.getCastToType();

            if ((null == myDesiredType) && (null != colMapIn)) {

                String myLeftId = mapItemIn.getLeftColumnLocalId();
                ColumnDef myColumn = (null != myLeftId) ? colMapIn.get(myLeftId) : null;

                myDesiredType = (null != myColumn) ? myColumn.getCsiType() : null;
            }
        }
        return myDesiredType;
    }

    private String escapeDateTimeValue(String value) {

        value = SqlTokens.TIME_STAMP_ESCAPE.replace("_", value);
        value = value.replace("''", "'");
        return value;
    }

    private String escapeDateValue(String value) {

        value = SqlTokens.DATE_ESCAPE.replace("_", value);
        value = value.replace("''", "'");
        return value;
    }

    private String escapeTimeValue(String value) {

        value = SqlTokens.TIME_ESCAPE.replace("_", value);
        value = value.replace("''", "'");
        return value;
    }

    private boolean requiresQuotes(CsiDataType type, FilterOperatorType operator) {
        return ((type == CsiDataType.String) && (operator != FilterOperatorType.BEGINS_WITH)
                && (operator != FilterOperatorType.ENDS_WITH) && (operator != FilterOperatorType.CONTAINS));
    }

    private boolean requiresEscaping(CsiDataType csiColType, String value) {
        if ((value == null) || (value.length() == 0)) {
            return false;
        }
        if ((CsiDataType.String == csiColType) || notNumeric(value)) {
            return value.contains("'");
        }
        return false;
    }

   private boolean requiresDateEscaping(CsiDataType csiColType, String value) {
//        if ((CsiDataType.Date == csiColType) && DateUtil.validDate(value)) {
      return ((value != null) && (value.length() != 0) && (CsiDataType.Date == csiColType));
   }

   private boolean requiresTimeEscaping(CsiDataType csiColType, String value) {
//        if ((CsiDataType.Time == csiColType) && DateUtil.validTime(value)) {
      return ((value != null) && (value.length() != 0) && (CsiDataType.Time == csiColType));
   }

   private boolean requiresDateTimeEscaping(CsiDataType csiColType, String value) {
//        if ((CsiDataType.DateTime == csiColType) && DateUtil.validDateTime(value)) {
      return ((value != null) && (value.length() != 0) && (CsiDataType.DateTime == csiColType));
   }

    private boolean notNumeric(String valueIn) {

        try {

            new Double(valueIn);
            return false;

        } catch (Exception IGNORE) {

            return true;
        }
    }
}
