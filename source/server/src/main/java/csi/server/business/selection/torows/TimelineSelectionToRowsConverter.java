package csi.server.business.selection.torows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Throwables;

import csi.server.business.helper.QueryHelper;
import csi.server.business.service.FilterActionsService;
import csi.server.business.service.chronos.storage.AbstractTimelineStorageService;
import csi.server.business.service.chronos.storage.TimelineStorage;
import csi.server.business.service.chronos.storage.postgres.DataToTimelineResultTransformer;
import csi.server.business.visualization.timeline.EventIdComparator;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.TimelineEventSelection;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.timeline.DetailedTimelineResult;
import csi.shared.core.visualization.timeline.SingularTimelineEvent;

/**
 * @author Centrifuge Systems, Inc.
 */
public class TimelineSelectionToRowsConverter implements SelectionToRowsConverter {
    private final TimelineViewDef visualizationDef;

    public TimelineSelectionToRowsConverter(DataView dataviewIn, TimelineViewDef visualizationDef, FilterActionsService filterActionsService) {
        this.visualizationDef = visualizationDef;
    }

//    @Override
//    public Set<Integer> convertToRows(Selection selection, boolean excludeRows) {
//        if(!(selection instanceof IntPrimitiveSelection)){
//            return Sets.newHashSet();
//        }
//        IntPrimitiveSelection mySelectionState = (IntPrimitiveSelection)selection;
//
//        if ((null != mySelectionState)
//                && (null != mySelectionState.getSelectedItems())
//                && (0 <  mySelectionState.getSelectedItems().size())) {
//
//            TimelineQueryBuilder timelineQueryBuilder = new TimelineQueryBuilder(dataview, visualizationDef);
//            String TimelineQuery = timelineQueryBuilder.buildQuery(mySelectionState, excludeRows);
//
//            return getRowsFromCacheTimeline(TimelineQuery);
//        } else {
//
//            return Sets.newHashSet();
//        }
//    }

   private Set<Integer> getRowsFromCacheTimeline(String timelineQuery) {
      Set<Integer> rows = new HashSet<Integer>();

      try (Connection conn = CsiPersistenceManager.getCacheConnection();
           ResultSet rs = QueryHelper.executeSingleQuery(conn, timelineQuery, null)) {
         while (rs.next()) {
            rows.add(Integer.valueOf(rs.getInt(1)));
         }
      } catch (Exception e) {
         Throwables.propagate(e);
      }
      return rows;
   }

    @Override
    public Set<Integer> convertToRows(Selection selection, boolean b) {
        if(selection instanceof TimelineEventSelection){

            String vizUuid = visualizationDef.getUuid();


            AbstractTimelineStorageService storageService = AbstractTimelineStorageService.instance();
            if(storageService.hasVisualizationData(vizUuid)){
                TimelineStorage storage = storageService.getTimelineStorage(vizUuid);
                DataToTimelineResultTransformer transformer = new DataToTimelineResultTransformer();
                DetailedTimelineResult result = transformer.apply(storage.getResult());

                IntPrimitiveSelection intSelection = (IntPrimitiveSelection)selection;
                IntCollection selectedItems = intSelection.getSelectedItems();

                List<SingularTimelineEvent> events = result.getEvents();

                selectedItems.sort();

                Collections.sort(events, new EventIdComparator());
                boolean focused = false;
                boolean filtered = false;
                Set<String> filteredKeys = new HashSet<String>();
                TimelineCachedState cachedState = visualizationDef.getState();
                if((cachedState != null)
                        && (cachedState.getTrackStates() != null)
                        && (cachedState.getFocusedTrack() != null)) {

                    filteredKeys.add(cachedState.getFocusedTrack());
                    focused = true;
                    filtered = true;

                } else if((cachedState != null)
                        && (cachedState.getTrackStates() != null)
                        && !cachedState.getTrackStates().isEmpty()) {
                    Set<TimelineTrackState> states = cachedState.getTrackStates();

                    for (TimelineTrackState state: states) {
                        if (!state.getVisible().booleanValue()) {
                            filteredKeys.add(state.getTrackName());
                            filtered = true;
                        }
                    }
                }

                HashSet<Integer> rows = new HashSet<Integer>();
                int jj=0;
                for(int ii=0; ii<selectedItems.size(); ii++){
                    int selectedItemId = selectedItems.get(ii);
                    for(; jj<events.size(); jj++){
                        SingularTimelineEvent singularTimelineEvent = events.get(jj);
                        if(filtered
                                && (singularTimelineEvent.getTrackValue() != null)) {

                            boolean contains = filteredKeys.contains(singularTimelineEvent.getTrackValue());
                            if(focused) {
                                if(!contains) {
                                    continue;
                                }
                            } else {
                                if(contains) {
                                    continue;
                                }
                            }
                        }
                        int eventDefinitionId = singularTimelineEvent.getEventDefinitionId();
                        if(selectedItemId == eventDefinitionId){
                            rows.add(singularTimelineEvent.getRowId());
                        } else if(selectedItemId > eventDefinitionId){
                            continue;
                        } else if(selectedItemId < eventDefinitionId){
                            break;
                        }
                    }
                }



                return rows;


            }
        }
//        else if(selection instanceof IntPrimitiveSelection){
//            IntPrimitiveSelection mySelectionState = (IntPrimitiveSelection)selection;
//
//            if ((null != mySelectionState)
//                    && (null != mySelectionState.getSelectedItems())
//                    && (0 <  mySelectionState.getSelectedItems().size())) {
//
//                TimelineQueryBuilder timelineQueryBuilder = new TimelineQueryBuilder(dataview, visualizationDef);
//                String timelineQuery = timelineQueryBuilder.buildQuery(mySelectionState, b);
//
//                return getRowsFromCacheTimeline(timelineQuery);
//            } else {
//
//                return Sets.newHashSet();
//            }
//        }


        return new HashSet<Integer>();
    }
}
