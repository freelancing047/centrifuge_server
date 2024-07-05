package csi.server.business.cachedb.querybuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.selection.cache.BroadcastResult;
import csi.server.business.selection.storage.AbstractBroadcastStorageService;
import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.RowsSelection;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTimeSetting;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.util.CacheUtil;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.api.AggregateFunction;
import csi.server.util.sql.impl.spi.SelectSQLSpi;

public class TimelineQueryBuilder extends AbstractQueryBuilder<TimelineViewDef> {
   private static final Logger LOG = LogManager.getLogger(TimelineQueryBuilder.class);

    public static final String SELECT_QUERY = "select %1$s from %2$s ";
    private List<String> selectColumns;
    private List<FieldDef> selectedColumns;
    private FieldDef groupByField;

    public TimelineQueryBuilder(DataView dataView, TimelineViewDef viewDef) {
        setDataView(dataView);
        setViewDef(viewDef);

        List<String> columnNames = new ArrayList<String>();
        List<FieldDef> selectedColumns = new ArrayList<FieldDef>();

        TimelineSettings settings = viewDef.getTimelineSettings();
        if(settings != null){

            FieldDef groupBy = settings.getGroupByField();
            if(groupBy != null){
                this.groupByField = groupBy;
                String groupByField = CacheUtil.getQuotedColumnName(groupBy);

                if(!columnNames.contains(groupByField)){
                    columnNames.add(groupByField);
                    selectedColumns.add(groupBy);
                }
            }

            FieldDef colorBy = settings.getColorByField();
            if(colorBy != null){
                String colorByField = CacheUtil.getQuotedColumnName(colorBy);

                if(!columnNames.contains(colorByField)){
                    columnNames.add(colorByField);
                    selectedColumns.add(colorBy);
                }
            }

            FieldDef dotSize = settings.getDotSize();
            if(dotSize != null){
                String dotSizeField = CacheUtil.getQuotedColumnName(dotSize);

                if(!columnNames.contains(dotSizeField)){
                    columnNames.add(dotSizeField);
                    selectedColumns.add(dotSize);
                }
            }

            for(TimelineEventDefinition event: settings.getEvents()){
                TimelineTimeSetting start = event.getStartField();
                if((start != null) && (start.getFieldDef() != null)){
                    String startField = CacheUtil.getQuotedColumnName(start.getFieldDef());

                    if(!columnNames.contains(startField)){
                        columnNames.add(startField);
                        selectedColumns.add(start.getFieldDef());
                    }
                }

                TimelineTimeSetting end = event.getEndField();
                if((end != null) && (end.getFieldDef() != null)){
                    String endField = CacheUtil.getQuotedColumnName(end.getFieldDef());

                    if(!columnNames.contains(endField)){
                        columnNames.add(endField);
                        selectedColumns.add(end.getFieldDef());
                    }
                }

                FieldDef label = event.getLabelField();
                if(label != null){
                    String labelField = CacheUtil.getQuotedColumnName(label);

                    if(!columnNames.contains(labelField)){
                        columnNames.add(labelField);
                        selectedColumns.add(label);
                    }
                }
            }

        }

        columnNames.add(0,CacheUtil.INTERNAL_ID_NAME);
        this.selectedColumns = selectedColumns;
        this.selectColumns = columnNames;
    }

    public void useMinimalColumns(){
        List<String> columnNames = new ArrayList<String>();
        List<FieldDef> selectedColumns = new ArrayList<FieldDef>();

        TimelineSettings settings = getViewDef().getTimelineSettings();
        if(settings != null){

            FieldDef groupBy = settings.getGroupByField();
            if(groupBy != null){
                String groupByField = CacheUtil.getQuotedColumnName(groupBy);

                if(!columnNames.contains(groupByField)){
                    columnNames.add(groupByField);
                    selectedColumns.add(groupBy);
                }
            }

//            FieldDef colorBy = settings.getColorByField();
//            if(colorBy != null){
//                String colorByField = CacheUtil.getQuotedColumnName(colorBy);
//
//                if(!columnNames.contains(colorByField)){
//                    columnNames.add(colorByField);
//                    selectedColumns.add(colorBy);
//                }
//            }

            FieldDef dotSize = settings.getDotSize();
            if(dotSize != null){
                String dotSizeField = CacheUtil.getQuotedColumnName(dotSize);

                if(!columnNames.contains(dotSizeField)){
                    columnNames.add(dotSizeField);
                    selectedColumns.add(dotSize);
                }
            }

            for(TimelineEventDefinition event: settings.getEvents()){
                TimelineTimeSetting start = event.getStartField();
                if((start != null) && (start.getFieldDef() != null)){
                    String startField = CacheUtil.getQuotedColumnName(start.getFieldDef());

                    if(!columnNames.contains(startField)){
                        columnNames.add(startField);
                        selectedColumns.add(start.getFieldDef());
                    }
                }

                TimelineTimeSetting end = event.getEndField();
                if((end != null) && (end.getFieldDef() != null)){
                    String endField = CacheUtil.getQuotedColumnName(end.getFieldDef());

                    if(!columnNames.contains(endField)){
                        columnNames.add(endField);
                        selectedColumns.add(end.getFieldDef());
                    }
                }

//                FieldDef label = event.getLabelField();
//                if(label != null){
//                    String labelField = CacheUtil.getQuotedColumnName(label);
//
//                    if(!columnNames.contains(labelField)){
//                        columnNames.add(labelField);
//                        selectedColumns.add(label);
//                    }
//                }
            }
        }
    }

