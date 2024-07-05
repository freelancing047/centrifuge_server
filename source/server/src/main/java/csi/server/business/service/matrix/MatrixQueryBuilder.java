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
package csi.server.business.service.matrix;

import static csi.server.common.enumerations.RelationalOperator.EQUAL;
import static csi.server.common.enumerations.RelationalOperator.IN;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.selection.MatrixPair;
import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.AbstractAttributeDefinition;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.SingleDoubleTypeCriterion;
import csi.server.common.model.visualization.chart.TwoDoubleTypeCriterion;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixType;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
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
 *
 */
public class MatrixQueryBuilder extends AbstractQueryBuilder<MatrixViewDef> {
   private static final Logger LOG = LogManager.getLogger(MatrixQueryBuilder.class);

    public SelectSQL getQuery(HashMap<ChartCriterion, Double> criteriaValue, HashMap<ChartCriterion, Integer> threshholdIndex, int size) {
        MatrixSettings matrixSettings = getViewDef().getMatrixSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        Map<AbstractAttributeDefinition, Column> columnsByDefintions = new HashMap<AbstractAttributeDefinition, Column>();
        // X & Y axis
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisX().get(0);
            Column column = tableSource.getColumn(mcd.getFieldDef()) //
                    .with(mcd.getBundleFunction()).withBundleParams(mcd.getStringParamters());
            sql.select(column);


            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }

