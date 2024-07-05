package csi.server.business.selection.toselection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import csi.server.business.service.FilterActionsService;
import csi.server.business.service.chronos.storage.AbstractTimelineStorageService;
import csi.server.business.service.chronos.storage.TimelineStorage;
import csi.server.business.service.chronos.storage.postgres.DataToTimelineResultTransformer;
import csi.server.business.visualization.timeline.RowIdComparator;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.util.sql.SQLFactory;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;

public class TimelineRowsToSelectionConverter implements RowsToSelectionConverter {
   private String vizUuid;

   public TimelineRowsToSelectionConverter(DataView dataView, TimelineViewDef visualizationDef, SQLFactory sqlFactory,
                                           FilterActionsService filterActionsService) {
      this.vizUuid = visualizationDef.getUuid();
   }

   @Override
   public Selection toSelection(Set<Integer> rows) {
      Selection results = null;
      AbstractTimelineStorageService storageService = AbstractTimelineStorageService.instance();

      if (storageService.hasVisualizationData(vizUuid)) {
         TimelineEventSelection selection = new TimelineEventSelection(vizUuid);
         TimelineStorage storage = storageService.getTimelineStorage(vizUuid);
         DataToTimelineResultTransformer transformer = new DataToTimelineResultTransformer();
         DetailedTimelineResult result = transformer.apply(storage.getResult());
         List<Integer> eventIds = new ArrayList<Integer>();
         List<SingularTimelineEvent> events = result.getEvents();
         List<Integer> rowList = new ArrayList<Integer>(rows);

         Collections.sort(rowList);
         Collections.sort(events, new RowIdComparator());

         int jj = 0;

         for (Integer ii : rowList) {
            for (; jj < events.size(); jj++) {
               SingularTimelineEvent singularTimelineEvent = events.get(jj);
               int rowId = singularTimelineEvent.getRowId();

               if (ii == rowId) {
                  eventIds.add(singularTimelineEvent.getEventDefinitionId());
               } else if (ii > rowId) {
                  continue;
               } else {
                  break;
               }
            }
         }
         selection.getSelectedItems().addAll(eventIds);
         results = selection;
      }
      return results;
   }
}