    public String buildQuery(IntPrimitiveSelection mySelectionState, boolean excludeRows) {
        StringBuilder builder = new StringBuilder();

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        String selection = buildSelection(dcHelper);

        String filterClause = buildFilterClause(dcHelper, mySelectionState, excludeRows);

        builder.append(String.format(SELECT_QUERY, selection, CacheUtil.getQuotedCacheTableName(getDataView().getUuid())));
        if ((filterClause != null) && !filterClause.isEmpty()) {
            builder.append(" WHERE ").append(filterClause);
        }

        //        if(startField != null){
        //        	builder.append(" ORDER BY ");
        //        	builder.append(CacheUtil.makeCastExpression(startField));
        //        }

        String toReturn = builder.toString();
        return toReturn;
    }

    public SelectSQL buildScrollableQuery(RowsSelection limitBySelection, boolean excludeRows) {

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        Column idColumn = tableSource.getRawIdColumn();
        idColumn.setAlias("internal_id");
        sql.select(idColumn);
        for(FieldDef field: selectedColumns){

            Column column = tableSource.getColumn(field);
            column.setAlias(CacheUtil.getQuotedColumnName(field));
            sql.select(column);
        }

        applyFilters(tableSource, sql, false);

        if(LOG.isDebugEnabled()) {
         LOG.debug(((SelectSQLSpi) sql).getSQL());
      }

        ///////////////////////////////////

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        return sql;
    }

    public SelectSQL buildScrollableBundleQuery(RowsSelection limitBySelection, boolean excludeRows) {

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        Column idColumn = tableSource.getRawIdColumn();
        idColumn.setAlias("internal_id");
        sql.select(idColumn);
        for(FieldDef field: selectedColumns){

            Column column = tableSource.getColumn(field);
            column.setAlias(CacheUtil.getQuotedColumnName(field));
            sql.select(column);
        }

        applyFilters(tableSource, sql, false);

        if(LOG.isDebugEnabled()) {
         LOG.debug(((SelectSQLSpi) sql).getSQL());
      }

        ///////////////////////////////////

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        return sql;
    }

    public SelectSQL buildCountQuery(RowsSelection limitBySelection, boolean excludeRows) {

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        TimelineSettings settings = getViewDef().getTimelineSettings();
        if(settings != null){

            for(TimelineEventDefinition event: settings.getEvents()){

                TimelineTimeSetting start = event.getStartField();
                FieldDef startField = null;
                if((start != null) && (start.getFieldDef() != null)){
                    startField = start.getFieldDef();
                }

                TimelineTimeSetting end = event.getEndField();
                FieldDef endField = null;
                if((end != null) && (end.getFieldDef() != null)){
                   endField = end.getFieldDef();
                }

                if((startField == null) && (endField != null)){
                    Column startCountColumn = tableSource.getColumn(endField);
                    startCountColumn.setAlias(CacheUtil.getQuotedColumnName(endField));
                    startCountColumn.with(AggregateFunction.COUNT);

                    sql.select(startCountColumn).where((tableSource.getColumn(endField).isNull().negate()));

                } else if((endField == null) && (startField != null)){
                    Column startCountColumn = tableSource.getColumn(startField);
                    startCountColumn.setAlias(CacheUtil.getQuotedColumnName(startField));
                    startCountColumn.with(AggregateFunction.COUNT);

                    sql.select(startCountColumn).where((tableSource.getColumn(startField).isNull().negate()));

                } else {
                    Column startCountColumn = tableSource.getColumn(startField);
                    startCountColumn.setAlias(CacheUtil.getQuotedColumnName(startField));
                    startCountColumn.with(AggregateFunction.COUNT);
                    Column endColumn = tableSource.getColumn(endField);
                    endColumn.setAlias(CacheUtil.getQuotedColumnName(endField));

                    sql.select(startCountColumn).where((tableSource.getColumn(startField).isNull().negate()).or(endColumn.isNull().negate()));

                }


            }
        }

//        for(FieldDef startField: selectedColumns){
//
//            Column startColumn = tableSource.getColumn(startField);
//            startColumn.setAlias(CacheUtil.getQuotedColumnName(startField));
//            startColumn.with(AggregateFunction.COUNT);
//            Column endColumn = tableSource.getColumn(endField);
//            endColumn.setAlias(CacheUtil.getQuotedColumnName(endField));
//
//            sql.select(startColumn).where(startColumn.isNull().negate().or(endColumn.isNull().negate()));
//        }

        applyFilters(tableSource, sql, false);

        if(LOG.isDebugEnabled()) {
         LOG.debug(((SelectSQLSpi) sql).getSQL());
      }

        ///////////////////////////////////

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        return sql;
    }

