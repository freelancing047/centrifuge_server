package csi.server.business.service.chart.storage.postgres;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.mongodb.DBObject;

import csi.server.business.service.chart.ChartTableGenerator;
import csi.server.business.service.chart.TableResult;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;

public class SelectionToTableResultTransformer implements Function<DBObject,TableResult> {

   ChartSelectionState selection;

   @Override
   public TableResult apply(DBObject table) {
      TableResult tableResult = new TableResult();

      tableResult.setCategories((List<String>) table.get(ChartKeyConstants.CATEGORIES_KEY));
      tableResult.setChartLimitExceeded((boolean) table.get(ChartKeyConstants.CHART_LIMIT_KEY));
      tableResult.setDimensionValues((List<List<Number>>) table.get(ChartKeyConstants.DIMENSION_VALUES_KEY));
      tableResult.setTableLimitExceeded((boolean) table.get(ChartKeyConstants.TABLE_LIMIT_KEY));
      tableResult.setRowCount(tableResult.getCategories().size());

      selection = validateSelection();

      int drillIndex = 0;
      List<String> drillSelection = selection.getDrillSelections();
      if (drillSelection != null) {
         drillIndex = drillSelection.size();
      }

      Set<String> selectedItems = new HashSet<String>();

      for (DrillCategory selection : selection.getSelectedItems()) {
         if ((selection.getCategories().size() - 1) >= drillIndex) {
            String item = selection.getCategories().get(drillIndex);
            if (item == null) {
               selectedItems.add(ChartTableGenerator.SECRET_NULL_CHART_LABEL);
            } else {
               selectedItems.add(selection.getCategories().get(drillIndex));
            }
         }
      }
      int rowCount = 0;

      List<String> filteredCategories = new ArrayList<String>();
      List<List<Number>> filteredDimensionValues = new ArrayList<List<Number>>();

      for (List<Number> values : tableResult.getDimensionValues()) {
         filteredDimensionValues.add(new ArrayList<Number>());
      }

      for (int ii = 0; ii < tableResult.getCategories().size(); ii++) {
         if (selectedItems.contains(tableResult.getCategories().get(ii))) {

            filteredCategories.add(tableResult.getCategories().get(ii));
            int count = 0;
            for (List<Number> measureValues : tableResult.getDimensionValues()) {
               filteredDimensionValues.get(count).add(measureValues.get(ii));
               count++;
            }

            rowCount++;
         }
      }

      tableResult.setRowCount(rowCount);
      tableResult.setCategories(filteredCategories);
      tableResult.setDimensionValues(filteredDimensionValues);

      return tableResult;
   }

   private ChartSelectionState validateSelection() {
      ChartSelectionState validatedState = new ChartSelectionState();

      for (DrillCategory category : selection.getSelectedItems()) {
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

      validatedState.setDrillSelections(selection.getDrillSelections());

      return validatedState;
   }

   public void setSelection(ChartSelectionState selection) {
      this.selection = selection;
   }

   public static boolean compareToNullString(String value) {
      if (value == null) {
         // kind of weird, but the idea is that nulls need to return true I guess...
         return true;
      } else if (value.length() == 0) {
         return false;
      }

      return value.equals(ChartTableGenerator.SECRET_NULL_CHART_LABEL);
   }
}
