/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.business.service.chart;

import static csi.server.common.enumerations.RelationalOperator.EQUAL;
import static csi.server.common.enumerations.RelationalOperator.IN;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.AbstractAttributeDefinition;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.chart.SingleDoubleTypeCriterion;
import csi.server.common.model.visualization.chart.SortDefinition;
import csi.server.common.model.visualization.chart.TwoDoubleTypeCriterion;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.ScrollCallback;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.SubSelectTableSource;
import csi.server.util.sql.api.AggregateFunction;
import csi.server.util.sql.impl.spi.SelectSQLSpi;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("ALL")
public class ChartQueryBuilder extends AbstractQueryBuilder<DrillChartViewDef> {
   private static final Logger LOG = LogManager.getLogger(ChartQueryBuilder.class);

    private List<String> categoryDrills = new ArrayList<String>();
    private List<Number> rowIdsToFilterBy = null;
    private Integer offset = null;
    private Integer limit = null;
    private boolean isAggregateOnly = false;
    private final static int SELECTION_QUERY_IN_LIMIT = 500;

    public SelectSQL getQuery(boolean fullDataFunction) {
        ChartSettings chartSettings = getViewDef().getChartSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        int howMany = categoryDrills.size();

        // Drill categories
        for (int i = 0; i < howMany; i++) {
            CategoryDefinition cd = chartSettings.getCategoryDefinitions().get(i);
            if ((categoryDrills.get(i) == null) || compareToNullString(categoryDrills.get(i))) {
                sql.select(tableSource.createConstantColumn(null, cd.getBundleFunction().getReturnType(cd.getFieldDef())));
            } else {
                sql.select(tableSource.createConstantColumn(categoryDrills.get(i).replace("'", "''"), cd.getBundleFunction().getReturnType(cd.getFieldDef())));
            }
        }

        // Categories
        Map<AbstractAttributeDefinition, Column> columnsByDefintions = new HashMap<AbstractAttributeDefinition, Column>();
        {
            int categoryIndex = categoryDrills.size();
            CategoryDefinition cd = chartSettings.getCategoryDefinitions().get(categoryIndex);
            if (cd.getFieldDef().getFieldType() == FieldType.STATIC) {
                if (!isAggregateOnly) {
                    sql.select(tableSource.createConstantColumn(cd.getFieldDef().getStaticText(), cd.getBundleFunction().getReturnType(cd.getFieldDef())));
                }
            } else {
                Column column = tableSource.getColumn(cd.getFieldDef()).with(cd.getBundleFunction()).withBundleParams(cd.getStringParamters());
                if (!fullDataFunction) {
                    sql.select(column);
                }
                if (!cd.isAllowNulls()) {
                    // If we use the column above where clause will have bundling. We don't want that.
                    sql.where(tableSource.getColumn(cd.getFieldDef()).isNull().negate());
                }
                if (!fullDataFunction) {
                    sql.groupBy(column);
                    columnsByDefintions.put(cd, column);
                }
            }
        }

        // Measures
        Column countStarColumn = null;
        if (chartSettings.isUseCountStarForMeasure() || chartSettings.getMeasureDefinitions().isEmpty()) {
            countStarColumn = tableSource.createConstantColumn(1).with(AggregateFunction.COUNT);
            sql.select(countStarColumn);
        } else {
            for (MeasureDefinition md : chartSettings.getMeasureDefinitions()) {
                Column column = tableSource.getColumn(md.getFieldDef()).with(md.getAggregateFunction());
                sql.select(column);
                if (!md.isAllowNulls()) {
                    // If we use the column above where clause will have bundling. We don't want that.
                    sql.where(tableSource.getColumn(md.getFieldDef()).isNull().negate());
                }
                columnsByDefintions.put(md, column);
            }
        }

        // Category drill where clauses
        for (int i = 0; i < categoryDrills.size(); i++) {
            CategoryDefinition cd = chartSettings.getCategoryDefinitions().get(i);
            // Skip static fields (as they don't exist in the database! duh!)
            if (cd.getFieldDef().getFieldType() != FieldType.STATIC) {
                Column column = tableSource.getColumn(cd.getFieldDef()).with(cd.getBundleFunction()).withBundleParams(cd.getStringParamters());
                if ((categoryDrills.get(i) == null) || compareToNullString(categoryDrills.get(i))) {
                    sql.where(column.isNull());
                } else {
                    sql.where(column.$(EQUAL).value(categoryDrills.get(i), cd.getBundleFunction().getReturnType(cd.getFieldDef()), false));
                }
            }
        }

        applySort(chartSettings, sql, columnsByDefintions, countStarColumn);

        if (rowIdsToFilterBy != null) {
            Column column = tableSource.getIdColumn();
            sql.where(column.$(IN).list(rowIdsToFilterBy, CsiDataType.Integer));
        }

        applyFilters(tableSource, sql, false);
        //
        //        sql.offset(offset);
        //        sql.limit(limit);

        LOG.debug(((SelectSQLSpi) sql).getSQL());
        return sql;
    }

