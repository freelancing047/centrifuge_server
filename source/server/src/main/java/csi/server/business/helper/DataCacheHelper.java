package csi.server.business.helper;

// TODO: BROKEN ???

// TODO: CASTING AND COLUMN ORDER

// TODO: Look over carefully for proper handling of installed tables

// TODO: Fixup Security for SPIN-OFF

// TODO: SEE HOW THIS FITS WITH CASTING

// TODO: Review establishResourceSecurity

// TODO: finalizeSpinoffSecurityData

import static csi.server.common.enumerations.RelationalOperator.IN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

import csi.config.Configuration;
import csi.config.DBConfig;
import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.cachedb.querybuilder.TableQueryBuilder;
import csi.server.business.cachedb.querybuilder.TimelineQueryBuilder;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.FilterActionsService;
import csi.server.common.dto.FieldConstraints;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.CsiSecurityException;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSortField;
import csi.server.common.model.visualization.table.VisibleTableField;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;
import csi.server.dao.CsiConnection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.CacheCommands;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.CacheTokens;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.ScrollCallback;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.SQLFactoryImpl;
import csi.server.util.sql.impl.TautologicalPredicate;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.TimelineResult;

public class DataCacheHelper {
   private static final Logger LOG = LogManager.getLogger(DataCacheHelper.class);

    private static final int EVENT_DEFINITION_LIMIT = 100;

    private static String _cacheDbName = null;
    private static String _metaDbName = null;

    /*
     * Note: to avoid direct static calls into other classes that
     * harbor a ref to the Injector, we expect a configuration
     * of listeners to be injected here.
     *
     * We'll take an internal factory approach, where in the constructor
     * we ask the provider for an instance of the Set of listeners;
     * minimizing explicit references to the DI runtime.
     */
    @Inject
    static Provider<Set<DataSyncListener>> ListenerProvider;
    Set<DataSyncListener> syncListeners = null;

    private FilterActionsService filterActionsService;

    public FilterActionsService getFilterActionsService() {
        if (filterActionsService == null) {
            SQLFactoryImpl sqlFactory = new SQLFactoryImpl();
            filterActionsService = new FilterActionsService();
            filterActionsService.setSqlFactory(sqlFactory);
        }
        return filterActionsService;
    }

    public void setFilterActionsService(FilterActionsService filterActionsService) {
        this.filterActionsService = filterActionsService;
    }

    public DataCacheHelper() {

        syncListeners = null;
        if (ListenerProvider != null) {

            syncListeners = ListenerProvider.get();
        }
    }

    public Set<DataSyncListener> getSyncListeners() {
        return syncListeners;
    }

