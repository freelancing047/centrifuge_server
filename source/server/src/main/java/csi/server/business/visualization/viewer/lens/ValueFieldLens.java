package csi.server.business.visualization.viewer.lens;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.common.model.visualization.viewer.field.ValueFieldLensDef;
import csi.server.dao.CsiConnection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.sql.api.AggregateFunction;
import csi.shared.gwt.viz.viewer.LensImage.FieldLensImage;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

public class ValueFieldLens implements Lens {
   private static final Logger LOG = LogManager.getLogger(FieldLens.class);

   @Override
   public LensImage focus(LensDef lensDef, Objective objective) {
      FieldLensImage fieldLensImage = null;
      ValueFieldLensDef ld = null;

      if (lensDef instanceof ValueFieldLensDef) {
         ld = (ValueFieldLensDef) lensDef;
      }
      if (ld != null) {
         String dataviewUuid = objective.getDataviewUuid();
         DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataviewUuid);
         VisualizationDef viz = null;

         for (VisualizationDef visualizationDef : dataView.getMeta().getModelDef().getVisualizations()) {
            if (visualizationDef.getUuid().equals(objective.getVisualizationUuid())) {
               viz = visualizationDef;
            }
         }
         if (viz != null) {
            Selection selection = objective.getSelection();
            Set<Integer> integers = new SelectionToRowsCoverterFactory(dataView, viz).create().convertToRows(selection, false);
            String rowIdsJoined = integers.stream().map(i -> Integer.toString(i.intValue())).collect(Collectors.joining(", "));
            fieldLensImage = new FieldLensImage();

            try (CsiConnection conn = CsiPersistenceManager.getCacheConnection()) {
               for (FieldDef fieldDef : dataView.getMeta().getModelDef().getFieldDefs()) {
                  if (fieldDef.getLocalId().equals(ld.getValue())) {
                     fieldLensImage.setLabel("Values of " + fieldDef.getName());
                     run2(dataviewUuid, dataView, rowIdsJoined, fieldLensImage, conn, fieldDef);
                  }
               }
            } catch (CentrifugeException e) {
               e.printStackTrace();
            } catch (SQLException sqle) {
            }
         }
      }
      return fieldLensImage;
   }

   @Override
   public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
      List<List<?>> result = null;
      String dataviewUuid = objective.getDataviewUuid();
      DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataviewUuid);
      VisualizationDef viz = null;

      for (VisualizationDef visualizationDef : dataView.getMeta().getModelDef().getVisualizations()) {
         if (visualizationDef.getUuid().equals(objective.getVisualizationUuid())) {
            viz = visualizationDef;
         }
      }
      if (viz != null) {
         Selection selection = objective.getSelection();
         Set<Integer> integers = new SelectionToRowsCoverterFactory(dataView, viz).create().convertToRows(selection, false);
         String rowIdsJoined = integers.stream().map(i -> Integer.toString(i.intValue())).collect(Collectors.joining(", "));
         result = new ArrayList<List<?>>();
         int rownumber = 0;
         StopWatch stopWatch = new StopWatch();

         try (CsiConnection conn = CsiPersistenceManager.getCacheConnection()) {
            for (FieldDef fieldDef : dataView.getMeta().getModelDef().getFieldDefs()) {
               if (fieldDef.getName().equals(token)) {
                  String colname = CacheUtil.makeCastExpression(fieldDef);
                  String sql =
                     new StringBuilder("SELECT ").append(CacheUtil.makeCastExpression(fieldDef))
                               .append(", ").append(AggregateFunction.COUNT.getAggregateExpression(colname)).append(" as myCount" +
                                        " FROM ").append(CacheUtil.getQuotedCacheTableName(dataviewUuid))
                               .append(" WHERE internal_id IN (").append(rowIdsJoined).append(")" +
                                       " group by ").append(colname)
                               .append(" order by myCount desc")
                               .toString();

                  try (Statement statement = conn.createStatement()) {
                     stopWatch.reset();
                     stopWatch.start();
                     statement.execute(sql);

                     try (ResultSet resultSet = statement.getResultSet()) {
                        while (resultSet.next()) {
                           List<Object> row = new ArrayList<Object>();

                           row.add(Integer.valueOf(rownumber++));
                           row.add(resultSet.getString(1));
                           row.add(Integer.valueOf(resultSet.getInt(2)));
                           result.add(row);
                        }
                     }
                     stopWatch.stop();
                     LOG.error("Query took(ms): {}", () -> Long.valueOf(stopWatch.getTime()));
                  } catch (SQLException ignored) {
                  }
               }
            }
         } catch (CentrifugeException e) {
            e.printStackTrace();
         } catch (SQLException sqle) {
         }
      }
      return result;
   }

   @Override
   public ViewerGridConfig getGridConfig() {
      return null;
   }

   private void run2(String dataviewUuid, DataView dataView, String rowIdsJoined, FieldLensImage fieldLensImage,
                     CsiConnection conn, FieldDef fieldDef) {
      String colname = CacheUtil.makeCastExpression(fieldDef);
      String sql =
         new StringBuilder("SELECT ").append(colname).append(", ")
                   .append(AggregateFunction.COUNT.getAggregateExpression(colname)).append(" as myCount" +
                            " FROM ").append(CacheUtil.getQuotedCacheTableName(dataviewUuid))
                   .append(" WHERE internal_id IN (").append(rowIdsJoined).append(")" +
                           " group by ").append(colname)
                   .append(" order by myCount")
                   .toString();
      StopWatch stopWatch = new StopWatch();

      try (Statement statement = conn.createStatement()) {
         stopWatch.start();
         statement.execute(sql);

         try (ResultSet resultSet = statement.getResultSet()) {
            while (resultSet.next()) {
               fieldLensImage.addFieldValue(fieldDef.getUuid(), resultSet.getString(1), resultSet.getInt(2));
            }
         }
         stopWatch.stop();
         LOG.error("Query took(ms): {}", () -> Long.valueOf(stopWatch.getTime()));
      } catch (SQLException ignored) {
      }
   }
}