    private ArrayList<String> generateHeaders(ChartSettings settings) {
        ArrayList<String> headers = new ArrayList<String>();
        if (settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().isEmpty()) {
            headers.add(ChartActionsServiceUtil.COUNT_STAR_MEASURE_NAME);
        } else {
           for (MeasureDefinition md : settings.getMeasureDefinitions()) {
                headers.add(md.getComposedName());
            }
        }

        return headers;
    }

    //    public SelectSQL getCriteriaFilteredQuery(){
    //        SelectSQL innerCategoryQuery = getCategoryQuery(true);
    //
    //        SubSelectTableSource sqlSource = getSqlFactory().getTableSourceFactory().create(innerCategoryQuery);
    //        SelectSQL outerSQL = getSqlFactory().createSelect(sqlSource);
    //
    //        List<? extends Column> columns = sqlSource.getSubSelectColumns();
    //
    //        for (ChartCriterion criterion : getViewDef().getChartSettings().getFilterCriteria()) {
    //            String operatorString = criterion.getOperatorString();
    //            switch (operatorString) {
    //                case "<":
    //                case "<=":
    //                case "==":
    //                case ">=":
    //                case ">":
    //                case "!=":
    //
    //                    break;
    //                case "<<":
    //                    if (!testTwoDoubleTypeCriterion(
    //                            table.get(headers.indexOf(criterion.getColumnHeader())).get(row).doubleValue(),
    //                            operatorString,
    //                            (TwoDoubleTypeCriterion) criterion)
    //                            )
    //                        return false;
    //                    break;
    //                case "Top":
    //                case "Top%":
    //                case "Bottom":
    //                case "Bottom%":
    //                    if (!testSingleIntegerTypeCriterion(
    //                                categories.get(row),
    //                                operatorString,
    //                                (SingleIntegerTypeCriterion) criterion,
    //                                statisticsHolder)
    //                                )
    //                        return false;
    //                    break;
    //                default:
    //                    return false;
    //            }
    //        }
    //
    //
    //        return outerSQL.scroll(new ScrollCallback<Integer>() {
    //            @Override
    //            public Integer scroll(ResultSet resultSet) throws SQLException {
    //                resultSet.next();
    //                return resultSet.getInt(1);
    //            }
    //        });
    //    }

    public SelectSQL getPagedQuery() {
        SelectSQL innerSQL = getCriteriaFilteredQuery();

        SubSelectTableSource innerSQLSource = getSqlFactory().getTableSourceFactory().create(innerSQL);
        SelectSQL outerSQL = getSqlFactory().createSelect(innerSQLSource);

        List<? extends Column> columns = innerSQLSource.getSubSelectColumns();

        for (Column column : columns) {
            outerSQL.select(innerSQLSource.getColumn(column));
        }

        outerSQL.limit(10);

        return outerSQL;
    }
    public SelectSQL getCriteriaFilteredQuery() {
        SelectSQL innerSQL = getCategoryQuery(true);

        SubSelectTableSource innerSQLSource = getSqlFactory().getTableSourceFactory().create(innerSQL);
        SelectSQL outerSQL = getSqlFactory().createSelect(innerSQLSource);

        ChartSettings chartSettings = getViewDef().getChartSettings();

        List<? extends Column> columns = innerSQLSource.getSubSelectColumns();

        for (Column column : columns) {
            outerSQL.select(innerSQLSource.getColumn(column));
        }

        applyCriteriaQuery(outerSQL, chartSettings);


        return outerSQL;
    }

