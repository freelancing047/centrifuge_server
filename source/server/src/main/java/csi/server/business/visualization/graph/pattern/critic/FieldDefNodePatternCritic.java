package csi.server.business.visualization.graph.pattern.critic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import prefuse.visual.VisualItem;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.shared.gwt.viz.graph.tab.pattern.settings.FieldDefNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class FieldDefNodePatternCritic implements NodePatternCritic {
    private Cache<String, Set<Integer>> results;

    public FieldDefNodePatternCritic() {
        results = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();
    }

    @Override
    public boolean criticizeNode(String dvUuid, VisualItem item, NodeStore details, PatternCriterion criterion, String jobId) {
        if (criterion instanceof FieldDefNodePatternCriterion) {
            Set<Integer> integers = results.getIfPresent(jobId);
            if (integers == null) {
                //make results.
                integers = findAllRowsThatMatch(dvUuid, criterion);
                //put results in cache
                results.put(jobId, integers);
            }
            List<Integer> allRows = getRowIntegers(details);
            for (Integer row : allRows) {
                if (integers.contains(row)) {
                    return true;
                }
            }
        }
        return false;
    }

   private Set<Integer> findAllRowsThatMatch(String dvUuid, PatternCriterion criterion) {
      Set<Integer> values = Sets.newTreeSet();

      try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
         boolean randomAccess = false;
         StringBuilder sb = new StringBuilder(1000);
         FieldDef fieldDef = FieldDefNodePatternCritic.getFieldDef(criterion,dvUuid);

         if (fieldDef != null) {
            String columnName = CacheUtil.getColumnName(fieldDef);

            sb.append("\"").append(columnName).append("\"");
            sb.append(" IN (\'");
            sb.append(criterion.getValue());
            sb.append("\')");
            String whereClause = sb.toString();

            try (ResultSet rs = DataCacheHelper.getCacheData(conn, dvUuid, null, whereClause, null, -1, -1, randomAccess)) {
               while (rs.next()) {
                  try {
                     int internal_id = Integer.parseInt(String.valueOf(rs.getObject("internal_id")));
                     values.add(Integer.valueOf(internal_id));
                  } catch (NumberFormatException ignored) {
                  }
               }
            }
         }
      } catch (SQLException ignored) {
      } catch (CentrifugeException ignored) {
      }
      return values;
   }

    private static List<Integer> getRowIntegers(NodeStore details) {
        Map<String, List<Integer>> rowMap = details.getRows();
        List<Integer> allRows = new ArrayList<Integer>();
        for (List<Integer> integers : rowMap.values()) {
            allRows.addAll(integers);
        }
        return allRows;
    }

    @Override
    public SafeHtml getObservedValue(VisualItem item, NodeStore details, PatternCriterion criterion, String dvUuid) {
        List<Integer> allRows = getRowIntegers(details);
        Set<String> values = null;
        String filter = FieldDefNodePatternCritic.determineFilter(allRows);
        FieldDef fieldDef = FieldDefNodePatternCritic.getFieldDef(criterion, dvUuid);
        if (fieldDef == null) {
            return SafeHtmlUtils.fromString("");
        }
        try (Connection conn = CsiPersistenceManager.getCacheConnection()) {
           values = FieldDefNodePatternCritic.getValuesFromConnection(dvUuid, conn, filter, fieldDef);

           if (values == null) {
              return SafeHtmlUtils.fromString("");
           }
        } catch (CentrifugeException ignored) {
        } catch (SQLException ignored) {
        }
        List<String> highlightedValues = FieldDefNodePatternCritic.styleMatches(criterion, values);
        return SafeHtmlUtils.fromSafeConstant(highlightedValues.stream().collect(Collectors.joining(", ")));
    }

    protected static List<String> styleMatches(PatternCriterion criterion, Set<String> values) {
        List<String> highlightedValues = new ArrayList<String>();
        for (String value : values) {
            boolean match = value.equals(criterion.getValue());
            if (match) {
                highlightedValues.add("<b>" + SafeHtmlUtils.htmlEscape(value) + "</b>");
            } else {
                highlightedValues.add(SafeHtmlUtils.htmlEscape(value));
            }
        }
        return highlightedValues;
    }

   protected static Set<String> getValuesFromConnection(String dvUuid, Connection conn, String filter, FieldDef fieldDef) throws CentrifugeException {
      Set<String> values = null;
      boolean randomAccess = false;

      try (ResultSet rs = DataCacheHelper.getCacheData(conn, dvUuid, null, filter, null, -1, -1, randomAccess)) {
         if (rs != null) {
            values = convertResultSetToSet(rs, fieldDef);
         }
      } catch (SQLException ignored) {
      }
      return values;
   }

    private static Set<String> convertResultSetToSet(ResultSet rs, FieldDef fieldDef) throws SQLException {
        List<FieldDef> fields = Lists.newArrayList();
        fields.add(fieldDef);
        CacheRowSet rowSet = new CacheRowSet(fields, rs);
        Set<String> values = Sets.newTreeSet();
        while (rowSet.nextRow()) {
            String string = rowSet.getString(fieldDef);
            if (string == null) {
                string = "null";
            }
            values.add(string);
        }
        return values;
    }

    private static FieldDef getFieldDef(PatternCriterion criterion, String dvUuid) {
        if (criterion instanceof FieldDefNodePatternCriterion) {
            FieldDefNodePatternCriterion patternCriterion = (FieldDefNodePatternCriterion) criterion;
            DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
            return dv.getMeta().getModelDef().getFieldListAccess().getFieldDefByName(patternCriterion.getFieldName());
        }
        return null;
    }

   private static String determineFilter(List<Integer> allRows) {
      return allRows.stream().map(i -> i.toString()).collect(Collectors.joining(",", "\"internal_id\" IN (", ")"));
   }
}