    public SelectSQL buildDistinctGroupsQuery(RowsSelection limitBySelection, boolean excludeRows) {

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        sql.distinct();
        TimelineSettings settings = getViewDef().getTimelineSettings();
        if(settings != null){

            FieldDef group = settings.getGroupByField();
            Column groupColumn = tableSource.getColumn(group);
            groupColumn.setAlias(CacheUtil.getQuotedColumnName(group));
            sql.select(groupColumn);
            //sql.orderBy(groupColumn, settings.getSortAscending() ? SortOrder.ASC:SortOrder.DESC);

//
//                if(startField == null && endField != null){
//                    Column startCountColumn = tableSource.getColumn(endField);
//                    startCountColumn.setAlias(CacheUtil.getQuotedColumnName(endField));
//                    startCountColumn.with(AggregateFunction.COUNT);
//
//                    sql.select(startCountColumn).where((tableSource.getColumn(endField).isNull().negate()));
//
//                }
        }

//        for(FieldDef startField: selectedColumns){
//
//            Column startColumn = tableSource.getColumn(startField);
//            startColumn.setAlias(CacheUtil.getQuotedColumnName(startField));
//            startColumn.with(AggregateFunction.COUNT);
//            Column endColumn = tableSource.getColumn(endField);
//            endColumn.setAlias(CacheUtil.getQuotedColumnName(endField));
//
//            sql.select(startColumn).where(startColumn.isNull().negate().or(endColumn.isNull().negate()));
//        }


        applyFilters(tableSource, sql, false);

        if(LOG.isDebugEnabled()) {
         LOG.debug(((SelectSQLSpi) sql).getSQL());
      }

        ///////////////////////////////////

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        return sql;
    }

    private String buildFilterClause(DataCacheHelper dcHelper, IntPrimitiveSelection mySelectionState, boolean excludeRows) {
        String filterClause = "";
        if (getViewDef().getFilterUuid() != null) {
            filterClause = dcHelper.buildFilterClause(getViewDef(), getDataView());
        }



        if((mySelectionState != null) && !mySelectionState.isCleared())
        {
            String attachFilterClause = DataCacheHelper.buildTableSelectionFilterClause(mySelectionState, false, getDataView());
            if (attachFilterClause.length() > 0) {
                if ((filterClause != null) && !filterClause.isEmpty()) {
                    filterClause += " AND " + attachFilterClause;
                } else {
                    filterClause = attachFilterClause;
                }
            }
        }
        //
        if(!excludeRows){
            BroadcastResult result = AbstractBroadcastStorageService.instance().getBroadcast(getViewDef().getUuid());
            String attachFilterClause = DataCacheHelper.buildTableSelectionFilterClause(result.getBroadcastFilter(), result.isExcludeRows(), getDataView());
            if (attachFilterClause.length() > 0) {
                if ((filterClause != null) && !filterClause.isEmpty()) {
                    filterClause += " AND " + attachFilterClause;
                } else {
                    filterClause = attachFilterClause;
                }
            }
        }
        return filterClause;
    }

   private String buildSelection(DataCacheHelper dcHelper) {
      return ((selectColumns == null) || selectColumns.isEmpty())
                ? "*"
                : selectColumns.stream().collect(Collectors.joining(", "));
   }

    public List<FieldDef> getSelectedColumns() {
        return selectedColumns;
    }

    public List<String> getSelectColumns() {
        return selectColumns;
    }

    public void setSelectColumns(List<String> selectColumns) {
        this.selectColumns = selectColumns;
    }

    public SelectSQL createScrollingGroupQuery(RowsSelection limitBySelection, boolean excludeRows, List<Object> groupsToQuery) {

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        Column idColumn = tableSource.getRawIdColumn();
        idColumn.setAlias("internal_id");
        sql.select(idColumn);
        for(FieldDef field: selectedColumns){

            Column column = tableSource.getColumn(field);
            column.setAlias(CacheUtil.getQuotedColumnName(field));
            sql.select(column);
        }


        Column groupColumn = tableSource.getColumn(groupByField);
        groupColumn.setAlias(CacheUtil.getQuotedColumnName(groupByField));

        Predicate predicate = groupColumn.$(RelationalOperator.IN).list(groupsToQuery, groupByField.getDataType());
        sql.where(predicate);

        applyFilters(tableSource, sql, false);

        if(LOG.isDebugEnabled()) {
         LOG.debug(((SelectSQLSpi) sql).getSQL());
      }

        ///////////////////////////////////

        DataCacheHelper dcHelper = new DataCacheHelper();
        dcHelper.setFilterActionsService(getFilterActionsService());

        return sql;
    }

}