    private void applyCriteriaQuery(SelectSQL outerSQL, ChartSettings chartSettings) {

        List<ChartCriterion> criterion = chartSettings.getFilterCriteria();
        SubSelectTableSource sqlSource = getSqlFactory().getTableSourceFactory().create(outerSQL);
        List<? extends Column> columns = sqlSource.getSubSelectColumns();

        List<String> headers = generateHeaders(chartSettings);
        List<MeasureDefinition> measures = chartSettings.getMeasureDefinitions();
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (ChartCriterion criteria : criterion) {

            int columnIndex = headers.indexOf(criteria.getColumnHeader());
            Column column = columns.get(columnIndex + 1);
            CsiDataType type;
            if (chartSettings.isUseCountStarForMeasure() || chartSettings.getMeasureDefinitions().isEmpty()) {
                type = CsiDataType.Number;
            } else {
                type = measures.get(columnIndex).getDerivedType();
            }

            String operatorString = criteria.getOperatorString();
            switch (operatorString) {
                case "<":
                    predicates.add(column.$(RelationalOperator.LT).value(((SingleDoubleTypeCriterion) criteria).getTestValue(), type));
                    break;
                case "<=":
                    predicates.add(column.$(RelationalOperator.LE).value(((SingleDoubleTypeCriterion) criteria).getTestValue(), type));
                    break;
                case "==":
                    predicates.add(column.$(RelationalOperator.EQUAL).value(((SingleDoubleTypeCriterion) criteria).getTestValue(), type));
                    break;
                case ">=":
                    predicates.add(column.$(RelationalOperator.GE).value(((SingleDoubleTypeCriterion) criteria).getTestValue(), type));
                    break;
                case ">":
                    predicates.add(column.$(RelationalOperator.GT).value(((SingleDoubleTypeCriterion) criteria).getTestValue(), type));
                    break;
                case "!=":
                    predicates.add(column.$(RelationalOperator.NOT_EQUAL).value(((SingleDoubleTypeCriterion) criteria).getTestValue(), type));
                    break;
                case "<<":
                    Predicate gt = column.$(RelationalOperator.GT).value(((TwoDoubleTypeCriterion) criteria).getMinValue(), type);
                    Predicate lt = column.$(RelationalOperator.LT).value(((TwoDoubleTypeCriterion) criteria).getMaxValue(), type);
                    predicates.add(gt.and(lt));
                    break;
                case "Top":
                case "Top%":
                case "Bottom":
                case "Bottom%":
                    //                    if (!testSingleIntegerTypeCriterion(
                    //                                categories.get(row),
                    //                                operatorString,
                    //                                (SingleIntegerTypeCriterion) criteria,
                    //                                statisticsHolder)
                    //                                )
                    //                        return false;
                    break;
                default:
                    ;
            }
        }

        for (Predicate predicate : predicates) {
            outerSQL.where(predicate);
        }

    }

