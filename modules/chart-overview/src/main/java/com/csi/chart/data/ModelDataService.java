package com.csi.chart.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.empire.db.DBCmpType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBColumnExpr;
import org.apache.empire.db.DBCommand;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBQuery;
import org.apache.empire.db.DBTable;
import org.apache.empire.db.expr.compare.DBCompareExpr;
import org.apache.log4j.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.csi.chart.dto.BaseRequest;
import com.csi.chart.dto.ChartSummary;
import com.csi.chart.dto.DataRequest;
import com.csi.chart.dto.DimensionSummary;
import com.csi.chart.dto.Page;
import com.csi.chart.dto.SortInfo;
import com.csi.chart.util.SqlUtil;
import com.csi.util.data.ExpressionRegistry;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import csi.server.business.helper.DataCacheHelper;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.ModelHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DrillDownChartViewDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.GenericProperties;
import csi.server.common.model.Property;
import csi.server.common.model.chart.ChartField;
import csi.server.common.model.chart.ChartMeasure;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.filter.FilterField;
import csi.server.common.util.CacheUtil;
import csi.server.common.util.sql.DBModelHelper;
import csi.server.dao.CsiPersistenceManager;

public class ModelDataService implements DataService {
    private static final String METRIC_TEMPLATE = "metric%1$d";
    private static final String DIMENSION_TEMPLATE = "dim%1$d";
    protected String resourceId;
    protected DrillDownChartViewDef definition;
    protected String dataviewUuid;
    protected boolean sortByMetric;

    protected Logger log;
    private DBTable dataTable;
    private DBTable broadcastTable;
    private DBDatabase db;

    public ModelDataService(String resourceId) throws SQLException, NamingException {
        log = Logger.getLogger(ModelDataService.class);
        this.resourceId = resourceId;

        loadDefinition();
        buildModels();
    }

    protected void loadDefinition() {
        this.definition = ModelHelper.find(DrillDownChartViewDef.class, resourceId);
        if (definition == null) {
            throw new IllegalStateException("Non-existent chart definition");
        }

        DataView dv = DataViewHelper.findDataViewFromVizId(definition.getUuid());
        dataviewUuid = dv.getUuid();

    }

    protected void buildModels() throws SQLException, NamingException {
        DBModelHelper dbHelper = new DBModelHelper();

        String dvTableName = CacheUtil.getCacheTableName(dataviewUuid);
        dataTable = dbHelper.getDBTable("public", dvTableName);

        db = dataTable.getDatabase();
        broadcastTable = getBroadcastTable(dbHelper, db);

    }

    @Override
    public void sortByValue(boolean flag) {
        this.sortByMetric = flag;
    }

