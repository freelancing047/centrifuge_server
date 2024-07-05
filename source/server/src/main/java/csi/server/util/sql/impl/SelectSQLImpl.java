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
package csi.server.util.sql.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import csi.config.Configuration;
import csi.config.DBConfig;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.SortOrder;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.SqlUtil;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.ScrollCallback;
import csi.server.util.sql.SelectResultSet;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.TableSource;
import csi.server.util.sql.impl.spi.ColumnSpi;
import csi.server.util.sql.impl.spi.PredicateSpi;
import csi.server.util.sql.impl.spi.SelectSQLSpi;
import csi.server.util.sql.impl.spi.TableSourceSpi;

/**
 * To be supported: having clause, DISTINCT, UNION, INTERSECT, EXCEPT
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectSQLImpl implements SelectSQLSpi {
   private static final Logger LOG = LogManager.getLogger(SelectSQLImpl.class);

    private static final String SEPARATOR_COMMA = ", ";
    private static final String CONJUNCTION_OPERATOR = " AND ";

    private TableSourceSpi tableSource;

    private List<ColumnSpi> columns = new ArrayList<ColumnSpi>();
    private List<ColumnSpi> groupBy = new ArrayList<ColumnSpi>();
    private List<ColumnOrdering> orderBy = new ArrayList<ColumnOrdering>();
    private List<PredicateSpi> predicates = new ArrayList<PredicateSpi>();
    private List<PredicateSpi> havings = new ArrayList<PredicateSpi>();

    private Integer offset;
    private Integer limit;
    private boolean distinct;

    public SelectSQLImpl(TableSource table) {
        this.tableSource = (TableSourceSpi) table;
    }

    @Override
    public SelectSQL select(Column... columns) {
        for (Column c : columns) {
            this.columns.add((ColumnSpi) c);
        }
        return this;
    }

    @Override
    public SelectSQL groupBy(Column... columns) {
        for (Column c : columns) {
            if (!this.columns.contains(c)) {
                throw new RuntimeException("Column " + c.toString() + " not added to select list. Group by disallowed.");
            }
            this.groupBy.add((ColumnSpi) c);
        }
        return this;
    }

    @Override
    public SelectSQL orderBy(Column column, SortOrder sortOrder) {
        if (!columns.contains(column)) {
            throw new RuntimeException("Column " + ((ColumnSpi) column).getSQL()
                    + " not added to select list. Order by disallowed.");
        }
        ColumnOrdering col = new ColumnOrdering(column, sortOrder);
        orderBy.add(col);
        return this;
    }

    @Override
    public SelectSQL where(Predicate... predicates) {
        for (Predicate p : predicates) {
            this.predicates.add((PredicateSpi) p);
        }

        return this;
    }

    @Override
    public SelectSQL having(Predicate... predicates) {
        for (Predicate p : predicates) {
            havings.add((PredicateSpi) p);
        }

        return this;
    }

    @Override
    public SelectSQL offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public SelectSQL limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public SelectSQL distinct() {
        this.distinct = true;
        return this;
    }

    @Override
    public String getSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        if (distinct) {
            builder.append("distinct ");
        }
        builder.append(getSelectColumnQueryString());
        builder.append(" FROM ");
        builder.append(tableSource.getAliasedSQL());

        builder.append(getWhereClause());
        builder.append(getGroupByClause());
        builder.append(getHavingClause());
        builder.append(getOrderByClause());

        if (offset != null) {
            builder.append(" offset ").append(offset);
        }
        if (limit != null) {
            builder.append(" limit ").append(limit);
        }
        return builder.toString();
    }

    private Object getOrderByClause() {
        if (orderBy.isEmpty()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            for (ColumnOrdering oc : orderBy) {
                builder.append(SEPARATOR_COMMA);
                builder.append(oc.getSQL());
            }
            return " ORDER BY " + builder.substring(SEPARATOR_COMMA.length());
        }
    }

    private Object getGroupByClause() {
        if (groupBy.isEmpty()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            for (ColumnSpi c : groupBy) {
                builder.append(SEPARATOR_COMMA);
                builder.append(c.getAlias());
            }
            return " GROUP BY " + builder.substring(SEPARATOR_COMMA.length());
        }
    }

    private String getHavingClause() {
        if (havings.isEmpty()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            for (PredicateSpi predicate : havings) {
                builder.append(SEPARATOR_COMMA);
                builder.append(predicate.getSQL());
            }
            return " HAVING " + builder.substring(SEPARATOR_COMMA.length());
        }
    }

    private String getWhereClause() {
        if (predicates.isEmpty()) {
            return "";
        } else {
            try{
                List<PredicateSpi> mergedPredicates = mergeMultiValuedClauses(this.predicates);

                StringBuilder builder = new StringBuilder();
                for (PredicateSpi predicate : mergedPredicates) {
                    builder.append(CONJUNCTION_OPERATOR);
                    builder.append(predicate.getAliasedSQL());
                }

                return " WHERE " + builder.substring(CONJUNCTION_OPERATOR.length());
            } catch(PredicateIsEmptyException e){
                //TODO: make this better, once we merge more than top level, this will be a problem
                return " WHERE TRUE = FALSE ";

            }
        }
    }

    private static List<PredicateSpi> mergeMultiValuedClauses(List<PredicateSpi> predicates) throws PredicateIsEmptyException {
        List<MultiValuedPredicate> multiValuedPredicates = new ArrayList<MultiValuedPredicate>();

        List<PredicateSpi> fullCopy = Lists.newArrayList();

        for(PredicateSpi p: predicates){
            if(p instanceof MultiValuedPredicate){
                multiValuedPredicates.add((MultiValuedPredicate) p);
            } else {
                fullCopy.add(p);
            }
        }

        List<MultiValuedPredicate> multiCopy = Lists.newArrayList(multiValuedPredicates);

        Set<Integer> indexesToRemove = Sets.newHashSet();
        for(MultiValuedPredicate p: multiValuedPredicates){
            multiCopy.remove(p);
            for(MultiValuedPredicate p2: multiCopy){
                if(p.canCombine(p2)){
                    indexesToRemove.add(multiValuedPredicates.indexOf(p2));
                    p.join(p2);
                }
            }
        }

        List<Integer> list = Lists.newArrayList(indexesToRemove);
        Collections.sort(list);

        for(int ii=list.size()-1; ii>=0; ii--){

            multiValuedPredicates.remove(list.get(ii).intValue());
        }

        for(int ii=multiValuedPredicates.size()-1; ii>=0; ii--){
            if((multiValuedPredicates.get(ii).getValues() == null) ||
                    multiValuedPredicates.get(ii).getValues().isEmpty()){
                throw new PredicateIsEmptyException();
            }
        }

        fullCopy.addAll(multiValuedPredicates);

        return fullCopy;
    }

    private String getSelectColumnQueryString() {
        assert !columns.isEmpty();
        StringBuilder builder = new StringBuilder();
        for (ColumnSpi column : columns) {
            builder.append(SEPARATOR_COMMA);
            builder.append(column.getAliasedSQL());
        }
        return builder.substring(SEPARATOR_COMMA.length());
    }

   @Override
   public <T> T scroll(ScrollCallback<T> callback) {
      T result = null;

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         DBConfig config = Configuration.getInstance().getDbConfig();

         try (Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            try {
                statement.setFetchSize(config.getRecordFetchSize());
            } catch (SQLException e) {
                LOG.warn("SetFetchSize() method not supported.");
            }
            try (ResultSet resultSet = statement.executeQuery(getSQL())) {
               result = callback.scroll(resultSet);
               conn.commit();
            }
         } catch (org.postgresql.util.PSQLException e) {
            SqlUtil.quietRollback(conn);

            // used to validate used provided regex, e.g.: in bundle functions
            if (e.getMessage().toLowerCase().indexOf("regexp") != -1) {
               LOG.warn("Encountered Regex parsing error.  Detailed Message: " + e.getMessage());
               throw new RuntimeException("Encountered Regex parsing error.  Detailed Message: " + e.getMessage());
            }
            throw Throwables.propagate(e);
         } catch (Exception e) {
            SqlUtil.quietRollback(conn);
            throw Throwables.propagate(e);
         }
      } catch (SQLException sqle) {
      } catch (CentrifugeException ce) {
      }
      return result;
   }

    @Override
    public SelectResultSet execute() {

        return scroll(new ScrollCallback<SelectResultSet>() {

            @Override
            public SelectResultSet scroll(ResultSet resultSet) throws SQLException {
                SelectResultSetImpl selectResultSet = new SelectResultSetImpl();
                ResultSetMetaData metaData = resultSet.getMetaData();

                Map<String, Integer> columnNameToIndex = new HashMap<String, Integer>();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    columnNameToIndex.put(metaData.getColumnLabel(i + 1), i);
                }

                selectResultSet.setColumnCount(metaData.getColumnCount());

                while (resultSet.next()) {
                    List<Object> values = new ArrayList<Object>();
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        values.add(resultSet.getObject(i + 1));
                    }
                    SelectResultRowImpl row = new SelectResultRowImpl(columnNameToIndex, values);
                    selectResultSet.add(row);
                }

                return selectResultSet;
            }
        });
    }

    @Override
    public List<? extends Column> getSelectColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return getSQL();
    }
}