    public SelectSQL getCategoryQuery(boolean forFilter) {

        ChartSettings chartSettings = getViewDef().getChartSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        int howMany = categoryDrills.size();

        // Drill categories
        for (int i = 0; i < howMany; i++) {
            CategoryDefinition cd = chartSettings.getCategoryDefinitions().get(i);
            if ((categoryDrills.get(i) == null) || compareToNullString(categoryDrills.get(i))) {
                sql.select(tableSource.createConstantColumn(null, cd.getBundleFunction().getReturnType(cd.getFieldDef())));
            } else {
                sql.select(tableSource.createConstantColumn(categoryDrills.get(i).replace("'", "''"), cd.getBundleFunction().getReturnType(cd.getFieldDef())));
            }
        }

        // Categories
        Map<AbstractAttributeDefinition, Column> columnsByDefintions = new HashMap<AbstractAttributeDefinition, Column>();
        {
            int categoryIndex = categoryDrills.size();
            CategoryDefinition cd = chartSettings.getCategoryDefinitions().get(categoryIndex);
            if (cd.getFieldDef().getFieldType() == FieldType.STATIC) {
                sql.select(tableSource.createConstantColumn(cd.getFieldDef().getStaticText(), cd.getBundleFunction().getReturnType(cd.getFieldDef())));
            } else {
                Column column = tableSource.getColumn(cd.getFieldDef()) //
                        .with(cd.getBundleFunction()) //
                        .withBundleParams(cd.getStringParamters());
                sql.select(column);
                if (!cd.isAllowNulls()) {
                    // If we use the column above where clause will have bundling. We don't want that.
                    sql.where(tableSource.getColumn(cd.getFieldDef()).isNull().negate());
                }
                sql.groupBy(column);
                columnsByDefintions.put(cd, column);
            }
        }

        // Measures
        Column countStarColumn = null;
        if (chartSettings.isUseCountStarForMeasure() || chartSettings.getMeasureDefinitions().isEmpty()) {
            countStarColumn = tableSource.createConstantColumn(1).with(AggregateFunction.COUNT);
            sql.select(countStarColumn);
        } else {
            for (MeasureDefinition md : chartSettings.getMeasureDefinitions()) {
                Column column = tableSource.getColumn(md.getFieldDef()) //
                        .with(md.getAggregateFunction());
                sql.select(column);
                //                if (!md.isAllowNulls()) {
                //                    // If we use the column above where clause will have bundling. We don't want that.
                //                    sql.where(tableSource.getColumn(md.getFieldDef()).isNull().negate());
                //                }
                columnsByDefintions.put(md, column);
            }
        }

        // Category drill where clauses
        for (int i = 0; i < categoryDrills.size(); i++) {
            CategoryDefinition cd = chartSettings.getCategoryDefinitions().get(i);
            // Skip static fields (as they don't exist in the database! duh!)
            if (cd.getFieldDef().getFieldType() != FieldType.STATIC) {
                Column column = tableSource.getColumn(cd.getFieldDef()) //
                        .with(cd.getBundleFunction()) //
                        .withBundleParams(cd.getStringParamters());
                if ((categoryDrills.get(i) == null) || compareToNullString(categoryDrills.get(i))) {
                    sql.where(column.isNull());
                } else {
                    sql.where(column.$(EQUAL).value(categoryDrills.get(i),
                            cd.getBundleFunction().getReturnType(cd.getFieldDef()), false));
                }
            }
        }

        if (!forFilter) {
            applySort(chartSettings, sql, columnsByDefintions, countStarColumn);
        }

        if (rowIdsToFilterBy != null) {
            Column column = tableSource.getIdColumn();
            sql.where(column.$(IN).list(rowIdsToFilterBy, CsiDataType.Integer));
        }

        applyFilters(tableSource, sql, false);
        //
        //        sql.offset(offset);
        //        sql.limit(limit);

        LOG.debug(((SelectSQLSpi) sql).getSQL());
        return sql;

    }

    private void applySort(ChartSettings chartSettings, SelectSQL sql,
                           Map<AbstractAttributeDefinition, Column> columnsByDefintions, Column countStarColumn) {
        if (!chartSettings.getQuickSortDef().isEmpty()) {
            SortDefinition quickSortDef = chartSettings.getQuickSortDef().get(0);
            if (quickSortDef.isCountStar()) {
                sql.orderBy(countStarColumn, quickSortDef.getSortOrder());
            } else {
                Column column = columnsByDefintions.get(quickSortDef.getChartAttributeDefinition());
                if (column != null) {
                  sql.orderBy(column, (quickSortDef.getSortOrder() == SortOrder.ASC) ? SortOrder.ASC_NULLS_FIRST : SortOrder.DESC_NULLS_LAST);

//                    sql.orderBy(column, quickSortDef.getSortOrder());
//                    sql.orderBy()
                }
            }

        } else {
            //Default Sort order if no sort exists

            if (chartSettings.getSortDefinitions().isEmpty() && !chartSettings.getCategoryDefinitions().isEmpty()) {
                Column column = columnsByDefintions.get(chartSettings.getCategoryDefinitions().get(categoryDrills.size()));
                if (column != null) {
                  sql.orderBy(column, SortOrder.ASC);
               }
            }

            // Order by
            for (SortDefinition sd : chartSettings.getSortDefinitions()) {
                if (sd.isCountStar()) {
                    sql.orderBy(countStarColumn, sd.getSortOrder());
                } else {
                    Column column = columnsByDefintions.get(sd.getChartAttributeDefinition());
                    // Column can be null because static fields and categories we have not yet drilled to will not be in the nao
                    if (column != null) {
                       sql.orderBy(column, (sd.getSortOrder() == SortOrder.ASC) ? SortOrder.ASC_NULLS_FIRST : SortOrder.DESC_NULLS_LAST);
                    }
                } // end else
            }
        }
        //
        //        if (rowIdsToFilterBy != null) {
        //        	Column column = tableSource.getIdColumn();
        //        	sql.where(column.$(IN).list(rowIdsToFilterBy, CsiDataType.Integer));
        //        }
        //
        //        applyFilters(tableSource, sql, false);
        //
        //        logger.info(((SelectSQLSpi) sql).getSQL());
        //        return sql;
    }