    /**
     * Supporting method to retrieve data for a non 2-d chart.
     * <p>
     * 
     * @see {{@link #getData(DataRequest)} for data requests.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public CategoryDataset getCategoryData(BaseRequest request) throws Exception {

        DBCommand command = db.createCommand();
        configureBaseCommand(request, dataTable, broadcastTable, command);

        Object[] params = null;
        boolean scrollable = false;
        DataSource dataSource = CsiPersistenceManager.getCacheDataSource();
        Connection conn = dataSource.getConnection();

        try {

            String query = command.getSelect();

            ResultSet rs = db.executeQuery(query, params, scrollable, conn);

            // Assume for now that we're only dealing with single metric.
            // update data extraction to handle multiple series -- track
            // series by metric name
            Comparable<String> series = "totals";
            Comparable name;
            double value;
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            while (rs.next()) {
                name = (Comparable) rs.getObject(1);
                value = rs.getDouble(2);

                dataset.addValue(value, series, name);
            }

            rs.close();
            conn.close();

            return dataset;
        } finally {
            SqlUtil.quietCloseConnection(conn);
        }
    }

    /**
     * Supporting method to retrieve a data set for 2-d charts.
     * <p>
     * 
     * @see {{@link #getData(DataRequest)} for data requests.
     */
    @Override
    public XYDataset getXYData(BaseRequest request) throws Exception {

        List<ChartField> dimensions = definition.getDimensions();

        DBColumnExpr x;
        DBColumnExpr y;

        DBCommand command = db.createCommand();

        if (request.drill == null || request.drill.size() == 0) {
            ChartField dim = dimensions.get(0);
            DBColumn col = getColumn(dataTable, dim);
            x = resolveExpression(dim, col);

            dim = dimensions.get(1);
            col = getColumn(dataTable, dim);
            y = resolveExpression(dim, col);
        } else {

            for (int i = 0; i < request.drill.size(); i++) {
                ChartField dim = dimensions.get(i);
                DBColumn column = getColumn(dataTable, dim);
                DBColumnExpr expr = resolveExpression(dim, column);
                command.where(expr.is(request.drill.get(i)));
            }

            ChartField dim = dimensions.get(request.drill.size());
            DBColumn col = getColumn(dataTable, dim);
            x = resolveExpression(dim, col);

            dim = dimensions.get(request.drill.size() + 1);
            col = getColumn(dataTable, dim);
            y = resolveExpression(dim, col);
        }

        Object[] params = new Object[0];

        DataSource ds = CsiPersistenceManager.getCacheDataSource();
        Connection conn = ds.getConnection();

        try {

            BiMap<Object, Number> xMap = getIndexedColumnValues(dataTable, x, conn, params);
            BiMap<Object, Number> yMap = getIndexedColumnValues(dataTable, y, conn, params);

            List<ChartMeasure> metrics = definition.getMetrics();

            ChartMeasure metric = metrics.get(request.metric);
            FieldDef measureField = metric.getMeasureField();
            DBColumn column = getColumn(dataTable, measureField);
            DBColumnExpr metricExpr = resolveExpression(metric, column);
            command = dataTable.getDatabase().createCommand();
            command.select(x, y);
            command.select(metricExpr);
            command.groupBy(x, y);
            command.orderBy(x, y);

            MatrixSeriesCollection dataset = new MatrixSeriesCollection();
            MatrixSeries series = new MatrixSeries("", xMap.size(), yMap.size());
            dataset.addSeries(series);
            ResultSet results = dataTable.getDatabase().executeQuery(command.getSelect(), params, false, conn);
            while (results.next()) {
                Object ox = results.getObject(1);
                Object oy = results.getObject(2);

                double val = results.getDouble(3);
                series.update(xMap.get(ox).intValue(), yMap.get(oy).intValue(), val);

            }

            results.close();
            return dataset;
        } finally {
            SqlUtil.quietCloseConnection(conn);
        }
    }

    // NB: connection left untouched. Caller's responsibility to clean up!
    private BiMap<Object, Number> getIndexedColumnValues(DBTable table,
                                                         DBColumnExpr column,
                                                         Connection conn,
                                                         Object[] params) throws SQLException
    {
        DBCommand command;
        ResultSet results;

        BiMap<Object, Number> index = HashBiMap.create();
        command = table.getDatabase().createCommand();

        // need to track y-axis unique values so that we can properly put into
        // each slot.
        command.select(column);
        command.selectDistinct();
        command.orderBy(column);
        int counter = 0;
        results = table.getDatabase().executeQuery(command.getSelect(), params, false, conn);
        while (results.next()) {
            index.put(results.getObject(1), counter);
            counter++;
        }

        results.close();
        return index;
    }

    @Override
    public boolean isCategoryChart() {
        return !isHeatMap();
    }

    @Override
    public boolean isHeatMap() {
        String chartType = definition.getChartType();
        String type = Strings.nullToEmpty(chartType).toLowerCase();
        boolean flag = type.equals("bubble") || type.equals("heatmap");
        return flag;
    }