   public static long getRowCount(String dataSource) {
      long rowCount = 0;

      if (dataSource != null) {
         try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
            String myDatabaseName = CsiPersistenceManager.getCacheDatabase();

            rowCount = QueryHelper.countRows(conn, myDatabaseName, "public", dataSource);
         } catch (Exception myException) {
         }
      }
      return rowCount;
   }

   public static List<String> getColumnNameList(Connection conn, String tableName) {
      List<String>columnNames = null;

      try (ResultSet results = getOrderedColumnList(conn, tableName)) {
         columnNames = new ArrayList<String>();

         while (results.next()) {
            columnNames.add(results.getString(1));
         }
      } catch (Exception exception) {
         LOG.error("Problem retrieving column names directly from Installed Table instance", exception);
      }
      return columnNames;
   }

   public static List<ValuePair<String,String>> getColumnList(Connection conn, String tableName) {
      List<ValuePair<String, String>> columns = null;

      try (ResultSet results = getOrderedColumnList(conn, tableName)) {
         columns = new ArrayList<ValuePair<String, String>>();

         while (results.next()) {
            columns.add(new ValuePair<String,String>(results.getString(1), results.getString(2)));
         }
      } catch (Exception exception) {
         LOG.error("Problem retrieving column names directly from Installed Table instance", exception);
      }
      return columns;
   }

    public static String getCacheName() {

        if (null == _cacheDbName) {

            try {

                _cacheDbName = getDatabaseName(CsiPersistenceManager.getCacheConnection());

            } catch (Exception myException) {

                LOG.error("Caught exception obtaining a connection to the cachedb database.", myException);
            }
        }
        return _cacheDbName;
    }

    public static String getMetaName() {

        if (null == _metaDbName) {

            try {

                _metaDbName = getDatabaseName(CsiPersistenceManager.getMetaConnection());

            } catch (Exception myException) {

                LOG.error("Caught exception obtaining a connection to the metadb database.", myException);
            }
        }
        return _metaDbName;
    }

   public static String getDatabaseName(Connection conn) {
      String databaseName = null;
      String sql = "SELECT current_database()";

      try (ResultSet results = QueryHelper.executeSingleQuery(conn, sql, null, null)) {
         if (results.next()) {
            databaseName = results.getString(1);
         }
      } catch (Exception myException) {
         LOG.error("Caught exception retrieving the name of the database.", myException);
      }
      return databaseName;
   }

    public static boolean cacheExists(String uuid) throws CentrifugeException {
        return tableExists(CacheUtil.getCacheTableName(uuid));
    }

   public static boolean tableExists(String tableName) throws CentrifugeException {
      boolean exists = false;

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         try {
            exists = tableExists(conn, tableName);
            conn.commit();
         } catch (SQLException e) {
            SqlUtil.quietRollback(conn);
            throw new CentrifugeException(e);
         }
      } catch (Exception e) {
         throw new CentrifugeException(e);
      }
      return exists;
   }

   public static boolean tableExists(Connection conn, String tableName) {
      boolean exists = false;

      try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
         exists = rs.next();
      } catch (Throwable e) {
         LOG.warn(String.format("Error checking cache for table %s", tableName), e);
      }
      return exists;
   }

    public static ResultSet getCacheData(String tableNameIn, Connection conn, String dvUuid, boolean randomAccess)
            throws CentrifugeException {
        return getCacheData(conn, dvUuid, tableNameIn, null, null, null, -1, -1, randomAccess);
    }

    public static ResultSet getCacheData(Connection conn, String dvUuid, boolean randomAccess)
            throws CentrifugeException {
        return getCacheData(conn, dvUuid, null, null, null, -1, -1, randomAccess);
    }