    private SelectSQL getRowIdsSQLQuery(List<DrillCategory> selections, boolean excludeBroadcast) {
        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        Column idColumn = tableSource.createDistinctColumn(tableSource.getIdColumn());
        idColumn.setAlias("internal_id"); // The broadcast service code expects this as the alias.
        sql.select(idColumn);

        Predicate disjunction = createPredicateFromDrillCategories(selections, tableSource);
        sql.where(disjunction);

        // Filter expression.
        applyFilters(tableSource, sql, excludeBroadcast);
        return sql;
    }

    private SelectSQL getRowIdsSQLQuery(ChartSelectionState selection, boolean excludeBroadcast) {
        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        Column idColumn = tableSource.createDistinctColumn(tableSource.getIdColumn());
        idColumn.setAlias("internal_id"); // The broadcast service code expects this as the alias.
        sql.select(idColumn);

        Predicate disjunction = createPredicateFromDrillCategories(selection, tableSource);
        sql.where(disjunction);

        // Filter expression.
        applyFilters(tableSource, sql, excludeBroadcast);
        return sql;
    }

    private SelectSQL getRowIdsWithCategoryQuery(int drillIndex, Set<String> items, List<String> drillSelection, boolean excludeBroadcast) {
        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        Column idColumn = tableSource.getRawIdColumn().with(AggregateFunction.ARRAY_AGG);
        idColumn.setAlias(INTERNAL_ID_COLUMN_NAME);

        int categoryIndex = categoryDrills.size();
        CategoryDefinition cd = getViewDef().getChartSettings().getCategoryDefinitions().get(categoryIndex);
        Column column;
        if (cd.getFieldDef().getFieldType() == FieldType.STATIC) {
            column = tableSource.createConstantColumn(cd.getFieldDef().getStaticText(), cd.getBundleFunction().getReturnType(cd.getFieldDef()));
            sql.select(column);
        } else {
            column = tableSource.getColumn(cd.getFieldDef()) //
                    .with(cd.getBundleFunction()) //
                    .withBundleParams(cd.getStringParamters());

            sql.select(column);
        }
        if (!cd.isAllowNulls()) {
            // If we use the column above where clause will have bundling. We don't want that.
            sql.where(tableSource.getColumn(cd.getFieldDef()).isNull().negate());
        }
        sql.groupBy(column);


        sql.select(idColumn);

        Predicate disjunction = createPredicateFromDrillCategories(drillIndex, items, drillSelection, tableSource);
        sql.where(disjunction);

        // Filter expression.
        applyFilters(tableSource, sql, excludeBroadcast);
        return sql;
    }

    private Predicate createPredicateFromDrillCategories(int drillIndex, Set<String> selectedCategories, List<String> drillSelection, CacheTableSource tableSource) {

        Predicate conjunction = getSqlFactory().predicate(true);
        Predicate drillConjunction = getSqlFactory().predicate(true);
        conjunction.and(drillConjunction);
        for (int ii = 0; ii < drillIndex; ii++) {

            CategoryDefinition drillCategory = getViewDef().getChartSettings().getCategoryDefinitions().get(ii);
            FieldDef fieldDef = drillCategory.getFieldDef();
            CsiDataType dataType = drillCategory.getBundleFunction().getReturnType(fieldDef);
            Column column;
            if (fieldDef.getFieldType() == FieldType.STATIC) {
                column = tableSource.createConstantColumn(fieldDef.getStaticText(), drillCategory.getBundleFunction().getReturnType(fieldDef));

            } else {
                column = tableSource.getColumn(fieldDef) //
                        .with(drillCategory.getBundleFunction()) //
                        .withBundleParams(drillCategory.getStringParamters());
            }
            Predicate predicate = null;
            if ((drillSelection.get(ii) == null) || compareToNullString(drillSelection.get(ii))) {
                predicate = column.isNull();
            } else {
                predicate = column.$(RelationalOperator.EQUAL).value(drillSelection.get(ii), dataType, false);
            }
            drillConjunction = drillConjunction.and(predicate);

        }

        //CategoryDefinition cd = getViewDef().getChartSettings().getCategoryDefinitions().get(drillIndex);
        //CsiDataType returnType = cd.getBundleFunction().getReturnType(cd.getFieldDef());


        return drillConjunction;
    }

