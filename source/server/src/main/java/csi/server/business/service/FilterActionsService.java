/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.business.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.RelationalOperator;
import csi.server.common.model.BundledFieldReference;
import csi.server.common.model.FieldDef;
import csi.server.common.model.SortOrder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.FilterPickEntry;
import csi.server.common.model.filter.IntervalValueDefinition;
import csi.server.common.model.filter.MultiValueDefinition;
import csi.server.common.model.filter.OperandTypeAndValue;
import csi.server.common.model.filter.ScalarValueDefinition;
import csi.server.common.model.filter.StaticFieldValueDefinition;
import csi.server.common.model.filter.ValueDefinition;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.service.api.FilterActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.PredicateFragment;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.SelectResultRow;
import csi.server.util.sql.SelectResultSet;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.api.AggregateFunction;
import csi.server.util.sql.impl.LevenshteinPredicate;
import csi.server.util.sql.impl.SoundexPredicate;
import csi.server.util.sql.impl.TautologicalPredicate;
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * Handles filter management and filter-definition to query translation logic for visualizations.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterActionsService implements FilterActionsServiceProtocol {
   @Inject
   private SQLFactory sqlFactory;

   public SQLFactory getSqlFactory() {
       return sqlFactory;
   }

   public void setSqlFactory(SQLFactory sqlFactory) {
      this.sqlFactory = sqlFactory;
   }

   /**
    * @param visualization Visualization to which SQL predicate is to be added.
    * @param dataView
    * @return SQL string (if visualization doesn't have an associated SQL, a noop
    *         predicate (1 = 1) is returned.
    */
   public String getPredicateSQL(VisualizationDef visualization, DataView dataView) {
      CacheTableSource tableSource = getCacheTableSource(dataView);
      return ((PredicateSpi) getPredicate(visualization, dataView, tableSource)).getSQL();
   }

   public String getAliasedPredicateSQL(VisualizationDef visualization, DataView dataView) {
      CacheTableSource tableSource = getCacheTableSource(dataView);
      return ((PredicateSpi) getPredicate(visualization, dataView, tableSource)).getAliasedSQL();
   }

   public CacheTableSource getCacheTableSource(DataView dataView) {
      return sqlFactory.getTableSourceFactory().create(dataView);
   }

   /**
    * @param visualization
    * @param dataView
    * @param tableSource
    * @return predicate to apply to query.
    */
   public Predicate getPredicate(VisualizationDef visualization, DataView dataView, CacheTableSource tableSource) {
      Predicate result = null;

      if (!StringUtils.isEmpty(visualization.getFilterUuid())) {
         Filter filter = null;

         for (Filter aFilter : dataView.getMeta().getFilters()) {
            if (aFilter.getUuid().equals(visualization.getFilterUuid())) {
               filter = aFilter;
               break;
            }
         }
         if (filter == null) {
            throw new RuntimeException("Visualization " + visualization.getName() + " has filter reference "
                  + visualization.getFilterUuid() + " that doesn't exist in the dataview.");
         }
         for (FilterExpression expression : filter.getFilterDefinition().getFilterExpressions()) {
            if (result == null) {
               result = getCheckPredicate(expression, tableSource);
            } else {
               result = result.and(getCheckPredicate(expression, tableSource));
            }
         }
      }
      return (result == null) ? new TautologicalPredicate() : result;
   }

   public Predicate getCheckPredicate(FilterExpression expressionIn, CacheTableSource tableSourceIn) {
      Predicate result = null;

      if ((expressionIn != null) && (RelationalOperator.IN == expressionIn.getOperator())) {
         ValueDefinition valueDefinition = expressionIn.getValueDefinition();
         List<OperandTypeAndValue<?>> valueList = (valueDefinition instanceof MultiValueDefinition)
                                                     ? ((MultiValueDefinition) valueDefinition).getValues()
                                                     : null;
         int indexToNull = hasNull(valueList);

         if (indexToNull >= 0) {
            FilterExpression expression = ((valueList != null) && (valueList.size() > 1))
                                             ? expressionIn.getClone()
                                             : null;
            FieldDef field = expressionIn.getFieldDef();

            if (expression != null) {
               valueDefinition = expression.getValueDefinition();
               valueList = ((MultiValueDefinition) valueDefinition).getValues();
               valueList.remove(indexToNull);
            }
            if (expressionIn.isNegated()) {
               Predicate predicate = getPredicate(FilterExpression.getIsNotNull(field), tableSourceIn);

               result = (expression != null) ? predicate.and(getPredicate(expression, tableSourceIn)) : predicate;
            } else {
               Predicate predicate = getPredicate(FilterExpression.getIsNull(field), tableSourceIn);

               result = (expression != null) ? predicate.or(getPredicate(expression, tableSourceIn)) : predicate;
            }
         } else {
            result = getPredicate(expressionIn, tableSourceIn);
         }
      } else {
         result = getPredicate(expressionIn, tableSourceIn);
      }
      return result;
   }

   private static int hasNull(List<OperandTypeAndValue<?>> listIn) {
      int result = -1;

      if ((listIn != null) && !listIn.isEmpty()) {
         int index = 0;

         for (OperandTypeAndValue<?> object : listIn) {
            Object value = ((OperandTypeAndValue<?>) object).getValue();

            if ((FilterOperandType.STATIC == ((OperandTypeAndValue<?>) object).getType()) &&
                (value instanceof String) && FilterExpression.NULL_INDICATOR.equals(value)) {
               result = index;
               break;
            }
            index++;
         }
      }
      return result;
   }

   private static void escapeSpecialCharacters(ScalarValueDefinition<String> scalarValueDefinition) {
       String value = scalarValueDefinition.getValue().getValue();

       value.replaceAll("\\(", "\\(");
       value.replaceAll("\\)", "\\)");
       value.replaceAll("\\[", "\\[");
       value.replaceAll("\\]", "\\]");
       scalarValueDefinition.getValue().setValue(value);
   }

   public Predicate getPredicate(FilterExpression expression, CacheTableSource tableSource) {
      Predicate predicate = null;
      RelationalOperator operator = expression.getOperator();
      Column column;

      if (expression.isSelectionFilter()) {
         column = tableSource.getIdColumn();
      } else {
         column = tableSource.getColumn(expression.getFieldDef());

         column.with(expression.getBundleFunction()).withBundleParams(expression.getStringParamters());
      }
      switch (expression.getValueDefinition().getCardinality()) {
         case NONE:
            predicate = column.$(operator).value(null);
            break;
         case SCALAR:
            PredicateFragment scalarFragment = column.$(operator);

            if (expression.getValueDefinition() instanceof StaticFieldValueDefinition) {
               StaticFieldValueDefinition sfvd = (StaticFieldValueDefinition) expression.getValueDefinition();
               predicate = scalarFragment.value(sfvd.getStaticFieldReference().getField().getStaticText());
            } else if (expression.getValueDefinition() instanceof ScalarValueDefinition) {
               ScalarValueDefinition<?> scalarValueDefinition = (ScalarValueDefinition<?>) expression.getValueDefinition();

               switch (scalarValueDefinition.getValue().getType()) {
                  case STATIC:
                     // This is to handle allowing of special characters like (, [, {, }, ], or ) in the caseless match.
                     if ((scalarValueDefinition.getDataType().equals(CsiDataType.String)) &&
                         (operator == RelationalOperator.MATCHES_CASELESS)) {
                        escapeSpecialCharacters((ScalarValueDefinition<String>) scalarValueDefinition);
                     }
                     switch (operator) {
                        case SOUNDEX:
                           predicate = new SoundexPredicate(column, scalarValueDefinition.getValue().getValue());
                           break;
                        case LEVENSHTEIN:
                           predicate = new LevenshteinPredicate(column, scalarValueDefinition.getValue().getValue());
                           break;
                        default:
                           predicate = scalarFragment.value(scalarValueDefinition.getValue().getValue(),
                                                            scalarValueDefinition.getDataType());
                           break;
                     }
                     break;
                  case COLUMN:
                     FieldDef fieldDef = (FieldDef) scalarValueDefinition.getValue().getValue();
//                     Column secondCol = tableSource.getColumn(fieldDef);

//                     secondCol.with(expression.getBundleFunction()).withBundleParams(expression.getBundleFunctionParameters());  //TODO
                    predicate = scalarFragment.column(tableSource.getColumn(fieldDef));
                     break;
                  case PARAMETER:  //TODO
                     break;
               }
            }
            break;
         case VECTOR:
            PredicateFragment vectorFragment = column.$(operator);
            MultiValueDefinition<?> multiValueDefinition = (MultiValueDefinition<?>) expression.getValueDefinition();
            List<Object> staticValues = new ArrayList<Object>();
            Collection<Column> columns = new ArrayList<Column>();

            for (OperandTypeAndValue<?> operandTypeAndValue : multiValueDefinition.getValues()) {
               switch (operandTypeAndValue.getType()) {
                  case STATIC:
                     staticValues.add(operandTypeAndValue.getValue());
                     break;
                  case COLUMN:
                     columns.add(tableSource.getColumn((FieldDef) operandTypeAndValue.getValue()));
                     break;
                  case PARAMETER:  //TODO
                     break;
               }
            }
            if (!staticValues.isEmpty()) {
               predicate = vectorFragment.list(staticValues, multiValueDefinition.getDataType());
            }
            for (Column eachColumn : columns) {
               PredicateFragment columnFragment = column.$(RelationalOperator.EQUAL);

               if (predicate == null) {
                  predicate = columnFragment.column(eachColumn);
               } else {
                  predicate = predicate.or(columnFragment.column(eachColumn));
               }
            }
            break;
         case RANGE:
            IntervalValueDefinition<?> intervalValueDefinition = (IntervalValueDefinition<?>) expression.getValueDefinition();
            PredicateFragment startFragment =
               column.$((intervalValueDefinition.isStartInclusive()) ? RelationalOperator.GE : RelationalOperator.GT);

            switch (intervalValueDefinition.getStart().getType()) {
               case STATIC:
                  predicate = startFragment.value(intervalValueDefinition.getStart().getValue(),
                                                  intervalValueDefinition.getDataType());
                  break;
               case COLUMN:
                  FieldDef fieldDef = (FieldDef) intervalValueDefinition.getStart().getValue();
//                  Column secondCol = tableSource.getColumn(fieldDef);

//                  secondCol.with(expression.getBundleFunction()).withBundleParams(expression.getBundleFunctionParameters());  //TODO

                  predicate = startFragment.column(tableSource.getColumn(fieldDef));
                  break;
               case PARAMETER:  //TODO
                  break;
            }
            PredicateFragment endFragment =
               column.$((intervalValueDefinition.isEndInclusive()) ? RelationalOperator.LE : RelationalOperator.LT);

            switch (intervalValueDefinition.getEnd().getType()) {
               case STATIC:
                  predicate = endFragment.value(intervalValueDefinition.getEnd().getValue(),
                                                intervalValueDefinition.getDataType());
                  break;
               case COLUMN:
                  FieldDef fieldDef = (FieldDef) intervalValueDefinition.getEnd().getValue();
//                  Column secondCol = tableSource.getColumn(fieldDef);

//                  secondCol.with(expression.getBundleFunction()).withBundleParams(expression.getBundleFunctionParameters());  //TODO

                  predicate = predicate.and(endFragment.column(tableSource.getColumn(fieldDef)));
                  break;
               case PARAMETER:  //TODO
                  break;
            }
            break;
      }
      if ((predicate != null) && expression.isNegated()) {
         predicate = predicate.negate();
      }
      return predicate;
   }

   @Override
   public PagingLoadResult<FilterPickEntry> getPickList(String dataViewUuid, BundledFieldReference fieldReference,
                                                        PagingLoadConfig loadConfig) {
      PagingLoadResultBean<FilterPickEntry> result = new PagingLoadResultBean<FilterPickEntry>();
      DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
      CacheTableSource tableSource = sqlFactory.getTableSourceFactory().create(dataView);
      Column column = tableSource.getColumn(fieldReference.getFieldDef())
                                 .with(fieldReference.getBundleFunction())
                                 .withBundleParams(fieldReference.getStringParamters());
      SelectSQL sql = getSqlFactory().createSelect(tableSource);

      sql.select(column);

      Column countColumn = tableSource.createConstantColumn(1).with(AggregateFunction.COUNT);

      sql.select(countColumn);
      sql.groupBy(column);

      SortInfo sortInfo = loadConfig.getSortInfo().isEmpty() ? null : loadConfig.getSortInfo().get(0);

      if (sortInfo != null) {
         if (sortInfo.getSortField().equalsIgnoreCase(FilterPickEntry.LABEL_VALUE)) {
            sql.orderBy(column, sortInfo.getSortDir() == SortDir.ASC ? SortOrder.ASC : SortOrder.DESC);
         } else {
            sql.orderBy(countColumn, sortInfo.getSortDir() == SortDir.ASC ? SortOrder.ASC : SortOrder.DESC);
         }
      }
      // sql.offset(loadConfig.getOffset()).limit(loadConfig.getLimit());

      List<FilterPickEntry> list = new ArrayList<FilterPickEntry>();
      SelectResultSet resultSet = sql.execute();
      int offsetCount = 0;

      for (SelectResultRow row : resultSet) {
         FilterPickEntry entry = new FilterPickEntry();
         Object o = row.<Object>getValue(0);

         if ((offsetCount >= loadConfig.getOffset()) && (offsetCount < (loadConfig.getOffset() + loadConfig.getLimit()))) {
            entry.setValue((o == null) ? "<<null>>" : o.toString());
            entry.setFrequency(row.<Long>getValue(1).intValue());
            list.add(entry);
         }
         offsetCount++;
      }
      result.setTotalLength(offsetCount);
      result.setData(list);
      result.setOffset(loadConfig.getOffset());
      return result;
   }

   @Override
   public List<String> getPickListSelection(String dataViewUuid, BundledFieldReference fieldReference,
                                            List<String> selectionQualifier) {
      List<String> list = new ArrayList<String>();
      DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataViewUuid);
      CacheTableSource tableSource = sqlFactory.getTableSourceFactory().create(dataView);
      Column column = tableSource.getColumn(fieldReference.getFieldDef())
                                 .with(fieldReference.getBundleFunction())
                                 .withBundleParams(fieldReference.getStringParamters());
      SelectSQL sql = getSqlFactory().createSelect(tableSource);

      sql.distinct();
      sql.select(column);

      Predicate predicate = column.$(RelationalOperator.IN).list(selectionQualifier, fieldReference.getDataType()).negate();

      sql.where(predicate);

      SelectResultSet resultSet = sql.execute();

      for (SelectResultRow row : resultSet) {
         Object o = row.<Object>getValue(0);

         list.add((o == null) ? "<<null>>" : o.toString());
      }
      return list;
   }
}