//    public ResultSet getFilteredCacheData(Connection conn, String dvUuid, VisualizationDef vizdef,
//                                          boolean randomAccess) throws CentrifugeException, SQLException {
//        String filter = getQueryFilter(dvUuid, vizdef);
//
//        return getCacheData(conn, dvUuid, null, filter, null, -1, -1, randomAccess);
//    }

    public String getTooltipFilter(String dvUuid, VisualizationDef vizdef, Set<Integer> rowsOfNode)
            throws CsiSecurityException {
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(vizdef.getUuid());
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        String filter = buildTableSelectionFilterClause(broadcastResult.getBroadcastFilter(), broadcastResult.isExcludeRows(), dataView);

        if ((filter != null) && !filter.isEmpty()) {
            filter += " AND ";
        }

        filter += buildTableRowFilterClause(rowsOfNode, false);

        if ((filter != null) && !filter.isEmpty()) {
            filter += " AND ";
        }

        filter += getFilterActionsService().getPredicateSQL(vizdef, dataView);

        return filter;
    }

    public String getQueryFilter(String dvUuid, VisualizationDef vizdef) throws CsiSecurityException {
        BroadcastResult broadcastResult = AbstractBroadcastStorageService.instance().getBroadcast(vizdef.getUuid());
        DataView dataView = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        String filter = buildTableSelectionFilterClause(broadcastResult.getBroadcastFilter(), broadcastResult.isExcludeRows(), dataView);

        if ((filter != null) && !filter.isEmpty()) {
            filter += " AND ";
        }
        filter += getFilterActionsService().getPredicateSQL(vizdef, dataView);

        return filter;
    }

    public static ResultSet getCacheData(Connection conn, String dvUuid, String selectItems, boolean randomAccess)
            throws CentrifugeException {
        return getCacheData(conn, dvUuid, selectItems, null, null, -1, -1, randomAccess);
    }

    public ResultSet getTableViewData(Connection conn, DataView dataviewIn,
                                      TableViewDef tabledef)
            throws CentrifugeException, SQLException {

        TableQueryBuilder queryBuilder = new TableQueryBuilder(dataviewIn, tabledef, null);

        queryBuilder.setFilterActionsService(getFilterActionsService());

        String tableQuery = queryBuilder.buildQuery();
        LOG.debug(tableQuery);
        return QueryHelper.executeSingleQuery(conn, tableQuery, null);
    }

    public ResultSet getTableViewData(Connection conn, DataView dataviewIn,
                                      TableViewDef tabledef, int startRow, int endRow, List<TableViewSortField> clientSortFields)
            throws CentrifugeException, SQLException {

        TableQueryBuilder queryBuilder = new TableQueryBuilder(dataviewIn, tabledef, clientSortFields);
        if ((0 <= startRow) && (startRow < endRow)) {
            queryBuilder.setOffset(startRow);
            queryBuilder.setLimit(endRow - startRow);
        }
        queryBuilder.setFilterActionsService(getFilterActionsService());

        String tableQuery = queryBuilder.buildQuery();
        LOG.debug(tableQuery);
        return QueryHelper.executeSingleQuery(conn, tableQuery, null);
    }

    public ResultSet getTableViewRowIds(Connection conn, DataView dataviewIn, TableViewDef tabledef)
            throws CentrifugeException {

        TableQueryBuilder queryBuilder = new TableQueryBuilder(dataviewIn, tabledef, null);

        List<String> cols = new ArrayList<String>();
        cols.add(SqlUtil.quote(CacheTokens.CSI_ROW_ID));
        queryBuilder.setSelectColumns(cols);
        String tableQuery = queryBuilder.buildQuery();

        return QueryHelper.executeSingleQuery(conn, tableQuery, null);
    }

    public int getCountOfEvents(Connection connection, TimelineQueryBuilder queryBuilder) {
        SelectSQL sql = queryBuilder.buildCountQuery(null, false);
        return sql.scroll(new ScrollCallback<Integer>(){

            Integer result = 0;
            @Override
            public Integer scroll(ResultSet resultSet) throws SQLException {
                resultSet.next();
                int increment = 1;
                //Putting a limit to 100 Event definitions for now, this is a ridiculous amount
                while(increment < EVENT_DEFINITION_LIMIT){
                    try{
                    result += resultSet.getInt(increment);
                    increment++;
                    } catch(Exception e){
                        break;
                    }
                }
                return result;
            }});
    }

    public void getTimelineViewData(Connection connection, TimelineQueryBuilder queryBuilder, ScrollCallback<TimelineResult> callback) throws CentrifugeException {
        queryBuilder.setFilterActionsService(getFilterActionsService());


        //String query = queryBuilder.buildQuery(null, false);
        SelectSQL sql = queryBuilder.buildScrollableQuery(null, false);
        sql.scroll(callback);
//        new ScrollCallback<ResultSet>() {
//            int count = 0;
//            @Override
//            public ResultSet scroll(ResultSet resultSet) throws SQLException {
//                if(count > limit){
//                    results = null;
//                }
//                results.add(resultSet);
//                resultSet.next();
//                count++;
//                return resultSet;
//            }
//        });
    }

    public ResultSet getTimelineView(Connection connection, TimelineQueryBuilder queryBuilder) throws CentrifugeException {
        queryBuilder.setFilterActionsService(getFilterActionsService());

        String query = queryBuilder.buildQuery(null, false);
        LOG.debug(query);
        return QueryHelper.executeSingleQuery(connection, query, null);
    }

    public ResultSet getSingleRow(Connection connection, int rowId, String dvUuid) throws CentrifugeException {
        //queryBuilder.setFilterActionsService(getFilterActionsService());

        StringBuilder builder = new StringBuilder();

        builder.append(String.format(CacheCommands.SELECT_QUERY, "*", CacheUtil.getQuotedCacheTableName(dvUuid)));
        builder.append(" WHERE ").append(CacheTokens.CSI_ROW_ID).append("=" + rowId);

        String query = builder.toString();
        LOG.debug(query);
        return QueryHelper.executeSingleQuery(connection, query, null);
    }

    public String buildFilterClause(VisualizationDef def, DataView dv) {
        String filterClause = getFilterActionsService().getPredicateSQL(def, dv);
        return filterClause;
    }

   public String buildSelectItems(DataModelDef dataModelIn, List<VisibleTableField> list) {
      String result = "*";

      if ((list != null) && !list.isEmpty()) {
         List<VisibleTableField> orderedList = new ArrayList<VisibleTableField>();

         orderedList.addAll(list);

         Collections.sort(orderedList, CacheCommands.VISIBLE_FIELD_COMPARATOR);

         StringBuilder buf = new StringBuilder(CacheUtil.quote(CacheTokens.CSI_ROW_ID));

         for (VisibleTableField vf : orderedList) {
            FieldDef myField = vf.getFieldDef(dataModelIn);
            FieldType ftype = myField.getFieldType();

            if (FieldType.STATIC == ftype) {
               continue;
            }
            buf.append(',').append(CacheUtil.getQuotedColumnName(myField));
         }
         result = buf.toString();
      }
      return result;
   }

   public String buildOrderByClause(DataModelDef dataModelIn, List<TableViewSortField> list) {
      String result = new StringBuilder(CacheUtil.quote(CacheTokens.CSI_ROW_ID)).append(" ASC").toString();

      if (list != null) {
         List<TableViewSortField> orderedList = new ArrayList<TableViewSortField>();

         orderedList.addAll(list);
         Collections.sort(orderedList, CacheCommands.SORTFIELD_COMPARATOR);

         StringBuilder buf = new StringBuilder();
         int i = 0;

         for (TableViewSortField sf : orderedList) {
            FieldDef myField = sf.getFieldDef(dataModelIn);
            FieldType ftype = myField.getFieldType();

            if (FieldType.STATIC == ftype) {
               continue;
            }
            String typedExpr = CacheUtil.makeCastExpression(myField);
            String sortOrder = sf.getSortOrder().name();
            String orderItem = typedExpr + " " + sortOrder;

            if (i++ > 0) {
               buf.append(",");
            }
            buf.append(orderItem);
         }
         // always include a sort by internal_id as the last order by element
         // to guarantee that rows will be returned in the exact same order
         // each time.

         if (i == 0) {
            buf.append(CacheUtil.quote(CacheTokens.CSI_ROW_ID) + " ASC");
         } else {
            buf.append(", " + CacheUtil.quote(CacheTokens.CSI_ROW_ID) + " ASC");
         }
         result = buf.toString();
      }
      return result;
   }

    public static ResultSet getCacheData(Connection conn, String dvUuid, String selectItems, String whereClause,
                                         String orderByClause, int start, int end, boolean randomAccess) throws CentrifugeException {

        return getCacheData(conn, dvUuid, null, selectItems, whereClause, orderByClause, start, end, randomAccess);
    }

    public static ResultSet getCacheData(Connection conn, String dvUuid, String tableNameIn, String selectItems, String whereClause,
                                  String orderByClause, int start, int end, boolean randomAccess) throws CentrifugeException {
    	return getCacheData(conn, dvUuid, tableNameIn, selectItems, whereClause, null, orderByClause, start, end, randomAccess);
    }

    public static ResultSet getCacheData(Connection conn, String dvUuid, String tableNameIn, String selectItems, String whereClause, String groupByClause,
		String orderByClause, int start, int end, boolean randomAccess) throws CentrifugeException {

        String myTableName = (null != tableNameIn) ? CacheUtil.quote(tableNameIn) : CacheUtil.getQuotedCacheTableName(dvUuid);
        Statement stmt = null;
        if ((selectItems == null) || selectItems.trim().isEmpty()) {
            selectItems = "*";
        }

        if ((orderByClause == null) || orderByClause.isEmpty()) {
            orderByClause = CacheUtil.quote(CacheTokens.CSI_ROW_ID) + " ASC";
        }

        String sql = String.format(CacheCommands.SELECT_QUERY, selectItems, myTableName);
        if ((whereClause != null) && !whereClause.isEmpty()) {
            sql = sql + " WHERE " + whereClause;
        }

        if ((groupByClause != null) && !groupByClause.trim().isEmpty()) {
        	sql = sql + " GROUP BY " + groupByClause;
        } else {
	        if (!orderByClause.trim().isEmpty()) {
	            sql = sql + " ORDER BY " + orderByClause;
	        }
        }
        try {

            if (randomAccess) {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } else {
                stmt = conn.createStatement();
            }

            DBConfig config = Configuration.getInstance().getDbConfig();

            stmt.setFetchSize(config.getRecordFetchSize());

            if (start > 0) {
                sql += " OFFSET " + start;
            }

            if ((end > 0) && (end >= start)) {
                int delta = end - start;
                if (delta > 0) {
                    sql += " LIMIT " + delta;
                    try {
                        stmt.setMaxRows(delta);
                    } catch (SQLException e) {
                        LOG.warn("SetMaxRows(i) method not supported.");
                    }
                }
            }
            StopWatch stopWatch = new StopWatch();

            stopWatch.start();
            ResultSet rs = QueryHelper.executeStatement(stmt, sql);
            stopWatch.stop();

            if (LOG.isTraceEnabled()) {
                LOG.trace("Query execution took: " + stopWatch.getTime());
            }

            return rs;
        } catch (SQLException e) {
            throw new CentrifugeException("Failed to get table data: " + sql, e);
        }
    }

    public ResultSet getLinkupCacheDataSet(Connection connectionIn, String dataviewUuidIn) throws CentrifugeException {

        String myQuery = String.format(CacheCommands.SELECT_ALL_QUERY, CacheUtil.getQuotedCacheTableName(dataviewUuidIn));

        ResultSet myResultSet = null;
        Statement myStatement = null;
        try {
            myStatement = connectionIn.createStatement();
            myResultSet = myStatement.executeQuery(myQuery);
        } catch (SQLException myException) {
            throw new CentrifugeException("Failed to get table data subset: " + myQuery, myException);
        }

        return myResultSet;
    }

    public ResultSet getLinkupCacheDataSubset(Connection connectionIn, String dataviewUuidIn, String internalIdsIn) throws CentrifugeException {

        if ((null != internalIdsIn) && (0 < internalIdsIn.length())) {

        } else {

            throw new CentrifugeException("No cache data selected for linkup!");
        }

        String myQuery = String.format(CacheCommands.SELECT_SUBSET_QUERY, CacheUtil.getQuotedCacheTableName(dataviewUuidIn), internalIdsIn);

        ResultSet myResultSet = null;
        Statement myStatement = null;
        try {
            myStatement = connectionIn.createStatement();
            myResultSet = myStatement.executeQuery(myQuery);
        } catch (SQLException myException) {
            throw new CentrifugeException("Failed to get table data subset: " + myQuery, myException);
        }

        return myResultSet;
    }

    public ResultSet getCacheDataSubset(Connection conn, String dvUuid, VisualizationDef vizdef, String internalIds) throws CentrifugeException {
        boolean hasSelectedRows = ((internalIds != null) && !internalIds.isEmpty());

        String sql = (hasSelectedRows) ? String.format(CacheCommands.SELECT_SUBSET_QUERY, CacheUtil.getQuotedCacheTableName(dvUuid),
                internalIds) : String.format(CacheCommands.SELECT_ALL_QUERY, CacheUtil.getQuotedCacheTableName(dvUuid));

        String filter = getQueryFilter(dvUuid, vizdef);
        if ((filter != null) && !filter.isEmpty()) {
            sql = sql + " AND " + filter;
        }

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new CentrifugeException("Failed to get table data subset: " + sql, e);
        }

        return rs;
    }

    /**
     * @param tableSource
     * @return Predicate if broadcast as filter is active, null otherwise.
     */
    public static Predicate getAttachFilterAsConditionPredicate(CacheTableSource tableSource, String vizUuid, DataView dataView) {
        BroadcastResult result = AbstractBroadcastStorageService.instance().getBroadcast(vizUuid);

        if (result==BroadcastResult.EMPTY_BROADCAST_RESULT) {
            return new TautologicalPredicate().negate();
        }

        List<Integer> rowIds = new ArrayList<Integer>();
        for (Integer row : result.getBroadcastFilter().getSelectedItems()) {
            rowIds.add(row);
        }

        if (rowIds.isEmpty()) {
            return new TautologicalPredicate();
        }

        csi.server.util.sql.Column column = tableSource.getIdColumn();
        Predicate predicate = null;
        if (result.isExcludeRows()) {
            rowIds = DataCacheHelper.inverseRows(dataView, rowIds);
        }


        predicate = column.$(IN).list(rowIds, CsiDataType.Integer);
        return predicate;
    }