    private Predicate createPredicateFromDrillCategories(List<DrillCategory> selections, CacheTableSource tableSource) {
        // Go through each selection and add predicate.
        Predicate disjunction = getSqlFactory().predicate(true);
        for (DrillCategory selection : selections) {
            int index = 0;
            Predicate conjunction = getSqlFactory().predicate(false);

            for (String part : selection.getCategories()) {
                CategoryDefinition cd = getViewDef().getChartSettings().getCategoryDefinitions().get(index);
                if (cd.getFieldDef().getFieldType() != FieldType.STATIC) {
                    Column column = tableSource.getColumn(cd.getFieldDef()) //
                            .with(cd.getBundleFunction()) //
                            .withBundleParams(cd.getStringParamters());
                    Predicate predicate = column.$(EQUAL).value(part, cd.getBundleFunction().getReturnType(cd.getFieldDef()));
                    conjunction = conjunction.and(predicate);
                }
                index++;
            }

            disjunction = disjunction.or(conjunction);
        }

        return disjunction;
    }

    private Predicate createPredicateFromDrillCategories(ChartSelectionState chartSelection, CacheTableSource tableSource) {
        Predicate conjunction = getSqlFactory().predicate(true);

        int drillIndex = 0;
        List<String> drillSelection = chartSelection.getDrillSelections();
        if (drillSelection != null) {
            drillIndex = drillSelection.size();
        }

        Predicate drillConjunction = getSqlFactory().predicate(true);
        conjunction.and(drillConjunction);
        for (int ii = 0; ii < drillIndex; ii++) {

            CategoryDefinition drillCategory = getViewDef().getChartSettings().getCategoryDefinitions().get(ii);
            FieldDef fieldDef = drillCategory.getFieldDef();
            CsiDataType dataType = drillCategory.getBundleFunction().getReturnType(fieldDef);
            Column column;

            Predicate predicate = null;
            if (fieldDef.getFieldType() == FieldType.STATIC) {
                column = tableSource.createConstantColumn(fieldDef.getStaticText(), drillCategory.getBundleFunction().getReturnType(fieldDef));
            } else {
                column = tableSource.getColumn(fieldDef) //
                        .with(drillCategory.getBundleFunction()) //
                        .withBundleParams(drillCategory.getStringParamters());
            }
            if ((drillSelection.get(ii) == null) || compareToNullString(drillSelection.get(ii))) {
                predicate = column.isNull();
            } else {
                predicate = column.$(RelationalOperator.EQUAL).value(drillSelection.get(ii), dataType, false);
            }
            drillConjunction = drillConjunction.and(predicate);

        }

        List<String> selectedItems = new ArrayList<String>();

        for (DrillCategory selection : chartSelection.getSelectedItems()) {
            if ((selection.getCategories().size() - 1) >= drillIndex) {
                String item = selection.getCategories().get(drillIndex);
                if ((item == null) || compareToNullString(item)) {
                    selectedItems.add(null);
                } else {
                    selectedItems.add(selection.getCategories().get(drillIndex));
                }
            }
        }


        CategoryDefinition cd = getViewDef().getChartSettings().getCategoryDefinitions().get(drillIndex);
        CsiDataType returnType = cd.getBundleFunction().getReturnType(cd.getFieldDef());

        Column column;
        if (cd.getFieldDef().getFieldType() == FieldType.STATIC) {
            column = tableSource.createConstantColumn(cd.getFieldDef().getStaticText(), cd.getBundleFunction().getReturnType(cd.getFieldDef()));
        } else {
            column = tableSource.getColumn(cd.getFieldDef()) //
                    .with(cd.getBundleFunction()) //
                    .withBundleParams(cd.getStringParamters());
        }

        Predicate predicate = null;
        if (selectedItems.contains(null)) {
            predicate = column.isNull();
            selectedItems.remove(null);

            if (!selectedItems.isEmpty()) {
               predicate = predicate.or(column.$(RelationalOperator.IN).list(selectedItems, returnType));
            }
        } else {
            predicate = column.$(RelationalOperator.IN).list(selectedItems, returnType);

        }

        conjunction = drillConjunction.and(predicate);


        //        for (DrillCategory selection : chartSelection.getSelectedItems()) {
        //            int index = 0;
        //            Predicate conjunction = getSqlFactory().predicate(false);
        //
        //            for (String part : selection.getCategories()) {
        //                if (cd.getFieldDef().getFieldType() != FieldType.STATIC) {
        //                    Column column = tableSource.getColumn(cd.getFieldDef()) //
        //                            .with(cd.getBundleFunction()) //
        //                            .withBundleParams(cd.getStringParamters());
        //                    Predicate predicate = column.$(EQUAL).value(part, cd.getBundleFunction().getReturnType(cd.getFieldDef()));
        //                    conjunction = conjunction.and(predicate);
        //                }
        //                index++;
        //            }
        //
        //            disjunction = disjunction.or(conjunction);
        //        }

        return conjunction;
    }