            columnsByDefintions.put(mcd, column);
            sql.groupBy(column);
        }
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisY().get(0);
            Column column = tableSource.getColumn(mcd.getFieldDef()).with(mcd.getBundleFunction()) //
                    .withBundleParams(mcd.getStringParamters());
            sql.select(column);
            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }
            columnsByDefintions.put(mcd, column);
            sql.groupBy(column);
        }


        // Measure
        {
            MatrixMeasureDefinition mmd = matrixSettings.getMatrixMeasureDefinition();
            Column column;
            if (matrixSettings.isUseCountForMeasure()) {
                column = tableSource.createConstantColumn(1).with(AggregateFunction.COUNT);
                sql.select(column);
            } else {
                column = tableSource.getColumn(mmd.getFieldDef()).with(mmd.getAggregateFunction());
                sql.select(column);
                if (!mmd.isAllowNulls()) {
                    // If we use the column above where clause will have bundling. We don't want that.
                    sql.where(tableSource.getColumn(mmd.getFieldDef()).isNull().negate());
                }
            }
            columnsByDefintions.put(mmd, column);
        }

        // id columns aggregated into array
        {
            Column idColumn = tableSource.getRawIdColumn().with(AggregateFunction.ARRAY_AGG);
            idColumn.setAlias(INTERNAL_ID_COLUMN_NAME);
            sql.select(idColumn);
        }

        // Order by (only for sort by measure for non-co-occurrence type)
        if (!matrixSettings.isSortByAxis() && (matrixSettings.getMatrixType() != MatrixType.CO_OCCURRENCE)
                && (matrixSettings.getMatrixType() != MatrixType.CO_OCCURRENCE_DIR)) {
            sql.orderBy(columnsByDefintions.get(matrixSettings.getMatrixMeasureDefinition()), matrixSettings.getMeasureSortOrder());
        }

        if(matrixSettings.getFilterCriteria().stream().anyMatch(chartCriterion -> chartCriterion.getOperatorString().equals("Top%") || chartCriterion.getOperatorString().equals("Bottom%")
                || chartCriterion.getOperatorString().equals("Top") || chartCriterion.getOperatorString().equals("Bottom"))){
            sql.orderBy(columnsByDefintions.get(matrixSettings.getAxisX().get(0)), matrixSettings.getAxisSortDefinitions() == null ? SortOrder.ASC : matrixSettings.getAxisSortDefinitions().get(0).getSortOrder());
            sql.orderBy(columnsByDefintions.get(matrixSettings.getAxisY().get(0)), matrixSettings.getAxisSortDefinitions() == null ? SortOrder.ASC : matrixSettings.getAxisSortDefinitions().get(1).getSortOrder());

        }

        // Filter
        applyFilters(tableSource, sql, false);


        // having clause selects...?
        SelectSQL innerSQL = sql;

        SubSelectTableSource innerSQLSource = getSqlFactory().getTableSourceFactory().create(innerSQL);
        SelectSQL outerSQL = getSqlFactory().createSelect(innerSQLSource);

        List<? extends Column> columns = innerSQLSource.getSubSelectColumns();

        for (Column column : columns) {
            outerSQL.select(innerSQLSource.getColumn(column));
        }

        if (matrixSettings.getFilterCriteria() != null) {
//
            applyCriteriaQuery(outerSQL, matrixSettings, innerSQLSource.getColumn(columns.get(columns.size() - 2)), criteriaValue, threshholdIndex, size);
            String sqlString = ((SelectSQLSpi) outerSQL).getSQL();
            LOG.trace(sqlString);
            return outerSQL;
        } else {
            String sqlString = ((SelectSQLSpi) sql).getSQL();
            LOG.trace(sqlString);
            return sql;
        }



    }

    // in order to apply having clause filter, we need index of "threshhold" and category limit
    public SelectSQL getPreFilterQuery() {
        MatrixSettings matrixSettings = getViewDef().getMatrixSettings();
        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);

        Map<AbstractAttributeDefinition, Column> columnsByDefintions = new HashMap<AbstractAttributeDefinition, Column>();
        // Measure

        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisX().get(0);
            Column column = tableSource.getColumn(mcd.getFieldDef()) //
                    .with(mcd.getBundleFunction()).withBundleParams(mcd.getStringParamters());
            sql.select(column);

            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }

            columnsByDefintions.put(mcd, column);
            sql.groupBy(column);
        }
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisY().get(0);
            Column column = tableSource.getColumn(mcd.getFieldDef()) //
                    .with(mcd.getBundleFunction()) //
                    .withBundleParams(mcd.getStringParamters());
            sql.select(column);
            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }
            columnsByDefintions.put(mcd, column);
            sql.groupBy(column);
        }


        {
            MatrixMeasureDefinition mmd = matrixSettings.getMatrixMeasureDefinition();
            Column column;
            if (matrixSettings.isUseCountForMeasure()) {
                column = tableSource.createConstantColumn(1).with(AggregateFunction.COUNT);
                sql.select(column);
            } else {
                column = tableSource.getColumn(mmd.getFieldDef()).with(mmd.getAggregateFunction());
                sql.select(column);
                if (!mmd.isAllowNulls()) {
                    // If we use the column above where clause will have bundling. We don't want that.
                    sql.where(tableSource.getColumn(mmd.getFieldDef()).isNull().negate());
                }
            }
            columnsByDefintions.put(mmd, column);
        }

        sql.orderBy(columnsByDefintions.get(matrixSettings.getMatrixMeasureDefinition()), SortOrder.DESC_NULLS_LAST);


        // Filter
        applyFilters(tableSource, sql, false);

        // having clause selects...?
        SelectSQL innerSQL = sql;

        SubSelectTableSource innerSQLSource = getSqlFactory().getTableSourceFactory().create(innerSQL);
        SelectSQL outerSQL = getSqlFactory().createSelect(innerSQLSource);

        List<? extends Column> columns = innerSQLSource.getSubSelectColumns();

        for (Column column : columns) {
            outerSQL.select(innerSQLSource.getColumn(column));
        }


        String sqlString = ((SelectSQLSpi) sql).getSQL();
        LOG.trace(sqlString);
        return sql;
    }

    private void applyCriteriaQuery(SelectSQL outerSQL, MatrixSettings matrixS, Column mColumn, HashMap<ChartCriterion, Double> criteriaValue, HashMap<ChartCriterion, Integer> threshholdIndex, int size) {
        List<ChartCriterion> criterion = matrixS.getFilterCriteria();
//        SubSelectTableSource sqlSource = getSqlFactory().getTableSourceFactory().create(outerSQL);
//        List<? extends Column> columns = sqlSource.getSubSelectColumns();

        List<Predicate> predicates = new ArrayList<Predicate>();
        for (ChartCriterion criteria : criterion) {

            Column column = mColumn;
            CsiDataType type;
            if (matrixS.isUseCountForMeasure()) {
                type = CsiDataType.Number;
            } else {
                type = matrixS.getMatrixMeasureDefinition().getDerivedType();
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
                    if (criteriaValue.get(criteria) != null) {
                        predicates.add(column.$(RelationalOperator.GE).value(criteriaValue.get(criteria)));
                    }
                    break;
                case "Top%":
                    if (criteriaValue.get(criteria) != null) {
//                        predicates.add(column.$(RelationalOperator.GE).value(criteriaValue.get(criteria)));
                    }
                    if (threshholdIndex.get(criteria) != null) {
//                       outerSQL.limit(threshholdIndex.get(criteria));
                    }
                    break;
                case "Bottom":
                    if (criteriaValue.get(criteria) != null) {
                        predicates.add(column.$(RelationalOperator.LE).value(criteriaValue.get(criteria)));
                    }
                    break;
                case "Bottom%":
                    if (criteriaValue.get(criteria) != null) {
//                        predicates.add(column.$(RelationalOperator.LE).value(criteriaValue.get(criteria)));
                    }
                    if (threshholdIndex.get(criteria) != null) {
//                        outerSQL.offset(size - threshholdIndex.get(criteria));
                    }
                    break;
                default:
                    break;
            }
        }

        for (Predicate predicate : predicates) {
            outerSQL.where(predicate);

        }

        // in case of top/botton N cells
        int offset = 0;
        int globalLimit = 0;
        for (ChartCriterion criteria : criterion) {
            String operatorString = criteria.getOperatorString();
            switch (operatorString) {
                case "Top":
                case "Top%":
                    if (!threshholdIndex.isEmpty()) {
                       globalLimit += threshholdIndex.get(criteria) == 0 ? 1 : threshholdIndex.get(criteria);
                    }
                    break;
                case "Bottom":
                case "Bottom%":
                    // so the number of items left in the list
                    offset += threshholdIndex.get(criteria);
                    globalLimit += size - threshholdIndex.get(criteria);
                    break;
            }
        }
        if ((globalLimit != 0) && (offset == 0)) {
            outerSQL.limit(globalLimit);
        }
        if (offset != 0) {
            outerSQL.offset(offset);
        }
    }

    private SelectSQL getRowIdsSQLQuery(List<String> xAxisCategories, List<String> yAxisCategories, boolean ignoreBroadcast) {
        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        Column idColumn = tableSource.createDistinctColumn(tableSource.getIdColumn());
        idColumn.setAlias(INTERNAL_ID_COLUMN_NAME); // The broadcast service code expects this as the alias.
        sql.select(idColumn);

        HashSet<String> dedupedX = new HashSet<String>(xAxisCategories);
        HashSet<String> dedupedY = new HashSet<String>(yAxisCategories);

        Predicate xAxisIn = createPredicateFromCategory(new ArrayList<String>(dedupedX), tableSource, getViewDef().getMatrixSettings().getAxisX().get(0));
        Predicate yAxisIn = createPredicateFromCategory(new ArrayList<String>(dedupedY), tableSource, getViewDef().getMatrixSettings().getAxisY().get(0));
        Predicate disjunction = createPredicateFromCategories(xAxisCategories, yAxisCategories, tableSource);

        sql.where(xAxisIn, yAxisIn, disjunction);


        // Filter expression.
        applyFilters(tableSource, sql, ignoreBroadcast);

        return sql;
    }

    private Predicate createPredicateFromCategory(List<String> dedupedy, CacheTableSource tableSource, MatrixCategoryDefinition md) {

        Predicate conjunction = null;

        FieldDef field = md.getFieldDef();


        Column column = tableSource.getColumn(md.getFieldDef()) //
                .with(md.getBundleFunction()) //
                .withBundleParams(md.getStringParamters());
        Predicate predicate = column.$(RelationalOperator.IN).list(dedupedy, field.getValueType());
        conjunction = predicate;

        return conjunction;
    }

    private Predicate createPredicateFromCategories(List<String> xAxisCategories, List<String> yAxisCategories, CacheTableSource tableSource) {
        // Go through each selection and add predicate.
        Predicate disjunction = getSqlFactory().predicate(true);
        for (int i = 0; i < xAxisCategories.size(); i++) {

            String categoryX = xAxisCategories.get(i);
            String categoryY = yAxisCategories.get(i);

            Predicate conjunction = null;

            // X-axis
            {
                String part = categoryX;
                MatrixCategoryDefinition md = getViewDef().getMatrixSettings().getAxisX().get(0);
                Column column = tableSource.getColumn(md.getFieldDef()) //
                        .with(md.getBundleFunction()) //
                        .withBundleParams(md.getStringParamters());
                Predicate predicate = column.$(EQUAL).value(part, md.getBundleFunction().getReturnType(md.getFieldDef()));
                conjunction = predicate;
            }
            // Y-axis
            {
                String part = categoryY;
                MatrixCategoryDefinition md = getViewDef().getMatrixSettings().getAxisY().get(0);
                Column column = tableSource.getColumn(md.getFieldDef()) //
                        .with(md.getBundleFunction()) //
                        .withBundleParams(md.getStringParamters());

                Predicate predicate = column.$(EQUAL).value(part, md.getBundleFunction().getReturnType(md.getFieldDef()));
                conjunction = conjunction.and(predicate);
            }

            disjunction = disjunction.or(conjunction);
        }
        return disjunction;
    }

    public List<MatrixPair> rowIdsToSelectionInfo(List<? extends Number> rowIds) {
        final MatrixSettings matrixSettings = getViewDef().getMatrixSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisX().get(0);
            Column column = tableSource.getColumn(mcd.getFieldDef()) //
                    .with(mcd.getBundleFunction()) //
                    .withBundleParams(mcd.getStringParamters());
            sql.select(column);
            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }
        }
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisY().get(0);
            Column column = tableSource.getColumn(mcd.getFieldDef()) //
                    .with(mcd.getBundleFunction()) //
                    .withBundleParams(mcd.getStringParamters());
            sql.select(column);
            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }
        }

        {
            Column idColumn = tableSource.getRawIdColumn();
            idColumn.setAlias(INTERNAL_ID_COLUMN_NAME); // The broadcast service code expects this as the alias.
            sql.select(idColumn);
            sql.orderBy(idColumn, SortOrder.ASC);
            // Add where clause for row ids
            sql.where(idColumn.$(IN).list(rowIds, CsiDataType.Integer));
        }


        // Filter expression.
        applyFilters(tableSource, sql, false);


        final List<MatrixPair> selections = new ArrayList<MatrixPair>();

        sql.scroll(new ScrollCallback<Void>() {

            @Override
            public Void scroll(ResultSet resultSet) throws SQLException {
                while (resultSet.next()) {
                    MatrixPair pair = new MatrixPair(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3));
                    selections.add(pair);
                }
                return null;
            }
        });
        return selections;
    }

    public List<Integer> widenRowSelection(List<? extends Number> rowIds) {
        final MatrixSettings matrixSettings = getViewDef().getMatrixSettings();

        CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
        SelectSQL sql = getSqlFactory().createSelect(tableSource);
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisX().get(0);
//            Column column = tableSource.getColumn(mcd.getFieldDef()) //
//                    .with(mcd.getBundleFunction()) //
//                    .withBundleParams(mcd.getStringParamters());
            //sql.select(column);
            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }
        }
        {
            MatrixCategoryDefinition mcd = matrixSettings.getAxisY().get(0);
//            Column column = tableSource.getColumn(mcd.getFieldDef()) //
//                    .with(mcd.getBundleFunction()) //
//                    .withBundleParams(mcd.getStringParamters());
            //sql.select(column);
            if (!mcd.isAllowNulls()) {
                sql.where(tableSource.getColumn(mcd.getFieldDef()).isNull().negate());
            }
        }

        {
            Column idColumn = tableSource.getRawIdColumn();
            idColumn.setAlias(INTERNAL_ID_COLUMN_NAME); // The broadcast service code expects this as the alias.
            sql.select(idColumn);
        }

        // Add where clause for row ids
        Column column = tableSource.getIdColumn();
        sql.where(column.$(IN).list(rowIds, CsiDataType.Integer));

        // Filter expression.
        applyFilters(tableSource, sql, false);

        sql.distinct();
        final List<Integer> selections = new ArrayList<Integer>();

        sql.scroll(new ScrollCallback<Void>() {

            @Override
            public Void scroll(ResultSet resultSet) throws SQLException {
                while (resultSet.next()) {
                    selections.add(resultSet.getInt(1));
                }
                return null;
            }
        });
        return selections;
    }
}