/*
    private void notifyListenersSyncStart(DataSyncContext syncContext) throws CentrifugeException {
        for (DataSyncListener listener : syncListeners) {
            try {
                listener.onStart(syncContext);
            } catch (Throwable t) {
                logListenerError(listener, t);
                if (t instanceof ConfigurationException) {
                    throw new CentrifugeException("Failed to initialize cache", t);
                }
            }
        }
    }
*/
   public static String buildTableSelectionFilterClause(IntegerRowsSelection selection, boolean exclude, DataView dataview) {
      if (selection.isCleared()) {
         return "TRUE = TRUE";
      }

      // we're in the state where we need to identify which rows to either include or exclude.
      List<Integer> rowids = selection.getSelectedItems();
      StringBuilder builder = new StringBuilder("(").append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));

      if (exclude) {
         rowids = inverseRows(dataview, rowids);
      }
      if (rowids.isEmpty()) {
         rowids.add(Integer.valueOf(-1));
      }
      builder.append(rowids.stream().map(i -> i.toString()).collect(Collectors.joining(",", " IN (", ")")))
             .append(")");
      return builder.toString();
   }

   public static String buildTableSelectionFilterClause(IntPrimitiveSelection selection, boolean exclude, DataView dataView) {
      if (selection.isCleared()) {
         return "TRUE = TRUE";
      }

      // we're in the state where we need to identify which rows to either include or exclude.
      IntCollection rowids = selection.getSelectedItems();
      StringBuilder builder = new StringBuilder("(").append(CacheUtil.quote(CacheTokens.CSI_ROW_ID));

      if (exclude) {
         rowids = inverseRows(dataView, rowids);
      }
      builder.append(rowids.stream().map(i -> i.toString()).collect(Collectors.joining(",", " IN (", ")")))
             .append(")");
      return builder.toString();
   }

    public List<Object> getDistinctGroups(Connection connection, TimelineQueryBuilder queryBuilder) {
        SelectSQL sql = queryBuilder.buildDistinctGroupsQuery(null, false);
        return sql.scroll(new ScrollCallback<List<Object>>(){

            List<Object> groups = new ArrayList<Object>();

            @Override
            public List<Object> scroll(ResultSet resultSet) throws SQLException {

                while(resultSet.next()){
                    try{
                        groups.add(resultSet.getObject(1));
                    } catch(Exception e){
                        break;
                    }
                }
                return groups;
            }});

    }
    public SelectSQL getTimelineDetailedData(Connection connection, TimelineQueryBuilder queryBuilder, ScrollCallback<DetailedTimelineResult> callback) throws CentrifugeException {
        queryBuilder.setFilterActionsService(getFilterActionsService());

        SelectSQL sql = queryBuilder.buildScrollableQuery(null, false);

        return sql;
    }

    public SelectSQL getTimelineBundledDataQuery(Connection connection, TimelineQueryBuilder queryBuilder) throws CentrifugeException {
        queryBuilder.setFilterActionsService(getFilterActionsService());
        queryBuilder.useMinimalColumns();
        SelectSQL sql = queryBuilder.buildScrollableBundleQuery(null, false);
        return sql;
    }


    public static List<Integer> inverseRows(DataView dataView, List<Integer> rowIds) {
        Set<Integer> convertedRows = new TreeSet<Integer>();
        convertedRows.addAll(rowIds);
        verifyDataviewHasSize(dataView);
        List<ValuePair<Long,Long>> ranges = dataView.getInternalIdRanges();

        List<Integer> inverseIds = new ArrayList<Integer>();

        for(ValuePair<Long, Long> range: ranges) {
            int start = range.getValue1().intValue();
            int end = range.getValue2().intValue();
            ContiguousSet<Integer> i2 = ContiguousSet.create(Range.closed(start, end), DiscreteDomain.integers());
            TreeSet<Integer> integers = Sets.newTreeSet(i2);
            integers.removeAll(convertedRows);
            inverseIds.addAll(integers);
        }


        return inverseIds;
    }

    public static void verifyDataviewHasSize(DataView dv) {
       if (dv.getSize() == 0) {
          String sql = "select count(1) as \"count\" from " + CacheUtil.getQuotedCacheTableName(dv.getUuid());

          try (CsiConnection conn = CsiPersistenceManager.getCacheConnection();
               PreparedStatement stat = conn.prepareStatement(sql);
               ResultSet rs = stat.executeQuery()) {
             rs.next();

             int size = rs.getInt(1);

             if (size != 0) {
                dv.setSize(size);
             }
          } catch (Exception e) {
          }
       }
    }

    public static IntCollection inverseRows(DataView dataView, IntCollection rowIds) {
        List<Integer> convertedRows = new ArrayList<Integer>();
        convertedRows.addAll(rowIds);
        List<Integer> inverseIds = inverseRows(dataView, convertedRows);

        IntCollection inverseCollection = new IntCollection();
        inverseCollection.addAll(inverseIds);

        return inverseCollection;
    }

   public String buildTableRowFilterClause(Collection<Integer> rows, boolean exclude) {
      String result = "TRUE = TRUE";

      if (!rows.isEmpty()) {
         StringBuilder builder = new StringBuilder(CacheUtil.quote(CacheTokens.CSI_ROW_ID));

         if (exclude) {
            builder.append(" NOT");
         }
         builder.append(rows.stream().map(i -> i.toString()).collect(Collectors.joining(",", " IN (", ")")));
         result = builder.toString();
      }
      return result;
   }

   public static FieldConstraints getFieldConstraints(String dvUuid, FieldDef currField,
                                                      boolean caseSensitive, int limit)
         throws CentrifugeException {
      StringBuilder selectBuf = new StringBuilder("SELECT ");
      String colname = CacheUtil.makeCastExpression(currField);

      if ((CsiDataType.String == currField.getValueType()) ||
          (CsiDataType.Boolean == currField.getValueType())) {
         if (caseSensitive) {
            selectBuf.append("DISTINCT ").append(colname);
         } else {
            selectBuf.append("DISTINCT ON (lower(").append(colname + "))").append(colname);
         }
      } else {
         selectBuf.append("MIN(").append(colname).append("), MAX(").append(colname).append(')');
      }
      selectBuf.append(" FROM ").append(CacheUtil.getQuotedCacheTableName(dvUuid));

      FieldConstraints newConstraints = new FieldConstraints();
      newConstraints.fieldDef = currField;
      String format = currField.getDisplayFormat();

      try (Connection conn = CsiPersistenceManager.getCacheConnection();
           Statement stmt = conn.createStatement();
           ResultSet rs =  QueryHelper.executeStatement(stmt, selectBuf.toString())) {
         if ((CsiDataType.String == currField.getValueType()) ||
             (CsiDataType.Boolean == currField.getValueType())) {
            List<String> vals = new ArrayList<String>();

            while (rs.next()) {
               Object o = rs.getObject(1);

               if ((o != null) && !"".equals(o)) {
                  vals.add((String) CsiTypeUtil.coerceType(o, CsiDataType.String, format));
               } else {
                  vals.add("");
               }
            }
            // The !(op1 & op2) represents a NOR
            if (!vals.isEmpty() && ((limit <= 0) || (vals.size() <= limit))) {
               newConstraints.availableValues = vals;
               newConstraints.valuesCount = newConstraints.availableValues.size();
            } else {
               newConstraints.availableValues = new ArrayList<String>();
               newConstraints.valuesCount = vals.size();
            }
         } else {
            rs.next();

            Object min = rs.getObject(1);
            Object max = rs.getObject(2);

            if ((min != null) && (max != null)) {
               // HACK: send dates as millis
               if (min instanceof Date) {
                  newConstraints.rangeMin = String.valueOf(((java.util.Date) min).getTime());
                  newConstraints.rangeMax = String.valueOf(((java.util.Date) max).getTime());
               } else {
                  newConstraints.rangeMin = (String) CsiTypeUtil.coerceType(min, CsiDataType.String, format);
                  newConstraints.rangeMax = (String) CsiTypeUtil.coerceType(max, CsiDataType.String, format);
               }
            }
         }
         conn.commit();
      } catch (SQLException sqle) {
         throw new CentrifugeException(
                    "Failed to get constraints for field: " + currField.getFieldName(), sqle);
      } catch (Exception e) {
      }
      return newConstraints;
   }

   public long getTableViewRowCount(DataView dataViewIn, TableViewDef tabledefIn)
         throws CentrifugeException {
      if (tabledefIn == null) {
         throw new IllegalArgumentException("null tableDef");
      } else if (dataViewIn == null) {
         throw new IllegalArgumentException("null DataView");
      }
      long result = 0L;

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         List<String> projection = new ArrayList<String>(Arrays.asList("COUNT(*)"));
         TableQueryBuilder queryBuilder = new TableQueryBuilder(dataViewIn, tabledefIn, null);

         queryBuilder.setFilterActionsService(getFilterActionsService());
         queryBuilder.setSelectColumns(projection);
         queryBuilder.setIncludeOrdering(false);
         String rowCountQuery = queryBuilder.buildQuery();

         try (ResultSet rs = QueryHelper.executeSingleQuery(conn, rowCountQuery, null)) {
            rs.next();
            result = rs.getLong(1);
         }
      } catch (SQLException e) {
         throw new CentrifugeException("Failed to get filtered row count", e);
      }
      return result;
   }

   public static StringBuilder buildLinkupFirstRowQuery(String linkupTableName) {
      return new StringBuilder(" SELECT ")
                       .append(CacheUtil.quote(CacheTokens.CSI_ROW_ID))
                       .append(" FROM ")
                       .append(CacheUtil.quote(linkupTableName))
                       .append(" LIMIT 1 ");
    }

   public static ResultSet getOrderedColumnList(Connection conn, String tableName) throws CentrifugeException {
      StringBuilder myBuffer = new StringBuilder();

        myBuffer.append(" SELECT column_name, data_type, ordinal_position ");
        myBuffer.append(" FROM information_schema.columns WHERE table_name = ");
        CacheUtil.singleQuote(myBuffer, StringUtil.trimAnyQuote(tableName));
        myBuffer.append(" ORDER BY ordinal_position");

        return QueryHelper.executeSingleQuery(conn, myBuffer.toString(), null);
    }

    public static boolean viewExists(Connection conn, String tableName) {
       boolean result = false;
       String sql = String.format(CacheCommands.COUNT_VIEW_QUERY, CacheUtil.quote(tableName));

       try (Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {
          resultSet.next();

          result = (resultSet.getInt(1) > 0);
       } catch (SQLException IGNORE) {
       }
       return result;
    }

    public static boolean actualTableExists(Connection conn, String tableName) {
       boolean result = false;
       String sql = String.format(CacheCommands.COUNT_TABLE_QUERY, CacheUtil.quote(tableName));

       try (Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {
          resultSet.next();

          result = (resultSet.getInt(1) > 0);
       } catch (SQLException IGNORE) {
       }
       return result;
    }

   public static void truncateTable(Connection connectionIn, String tableNameIn) {
      String myCommand = String.format(CacheCommands.TRUNCATE_TABLE_COMMAND, CacheUtil.quote(tableNameIn));

      try (Statement statement = connectionIn.createStatement()) {
         statement.execute(myCommand);
      } catch (SQLException IGNORE) {
      }
   }
}

class ExternalDataExporter implements Runnable {
   private static final Logger LOG = LogManager.getLogger(ExternalDataExporter.class);

   private static boolean _doDebug = LOG.isDebugEnabled();

   private Connection extConn;
    private String streamQuery;
    private List<QueryParameterDef> dataSetParameters;

    public ExternalDataExporter(Connection extConn, String streamQuery, List<QueryParameterDef> dataSetParameters) {
        this.extConn = extConn;
        this.streamQuery = streamQuery;
        this.dataSetParameters = dataSetParameters;
    }

    @Override
   public void run() {
        try {
            int rowCount = QueryHelper.executeSqlUpdate(extConn, streamQuery, dataSetParameters);
            if (_doDebug) {
               LOG.debug("External data export rowCount : " + rowCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class ThreadInterrupter extends TimerTask {

    Thread target = null;

    public ThreadInterrupter(Thread target) {
        this.target = target;
    }

    @Override
    public void run() {
        target.interrupt();
    }

}
