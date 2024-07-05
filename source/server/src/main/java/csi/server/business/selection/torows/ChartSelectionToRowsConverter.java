package csi.server.business.selection.torows;

import java.util.HashSet;
import java.util.Set;

import csi.server.business.service.FilterActionsService;
import csi.server.business.service.chart.ChartQueryBuilder;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.util.sql.SQLFactory;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartSelectionToRowsConverter implements SelectionToRowsConverter {
   private final DrillChartViewDef visualizationDef;
   private final DataView dataView;
   private final SQLFactory sqlFactory;
   private final FilterActionsService filterActionsService;
   private boolean validate = true;

   public ChartSelectionToRowsConverter(DataView dataView, DrillChartViewDef visualizationDef, SQLFactory sqlFactory,
                                        FilterActionsService filterActionsService) {
      this.dataView = dataView;
      this.visualizationDef = visualizationDef;
      this.sqlFactory = sqlFactory;
      this.filterActionsService = filterActionsService;
   }

   @Override
   public Set<Integer> convertToRows(Selection selection, boolean excludeBroadcast) {
      Set<Integer> result = new HashSet<Integer>();

      if (selection instanceof ChartSelectionState) {
         ChartSelectionState chartSelectionState = (ChartSelectionState) selection;

         if (validate) {
            ChartSelectionState validatedState = new ChartSelectionState();

            for (DrillCategory category : chartSelectionState.getSelectedItems()) {
               boolean add = true;

               for (String categoryPart : category.getCategories()) {
                  if (categoryPart == null) {
                     add = false;
                     break;
                  }
               }
               if (!add && (category.getCategories() != null) && (category.getCategories().size() == 1)) {
                  validatedState.getSelectedItems().add(category);
               } else if (add) {
                  validatedState.getSelectedItems().add(category);
               }
            }
            validatedState.setDrillSelections(chartSelectionState.getDrillSelections());
            chartSelectionState = validatedState;
         }
         if ((chartSelectionState.getSelectedItems() != null) &&
             !chartSelectionState.getSelectedItems().isEmpty()) {
            ChartQueryBuilder chartQueryBuilder = new ChartQueryBuilder();

            chartQueryBuilder.setDataView(dataView);
            chartQueryBuilder.setViewDef(visualizationDef);
            chartQueryBuilder.setSqlFactory(sqlFactory);
            chartQueryBuilder.setFilterActionsService(filterActionsService);

            result = chartQueryBuilder.selectionValuesToRows(chartSelectionState, excludeBroadcast);
            // return
            // chartQueryBuilder.selectionValuesToRows(chartSelectionState.getSelectedItems(),
            // excludeBroadcast);
         }
      }
      return result;
   }

   public boolean isValidate() {
      return validate;
   }

   public void setValidate(boolean validate) {
      this.validate = validate;
   }
}
