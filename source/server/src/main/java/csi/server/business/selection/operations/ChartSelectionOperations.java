package csi.server.business.selection.operations;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import csi.server.business.selection.torows.ChartSelectionToRowsConverter;
import csi.server.business.selection.toselection.ChartRowsToSelectionConverter;
import csi.server.business.service.FilterActionsService;
import csi.server.common.model.broadcast.BroadcastRequest;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.chart.DrillChartViewDef;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskController;
import csi.server.util.sql.SQLFactory;
import csi.server.util.sql.impl.SQLFactoryImpl;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartSelectionOperations implements SelectionOperations<ChartSelectionState> {

    private SQLFactory sqlFactory;
    private FilterActionsService filterActionsService;

    @Override
    public void add(ChartSelectionState existingSelection, ChartSelectionState selectionToAdd) {
        //TODO: Need to refactor ChartSelectionState, this is awful, #BlamePledbetter
        sqlFactory = new SQLFactoryImpl();
        filterActionsService = new FilterActionsService();
        filterActionsService.setSqlFactory(sqlFactory);

        TaskController controller = TaskController.getInstance();
        Object[] params = controller.getCurrentContext().getMethodArgs();
        Object object = params[0];

        if(object instanceof BroadcastRequest){
            BroadcastRequest request = (BroadcastRequest) object;
            String vizUuid = request.getListeningVizUuid();
            DataView dataView = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
            DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, vizUuid);

            ChartSelectionToRowsConverter converter = new ChartSelectionToRowsConverter(dataView, viewDef, sqlFactory, filterActionsService);
            converter.setValidate(false);
            Set<Integer> rows = new TreeSet<Integer>();

            if (existingSelection != null) {
               if (!existingSelection.isCleared()) {
                  rows.addAll(converter.convertToRows(existingSelection, false));
               }
               selectionToAdd.setDrillSelections(existingSelection.getDrillSelections());
            }
            rows.addAll(converter.convertToRows(selectionToAdd, false));

            ChartRowsToSelectionConverter inverseConverter = new ChartRowsToSelectionConverter(dataView, viewDef, sqlFactory, filterActionsService);
            replace(existingSelection, (ChartSelectionState) inverseConverter.toSelection(rows));
        }

//        for(DrillCategory drillCategory : selectionToAdd.getSelectedItems()) {
//            if (indexOf(existingSelection.getSelectedItems(), drillCategory) == -1) {
//                DrillCategory drillCategoryToAdd = new DrillCategory();
//                drillCategoryToAdd.getCategories().addAll(drillCategory.getCategories());
//                existingSelection.getSelectedItems().add(drillCategoryToAdd);
//            }
//        }
    }

    private int indexOf(List<DrillCategory> selectedItems, DrillCategory drillCategory) {
        int index = 0;
        for (DrillCategory selectedItem : selectedItems) {
            if(selectedItem.convertCategoriesToString().equals(drillCategory.convertCategoriesToString())){
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public void remove(ChartSelectionState existingSelection, ChartSelectionState removalSelection) {
        sqlFactory = new SQLFactoryImpl();
        filterActionsService = new FilterActionsService();
        filterActionsService.setSqlFactory(sqlFactory);

        TaskController controller = TaskController.getInstance();
        Object[] params = controller.getCurrentContext().getMethodArgs();
        Object object = params[0];

        if(object instanceof BroadcastRequest){
            BroadcastRequest request = (BroadcastRequest) object;
            String vizUuid = request.getListeningVizUuid();
            DataView dataView = CsiPersistenceManager.findObject(DataView.class, request.getDataViewUuid());
            DrillChartViewDef viewDef = CsiPersistenceManager.findObject(DrillChartViewDef.class, vizUuid);

            viewDef.getChartSettings();

            ChartSelectionToRowsConverter converter = new ChartSelectionToRowsConverter(dataView, viewDef, sqlFactory, filterActionsService);
            converter.setValidate(false);
            Set<Integer> rows = new TreeSet<Integer>();

            if (existingSelection != null) {
               if (!existingSelection.isCleared()) {
                  rows.addAll(converter.convertToRows(existingSelection, false));
               }
               removalSelection.setDrillSelections(existingSelection.getDrillSelections());
            }
            rows.removeAll(converter.convertToRows(removalSelection, false));

            ChartRowsToSelectionConverter inverseConverter = new ChartRowsToSelectionConverter(dataView, viewDef, sqlFactory, filterActionsService);
            replace(existingSelection, (ChartSelectionState) inverseConverter.toSelection(rows));
        }
    }

    @Override
    public void replace(ChartSelectionState existingSelection, ChartSelectionState replacingSelection) {
        existingSelection.clearSelection();
        existingSelection.makeSelectionStateForCategories(replacingSelection.getSelectedItems());
    }
}