    @Override
    public ChartSummary calculateSummaryInfo(DataRequest request) throws Exception {
        DBCommand command = db.createCommand();
        configureBaseCommand(request, dataTable, broadcastTable, command);

        command.clearSelect();
        command.select(dataTable.count());

        String query = command.getSelect();

        ChartSummary summary = new ChartSummary();
        DataSource dataSource = CsiPersistenceManager.getCacheDataSource();
        Connection connection = dataSource.getConnection();

        try {
//            summary.count = SqlUtil.getLong(connection, query);
            
            if( request.dimensionCount == null ) {
                request.dimensionCount = 1;
            }

            if (request.dimensionCount > 0) {
                computeDimensionSummaries(summary, request);
            }

            return summary;
        } finally {
            SqlUtil.quietCloseConnection(connection);
        }
    }

    /*
     * 
     */
    private void computeDimensionSummaries(ChartSummary summary, DataRequest request) throws CentrifugeException, SQLException {
        
        
        Connection connection = CsiPersistenceManager.getCacheConnection();
        List<ChartField> dimensions = definition.getDimensions();
        
        try {
            for( int i=0; i < request.dimensionCount && i < dimensions.size(); i++ ) {
                ChartField dim = dimensions.get(i);
                DBColumn column = getColumn(dataTable, dim);
                
                DBColumnExpr expr = resolveExpression(dim, column);
                
                DBCommand inner = db.createCommand();
                configureVisualizationCommand(inner, dataTable, broadcastTable);
                inner.select(expr.as("dim"));
                inner.selectDistinct();
                
                DBQuery subquery = new DBQuery(inner);
                
                DBCommand query = db.createCommand();
                query.select(subquery.findQueryColumn(expr.as("dim")).count());
                
                String sql = query.getSelect();
                
                long count = SqlUtil.getLong(connection, sql);
                
                DimensionSummary info = new DimensionSummary();
                info.count = count;
                info.type = expr.getDataType().toString();
                info.name = dim.getDimension().getFieldName();
                
                summary.dimensions.add(info);
            }
        } finally {
            SqlUtil.quietCloseConnection(connection);
        }
        
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Page getData(DataRequest request) throws Exception {

        DBCommand command = db.createCommand();

        configureBaseCommand(request, dataTable, broadcastTable, command);

        // paging support....
        int start = request.offset;
        command.limitRows(request.size);
        command.skipRows((int) start);
        String query = command.getSelect();

        DataSource dataSource = CsiPersistenceManager.getCacheDataSource();
        Connection connection = dataSource.getConnection();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            Page data = new Page(request.offset, request.size);
            while (rs.next()) {
                Map row = SqlUtil.mapRow(rs);
                data.addPoint(row);
            }

            return data;
        } finally {
            SqlUtil.quietCloseConnection(connection);
        }
    }

    public DBTable getBroadcastTable(DBModelHelper dbHelper, DBDatabase db) throws SQLException, NamingException {
        String bcTableName = DataCacheHelper.getBroadcastTableName(dataviewUuid);
        DBTable broadcastTable = dbHelper.getDBTable(db, "public", bcTableName);
        return broadcastTable;
    }

    public DBTable getDataTable(DBModelHelper dbHelper) throws SQLException, NamingException {
        DataView dv = DataViewHelper.findDataViewFromVizId(definition.getUuid());
        String dvTableName = CacheUtil.getCacheTableName(dv.getUuid());
        DBTable dataTable = dbHelper.getDBTable("public", dvTableName);
        return dataTable;
    }

