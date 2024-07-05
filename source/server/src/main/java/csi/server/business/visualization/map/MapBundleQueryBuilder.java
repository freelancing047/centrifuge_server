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
package csi.server.business.visualization.map;

import static csi.server.common.enumerations.RelationalOperator.EQUAL;
import static csi.server.common.enumerations.RelationalOperator.IN;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.service.AbstractQueryBuilder;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.map.Crumb;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.util.CacheUtil;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Column;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.ScrollCallback;
import csi.server.util.sql.SelectSQL;
import csi.server.util.sql.impl.AggregateDisjunctivePredicate;
import csi.server.util.sql.impl.spi.PredicateSpi;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MapBundleQueryBuilder extends AbstractQueryBuilder<MapViewDef> {
   private static final Logger LOG = LogManager.getLogger(MapBundleQueryBuilder.class);

	public List<String> rowIdsToSelectionInfo(List<? extends Number> rowIds) {
		List<String> categories = new ArrayList<String>();

		CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
		SelectSQL sql = getSqlFactory().createSelect(tableSource);

		Predicate conjunction = getSqlFactory().predicate(true);
		DataView dataView = getDataView();
		MapViewDef mapViewDef = getViewDef();
		List<Crumb> breadCrumb = MapServiceUtil.getBreadcrumb(dataView.getUuid(), mapViewDef.getUuid());
		int index = 0;
		if ((breadCrumb != null) && !breadCrumb.isEmpty()) {
			while (index < breadCrumb.size()) {
				FieldDef fieldDef = mapViewDef.getMapSettings().getMapBundleDefinitions().get(index).getFieldDef();
				Column column = tableSource.getColumn(fieldDef);
				Predicate predicate = column.$(EQUAL).value(breadCrumb.get(index).getCriterion(),
						fieldDef.getDataType());
				conjunction = conjunction.and(predicate);
				index++;
			}
		}
		sql.where(conjunction);

		FieldDef fieldDef = mapViewDef.getMapSettings().getMapBundleDefinitions().get(index).getFieldDef();
		Column selectColumn = tableSource.getColumn(fieldDef);
		sql.select(selectColumn);

		// Add where clause for row ids
		Column idColumn = tableSource.getIdColumn();
		sql.where(idColumn.$(IN).list(rowIds, CsiDataType.Integer));

		// Filter expression.
		applyFilters(tableSource, sql, false);

		sql.distinct();

		sql.scroll(new ScrollCallback<Void>() {
			@Override
			public Void scroll(ResultSet resultSet) throws SQLException {
				try {
					while (resultSet.next()) {
						categories.add(resultSet.getString(1));
					}
					return null;
				} catch (SQLException sqe) {
				   LOG.error("Error executing query: " + sql.toString(), sqe);
					throw sqe;
				}

			}
		});

		return categories;
	}

	public Set<Integer> selectionValuesToRows(List<String> nodeCriteria, boolean excludeBroadcast) {
		return getRowIdsSQLQuery(nodeCriteria, excludeBroadcast).scroll(new ScrollCallback<Set<Integer>>() {
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

	private SelectSQL getRowIdsSQLQuery(List<String> nodeCriteria, boolean excludeBroadcast) {
		CacheTableSource tableSource = getSqlFactory().getTableSourceFactory().create(getDataView());
		SelectSQL sql = getSqlFactory().createSelect(tableSource);
		Column idColumn = tableSource.createDistinctColumn(tableSource.getIdColumn());
		idColumn.setAlias(CacheUtil.INTERNAL_ID_NAME); // The broadcast service code expects this as the alias.
		sql.select(idColumn);

		Predicate predicate = createPredicateFromDrillCategories(nodeCriteria, tableSource);
		sql.where(predicate);

		// Filter expression.
		applyFilters(tableSource, sql, excludeBroadcast);
		return sql;
	}

	private Predicate createPredicateFromDrillCategories(List<String> nodeCriteria, CacheTableSource tableSource) {
		Predicate conjunction = getSqlFactory().predicate(true);
		DataView dataView = getDataView();
		MapViewDef mapViewDef = getViewDef();
		List<Crumb> breadCrumb = MapServiceUtil.getBreadcrumb(dataView.getUuid(), mapViewDef.getUuid());
		int index = 0;
		if ((breadCrumb != null) && !breadCrumb.isEmpty()) {
			while (index < breadCrumb.size()) {
				FieldDef fieldDef = mapViewDef.getMapSettings().getMapBundleDefinitions().get(index).getFieldDef();
				Column column = tableSource.getColumn(fieldDef);
				Predicate predicate;
				String criterion = breadCrumb.get(index).getCriterion();
				if ((criterion == null) || criterion.equalsIgnoreCase("NULL")) {
					predicate = column.isNull();
				} else {
					predicate = column.$(EQUAL).value(criterion, fieldDef.getDataType());
				}
				conjunction = conjunction.and(predicate);
				index++;
			}
		}
		if ((nodeCriteria != null) && !nodeCriteria.isEmpty()) {
			List<String> newNodeCriteria = new ArrayList<String>();
			boolean hasNull = false;
			for (String nodeCriterion : nodeCriteria) {
				if ((nodeCriterion == null) || nodeCriterion.equalsIgnoreCase("NULL")) {
					hasNull = true;
				} else {
					newNodeCriteria.add(nodeCriterion);
				}
			}
			FieldDef fieldDef = mapViewDef.getMapSettings().getMapBundleDefinitions().get(index).getFieldDef();
			Column column = tableSource.getColumn(fieldDef);
			AggregateDisjunctivePredicate disjunction = new AggregateDisjunctivePredicate(true);
			if (!newNodeCriteria.isEmpty()) {
				PredicateSpi predicate = (PredicateSpi)(column.$(IN).list(nodeCriteria, fieldDef.getDataType()));
				disjunction.addToAggregate(predicate);
			}
			if (hasNull) {
				PredicateSpi predicate = (PredicateSpi)(column.isNull());
				disjunction.addToAggregate(predicate);
			}
			conjunction = conjunction.and(disjunction);
		}
		return conjunction;
	}
}
