package csi.server.business.visualization.viewer.lens;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.common.model.visualization.viewer.field.MaxFieldLensDef;
import csi.server.dao.CsiConnection;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.sql.api.AggregateFunction;
import csi.shared.gwt.viz.viewer.LensImage.FieldLensImage;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

public class MaxFieldLens implements Lens {
    @Override
    public LensImage focus(LensDef lensDef, Objective objective) {
        if (lensDef instanceof MaxFieldLensDef) {
            AggregateFunction function = AggregateFunction.MAXIMUM;
            MaxFieldLensDef sfld = (MaxFieldLensDef) lensDef;

            String dataviewUuid = objective.getDataviewUuid();
            DataView dataView = CsiPersistenceManager.findObject(DataView.class, dataviewUuid);
            VisualizationDef viz = null;
            for (VisualizationDef visualizationDef : dataView.getMeta().getModelDef().getVisualizations()) {
                if (visualizationDef.getUuid().equals(objective.getVisualizationUuid())) {
                    viz = visualizationDef;
                }
            }
            String rowIdsJoined = "";
            if (viz != null) {
                Selection selection = objective.getSelection();
                Set<Integer> integers = new SelectionToRowsCoverterFactory(dataView, viz).create().convertToRows(selection, false);
                rowIdsJoined = integers.stream().map(i -> Integer.toString(i.intValue())).collect(Collectors.joining(", "));
            } else {

                return null;
            }
            FieldLensImage fieldLensImage = new FieldLensImage();

            try (CsiConnection conn = CsiPersistenceManager.getCacheConnection()) {
                for (FieldDef fieldDef : dataView.getMeta().getModelDef().getFieldDefs()) {
                    String value = sfld.getValue();
                    if (StringUtils.isEmpty(value)) {
                        return null;

                    }
                    if (fieldDef.getLocalId().equals(value)) {
                        fieldLensImage.setLabel(function.getLabel()+" of "+ fieldDef.getName());
                        AggregateFunction aggregateFunction = function;
                        if (aggregateFunction.isApplicableFor(fieldDef.getDataType())) {


                            run(dataviewUuid, rowIdsJoined, fieldLensImage, conn, fieldDef, aggregateFunction);
                        }
                    }
                }

            } catch (CentrifugeException e) {
                e.printStackTrace();
            } catch (SQLException sqle) {
            }
            return fieldLensImage;
        }

        return null;
    }


    @Override
    public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
        return null;
    }

    @Override
    public ViewerGridConfig getGridConfig() {
        return null;
    }

   private void run(String dataviewUuid, String rowIdsJoined, FieldLensImage fieldLensImage, CsiConnection conn, FieldDef fieldDef, AggregateFunction aggregateFunction) {
      StringBuilder sb = new StringBuilder(5000);

      sb.append("SELECT ");
      List<String> strings = new ArrayList<String>();
      String colname = CacheUtil.makeCastExpression(fieldDef);
      strings.add(aggregateFunction.getAggregateExpression(colname));
      sb.append(strings.stream().collect(Collectors.joining(", ")));
      sb.append(" FROM ").append(CacheUtil.getQuotedCacheTableName(dataviewUuid));
      sb.append(" WHERE internal_id IN (");
      sb.append(rowIdsJoined);
      sb.append(")");
      String query = sb.toString();

      try (Statement statement = conn.createStatement()) {
         statement.execute(query);

         try (ResultSet resultSet = statement.getResultSet()) {
            if (resultSet.next()) {
               String column = fieldDef.getUuid();
               String row = aggregateFunction.toString();
               String value = resultSet.getString(1);

               fieldLensImage.add(column, row, value);
            }
         }
      } catch (SQLException ignored) {
      }
   }
}