    @Override
    public Page getData2D(DataRequest request) throws Exception {

        int colOffset = (request.drill == null) ? 0 : request.drill.size();
        List<ChartField> dimensions = definition.getDimensions();

        if (dimensions.size() < (colOffset + 1)) {
            // asking for 2-d data, but we don't have enough dimensions. this
            // could be due to drill-level or just having a single dimension
            // defined.
            throw new IllegalStateException("Not enough dimensions available");
        }

        DBColumnExpr x;
        DBColumnExpr y;
        ChartField dim = dimensions.get(request.drill.size());
        DBColumn col = getColumn(dataTable, dim);
        x = resolveExpression(dim, col);

        dim = dimensions.get(request.drill.size() + 1);
        col = getColumn(dataTable, dim);
        y = resolveExpression(dim, col);

        DBColumnExpr metricExpr = getMetricExpression(dataTable, request);

        String xAlias = String.format(DIMENSION_TEMPLATE, request.drill.size());
        String yAlias = String.format(DIMENSION_TEMPLATE, request.drill.size() + 1);
        String metricAlias = String.format(METRIC_TEMPLATE, request.metric);

        /*
         * let's do some work. i) Identify subset of values along each axis.
         * this uses paging information. ii) With subset of values, construct a
         * query to retrieve the 2-d window of data.
         * 
         * Ensure that with ii) the ordering in the query is stable. otherwise
         * pages can potentially get differing data.
         */

        DataSource dataSource = CsiPersistenceManager.getCacheDataSource();
        Connection connection = dataSource.getConnection();

        try {

            List<Object> xRange = getColumnPageSubset(connection, dataTable, x, request.offset, request.size);
            List<Object> yRange = getColumnPageSubset(connection, dataTable, y, request.yOffset, request.ySize);

            DBCommand command = db.createCommand();
            configureVisualizationCommand(command, dataTable, broadcastTable);
            applyDrillValues(command, dataTable, request);

            command.select(x.as(xAlias), y.as(yAlias));
            command.select(metricExpr.as(metricAlias));
            command.groupBy(x, y);
            command.orderBy(x, y);
            command.where(x.in(xRange));
            command.where(y.in(yRange));

            String query = command.getSelect();

            if (log.isTraceEnabled()) {
                log.trace("Executing 2-D Query: " + query);
            }

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            Page data = new Page(request.offset, request.size);
            while (rs.next()) {
                @SuppressWarnings("rawtypes")
                Map row = SqlUtil.mapRow(rs);
                data.addPoint(row);
            }

            return data;
        } finally {
            SqlUtil.quietCloseConnection(connection);
        }

    }

    private DBColumnExpr getMetricExpression(DBTable dataTable, DataRequest request) {
        List<ChartMeasure> metrics = definition.getMetrics();

        Preconditions.checkElementIndex(request.metric, metrics.size());

        ChartMeasure metric = metrics.get(request.metric);
        FieldDef measureField = metric.getMeasureField();
        DBColumn column = getColumn(dataTable, measureField);
        DBColumnExpr expr = resolveExpression(metric, column);
        return expr;
    }

    private List<Object> getColumnPageSubset(Connection connection,
                                             DBTable table,
                                             DBColumnExpr expr,
                                             int offset,
                                             int size) throws SQLException
    {
        DBCommand command = table.getDatabase().createCommand();

        command.select(expr);
        command.selectDistinct();
        command.orderBy(expr);

        int start = offset;
        command.limitRows(size);
        command.skipRows(start);

        String query = command.getSelect();

        List<Object> pageValues = new ArrayList<Object>(size);

        Statement stmt = connection.createStatement();
        ResultSet results = stmt.executeQuery(query);
        while (results.next()) {
            pageValues.add(results.getObject(1));
        }

        results.close();
        stmt.close();

        return pageValues;
    }

    private void applyDrillValues(DBCommand command, DBTable table, BaseRequest request) {
        if (request.drill == null || request.drill.size() == 0) {
            return;
        }

        List<ChartField> dimensions = definition.getDimensions();

        for (int i = 0; i < request.drill.size() && i < request.drill.size(); i++) {
            ChartField dim = dimensions.get(i);
            DBColumn column = getColumn(table, dim);

            DBColumnExpr expr = column;
            expr = resolveExpression(dim, column);
            command.where(expr.is(request.drill.get(i)));
        }
    }

    /*
     * Configures the command for generic usage against this visualization. This
     * includes any filters declared on the definition as well as broadcast
     * information.
     */
    private void configureVisualizationCommand(DBCommand command, DBTable data, DBTable broadcast) {
        addVisualizationFilters(command, data);
        addBroadcastFilters(command, data, broadcast);
    }