    public void setRowIdsToFilterBy(List<Number> rowIds) {
        this.rowIdsToFilterBy = rowIds;
    }

    public List<DrillCategory> rowIdsToSelectionInfo(List<? extends Number> rowIds) {

        final ChartSettings chartSettings = getViewDef().getChartSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        final SelectSQL sql = getSqlFactory().createSelect(tableSource);
        List<CategoryDefinition> categoryDefinitions = chartSettings.getCategoryDefinitions();
        ChartSelectionState selection = getViewDef().getSelection();
        final List<String> drillSelections;

        if ((selection != null) && (selection.getDrillSelections() != null)) {
            drillSelections = selection.getDrillSelections();
        } else {
            drillSelections = new ArrayList<String>();
        }
        int howMany = categoryDefinitions.size();

        for (int i = 0; i < howMany; i++) {
            CategoryDefinition cd = categoryDefinitions.get(i);
            if (cd.getFieldDef().getFieldType() == FieldType.STATIC) {
                Column column = tableSource.createConstantColumn(cd.getFieldDef().getStaticText(), cd.getBundleFunction().getReturnType(cd.getFieldDef()));
                sql.select(column);
            } else {
                Column column = tableSource.getColumn(cd.getFieldDef()).with(cd.getBundleFunction()).withBundleParams(cd.getStringParamters());
                sql.select(column);
                //TODO: CEN-2541 - We need the nulls to go through for broadcasts, will remove pointless nulls on client
                //Better fix would be to allow nulls in all but current drill levels metric
                if (false) {//!cd.isAllowNulls()) {
                    // If we use the column above where clause will have bundling. We don't want that.
                    sql.where(tableSource.getColumn(cd.getFieldDef()).isNull().negate());
                }
                sql.groupBy(column);

                if ((drillSelections != null) && !drillSelections.isEmpty()) {
                    if (drillSelections.size() > i) {
                        sql.where(column.$(EQUAL).value(drillSelections.get(i), CsiDataType.String));
                    }
                } else {
                    break;
                }
            }
        }

        // Add where clause for row ids
        Column column = tableSource.getIdColumn();
        sql.where(column.$(IN).list(rowIds, CsiDataType.Integer));

        // Filter expression.
        applyFilters(tableSource, sql, false);

        sql.distinct();
        final List<DrillCategory> selections = new ArrayList<DrillCategory>();

        sql.scroll(new ScrollCallback<Void>() {

            @Override
            public Void scroll(ResultSet resultSet) throws SQLException {
                try {
                    while (resultSet.next()) {
                        ArrayList<String> parts = new ArrayList<String>(categoryDefinitions.size());
                        for (int i = 0; i < (drillSelections.size() + 1); i++) {
                            parts.add(ChartActionsServiceUtil.getParsedString(resultSet.getObject(i + 1)));
                        }
                        DrillCategory drillCategory = new DrillCategory();
                        drillCategory.setCategories(parts);

                        selections.add(drillCategory);
                    }
                    return null;
                } catch (SQLException sqe) {
                   LOG.error("Error executing query: " + sql.toString(), sqe);
                    throw sqe;
                }

            }
        });
        return selections;
    }