    /*
     * Update the command object to account for broadcasting if
     * attached/listening.
     */
    private void addBroadcastFilters(DBCommand command, DBTable data, DBTable broadcast) {
        if (definition.getIsAttached()) {
            // we currently determine broadcast by using a where id in (select
            // row_id from broadcast).
            // this isn't necessarily the most optimal. if this proves to be an
            // issue, we'll have to
            // resort to an actual join.
            DBDatabase db = data.getDatabase();
            DBColumn dataRowId = data.getColumn("\"internal_id\"");
            if (broadcast.getColumns().size() > 0) {
                DBCommand filterCommand = db.createCommand();
                filterCommand.select(broadcast.getColumn("row_id"));
                command.where(dataRowId.in(filterCommand));
            }
        }

    }

    private void configureBaseCommand(BaseRequest request, DBTable dbTable, DBTable broadcastTable, DBCommand command) {
        // apply blanket things always applied to any query for this
        // visualization.
        configureVisualizationCommand(command, dbTable, broadcastTable);

        List<ChartField> dimensions = definition.getDimensions();

        if (request.drill == null || request.drill.size() == 0) {

            int colCount = (request.dimensionCount != null) ? request.dimensionCount : 1;
            String dimTemplate = DIMENSION_TEMPLATE;

            for (int i = 0; i < colCount && i < dimensions.size(); i++) {
                ChartField dim = dimensions.get(i);
                DBColumn column = getColumn(dbTable, dim);
                DBColumnExpr expr = resolveExpression(dim, column);
                String alias = String.format(dimTemplate, i);
                command.select(expr.as(alias));
                command.groupBy(expr);
            }
        } else {
            // dealing with drill down, so we'll use whatever values passed
            // for the drill down categories, then select the next dimension for
            // query
            applyDrillValues(command, dbTable, request);

            int dimCount = (request.dimensionCount != null) ? request.dimensionCount.intValue() : 1;

            for (int i = request.drill.size(); i < dimensions.size() && dimCount > 0; i++, dimCount--) {
                ChartField dim = dimensions.get(i);
                DBColumn column = getColumn(dbTable, dim);
                DBColumnExpr expr = resolveExpression(dim, column);
                String alias = String.format(DIMENSION_TEMPLATE, i);
                command.select(expr.as(alias));
                command.groupBy(expr);
            }
        }

        addMetrics(dbTable, command);

        // nb: names here are field uuids.
        if (request.sorting != null && request.sorting.size() > 0) {
            // sort support ---- resolve using configuration
            // versus request supplied
            for (SortInfo sort : request.sorting) {
                DBColumn col = dbTable.getColumn(sort.name);
                if (col != null) {
                    command.orderBy(col, sort.desc);
                } else {
                    // TODO: we don't currently support by name lookups, only
                    // refs by uuid. If required we can add this in.
                    if (log.isDebugEnabled()) {
                        log.debug("Request for sorting for an unknown field name: " + sort.name);
                    }
                }
            }
        }
    }

    public void addMetrics(DBTable dbTable, DBCommand command) {
        List<ChartMeasure> metrics = definition.getMetrics();
        for (int i = 0; i < metrics.size(); i++) {
            ChartMeasure metric = metrics.get(i);
            FieldDef fieldRef = metric.getMeasureField();

            String persistedName = CacheUtil.toQuotedDbUuid(fieldRef.getUuid());
            DBColumn column = dbTable.getColumn(persistedName);
            DBColumnExpr expr = resolveExpression(metric, column);
            String alias = String.format(METRIC_TEMPLATE, i);
            command.select(expr.as(alias));
        }
    }

    private void addVisualizationFilters(DBCommand command, DBTable dbTable) {
        List<FilterField> filterFields = definition.getFilterFields();
        if (filterFields == null || filterFields.size() == 0) {
            return;
        }

        for (FilterField ff : filterFields) {
            DBColumn target = getColumn(dbTable, ff.field);
            DBCompareExpr expr = null;
            // this replicates the existing application logic for filtering
            // values
            // see DataCacheHelper.buildFilterFragment.
            //
            // TODO: simplify this logic/assumptions in processing.
            if (Utils.isStringOrBooleanField(ff.field)) {
                if (isSingleEmptyValue(ff)) {
                    expr = target.cmp(DBCmpType.NULL, null);
                } else {
                    DBCompareExpr temp = target.in(ff.selectedValues);
                    if (ff.selectedValues.indexOf("") != -1) {
                        temp = temp.or(target.cmp(DBCmpType.NULL, null));
                    }
                    expr = temp;
                }
            } else {
                if (!Strings.isNullOrEmpty(ff.startValue) && !Strings.isNullOrEmpty(ff.endValue)) {
                    expr = target.isBetween(ff.startValue, ff.endValue);
                } else {
                }
            }

            if (expr != null) {
                command.where(expr);
            }
        }
    }

    private boolean isSingleEmptyValue(FilterField filterField) {
        boolean flag = filterField.selectedValues.size() == 1 && "".equals(filterField.selectedValues.get(0));
        return flag;

    }

    /*
     * Applies casting and functions to the column as defined by the chart
     * field. The casting is required since the data cache table stores the
     * values in the original data format and not what a user has potentially
     * changed the field type to i.e. changing a string -> date.
     */
    protected DBColumnExpr resolveExpression(ChartField field, DBColumn column) {

        DBColumnExpr colExpr = column;
        FieldDef fieldRef = field.getDimension();
        if (Utils.requiresCast(fieldRef.getValueType(), fieldRef.getCacheType())) {
            colExpr = Utils.castAs(column, fieldRef.getValueType());
        }

        String exprFunction = field.getBundleFunction();
        if (Strings.isNullOrEmpty(exprFunction)) {
            return colExpr;
        }

        GenericProperties params = field.getBundleParams();
        String[] paramValues = getParamValues(params);

        ExpressionRegistry registry = ExpressionRegistry.instance();
        DBColumnExpr resolved = registry.getExpression(exprFunction, colExpr, paramValues);
        return resolved;
    }

    /**
     * Applies casting and functions to the column to formulate the appropriate
     * expression for a query. Note that there potentially 2 levels of
     * functions. The first is a bundle function on the column itself, the
     * second is the aggregate function used to compute the metric.
     * 
     * 
     * @see {{@link #resolveExpression(ChartField, DBColumn)}
     */
    private DBColumnExpr resolveExpression(ChartMeasure metric, DBColumn column) {
        DBColumnExpr expr = column;
        ExpressionRegistry registry = ExpressionRegistry.instance();

        String bundleFunction = metric.getBundleFunction();
        if (!Strings.isNullOrEmpty(bundleFunction)) {
            GenericProperties params = metric.getBundleParams();
            String[] paramValues = new String[0];
            if (params != null) {
        		paramValues = getParamValues(params);
            }

            expr = registry.getExpression(bundleFunction, column, paramValues);
        }

        String aggregateFunction = metric.getMeasureFunction();
        if (Strings.isNullOrEmpty(aggregateFunction)) {
            aggregateFunction = getDefaultAggregateFunction();
        }

        DBColumnExpr resolved = registry.getExpression(aggregateFunction, expr, new Object[0]);
        return resolved;
    }

    private String getDefaultAggregateFunction() {
        return "count";
    }

    private DBColumn getColumn(DBTable dbTable, ChartField dim) {
        FieldDef fieldRef = dim.getDimension();
        return getColumn(dbTable, fieldRef);

    }

    private DBColumn getColumn(DBTable dbTable, FieldDef fieldRef) {
        String persistedName = CacheUtil.toQuotedDbUuid(fieldRef.getUuid());
        DBColumn column = dbTable.getColumn(persistedName);
        return column;
    }

    private String[] getParamValues(GenericProperties params) {
    	if (params == null) {
    		return new String[0];
    	}
        List<Property> properties = params.getProperties();

        String[] paramValues = new String[properties.size()];
        for (int i = 0; i < paramValues.length; i++) {
            paramValues[i] = properties.get(i).getValue().toString();
        }
        return paramValues;
    }

}