    public Set<Integer> selectionValuesToRows(List<DrillCategory> itemListIn, boolean excludeBroadcast) {
        return getRowIdsSQLQuery(itemListIn, excludeBroadcast).scroll(new ScrollCallback<Set<Integer>>() {

            @Override
            public Set<Integer> scroll(ResultSet resultSet) throws SQLException {
                Set<Integer> rowids = new HashSet<Integer>();
                while (resultSet.next()) {
                    rowids.add(resultSet.getInt(1));
                }
                return rowids;
            }
        });
    }

    public Set<Integer> selectionValuesToRows(ChartSelectionState chartSelection, boolean excludeBroadcast) {

        int drillIndex = 0;
        List<String> drillSelection = chartSelection.getDrillSelections();
        if (drillSelection != null) {
            drillIndex = drillSelection.size();
        }

        Set<String> selectedItems = new HashSet<String>();

        for (DrillCategory selection : chartSelection.getSelectedItems()) {
            if ((selection.getCategories().size() - 1) >= drillIndex) {
                String item = selection.getCategories().get(drillIndex);
                if ((item == null) || compareToNullString(item)) {
                    selectedItems.add(null);
                } else {
                    selectedItems.add(selection.getCategories().get(drillIndex));
                }
            }
        }
        List<Integer> rowids;

        //We use the IN(Selection) query if there aren't too many, otherwise we query everything and filter in Java
        if (selectedItems.size() < SELECTION_QUERY_IN_LIMIT) {
            rowids = getRowIdsSQLQuery(chartSelection, excludeBroadcast).scroll(new ScrollCallback<List<Integer>>() {

                @Override
                public List<Integer> scroll(ResultSet resultSet) throws SQLException {

                    List<Integer> scrollingIds = new ArrayList<Integer>();
                    while (resultSet.next()) {
                        scrollingIds.add(resultSet.getInt(1));
                    }
                    return scrollingIds;
                }
            });
        } else {
            rowids = getRowIdsWithCategoryQuery(drillIndex, selectedItems, drillSelection, excludeBroadcast).scroll(new ScrollCallback<List<Integer>>() {

                @Override
                public List<Integer> scroll(ResultSet resultSet) throws SQLException {

                    List<Integer> scrollingIds = new ArrayList<Integer>();
                    while (resultSet.next()) {

                        Object categoryName = resultSet.getObject(1);
                        if (categoryName == null) {
                            if (selectedItems.contains(null)) {
                                int[] ids = (int[]) resultSet.getArray(2).getArray();
                                for (int ii = 0; ii < ids.length; ii++) {
                                    scrollingIds.add(ids[ii]);
                                }
                                continue;
                            }
                        }
                        if (selectedItems.contains(ChartActionsServiceUtil.getParsedString(categoryName))) {
                            Integer[] ids = (Integer[]) resultSet.getArray(2).getArray();
                            for (int ii = 0; ii < ids.length; ii++) {
                                scrollingIds.add(ids[ii]);
                            }
                        }
                    }
                    return scrollingIds;
                }
            });
        }

        return new HashSet<Integer>(rowids);
    }

    public void setCategoryDrills(List<String> categoryDrills) {
        //        this.categoryDrills = Lists.newArrayList();
        //        for (String categoryDrill : categoryDrills) {
        //        	this.categoryDrills.add(categoryDrill.replace("'", "''"));
        //        }
        this.categoryDrills = categoryDrills;
    }

    public void setPage(int start, int end) {
        this.limit = end - start;
        this.offset = start;
    }

    public void setAggregateOnly() {
        isAggregateOnly = true;
    }

    public static boolean compareToNullString(String value) {
        if (value == null) {
            //kind of weird, but the idea is that nulls need to return true I guess...
            return true;
        } else if (value.length() == 0) {
            return false;
        }

        return value.equals(ChartTableGenerator.SECRET_NULL_CHART_LABEL);
    }

}
